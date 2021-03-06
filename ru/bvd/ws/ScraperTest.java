package ru.bvd.ws;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test for Scraper. Check result output test case. Contains socket server for get test documents.
 */

public class ScraperTest {
    private static final char LF = '\n';
    private static final char CR = '\r';
    private static final String CRLF = "" + CR + LF;
    private static final int port = 8080;
    private static final String testDocument1 = "/home/vadim/IdeaProjects/webscraper/src/resources/testDocument1.html";
    private static final String testDocument2 = "/home/vadim/IdeaProjects/webscraper/src/resources/testDocument2.html";

    private ServerSocket serverSocket;

    public static void main(String[] args) throws Exception {
        ScraperTest scraperTest = new ScraperTest();
        scraperTest.process();
    }

    private void  process () throws Exception {
        serverSocket = new ServerSocket(port);
        Thread thread = new Thread( new ScraperTestServer() );
        thread.start();

        String scraperString = "-f /home/vadim/IdeaProjects/webscraper/src/resources/testList.txt API,xpath -w -c -e";
        String[] scraperArgs = scraperString.split(" ");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);
        PrintStream oldps = System.out;
        System.setOut(ps);

        Scraper.main(scraperArgs);
        System.out.flush();
        System.setOut(oldps);

        String outStr = out.toString();
        if (outStr.contains("http://localhost:8080/test1, characters count=86219, words include=3"))
            System.out.println("Test1 success");
        else
            System.out.println("Test1 error");
        if (outStr.contains("http://localhost:8080/test2, characters count=443322, words include=13"))
            System.out.println("Test2 success");
        else
            System.out.println("Test2 error");

        System.out.println(outStr);

        thread.interrupt();
        serverSocket.close();
    }

    private class ScraperTestServer implements Runnable {
        @Override
        public void run() {

            List<String> linesFileTest1;
            try {
                linesFileTest1 = Files.readAllLines(Paths.get(testDocument1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<String> linesFileTest2;
            try {
                linesFileTest2 = Files.readAllLines(Paths.get(testDocument2));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = serverSocket.accept();

                    String request;
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        request = bufferedReader.readLine();
                        OutputStream output = socket.getOutputStream();
                        output.write(("HTTP/1.0 200 OK" + CRLF + CRLF).getBytes());
                        if (request.contains("/test1")) {
                            for (String s : linesFileTest1) {
                                output.write(s.getBytes());
                            }
                        }
                        else if (request.contains("test2")) {
                            for (String s : linesFileTest2) {
                                output.write(s.getBytes());
                            }
                        }
                        output.flush();
                        //System.out.println(request);
                    } catch (IOException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                catch (IOException e) {
                    Thread.currentThread().interrupt();
                }
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}