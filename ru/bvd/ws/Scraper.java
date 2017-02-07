package ru.bvd.ws;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class Scraper {
    //---------static
    static final Scraper INSTANCE = new Scraper();
    private static final String man = " webscraper - console web scraper (http://en.wikipedia.org/wiki/Web_scraping)\n"+
            "\n"+
            " SYNOPSIS\n"+
            "   webscraper [URL] [-f URLs file] WORDS [OPTIONS]\n"+
            " OPTIONS\n"+
            "    -v output verbosity flag,  if on then the output should contains information about time spend on data scraping and data processing\n"+
            "    -w count number of provided word(s) occurrence on webpage(s)\n"+
            "    -c count number of characters of each web page\n"+
            "    -e extract sentences’ which contain given words\n"+
            " EXAMPLE\n"+
            "    java –jar scraper.jar http://www.cnn.com Trump,Trump's –v –w –c –e\n"+
            "    java –jar scraper.jar -f urls.txt Greece,default –v –w –c –e\n" +
            " OUTPUT FORMAT\n" +
            "    URL | word | tag | sentence";

    static class Documents extends ArrayList<Document> {
        @Override
        public synchronized boolean add(Document document) {
            return super.add(document);
        }
    }
    static class Words extends ArrayList<String>{}
    static class Includes extends ArrayList<Include>{}

    private static class Config {
        String urlString;
        String urlFile;
        String wordString;
        boolean verbosityOpt;
        boolean wordCountOpt;
        boolean characterCountOpt;
        boolean extractSentencesOpt;

        public Config() {
        }

        public Config(Config conf) {
            this();
            urlString = conf.urlString;
            urlFile = conf.urlFile;
            wordString = conf.wordString;
            verbosityOpt = conf.verbosityOpt;
            wordCountOpt = conf.wordCountOpt;
            characterCountOpt = conf.characterCountOpt;
            extractSentencesOpt = conf.extractSentencesOpt;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "urlString='" + urlString + '\'' +
                    ", urlFile='" + urlFile + '\'' +
                    ", wordString='" + wordString + '\'' +
                    ", verbosityOpt=" + verbosityOpt +
                    ", wordCountOpt=" + wordCountOpt +
                    ", characterCountOpt=" + characterCountOpt +
                    ", extractSentencesOpt=" + extractSentencesOpt +
                    '}';
        }
    }

    private final BlockingDeque<String> queue = new LinkedBlockingDeque<>();

    //----------get/set
    private void setConfig(Config conf) {
        this.config = new Config(conf);
    }
    BlockingDeque<String> getQueue() { return queue; }

    //-----------private
    private Words words = new Words();
    private Documents documents = new Documents();
    private Includes includes = new Includes();
    private Config config;
    //------------


    private void process () throws InterruptedException {
        LocalDateTime durationFrom = LocalDateTime.now();

        if (config.urlFile == null) {
            queue.add(config.urlString);
        }
        else {
            try {
                List<String> linesFile = Files.readAllLines(Paths.get(config.urlFile));
                for (String l: linesFile) {
                    queue.add(l);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String[] wordsArr = config.wordString.split(",");
        for (int i = 0; i < wordsArr.length; i++) {
            words.add( wordsArr[i] );
        }

        int threadCount = Math.min(queue.size(),5);
        final ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i=0; i<threadCount; i++) {
            futures.add( pool.submit( new Extractor(documents, words) ) );
        }
        for (Future<?> future : futures) {
            try {
                includes.addAll((List<Include>) future.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();

        StringBuilder outLines = new StringBuilder();
        //scrape errors output
        for (Document d: documents) {
            if (d.getStatusError()!=null) {
                outLines.append(d.getUrl())
                        .append(", description=\"")
                        .append( d.getStatusError() )
                        .append( "\", http status=" )
                        .append( d.getHttpStatus() );
            }
        }
        if (outLines.length()>0) {
            System.out.println("Error:");
            System.out.println(outLines.toString());
            System.out.println();
            outLines.setLength(0);
        }

        //includes output
        if (includes.size()>0) {
            for (Include i : includes) {
                //System.out.println( i.toString() );
                String url;
                if ( i.getDocument().getUrl().length() > 50 ) {
                    url = i.getDocument().getUrl().substring(0, 49) + "...";
                } else {
                    url = i.getDocument().getUrl();
                }
                outLines.append(url);
                outLines.append(" | ").append(i.getSample() );
                outLines.append(" | ").append(i.getTag() );
                if (config.extractSentencesOpt)
                    outLines.append(" | ").append(i.getStatment() );
                outLines.append("\n");
            }
            System.out.println(outLines);
            outLines.setLength(0);
        }
        //web resourses output
        if (config.verbosityOpt || config.characterCountOpt || config.wordCountOpt) {
            for (Document d: documents) {
                if (d.getStatusError() == null) {
                    outLines.append(d.getUrl());
                    if (config.verbosityOpt && d.getProcessDuration() != null) {
                        outLines.append(", duration=")
                                .append( (d.getRequestDuration().toMillis()+d.getProcessDuration().toMillis()) )
                                .append("(request=")
                                .append( d.getRequestDuration().toMillis() )
                                .append(", process=")
                                .append( d.getProcessDuration().toMillis() )
                                .append(")");
                    }
                    if (config.characterCountOpt && d.getContent() != null) {
                        outLines.append(", characters count=").append( d.getContent().length());
                    }
                    if (config.wordCountOpt) {
                        outLines.append(", words include=").append( d.getWordsCount() );
                    }
                    outLines.append("\n");
                }
                //System.out.println(d.content);
            }
            outLines.append("Summary: duration=")
                    .append( (Duration.between(durationFrom, LocalDateTime.now()).toMillis()) );
            System.out.println(outLines);
            System.out.println();
            outLines.setLength(0);
        }
    }


    //    java –jar scraper.jar http://www.cnn.com Greece,default –v –w –c –e
    //    java –jar scraper.jar -f urls.txt Greece,credit*. –v –w –c –e
    public static void main(String[] args) throws Exception {
        if ("--help".equals(args[0])) {
            System.out.println(Scraper.man);
            return;
        }

        Config config = new Config();
        int i = 0;

        if ("-f".equals(args[i])) {
            i++;
            config.urlFile = args[i];
        }
        else {
            config.urlString = args[i];
        }
        i++;
        config.wordString = args[i];
        i++;

        //parse options
        for (; i < args.length; i++) {
            if (args[i]!=null && args[i].charAt(0)=='-') {
                if (args[i].length() < 2)
                    throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                switch (args[i].charAt(1)) {
                    case 'v':
                        config.verbosityOpt = true;
                        break;
                    case 'w':
                        config.wordCountOpt = true;
                        break;
                    case 'c':
                        config.characterCountOpt = true;
                        break;
                    case 'e':
                        config.extractSentencesOpt = true;
                        break;
                    default:
                        throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                }
            }
        }

        //Scraper singleton
        Scraper scraper = Scraper.INSTANCE;
        System.out.println(config.toString());
        scraper.setConfig(config);
        scraper.process();
    }
}
