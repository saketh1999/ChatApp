package com.example.kacchat;

public class Messages {
    private String from,message,type,to,mesageID,time,date,name;
    public Messages(){

    }

    public Messages(String from, String message, String type, String to, String mesageID, String time, String date, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.mesageID = mesageID;
        this.time = time;
        this.date = date;
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMesageID() {
        return mesageID;
    }

    public void setMesageID(String mesageID) {
        this.mesageID = mesageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
