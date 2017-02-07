package ru.bvd.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ScraperTest {
    private static final char LF = '\n';
    private static final char CR = '\r';
    private static final String CRLF = "" + CR + LF;
    private static final int port = 8080;
    private static final String testDocument1 = "/home/vadim/IdeaProjects/webscraper/src/resources/testDocument1.html";

    private ServerSocket serverSocket;

    public static void main(String[] args) throws InterruptedException, IOException {
        ScraperTest scraperTest = new ScraperTest();
        scraperTest.process();
    }

    private void  process () throws InterruptedException, IOException {
        serverSocket = new ServerSocket(port);
        Thread thread = new Thread( new ScraperTestServer() );
        thread.start();

        //test1
        URL url;
        try {
            url = new URL("http://localhost:" + port +"/test1");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int httpStatus = con.getResponseCode();

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(response);



            thread.interrupt();
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            thread.interrupt();
        }



    }

    private class ScraperTestServer implements Runnable {
        @Override
        public void run() {

            List<String> linesFile;
            try {
                linesFile = Files.readAllLines(Paths.get(testDocument1));
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
                        for (String s: linesFile) {
                            output.write(s.getBytes());
                        }

                        output.flush();
                        System.out.println(request);
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