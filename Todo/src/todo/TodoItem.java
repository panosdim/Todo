/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import javafx.scene.control.Label;

/**
 *
 * @author ckok
 */
public class TodoItem {

    private long id;
    private Label description;
    private String date;
    private int status;
    private int star;
    private int rank;
    private String alarm;
    private int folder_id;

    public TodoItem(long id, Label description, String date, int status, int star, int rank, String alarm, int folder_id) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.status = status;
        this.star = star;
        this.rank = rank;
        this.alarm = alarm;
        this.folder_id = folder_id;
    }

    public int getFolderId() {
        return folder_id;
    }

    public void setFolderId(int folder_id) {
        this.folder_id = folder_id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Label getDescription() {
        return description;
    }

    public void setDescription(Label description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

}
