package example;

import ru.bvd.ws.Scraper;

public class Example {
    public static void main(String[] args) throws Exception {
        String scraperString = "-f /home/vadim/IdeaProjects/webscraper/src/resources/list.txt brexit,cameron -v -w -c -e";
        //String scraperString = "--help";
        String[] scraperArgs = scraperString.split(" ");

        Scraper.main(scraperArgs);
    }
}