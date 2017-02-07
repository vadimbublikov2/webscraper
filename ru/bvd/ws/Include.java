package ru.bvd.ws;

public class Include {
    private Document document;
    private String statment;
    private String tag;
    private String sample;
    private int positionStart;
    private int positionStop;

    public Include(){}

    public Include(Document document, String statment, String tag, String sample, int positionStart, int positionStop) {
        this.document = document;
        this.statment = statment;
        this.tag = tag;
        this.sample = sample;
        this.positionStart = positionStart;
        this.positionStop = positionStop;
    }


    //get/set
    public void setDocument(Document document) {
        this.document = document;
    }

    public void setStatment(String statment) {
        this.statment = statment;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public void setPositionStart(int positionStart) {
        this.positionStart = positionStart;
    }

    public void setPositionStop(int positionStop) {
        this.positionStop = positionStop;
    }

    public Document getDocument() {
        return document;
    }

    public String getStatment() {
        return statment;
    }

    public String getTag() {
        return tag;
    }

    public String getSample() {
        return sample;
    }

    public int getPositionStart() {
        return positionStart;
    }

    public int getPositionStop() {
        return positionStop;
    }
}
