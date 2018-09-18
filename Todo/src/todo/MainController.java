/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.net.URL;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 *
 * @author ckok
 */
public class MainController implements Initializable {
    DBHandler db;
    private String descriptionText;
    
    @FXML
    private Label label;
    @FXML
    private TextField description;
    @FXML
    private TableView myList;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
       // db.insertToDoItem(description, 1);
        
       ResultSet activetoDoList = db.viewTable();
        
    }
    
    @FXML
    private void insertTextField(ActionEvent event) {
        System.out.println("Insert text!");
        descriptionText = description.getText();
        
        db.insertToDoItem(descriptionText, 1);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        db = new DBHandler("tasks");
                
    }    
    
}
