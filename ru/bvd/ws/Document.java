package ru.bvd.ws;

import java.time.Duration;

public class Document {
    private String url;
    private int httpStatus = 0;
    private String content;
    private String statusError;
    private Duration getDuration;
    private Duration processDuration;
    private int wordsCount = 0;

    public Document(String url) {
        this.url = url;
    }

    //get/set
    public void setUrl(String url) {
        this.url = url;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStatusError(String statusError) {
        this.statusError = statusError;
    }

    public void setGetDuration(Duration getDuration) {
        this.getDuration = getDuration;
    }

    public void setProcessDuration(Duration processDuration) {
        this.processDuration = processDuration;
    }

    public void setWordsCount(int wordsCount) {
        this.wordsCount = wordsCount;
    }

    public String getUrl() {
        return url;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getContent() {
        return content;
    }

    public String getStatusError() {
        return statusError;
    }

    public Duration getGetDuration() {
        return getDuration;
    }

    public Duration getProcessDuration() {
        return processDuration;
    }

    public int getWordsCount() {
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