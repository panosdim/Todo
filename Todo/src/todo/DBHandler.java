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
public class DBHandler { 
    
   private Connection con;

    DBHandler() {
        try {
            String dbURL = "jdbc:sqlite:Todo.db";
            con = DriverManager.getConnection(dbURL);
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(ex.getMessage());
            alert.setContentText(ex.toString());

            alert.showAndWait();
            exit();
        }
    } 
}
