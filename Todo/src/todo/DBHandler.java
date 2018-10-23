/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import java.text.SimpleDateFormat;
import static javafx.application.Platform.exit;
import java.util.Date;
import javafx.collections.FXCollections;

/**
 *
 * @author ckok
 */
// con is a Connection object and dbName is the name of the database
public class DBHandler {

    private Connection con;
    private String dbName;

    DBHandler(String dbName1) {

        dbName = dbName1;

        try {
            String dbURL = "jdbc:sqlite:Todo.db";
            // First, you need to establish a connection with the data source you want to use. 
            // A data source can be a DBMS, a legacy file system, or some other source of data 
            // with a corresponding JDBC driver. Typically, a JDBC application connects to a 
            // target data source using one of two classes:
            // DriverManager: This fully implemented class connects an application to a data source, which is specified 
            // by a database URL. When this class first attempts to establish a connection, it automatically loads any 
            // JDBC 4.0 drivers found within the class path. Note that your application must manually load any JDBC drivers 
            // prior to version 4.0.    

            con = DriverManager.getConnection(dbURL);

            System.out.println(dbName + " Connection to SQLite has been established.");

            // When JDBC encounters an error during an interaction with a data source, it throws an instance of SQLException as 
            // opposed to Exception. (A data source in this context represents the database to which a Connection object is connected.) 
            // The SQLException instance contains the following information that can help you determine the cause of the error:
            // A description of the error. Retrieve the String object that contains this description by calling the method 
            // SQLException.getMessage.
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(ex.getMessage());
            alert.setContentText(ex.toString());

            alert.showAndWait();
            exit();
        }
    }

    //This method inserts new entry into DB, table folders
    //with name from parameter list
    public void insertFolderItem(String name) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //     Date d = new Date();
            //     SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
            //INSERT query with description and status from parameters
            //and current date
            //no id is provided, DB handles on it's own
            String query = "INSERT INTO " + dbName + " (name) "
                    + "VALUES ('" + name + "')";
            System.out.println(query);
            statement.executeQuery(query);

        } catch (SQLException sQLException) {
        }
    }

    //This method deletes one entry from DB
    //based on parameter id
    public void deleteFolder(int folder_id) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //DELETE query is created WHERE id matches given id
            String query = "DELETE FROM " + dbName + " WHERE folder_id=" + folder_id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    // This method outputs the contents of the table folders
    // In the method, con is a Connection object and dbName is the name of 
    // the database in which you are creating the table.
    public ObservableList<FolderItem> viewFolderTable() {

        try {
            Statement statement = con.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String query = "SELECT * FROM " + dbName;

            System.out.println(query);
            ResultSet rs = statement.executeQuery(query);
            ObservableList<FolderItem> allFolders = FXCollections.observableArrayList();
            while (rs.next()) {
                allFolders.add(new FolderItem(rs.getInt("folder_id"), rs.getString("name")));
            }
            return allFolders;
        } catch (SQLException sQLException) {
        }
        return null;
    }

    //This method inserts new entry into DB
    //with description and status from parameter list
    //and current date
    //DB handles ID on it's own
    public void insertToDoItem(String Description, int Status, String date, int star, int rank) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //     Date d = new Date();
            //     SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
            //INSERT query with description and status from parameters
            //and current date
            //no id is provided, DB handles on it's own
            String query = "INSERT INTO " + dbName + " (Description, Date, Status, Starred, rank, folder_id) "
                    + "VALUES ('" + Description + "', '" + date + "', " + Status + ", " + star + ", " + rank + ", 1)";
            System.out.println(query);
            statement.executeQuery(query);

        } catch (SQLException sQLException) {
        }
    }

    //This method deletes all entries from DB
    //since parameter id is missing
    public void deleteToDoItem() {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //DELETE all query
            String query = "DELETE FROM " + dbName;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //This method deletes one entry from DB
    //based on parameter id
    public void deleteToDoItem(long id) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //DELETE query is created WHERE id matches given id
            String query = "DELETE FROM " + dbName + " WHERE id=" + id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //This method deletes entries from specified folder
    //based on parameter id
    public void deleteFolderItems(int folder_id) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //DELETE query is created WHERE id matches given id
            String query = "DELETE FROM " + dbName + " WHERE folder_id=" + folder_id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //This method moves all Items from one folder to default folder (id=1)
    public void moveItemsDefault(int folder_id) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //UPDATE query is created WHERE folder_id matches given folder_id
            //SET folder_id = 1
            String query = "UPDATE " + dbName + " SET folder_id = 1 WHERE folder_id=" + folder_id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //setDueDate
    //This method changes status of one entry from DB
    //based on parameter id and status
    public void setDueDate(long id, String date) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //UPDATE query is created WHERE id matches given id
            //SET Date = date
            String query = "UPDATE " + dbName + " SET Date = '" + date + "' WHERE id=" + id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //assignFolder
    //This method assigns folder to the item from DB
    //based on parameter id and folderName
    public void assignFolder(long id, String folderName) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //UPDATE query is created WHERE id matches given id
            //SET Date = date
            String query = "UPDATE " + dbName + " SET Folder = '" + folderName + "' WHERE id=" + id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //editDescription
    //This method edits description of one entry from DB
    //based on parameter id and desc
    public void editDescription(long id, String desc) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //UPDATE query is created WHERE id matches given id
            //SET Date = date
            String query = "UPDATE " + dbName + " SET Description = '" + desc + "' WHERE id=" + id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

//changeStarred
    //This method changes status of one entry from DB
    //based on parameter id and status
    public void changeStarred(long id, int star) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //UPDATE query is created WHERE id matches given id
            //SET Status = 1 (active)
            String query = "UPDATE " + dbName + " SET Starred = " + star + " WHERE id=" + id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //changeItemStatus
    //This method changes status of one entry from DB
    //based on parameter id and status
    public void changeItemStatus(long id, int status, int rank) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String query;
            //UPDATE query is created WHERE id matches given id
            //SET Status = status (active(1)/done(0))
            switch (status) {
                case 0:
                    query = "UPDATE " + dbName + " SET Status = " + status + ", alarm = null, Starred = 0, Rank = -1 WHERE id=" + id;
                    break;
                case 1:
                    query = "UPDATE " + dbName + " SET Status = " + status + ", alarm = null, Starred = 0, Rank = " + rank + " WHERE id=" + id;
                    break;
                default: //case 2:
                    query = "UPDATE " + dbName + " SET Status = " + status + ", alarm = null, Starred = 0 WHERE id=" + id;
                    break;
            }

            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //changeItemRank
    //This method changes rank of one entry from DB
    //based on parameter id and rank
    public void changeItemRank(long id, int rank) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //UPDATE query is created WHERE id matches given id
            //SET Status = 1 (active)
            String query = "UPDATE " + dbName + " SET Rank = " + rank + " WHERE id=" + id;
            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    //setAlarm
    //This method changes rank of one entry from DB
    //based on parameter id and rank
    public void setAlarm(long id, String alarm) {

        try {
            Statement statement = con.createStatement();

            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            //UPDATE query is created WHERE id matches given id
            //SET Status = 1 (active)
            String query;
            if (alarm == null) {
                query = "UPDATE " + dbName + " SET alarm = " + alarm + " WHERE id=" + id;
            } else {
                query = "UPDATE " + dbName + " SET alarm = '" + alarm + "' WHERE id=" + id;
            }

            System.out.println(query);
            statement.executeQuery(query);
        } catch (SQLException sQLException) {
        }
    }

    // This method outputs the contents of the table Tasks
    // In the method, con is a Connection object and dbName is the name of 
    // the database in which you are creating the table.
    public ArrayList<TodoItem> viewTable() {

        try {
            Statement statement = con.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            String query = "SELECT * FROM " + dbName;;

            System.out.println(query);
            ResultSet rs = statement.executeQuery(query);
            ArrayList<TodoItem> allItems = new ArrayList<TodoItem>();
            while (rs.next()) {
                allItems.add(new TodoItem(rs.getInt("id"), rs.getString("description"), rs.getString("date"), rs.getInt("status"), rs.getInt("starred"), rs.getInt("rank"), rs.getString("alarm"), rs.getInt("folder_id")));
            }
            return allItems;
        } catch (SQLException sQLException) {
        }
        return null;
    }
}
