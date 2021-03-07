package com.notesapp.notes;

import java.util.Date;

public class Note {
    private int id;
    private Date createdAt;
    private String objectId;
    private String title;
    private String note;
    private int pinned;
    private String toEdit;
    private String toDelete;
    private int notificationID;

    //Creating constructors
    public Note(String objectId, String title, String note, int pinned, String toEdit, String toDelete, int notificationID) {
        this.objectId = objectId;
        this.title = title;
        this.note = note;
        this.pinned = pinned;
        this.toEdit = toEdit;
        this.toDelete = toDelete;
        this.notificationID = notificationID;
    }

    public Note(int id, Date createdAt, String objectId, String title, String note, int pinned, String toEdit, String toDelete, int notificationID) {
        this.id = id;
        this.createdAt = createdAt;
        this.objectId = objectId;
        this.title = title;
        this.note = note;
        this.pinned = pinned;
        this.toEdit = toEdit;
        this.toDelete = toDelete;
        this.notificationID = notificationID;
    }

    public Note(Date createdAt, String objectId, String title, String note, int pinned, String toEdit, String toDelete, int notificationID) {
        this.createdAt = createdAt;
        this.objectId = objectId;
        this.title = title;
        this.note = note;
        this.pinned = pinned;
        this.toEdit = toEdit;
        this.toDelete = toDelete;
        this.notificationID = notificationID;
    }

    //Setter for the id
    public void setId(int id) {
        this.id = id;
    }

    //Getter for all member variables

    public int getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public int getPinned() {
        return pinned;
    }

    public String getToEdit() {
        return toEdit;
    }

    public String getToDelete() {
        return toDelete;
    }

    public int getNotificationID() {
        return notificationID;
    }
}
