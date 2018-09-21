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
    private int id = -1; //id read from DB used to delete single task
    private ArrayList<Integer> ids = new ArrayList<Integer>(); // list of DB's IDs
    private ObservableList<String> items =FXCollections.observableArrayList (); //ObservableList of items
     
    @FXML
    private Label label;
    @FXML
    private TextField description;
    @FXML
    private ListView<String> myList = new ListView<String>();

    //method handling selection of single task via mouse click event
    @FXML
    private void selectListItem(MouseEvent event) {
        //first id from list of taks is read, then it's used as index of
        // ArrayList ids to find DB's ID
        id = ids.get(myList.getSelectionModel().getSelectedIndex());
        //set no warning via lable
        label.setText("");
        //terminal printout of both: list ID and DB's ID just for check
        System.out.println(id + "\t" + myList.getSelectionModel().getSelectedIndex());
    }
    
    //method handling action on 'Deleted Selected' button
    @FXML
    private void handleButtonDeleteOneAction(ActionEvent event) {
        
        //If there was no selection prior, id==-1, no deletion
        //warning via label
        if(id == -1) {
            label.setText("No item selected!!!");
            
        //Selection done prior, id!=-1, perform deletion of selected task
        } else {
            
            db.deleteToDoItem(id);
            //reset id to -1
            id = -1;
        }
        //refresh list of tasks
        listAllTasks ();
    }

    //method handling action on 'Clear list' button
    @FXML
    private void handleButtonDeleteAllAction(ActionEvent event) {
        
        //call deleteToDoItem without parameters to delete all
        db.deleteToDoItem();
        
        //refresh list of tasks
        listAllTasks ();
    }
    
    //method handling action on textField to add new task (ENTER press)
    @FXML
    private void insertTextField(ActionEvent event) {
        
        //get description from textField
        descriptionText = description.getText();
        
        //insert new task with description and status=1
        db.insertToDoItem(descriptionText, 1);
        
        //clear description field, ready for next task
        description.clear();
        
        //reset prompt text of textField
        description.setPromptText("Add todo task");
        
        //refresh list of tasks after every task
        listAllTasks ();

    }
    
    //private method for refreshing list of tasks
    private void listAllTasks () {
        
        //read all DB and save to local variable 'activetoDoList'
        ResultSet activetoDoList = db.viewTable();
        
         try {
             
             //clear both Lists
             ids.clear();
             items.clear();
             
             //scan all 'activetoDoList'
             while (activetoDoList.next()) {
                 
                //add 'description' from DB as new item in ObservableList
                items.add(activetoDoList.getString("description"));
                
                //store DB's ID to ArrayList
                ids.add(activetoDoList.getInt("id"));
                //System.out.println("List="+ids.indexOf(activetoDoList.getInt("id"))+" DB="+activetoDoList.getInt("id"));
                
         }
             //populate myList to be displayed in App from ObservableList            
             myList.setItems(items);
             
        } catch (SQLException sQLException) {
        }        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        //create new DBHandler object and establish connection to DB
        db = new DBHandler("tasks");
        
        //refresh list of tasks
        listAllTasks ();        
    }    
    
}
