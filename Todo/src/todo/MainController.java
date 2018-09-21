/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 *
 * @author ckok
 */
public class MainController implements Initializable {
    DBHandler db;
    private String descriptionText;
    private int id = -1;
    //private String itemToDelete = null;
    private ArrayList<Integer> ids = new ArrayList<Integer>();
     
    @FXML
    private Label label;
    @FXML
    private TextField description;
    @FXML
    private ListView<String> myList = new ListView<String>();

    
    @FXML
    private void selectListItem(MouseEvent event) {
        id = ids.get(myList.getSelectionModel().getSelectedIndex());
        label.setText("");
        //itemToDelete = myList.getSelectionModel().getSelectedItem();
        System.out.println(id + "\t" + myList.getSelectionModel().getSelectedIndex());
    }
    
    @FXML
    private void handleButtonDeleteOneAction(ActionEvent event) {
        
        if(id == -1) {
            label.setText("No item selected!!!");
        } else {
            
            db.deleteToDoItem(id);
            id = -1;
        }
        listAllTasks ();
    }

    @FXML
    private void handleButtonDeleteAllAction(ActionEvent event) {
        
        db.deleteToDoItem();
        listAllTasks ();
    }
    
    @FXML
    private void insertTextField(ActionEvent event) {
        //System.out.println("Insert text!");
        descriptionText = description.getText();
        
        db.insertToDoItem(descriptionText, 1);
        description.clear();
        description.setPromptText("Add todo task");
        listAllTasks ();

    }
    
    private void listAllTasks () {
        ResultSet activetoDoList = db.viewTable();
        ObservableList<String> items =FXCollections.observableArrayList ();
       
         try {
            while (activetoDoList.next()) {
                items.add(activetoDoList.getString("description"));
                ids.add(activetoDoList.getInt("id"));
                //System.out.println(activetoDoList.getInt("id") + "\t" + activetoDoList.getString("description"));
         }
         myList.setItems(items);
        } catch (SQLException sQLException) {
        }        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        db = new DBHandler("tasks");
        listAllTasks ();        
    }    
    
}
