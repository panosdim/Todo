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
import static javafx.application.Platform.exit;

/**
 *
 * @author ckok
 */
// con is a Connection object and dbName is the name of the database
public class DBHandler {

    private Connection con;

    DBHandler() {
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

            System.out.println("Connection to SQLite has been established.");

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

    // This method outputs the contents of the table Tasks
    // In the method, con is a Connection object and dbName is the name of 
    // the database in which you are creating the table.
    public void viewTable(String dbName) {

          try {
            Statement statement = con.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            ResultSet rs = statement.executeQuery("SELECT * FROM " + dbName);
            while (rs.next()) {
                // read the result set
                System.out.println("date = " + rs.getString("date"));
                System.out.println("description = " + rs.getString("description"));
                System.out.println("status = " + rs.getInt("status"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException sQLException) {
        }
    }
}
