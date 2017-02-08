package ru.bvd.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Multithreading request data from web resources and processing includes words
 */

public class Extractor implements Callable<Scraper.Includes> {
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0";
    private Scraper.Documents documents;
    private Scraper.Words words;

    public Extractor(Scraper.Documents documents, Scraper.Words words) {
        this.documents = documents;
        this.words = words;
    }


    @Override
    public Scraper.Includes call() {

        Scraper.Includes includesLocal = new Scraper.Includes();
        final BlockingDeque<String> queue = Scraper.INSTANCE.getQueue();

        while (queue.size()>0 && !Thread.currentThread().isInterrupted()) {
            LocalDateTime durationFrom = LocalDateTime.now();
            String urlFromQueue = queue.poll();
            Document doc = new Document( urlFromQueue );

            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(urlFromQueue);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", USER_AGENT);
                doc.setHttpStatus( con.getResponseCode() );
                if (doc.getHttpStatus() != 200) {
                    doc.setStatusError("Response code not 200 OK");
                    documents.add(doc);
                    continue;
                }

                //System.out.println(Thread.currentThread().getName() + " Sending 'GET' request to URL : " + doc.url);

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } catch (IOException e) {
                    doc.setStatusError( e.getMessage() );
                    documents.add(doc);
                    continue;
                }
            }
            catch (Exception e) {
                doc.setStatusError( e.getMessage() );
                documents.add(doc);
                continue;
            }
            //System.out.println(response.toString());
            doc.setContent( response.toString() );
            documents.add(doc);
            if (doc.getStatusError() != null)
                return includesLocal;

            int wordsCount = 0;
            doc.setRequestDuration( Duration.between(durationFrom,LocalDateTime.now()) );
            durationFrom = LocalDateTime.now();
            for (String word : words) {
                Pattern regexp = Pattern.compile("(>| |\\.)"+word+"(<| |\\.)",Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                Matcher m = regexp.matcher(doc.getContent());
                while (m.find()) {
                    wordsCount++;
                    char symbol1;
                    char symbol2;
                    //search open tag
                    int i = m.start()-1;
                    do {
                        symbol1 = doc.getContent().charAt(i);
                        if ('<'==symbol1) break;
                        i--;
                    } while( true );
                    int startTagName = i+1;
                    //search name tag
                    i++;
                    do {
                        symbol1 = doc.getContent().charAt(i);
                        if ('>'==symbol1 || ' '==symbol1) break;
                        i++;
                    } while ( true );
                    int endTagName = i;
                    String tagName = doc.getContent().substring(startTagName,endTagName).toLowerCase();

                    //search start statement
                    i = m.start();
                    do {
                        symbol1 = doc.getContent().charAt(i);
                        if('>'==symbol1 || '<'==symbol1 || ("script;meta;img;/p;p;iframe;".contains(tagName+";")&&'"'==symbol1) ) {
                            i++;
                            break;
                        }
                        symbol2 = doc.getContent().charAt(i+1);
                        if('.'==symbol1 && ' '==symbol2) {
                            i=i+2;
                            break;
                        }
                        i--;
                    } while( true );
                    int startStatement = i;
                    //search next tag or end statement
                    i = m.start()+1;
                    do {
                        symbol1 = doc.getContent().charAt(i);
                        if('>'==symbol1 || '<'==symbol1  || ("script;meta;img;/p;p;iframe;".contains(tagName+";")&&'"'==symbol1) ){
                            break;
                        }
                        symbol2 = doc.getContent().charAt(i+1);
                        if('.'==symbol1 && ' '==symbol2){
                            i++;
                            break;
                        }
                        i++;
                    } while( true );
                    int endStatement = i;

//                  startStatement = Math.max(startStatement,m.start()-50);
//                  endStatement = Math.min(endStatement,m.start()+200);
                    includesLocal.add( new Include(doc, doc.getContent().substring(startStatement,endStatement), tagName,word, 0, 0) );
                }
            }
            doc.setProcessDuration( Duration.between(durationFrom, LocalDateTime.now()) );
            doc.setWordsCount( wordsCount );
        }
        return includesLocal;
    }
}
