package ru.bvd.ws;

import java.time.Duration;

public class Document {
    String url;
    int httpStatus = 0;
    String content;
    String statusError;
    Duration getDuration;
    Duration processDuration;
    int wordsCount = 0;

    public Document(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Document{" +
                "url=" + url +
                ", httpStatus=" + httpStatus +
                ", statusError='" + statusError + '\'' +
                ", processDuration=" + processDuration.toMillis() +
                '}';
    }
}