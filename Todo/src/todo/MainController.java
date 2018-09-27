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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author ckok
 */
public class MainController implements Initializable {

    DBHandler db;
    private String descriptionText;
    private long id = -1; //id read from DB used to delete single task
    private ObservableList<TodoItem> tableItems = FXCollections.observableArrayList(); //ObservableList of items
    private boolean onlyActive = true; //used to view all items or only active ones, default=true
    private LocalDate localDate = null; //used to read from DatePicker 
    private LocalDate showDate = null; //used to filter items for one day only

    //declarations of Menu Items
    MenuItem deleteMenuItem = new MenuItem("Delete Item");
    MenuItem setDoneMenuItem = new MenuItem("Set Item to Done");
    MenuItem setActiveItem = new MenuItem("Set Item to Pending");
    MenuItem setDueDate = new MenuItem("Set Due Date");

    //
    //private final String strikeThrough = getClass().getResource("sceneCSS.css").toExternalForm();
    //@FXML
    //private Label label;
    @FXML
    private Button buttonShowOptions;
    @FXML
    private Button buttonShowDate;
    @FXML
    private TextField description;
    @FXML
    private DatePicker datePicker = new DatePicker();

    //@FXML
    //private ListView<String> myActiveList = new ListView<String>();
    //@FXML
    //private ListView<String> myDoneList = new ListView<String>();
    @FXML
    private TableView<TodoItem> tblitems = new TableView<TodoItem>();
    @FXML
    private TableColumn tblColId;
    @FXML
    private TableColumn tblColDesc;
    @FXML
    private TableColumn tblColDate;
    @FXML
    private TableColumn tblColStat;

    //method handling selection of single task via mouse click event
    @FXML
    private void selectTableItem(MouseEvent event) {

        //index from list of taks is read via MouseEvent (any for now)
        int index = tblitems.getSelectionModel().getSelectedIndex();

        //if valid, it's used to find DB's ID
        if (index != -1) {

            id = tblitems.getItems().get(index).getId();
            //terminal printout of both: list ID and DB's ID just for check
            System.out.println(id + "\t" + index);

            //doubleclick sets to done
            if (event.getClickCount() == 2) {
                db.changeItemStatus(id, 0);
                listAllTasks();
            }

            //reset index
            index = -1;
        } else {
            //clear any previous value of id
            id = -1;

        }
    }

    //method handling action on 'Clear list' button
    @FXML
    private void handleButtonDeleteAllAction(ActionEvent event) {

        //call deleteToDoItem without parameters to delete all
        db.deleteToDoItem();
        //refresh list of tasks
        listAllTasks();

    }

    //method handling action on 'Show All/Show active' button
    @FXML
    private void handleButtonShowOptions(ActionEvent event) {

        //call deleteToDoItem without parameters to delete all
        if (onlyActive == true) {
            onlyActive = false;
            buttonShowOptions.setText("Show Active");
        } else {
            onlyActive = true;
            buttonShowOptions.setText("Show All");
        }

        //refresh list of tasks
        listAllTasks();
    }

    //handleDatePicker
    @FXML
    private void handleDatePicker() {
        localDate = datePicker.getValue();
        if (localDate != null) {
            System.out.println(localDate.toString());
            buttonShowDate.setText("Show Date Only");
        } else {
            buttonShowDate.setText("Show All Dates");
        }

    }

    //handle Show Date button
    @FXML
    private void handleButtonShowDate(ActionEvent event) {

        //assign date from localDate from datePicker
        showDate = localDate;
        //if null, keep button text
        if (showDate == null) {
            buttonShowDate.setText("Show All Dates");

            //if not null, change button text
        } else {
            buttonShowDate.setText("Show Date Only");

        }
        //reset datePicker value
        datePicker.setValue(null);
        //refresh list of tasks
        listAllTasks();
    }

    //method to build context menu
    private void buildTableContextMenu() {

        
        //declarations of actions for Menu Items
        //Set Done
        EventHandler<ActionEvent> actionSetDone = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeItemStatus(id, 0);
                listAllTasks();

            }
        };
        
        //Set Active
        EventHandler<ActionEvent> actionSetActive = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeItemStatus(id, 1);
                listAllTasks();
            }
        };

        //Delete
        EventHandler<ActionEvent> actionDelete = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.deleteToDoItem(id);
                listAllTasks();
            }
        };

        //Set Due Date
        EventHandler<ActionEvent> actionSetDueDate = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (localDate == null) {
                    //insert new task with description and status=1 and date=""
                    db.setDueDate(id, "");

                } else {
                    //insert new task with description and status=1 and set date
                    db.setDueDate(id, localDate.toString());
                }
                //reset date in DatePicker
                datePicker.setValue(null);
                listAllTasks();

            }
        };

        //Assignment of actions to Menu Items
        setDoneMenuItem.setOnAction(actionSetDone);
        setActiveItem.setOnAction(actionSetActive);
        deleteMenuItem.setOnAction(actionDelete);
        setDueDate.setOnAction(actionSetDueDate);
        //context menu for TableView
        ContextMenu tableContextMenu = new ContextMenu(setDueDate, setDoneMenuItem, setActiveItem, deleteMenuItem);

        //set context menu for tblitems TableView object
        tblitems.setContextMenu(tableContextMenu);

    }

    //method handling action on textField to add new task (ENTER press)
    @FXML
    private void insertTextField(ActionEvent event) {

        //get description from textField
        descriptionText = description.getText();

        if (localDate == null) {
            //insert new task with description and status=1 and date=""
            db.insertToDoItem(descriptionText, 1, "");

        } else {
            //insert new task with description and status=1 and set date
            db.insertToDoItem(descriptionText, 1, localDate.toString());
        }

        //clear description field, ready for next task
        description.clear();

        //reset prompt text of textField
        description.setPromptText("Add todo task");

        //reset date in DatePicker
        datePicker.setValue(null);

        //refresh list of tasks after every task
        listAllTasks();

    }

    //translate DB's Status (Integer) to readable string
    private String mapStatus(int intStatus) {

        String stringStatus;
        switch (intStatus) {
            case 0:
                stringStatus = "done";
                break;
            case 1:
                stringStatus = "pending";
                break;
            case 2:
                stringStatus = "overdue";
                break;
            default:
                stringStatus = null;
        }
        return stringStatus;
    }

    //method that checks if status should not be set to overdue
    private int checkOverDue(int inStatus, String dueDateString, int id) {
        int outStatus = inStatus;
        //if pending, check the dates
        if (outStatus == 1 && !dueDateString.isEmpty()) {
            Date today = new Date();
            String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);

            //if today is more than dueDate, change status to 'overdue'
            if (todayString.compareTo(dueDateString) > 0) {
                outStatus = 2;
                //update DB as well
                db.changeItemStatus(id, outStatus);
            }
        }
        //if overdue, check dates
        if (outStatus == 2) {
            if (!dueDateString.isEmpty()) {
                Date today = new Date();
                String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);
                //System.out.println(todayString + "\t" + dueDateString);
                //if today is less than dueDate, change status to 'pending'
                if (dueDateString.compareTo(todayString) > 0) {
                    outStatus = 1;
                }
                //if date was removed, cant be overdue, change to pending    
            } else {
                outStatus = 1;
                //update DB as well
                db.changeItemStatus(id, outStatus);
            }

        }

        return outStatus;
    }

//private method for refreshing list of tasks
    private void listAllTasks() {

        //read all DB and save to local variable 'activetoDoList'
        ResultSet toDoList = db.viewTable();

        try {

            //clear ObservableList<TodoItem>
            tableItems.clear();

            //scan all 'activetoDoList'
            while (toDoList.next()) {

                int intStatus = checkOverDue(toDoList.getInt("status"), toDoList.getString("date"), toDoList.getInt("id"));
                //int intStatus = toDoList.getInt("status");
                //if showing only active items
                if (onlyActive == true) {
                    //check if active?
                    if (intStatus != 0) {
                        //check if showDate is set
                        if (showDate == null) {
                            //show all
                            tableItems.add(new TodoItem(toDoList.getInt("id"), toDoList.getString("description"), toDoList.getString("date"), mapStatus(intStatus)));
                        } else {
                            //show only for selected date
                            if (showDate.toString().equals(toDoList.getString("date"))) {
                                tableItems.add(new TodoItem(toDoList.getInt("id"), toDoList.getString("description"), toDoList.getString("date"), mapStatus(intStatus)));
                            }
                        }

                    }
                    //onlyActive is false, show all
                } else {
                    //check if showDate is set
                    if (showDate == null) {
                        //show all
                        tableItems.add(new TodoItem(toDoList.getInt("id"), toDoList.getString("description"), toDoList.getString("date"), mapStatus(intStatus)));
                    } else {
                        //show only for selected date
                        if (showDate.toString().equals(toDoList.getString("date"))) {
                            tableItems.add(new TodoItem(toDoList.getInt("id"), toDoList.getString("description"), toDoList.getString("date"), mapStatus(intStatus)));
                        }
                    }

                }

            }

            //set properties of cells
            //tblColId = new TableColumn("Id");
            tblColId.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("id"));

            //tblColDesc = new TableColumn("Description");
            tblColDesc.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("description"));
            tblColDesc.setStyle( "-fx-alignment: LEFT;");

            //tblColDate = new TableColumn("Date");
            tblColDate.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("date"));
            tblColDate.setStyle( "-fx-alignment: CENTER;");

            //tblColStat = new TableColumn("Status");
            tblColStat.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("status"));
            tblColStat.setStyle( "-fx-alignment: CENTER;");
            /*
            //set text strike-through for Done items
            tblitems.setRowFactory(new Callback<TableView<TodoItem>, TableRow<TodoItem>>() {
                @Override
                public TableRow<TodoItem> call(TableView<TodoItem> tblitemsView) {
                    return new TableRow<TodoItem>() {
                        @Override
                        protected void updateItem(TodoItem todoItem, boolean b) {
                            super.updateItem(todoItem, b);

                            if (todoItem == null) {
                                return;
                            }

                            if (todoItem.getStatus().equals("Done")) // Example requirement
                            {
                                getStyleClass().add("strike");
                            }
                        }
                    };
                }
            });
            */ 
            //populate tblitems (TableView<TodoItem>) to be displayed in App from ObservableList<TodoItem>            
            tblitems.setItems(tableItems);
            tblitems.getColumns().setAll(tblColId, tblColDesc, tblColDate, tblColStat);
            tblColDate.setSortType(TableColumn.SortType.ASCENDING);
            tblitems.getSortOrder().setAll(tblColDate);
            tblitems.getStyleClass().add("strike");

        } catch (SQLException sQLException) {
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //create new DBHandler object and establish connection to DB
        db = new DBHandler("tasks");
        //build context menu
        buildTableContextMenu();
        listAllTasks();

    }
}
