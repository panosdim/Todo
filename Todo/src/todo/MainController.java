/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.awt.Graphics;
import java.awt.Image;
import static java.awt.SystemColor.control;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.IOException;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.TilePane;
import javafx.stage.Popup;
import javafx.util.Callback;
import javax.swing.*;

/**
 *
 * @author ckok
 */
public class MainController implements Initializable {

    DBHandler db;
    private String descriptionText;
    private long id = -1; //id read from DB used to delete single task
    private ObservableList<TodoItem> tableItems = FXCollections.observableArrayList(); //ObservableList of items
    private ObservableList<String> menuItems = FXCollections.observableArrayList(); //ObservableList of items
    private boolean onlyActive = true; //used to view all items or only active ones, default=true
    private boolean onlyStarred = false;
    private LocalDate localDate = null; //used to read from DatePicker 
    private LocalDate showDateStart = null; //used to filter items for one day only
    private LocalDate showDateEnd = null;
    private DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

    //declarations of Menu Items
    MenuItem deleteMenuItem = new MenuItem("Delete Item");
    MenuItem setDoneMenuItem = new MenuItem("Set Item to Done");
    MenuItem setActiveItem = new MenuItem("Set Item to Pending");
    Menu setDueDate = new Menu("Set Due Date");
    //MenuItem setDueToday = new MenuItem("today");
    //MenuItem setDueTomorrow = new MenuItem("tomorrow");
    DatePicker menuDatePicker = new DatePicker();
    MenuItem setDueDatePicker = new MenuItem();
    MenuItem editItem = new MenuItem("Edit");
    MenuItem starItem = new MenuItem("Set Favorite");
    MenuItem unstarItem = new MenuItem("Unfavor");

    //
    //private final String strikeThrough = getClass().getResource("sceneCSS.css").toExternalForm();
    //@FXML
    //private Label label;
    @FXML
    private Button buttonShowOptions;
    //@FXML
    //private Button buttonShowDate;
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
    @FXML
    private TableColumn tblColStar;

    @FXML
    private ListView menuList = new ListView<String>();
    @FXML
    private TilePane PaneEditItem;
    
    @FXML
    private Label Descriptionlabel;
    @FXML
    private Label Statuslabel;
    @FXML
    private Label Duedatelabel;
    @FXML
    private TextField descriptionEdit;
    @FXML
    private TextField StatusEdit;
    @FXML
    private DatePicker datePickerEdit = new DatePicker();

    //method handling tooltip in the left list via mouse 
    @FXML
    private void toolTipList(MouseEvent event) {
        //index from list of task is read via MouseEvent (any for now)
        int index = menuList.getSelectionModel().getSelectedIndex();
        System.out.println(index);
        //if valid, it's used for menu actions (filtering items by dates)
        switch (index) {
            case -1:
                break;
            case 0:
                menuList.tooltipProperty();
                menuList.setTooltip(new Tooltip("khdfbjkdsfhb"));
                //menuList.setToolTipText("Search...");
                System.out.println("test0");
                index = -1;
                event.consume();
                break;
            case 1:
                System.out.println("test1");
                index = -1;
                event.consume();
                break;
            case 2:
                System.out.println("test2");
                index = -1;
                event.consume();
                break;
            case 3:
                System.out.println("test3");
                index = -1;
                event.consume();
                break;
            case 4:
                System.out.println("test4");
                index = -1;
                event.consume();
                break;
            case 5:
                System.out.println("test5");
                index = -1;
                event.consume();
                break;
        }
    }

    //method handling selection of single task via mouse click event
    @FXML
    private void selectTableItem(MouseEvent event) {

        //index from list of task is read via MouseEvent (any for now)
        int index = tblitems.getSelectionModel().getSelectedIndex();

        //if valid, it's used to find DB's ID
        if (index != -1) {

            id = tblitems.getItems().get(index).getId();
            //terminal printout of both: list ID and DB's ID just for check
            System.out.println(id + "\t" + index + "\t" + tblitems.getItems().get(index).getStar());

            //dynamic context menu
            dynamicContextMenu(index);

            //doubleclick sets to done
            /*if (event.getClickCount() == 2) {
                db.changeItemStatus(id, 0);
                listAllTasks();
            }*/
            //reset index
            index = -1;
        } else {
            //clear any previous value of id
            id = -1;

        }
    }

    @FXML
    private void selectMenuList(MouseEvent event) {

        //index from list of task is read via MouseEvent (any for now)
        int index = menuList.getSelectionModel().getSelectedIndex();

        //if valid, it's used for menu actions (filtering items by dates)
        switch (index) {
            case -1:
                break;
            case 0:
                showDateStart = null;
                onlyStarred = false;
                description.setPromptText("Add new todo task");
                listAllTasks();
                index = -1;
                break;
            case 1:
                showDateStart = null;
                onlyStarred = true;
                description.setPromptText("Add new favorite todo task");
                listAllTasks();
                index = -1;
                break;
            case 2:
                showDateStart = showDateEnd = LocalDate.now();
                onlyStarred = false;
                description.setPromptText("Add new todo task for today");
                listAllTasks();
                index = -1;
                break;
            case 3:
                showDateStart = showDateEnd = LocalDate.now().plus(1, ChronoUnit.DAYS);
                onlyStarred = false;
                description.setPromptText("Add new todo task for tomorrow");
                listAllTasks();
                index = -1;
                break;
            case 4:
                showDateStart = LocalDate.now().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
                showDateEnd = LocalDate.now().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7);
                onlyStarred = false;
                description.setPromptText("Add new todo task for today");
                listAllTasks();
                index = -1;
                break;
            case 5:
                showDateStart = LocalDate.now().withDayOfMonth(1);
                showDateEnd = LocalDate.now().withDayOfMonth(showDateStart.lengthOfMonth());
                onlyStarred = false;
                description.setPromptText("Add new todo task for today");
                listAllTasks();
                index = -1;
                break;
        }

    }

    //method handling action on 'Clear list' button
    @FXML
    private void handleButtonDeleteAllAction(ActionEvent event) {
        //Dialog Box
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Would You Like To Delete All?");
        alert.setContentText("Please choose an option.");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yesButton) {
            db.deleteToDoItem();
            //refresh list of tasks
            listAllTasks();
        } else if (result.get() == noButton) {
            event.consume();
        } else if (result.get() == cancelButton) {
            event.consume();
        }
    }

     //method handling action on 'Help' button
    @FXML
    private void handleButtonHelpAction(ActionEvent event) {  

        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler" + " C:\\Users\\ckok\\Downloads\\Todo\\Todo\\src\\todo\\help.pdf");  //file path
            
            }
        catch (IOException e) {
        }
         
      
    }
    //method handling action on 'Show All/Show active' button
    @FXML
    private void handleButtonShowOptions(ActionEvent event) {

        //call deleteToDoItem without parameters to delete all
        if (onlyActive == true) {
            onlyActive = false;
            buttonShowOptions.setText("Hide Done");
        } else {
            onlyActive = true;
            buttonShowOptions.setText("Show Done");
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
            description.setPromptText("Add new todo task for selected date");

        } else {
            //buttonShowDate.setText("Show All Dates");
        }

    }

    @FXML
    private void handleDescriptionEdit() {
        
        
        //TodoItem editedItem;
        //String newDesc = tblitems.;

        //db.editDescription(tblitems.getItems().get(tblitems.getSelectionModel().getSelectedIndex()).getId(), newDesc);
        //listAllTasks();
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

        //Set Starred
        EventHandler<ActionEvent> actionSetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 1);
                listAllTasks();
            }
        };

        //UnStar
        EventHandler<ActionEvent> actionResetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 0);
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

                localDate = menuDatePicker.getValue();
                //menuDatePicker.getOnMouseClicked();
                if (localDate == null) {
                    //dont do anything!!!
                    //db.setDueDate(id, "");

                } else {
                    //insert new task with description and status=1 and set date
                    db.setDueDate(id, localDate.toString());
                    //db.setDueDate(id, menuDatePicker.getValue().toString());
                }
                //reset date in DatePicker
                menuDatePicker.setValue(null);
                listAllTasks();

            }
        };
        /*
        EventHandler<ActionEvent> actionSetDueDateToday = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //change due date to today
                db.setDueDate(id, LocalDate.now().toString());

                //reset date in DatePicker
                //datePicker.setValue(null);
                listAllTasks();

            }
        };

        EventHandler<ActionEvent> actionSetDueDateTomorrow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //change due date to tomorrow
                db.setDueDate(id, LocalDate.now().plus(1, ChronoUnit.DAYS).toString());

                //reset date in DatePicker
                //datePicker.setValue(null);
                listAllTasks();

            }
        };
         */
        //Edit description
        EventHandler<ActionEvent> actionEdit;
        actionEdit = new EventHandler<ActionEvent>() {
            @Override
            
            public void handle(ActionEvent event) {
                System.out.println("test1");
                PaneEditItem.setVisible(true);
                
                int selectedRowIndex = tblitems.getSelectionModel().getSelectedIndex();
                tblitems.edit(selectedRowIndex, tblitems.getColumns().get(1));

                //tblitems.fireEvent(event);
                
            }
        };

        //Assignment of actions to Menu Items
        setDoneMenuItem.setOnAction(actionSetDone);
        setDoneMenuItem.setGraphic(new ImageView("/todo/done.png"));
        setActiveItem.setOnAction(actionSetActive);
        setActiveItem.setGraphic(new ImageView("/todo/undo.png"));
        deleteMenuItem.setOnAction(actionDelete);
        deleteMenuItem.setGraphic(new ImageView("/todo/delete.png"));
        //setDueDate.setOnAction(actionSetDueDate);
        //setDueToday.setOnAction(actionSetDueDateToday);
        //setDueTomorrow.setOnAction(actionSetDueDateTomorrow);
        setDueDatePicker.setGraphic(menuDatePicker);
        setDueDatePicker.setOnAction(actionSetDueDate);
        setDueDate.getItems().addAll(/*setDueToday, setDueTomorrow,*/setDueDatePicker);
        setDueDate.setGraphic(new ImageView("/todo/calendar.png"));
        editItem.setOnAction(actionEdit);
        editItem.setGraphic(new ImageView("/todo/edit.png"));
        starItem.setOnAction(actionSetStarred);
        starItem.setGraphic(new ImageView("/todo/star.png"));
        unstarItem.setOnAction(actionResetStarred);
        unstarItem.setGraphic(new ImageView("/todo/unstar.png"));
        //context menu for TableView
        ContextMenu tableContextMenu = new ContextMenu(editItem, starItem, unstarItem, setDueDate, setDoneMenuItem, setActiveItem, deleteMenuItem);

        //set context menu for tblitems TableView object
        tblitems.setContextMenu(tableContextMenu);

    }

    //method handling action on textField to add new task (ENTER press)
    @FXML

    private void insertTextField(ActionEvent event) {

        //get description from textField
        descriptionText = description.getText();
        //save previous prompt text
        String prevPromptText = description.getPromptText();

        if (localDate == null) {

            if (showDateStart == null) {
                //insert new task with description and status=1 and date=today by default
                db.insertToDoItem(descriptionText, 1, LocalDate.now().toString(), onlyStarred ? 1 : 0);
            } else if (showDateStart == showDateEnd) {
                //insert new task with description and status=1 and date=showDateStart (today or tomorrow only)
                db.insertToDoItem(descriptionText, 1, showDateStart.toString(), onlyStarred ? 1 : 0);
            } else {
                //insert new task with description and status=1 and date=today by default
                db.insertToDoItem(descriptionText, 1, LocalDate.now().toString(), onlyStarred ? 1 : 0);
            }

        } else {
            //insert new task with description and status=1 and set date
            db.insertToDoItem(descriptionText, 1, localDate.toString(), onlyStarred ? 1 : 0);
        }

        //clear description field, ready for next task
        description.clear();

        //reset prompt text of textField
        description.setPromptText(prevPromptText);

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
                if (dueDateString.compareTo(todayString) >= 0) {
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

    //build side menu
    private void buildSideMenu() {
        menuItems.add("Show All");
        menuItems.add("Show Favorites");
        menuItems.add("Show Today");
        menuItems.add("Show Tomorrow");
        menuItems.add("Show Week");
        menuItems.add("Show Month");

        menuList.setItems(menuItems);
    }

//private method for refreshing list of tasks
    private void listAllTasks() {

        //read all DB and save to local variable 'toDoList'
        ResultSet toDoList = db.viewTable(onlyActive, onlyStarred);

        try {

            //clear ObservableList<TodoItem>
            tableItems.clear();

            //scan all 'activetoDoList'
            while (toDoList.next()) {

                int intStatus = checkOverDue(toDoList.getInt("status"), toDoList.getString("date"), toDoList.getInt("id"));

                //check if showDate is set
                if (showDateStart == null) {
                    //show all
                    tableItems.add(new TodoItem(toDoList.getInt("id"), toDoList.getString("description"), toDoList.getString("date"), mapStatus(intStatus), toDoList.getInt("starred")));
                } else {
                    //show only for selected dates
                    if (showDateStart.toString().compareTo(toDoList.getString("date")) <= 0) {
                        if (showDateEnd.toString().compareTo(toDoList.getString("date")) >= 0) {
                            tableItems.add(new TodoItem(toDoList.getInt("id"), toDoList.getString("description"), toDoList.getString("date"), mapStatus(intStatus), toDoList.getInt("starred")));
                        }
                    }
                }
            }

            //populate tblitems (TableView<TodoItem>) to be displayed in App from ObservableList<TodoItem>            
            tblitems.setItems(tableItems);

            //build table's properties
            buildTable();

        } catch (SQLException sQLException) {
        }
    }

    //buildTable
    //defines all properties for cells, rows, columns and table itself
    //add any change here...
    private void buildTable() {
        //set properties of cells
        //tblColId = new TableColumn("Id");
        tblColId.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("id"));

        //tblColDesc = new TableColumn("Description");
        tblColDesc.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("description"));
        //tblColDesc.setStyle("-fx-alignment: LEFT;");
        tblColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        tblColDesc.setOnEditCommit(
                new EventHandler<CellEditEvent<TodoItem, String>>() {
            @Override
            public void handle(CellEditEvent<TodoItem, String> t) {
                ((TodoItem) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setDescription(t.getNewValue());

                db.editDescription(tblitems.getItems().get(tblitems.getSelectionModel().getSelectedIndex()).getId(), tblitems.getItems().get(tblitems.getSelectionModel().getSelectedIndex()).getDescription());
                listAllTasks();
            }
        }
        );
        //tblColDesc.isEditable();

        //tblColDate = new TableColumn("Date");
        tblColDate.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("date"));
        //tblColDate.setStyle("-fx-alignment: CENTER;");

        //tblColStat = new TableColumn("Status");
        tblColStat.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("status"));
        //tblColStat.setStyle("-fx-alignment: CENTER;");

        //tblColStat = new TableColumn("Status");
        tblColStar.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("star"));
        //set tblColStar to button
        Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>> cellFactory;
        cellFactory = new Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>>() {
            @Override
            public TableCell<TodoItem, Integer> call(final TableColumn<TodoItem, Integer> param) {
                final TableCell<TodoItem, Integer> cell = new TableCell<TodoItem, Integer>() {

                    private ImageView star = new ImageView();

                    @Override
                    public void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            if (item == 1) {
                                setGraphic(new ImageView("/todo/star.png"));
                            } else {
                                setGraphic(new ImageView("/todo/unstar.png"));
                            }
                        }
                    }
                };

                cell.setOnMouseClicked((event) -> {
                    if (cell.getItem() == 1) {
                        cell.setItem(0);
                    } else {
                        cell.setItem(1);
                    }

                    db.changeStarred(tblitems.getItems().get(tblitems.getSelectionModel().getSelectedIndex()).getId(), cell.getItem());
                    listAllTasks();
                });

                return cell;
            }
        };
        
        Callback<TableView<TodoItem>, TableRow<TodoItem>> cellFactoryRow;
        cellFactoryRow = new CallbackImpl();
        tblColStar.setCellFactory(cellFactory);

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
        tblitems.setRowFactory(tv -> {
            TableRow<TodoItem> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    TodoItem draggedTodoItem = tblitems.getItems().remove(draggedIndex);

                    int dropIndex;

                    if (row.isEmpty()) {
                        dropIndex = tblitems.getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }

                    tblitems.getItems().add(dropIndex, draggedTodoItem);

                    event.setDropCompleted(true);
                    tblitems.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });

            return row;
        });
        tblitems.getColumns().setAll( tblColId, tblColDesc, tblColDate, tblColStat, tblColStar);
        tblColDate.setSortType(TableColumn.SortType.ASCENDING);
        //tblColStat.setSortType(TableColumn.SortType.DESCENDING);
        tblitems.getSortOrder().setAll(tblColDate);
        //tblitems.getStyleClass().add("strike");
        tblitems.setEditable(true);
        tblitems.setFixedCellSize(30);
        tblitems.prefHeightProperty().bind(tblitems.fixedCellSizeProperty().multiply(tblitems.getItems().size() + 1).add(1.01));
        tblitems.minHeightProperty().bind(tblitems.prefHeightProperty());
        tblitems.maxHeightProperty().bind(tblitems.prefHeightProperty());
    }

    //disable menu options based on items parameters
    private void dynamicContextMenu(int row) {
        //disable options according to items parameters
        TodoItem currentRow = tblitems.getItems().get(row);
        if (currentRow.getStatus().equals("done")) {
            setDoneMenuItem.setDisable(true);
            setActiveItem.setDisable(false);
        } else {
            setDoneMenuItem.setDisable(false);
            setActiveItem.setDisable(true);
        }

        if (currentRow.getStar() == 1) {
            starItem.setDisable(true);
            unstarItem.setDisable(false);
        } else {
            starItem.setDisable(false);
            unstarItem.setDisable(true);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //create new DBHandler object and establish connection to DB
        db = new DBHandler("tasks");
        buildTableContextMenu();
        buildSideMenu();
        listAllTasks();

    }

    private class CallbackImpl implements Callback<TableView<TodoItem>, TableRow<TodoItem>> {

        public CallbackImpl() {
        }

       // @Override
        public TableRow<TodoItem> call(final TableRow<TodoItem> param) {
            final TableRow<TodoItem> row = new TableRow<TodoItem> () {
                
                //private ImageView star = new ImageView();
                
                @Override
                
                public void updateItem(TodoItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        //setGraphic(null);
                    } else {
                        if (item.getStatus().equals("overdue")) {
                            //getStyleClass().add("redcolor");
                            getStyleClass().add("table-row");
                        }
                    }
                }
            };
            
            return row;
        }

        @Override
        public TableRow<TodoItem> call(TableView<TodoItem> param) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
