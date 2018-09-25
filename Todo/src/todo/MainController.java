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
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;

/**
 *
 * @author ckok
 */
public class MainController implements Initializable {
    DBHandler db;
    private String descriptionText;
    private int id = -1; //id read from DB used to delete single task
    private ArrayList<Integer> activeIds = new ArrayList<Integer>(); // list of DB's IDs
    private ArrayList<Integer> doneIds = new ArrayList<Integer>(); // list of DB's IDs
    private ObservableList<String> activeItems =FXCollections.observableArrayList (); //ObservableList of items
    private ObservableList<String> doneItems =FXCollections.observableArrayList (); //ObservableList of items



    

    @FXML
    private Label label;
    @FXML
    private TextField description;
    @FXML
    private ListView<String> myActiveList = new ListView<String>();
    @FXML
    private ListView<String> myDoneList = new ListView<String>();
    @FXML
    private TableView<TodoItem> tblitems = new TableView<>(); 
    
    
    //method handling selection of single task via mouse click event
    @FXML
    private void selectActiveListItem(MouseEvent event) {
        
        //index from list of taks is read via MouseEvent (any for now)
        int index = myActiveList.getSelectionModel().getSelectedIndex();
        
        //if valid, it's used to find DB's ID
        if (index !=-1){
            
            //for right click
            //if (event.getButton() == MouseButton.SECONDARY) {
                
            //} else {
                //for left click
                id = activeIds.get(index);
                //set no warning via lable
                label.setText("");
                //terminal printout of both: list ID and DB's ID just for check
                System.out.println(id + "\t" + index); 
                //reset index
                index = -1;                
            //}

        } else {
            //clear any previous value of id
            id = -1;
            
        }
    }

    //method handling selection of single task via mouse click event
    @FXML
    private void selectDoneListItem(MouseEvent event) {
        
        //index from list of taks is read via MouseEvent (any for now)
        int index = myDoneList.getSelectionModel().getSelectedIndex();
        
        //if valid, it's used to find DB's ID
        if (index !=-1){
            
            id = doneIds.get(index);
            //set no warning via lable
            label.setText("");
            //terminal printout of both: list ID and DB's ID just for check
            System.out.println(id + "\t" + index); 
            //reset index
            index = -1;
        } else {
            //clear any previous value of id
            id = -1;
            
        }
    }

 /*   
    //method handling action on 'Set to Done' button
    @FXML
    private void handleButtonSetDoneAction(ActionEvent event) {
        
        //If there was no selection prior, id==-1, no deletion
        //warning via label
        if(id == -1) {
            label.setText("No item selected!!!");
            
        //Selection done prior, id!=-1, perform setting to done of selected task
        } else {
            
            db.setItemToDone(id);
            //reset id to -1
            id = -1;
        }
        //refresh list of tasks
        listAllTasks ();
    }
*/

/*    
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

*/
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
        
        //declarations of Menu Items
        MenuItem deleteMenuItem1 = new MenuItem("Delete Item");
        MenuItem deleteMenuItem2 = new MenuItem("Delete Item");
        MenuItem setDoneMenuItem = new MenuItem("Set Item to Done");
        MenuItem setActiveItem = new MenuItem("Set Item to Pending");
    
        //declarations of actions for Menu Items
        //Set Done
        EventHandler<ActionEvent> actionSetDone = new EventHandler<ActionEvent>() {
            @Override    
            public void handle(ActionEvent event) {
             db.setItemToDone(id);
             listAllTasks ();
            }        
        };
        
        //Set Active
        EventHandler<ActionEvent> actionSetActive = new EventHandler<ActionEvent>() {
            @Override    
            public void handle(ActionEvent event) {
             db.setItemToActive(id);
             listAllTasks ();
            }        
        };

        //Delete
        EventHandler<ActionEvent> actionDelete = new EventHandler<ActionEvent>() {
            @Override    
            public void handle(ActionEvent event) {
             db.deleteToDoItem(id);
             listAllTasks ();
            }        
        };
        
        //Assignment of actions to Menu Items
        setDoneMenuItem.setOnAction(actionSetDone);
        setActiveItem.setOnAction(actionSetActive);
        deleteMenuItem1.setOnAction(actionDelete);
        deleteMenuItem2.setOnAction(actionDelete);
        
        //context menu for both lists
        ContextMenu activeContextMenu = new ContextMenu(setDoneMenuItem,deleteMenuItem1);        
        ContextMenu doneContextMenu = new ContextMenu(setActiveItem,deleteMenuItem2);                
        
        //read all DB and save to local variable 'activetoDoList'
        ResultSet toDoList = db.viewTable();
        
         try {
             
             //clear all Lists
             activeIds.clear();
             activeItems.clear();
             doneIds.clear();
             doneItems.clear();             
             
             //scan all 'activetoDoList'
             while (toDoList.next()) {
                
                 //read status
                 //if active, add to activeItems
                 if(toDoList.getInt("status") == 1){
                     
                    //add 'description' from DB as new item in ObservableList
                    activeItems.add(toDoList.getString("description"));
                
                    //store DB's ID to ArrayList
                    activeIds.add(toDoList.getInt("id"));
                    //System.out.println("List="+ids.indexOf(activetoDoList.getInt("id"))+" DB="+activetoDoList.getInt("id"));
                    
                 //if done, add to doneItems   
                 } else {
     
                    //add 'description' from DB as new item in ObservableList
                    doneItems.add(toDoList.getString("description"));
                
                    //store DB's ID to ArrayList
                    doneIds.add(toDoList.getInt("id"));
                    //System.out.println("List="+ids.indexOf(activetoDoList.getInt("id"))+" DB="+activetoDoList.getInt("id"));
                          
                 }
                 
                
         }
             //populate myActiveList to be displayed in App from ObservableList            
             myActiveList.setItems(activeItems);
             myActiveList.setContextMenu(activeContextMenu);
             
             //populate myDoneList to be displayed in App from ObservableList            
             myDoneList.setItems(doneItems);
             myDoneList.setContextMenu(doneContextMenu);
             
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
