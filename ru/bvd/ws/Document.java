package ru.bvd.ws;

import java.time.Duration;

/**
 * Storage content, request status, summary extract and processing
 */

public class Document {
    private String url;
    private int httpStatus = 0;
    private String content;
    private String statusError;
    private Duration requestDuration;
    private Duration processDuration;
    private int wordsCount = 0;

    public Document(String url) {
        this.url = url;
    }

    //get/set
    void setUrl(String url) {
        this.url = url;
    }

    void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    void setContent(String content) {
        this.content = content;
    }

    void setStatusError(String statusError) {
        this.statusError = statusError;
    }

    void setRequestDuration(Duration requestDuration) {
        this.requestDuration = requestDuration;
    }

    void setProcessDuration(Duration processDuration) {
        this.processDuration = processDuration;
    }

    void setWordsCount(int wordsCount) {
        this.wordsCount = wordsCount;
    }

    String getUrl() {
        return url;
    }

    int getHttpStatus() {
        return httpStatus;
    }

    String getContent() {
        return content;
    }

    String getStatusError() {
        return statusError;
    }

    Duration getRequestDuration() {
        return requestDuration;
    }

    Duration getProcessDuration() {
        return processDuration;
    }

    int getWordsCount() {
        return wordsCount;
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