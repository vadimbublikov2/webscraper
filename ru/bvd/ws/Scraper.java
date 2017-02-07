package ru.bvd.ws;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

class Scraper {
    //---------static
    static final Scraper INSTANCE = new Scraper();
    static final String man = " webscraper - console web scraper (http://en.wikipedia.org/wiki/Web_scraping)\n"+
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


    private final BlockingDeque<String> queue = new LinkedBlockingDeque<>();
    BlockingDeque<String> getQueue() {
        return queue;
    }


}
