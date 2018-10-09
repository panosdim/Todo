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
import java.io.File;
import java.io.IOException;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
    private ObservableList<TodoItem> activeItems = FXCollections.observableArrayList(); //ObservableList of active items
    private ObservableList<TodoItem> doneItems = FXCollections.observableArrayList(); //ObservableList of done items
    private ObservableList<String> menuItems = FXCollections.observableArrayList(); //ObservableList of items
    private boolean onlyActive = true; //used to view all items or only active ones, default=true
    private boolean onlyStarred = false;
    private LocalDate localDate = null; //used to read from DatePicker 
    private LocalDate showDateStart = null; //used to filter items for one day only
    private LocalDate showDateEnd = null;
    private DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private ArrayList<TodoItem> allItems; //stores all DB in ArrayList of TodoItem type

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
    private TableView<TodoItem> activeItemsTable = new TableView<TodoItem>();
    @FXML
    private TableColumn activeItemsTableColId;
    @FXML
    private TableColumn activeItemsTableColDesc;
    @FXML
    private TableColumn activeItemsTableColDate;
    @FXML
    private TableColumn activeItemsTableColStat;
    @FXML
    private TableColumn activeItemsTableColStar;
    @FXML
    private TableColumn activeItemsTableColRank;

    @FXML
    private TableView<TodoItem> doneItemsTable = new TableView<TodoItem>();
    @FXML
    private TableColumn doneItemsTableColId;
    @FXML
    private TableColumn doneItemsTableColDesc;
    @FXML
    private TableColumn doneItemsTableColDate;
    @FXML
    private TableColumn doneItemsTableColStat;
    @FXML
    private TableColumn doneItemsTableColStar;
    @FXML
    private TableColumn doneItemsTableColRank;

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
        int index = activeItemsTable.getSelectionModel().getSelectedIndex();

        //if valid, it's used to find DB's ID
        if (index != -1) {

            id = activeItemsTable.getItems().get(index).getId();
            //terminal printout of both: list ID and DB's ID just for check
            System.out.println(id + "\t" + index + "\t" + activeItemsTable.getItems().get(index).getStar() + "\t rank =" + activeItemsTable.getItems().get(index).getRank());

            //dynamic context menu
            dynamicContextMenu(index);

            //doubleclick sets to done
            /*if (event.getClickCount() == 2) {
                db.changeItemStatus(id, 0);
                listTasks(onlyActive, onlyStarred);
            }*/
            //reset index
            index = -1;
        } else {
            //clear any previous value of id
            id = -1;

        }
    }

    //method handling selection of single task via mouse click event
    @FXML
    private void selectDoneTableItem(MouseEvent event) {

        //index from list of task is read via MouseEvent (any for now)
        int index = doneItemsTable.getSelectionModel().getSelectedIndex();

        //if valid, it's used to find DB's ID
        if (index != -1) {

            id = doneItemsTable.getItems().get(index).getId();
            //terminal printout of both: list ID and DB's ID just for check
            System.out.println(id + "\t" + index + "\t" + doneItemsTable.getItems().get(index).getStar() + "\t rank =" + doneItemsTable.getItems().get(index).getRank());

            //dynamic context menu
            dynamicDoneContextMenu(index);

            //doubleclick sets to done
            /*if (event.getClickCount() == 2) {
                db.changeItemStatus(id, 0);
                listTasks(onlyActive, onlyStarred);
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
                listTasks(onlyActive, onlyStarred);
                index = -1;
                break;
            case 1:
                showDateStart = null;
                onlyStarred = true;
                //onlyActive = true;
                description.setPromptText("Add new favorite todo task");
                listTasks(true, onlyStarred);
                index = -1;
                break;
            case 2:
                showDateStart = showDateEnd = LocalDate.now();
                onlyStarred = false;
                description.setPromptText("Add new todo task for today");
                listTasks(onlyActive, onlyStarred);
                index = -1;
                break;
            case 3:
                showDateStart = showDateEnd = LocalDate.now().plus(1, ChronoUnit.DAYS);
                onlyStarred = false;
                description.setPromptText("Add new todo task for tomorrow");
                listTasks(onlyActive, onlyStarred);
                index = -1;
                break;
            case 4:
                showDateStart = LocalDate.now().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
                showDateEnd = LocalDate.now().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7);
                onlyStarred = false;
                description.setPromptText("Add new todo task for today");
                listTasks(onlyActive, onlyStarred);
                index = -1;
                break;
            case 5:
                showDateStart = LocalDate.now().withDayOfMonth(1);
                showDateEnd = LocalDate.now().withDayOfMonth(showDateStart.lengthOfMonth());
                onlyStarred = false;
                description.setPromptText("Add new todo task for today");
                listTasks(onlyActive, onlyStarred);
                index = -1;
                break;
        }
        //set visibility of show done button
        buttonShowOptions.setVisible(!onlyStarred);
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
            listTasks(onlyActive, onlyStarred);
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
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler" + " help.pdf");  //file path

        } catch (IOException e) {
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
        listTasks(onlyActive, onlyStarred);
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
        //listTasks(onlyActive, onlyStarred);
    }

    //method to build context menu
    private void buildTableContextMenu() {

        //declarations of actions for Menu Items
        //Set Done
        EventHandler<ActionEvent> actionSetDone = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeItemStatus(id, 0);
                String musicFile = "button-done.mp3";

                Media sound = new Media(new File(musicFile).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.play();
                listTasks(onlyActive, onlyStarred);

            }
        };

        //Set Active
        EventHandler<ActionEvent> actionSetActive = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeItemStatus(id, 1);
                listTasks(onlyActive, onlyStarred);
            }
        };

        //Set Starred
        EventHandler<ActionEvent> actionSetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 1);
                listTasks(onlyActive, onlyStarred);
            }
        };

        //UnStar
        EventHandler<ActionEvent> actionResetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 0);
                listTasks(onlyActive, onlyStarred);
            }
        };

        //Delete
        EventHandler<ActionEvent> actionDelete = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.deleteToDoItem(id);
                listTasks(onlyActive, onlyStarred);
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
                listTasks(onlyActive, onlyStarred);

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
                listTasks(onlyActive, onlyStarred);

            }
        };

        EventHandler<ActionEvent> actionSetDueDateTomorrow = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //change due date to tomorrow
                db.setDueDate(id, LocalDate.now().plus(1, ChronoUnit.DAYS).toString());

                //reset date in DatePicker
                //datePicker.setValue(null);
                listTasks(onlyActive, onlyStarred);

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

                int selectedRowIndex = activeItemsTable.getSelectionModel().getSelectedIndex();
                activeItemsTable.edit(selectedRowIndex, activeItemsTable.getColumns().get(1));

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
        activeItemsTable.setContextMenu(tableContextMenu);
        doneItemsTable.setContextMenu(tableContextMenu);

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
                db.insertToDoItem(descriptionText, 1, LocalDate.now().toString(), onlyStarred ? 1 : 0, allItems.size());
            } else if (showDateStart == showDateEnd) {
                //insert new task with description and status=1 and date=showDateStart (today or tomorrow only)
                db.insertToDoItem(descriptionText, 1, showDateStart.toString(), onlyStarred ? 1 : 0, allItems.size());
            } else {
                //insert new task with description and status=1 and date=today by default
                db.insertToDoItem(descriptionText, 1, LocalDate.now().toString(), onlyStarred ? 1 : 0, allItems.size());
            }

        } else {
            //insert new task with description and status=1 and set date
            db.insertToDoItem(descriptionText, 1, localDate.toString(), onlyStarred ? 1 : 0, allItems.size());
        }

        //clear description field, ready for next task
        description.clear();

        //reset prompt text of textField
        description.setPromptText(prevPromptText);

        //reset date in DatePicker
        datePicker.setValue(null);

        //refresh list of tasks after every task
        listTasks(onlyActive, onlyStarred);

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
    private int checkOverDue(int inStatus, String dueDateString, long id) {
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
    private void listTasks(boolean onlyActive, boolean onlyStarred) {

        //read all DB and save to local variable 'toDoList'
        allItems = db.viewTable(/*onlyActive, onlyStarred*/);

        //clear ObservableList<TodoItem>
        activeItems.clear();
        doneItems.clear();

        //scan all 'activetoDoList'
        for (int i = 0; i < allItems.size(); i++) {

            int intStatus = checkOverDue(allItems.get(i).getStatus(), allItems.get(i).getDate(), allItems.get(i).getId());
            //only active items
            if (intStatus != 0) {
                //check if only favorites
                if (onlyStarred) {
                    //check star
                    if (allItems.get(i).getStar() == 1) {
                        activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank()));
                    }
                    //otherwise, check dates
                } else {
                    //check if showDate is set
                    if (showDateStart == null) {
                        //show all
                        activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank()));
                    } else {
                        //show only for selected date
                        if (showDateStart.toString().compareTo(allItems.get(i).getDate()) <= 0) {
                            if (showDateEnd.toString().compareTo(allItems.get(i).getDate()) >= 0) {
                                activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank()));
                            }
                        }
                    }
                }

                //done items, all, no filter
            } else {
                //check if showDateStart is set
                if (showDateStart == null) {
                    //show all
                    doneItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank()));
                } else {
                    //show only for selected date
                    if (showDateStart.toString().compareTo(allItems.get(i).getDate()) <= 0) {
                        if (showDateEnd.toString().compareTo(allItems.get(i).getDate()) >= 0) {
                            doneItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank()));
                        }
                    }
                }

            }

        }
        //populate tblitems (TableView<TodoItem>) to be displayed in App from ObservableList<TodoItem>
        activeItemsTable.setItems(activeItems);
        doneItemsTable.setItems(doneItems);

        doneItemsTable.setVisible(!onlyActive);

        //build table's properties
        buildTable();
        buildDoneTable();
    }

    //buildTable
    //defines all properties for cells, rows, columns and table itself
    //add any change here...
    private void buildTable() {
        //set properties of cells
        //tblColId = new TableColumn("Id");
        activeItemsTableColId.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("id"));

        //tblColDesc = new TableColumn("Description");
        activeItemsTableColDesc.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("description"));
        //tblColDesc.setStyle("-fx-alignment: LEFT;");
        activeItemsTableColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        activeItemsTableColDesc.setOnEditCommit(
                new EventHandler<CellEditEvent<TodoItem, String>>() {
            @Override
            public void handle(CellEditEvent<TodoItem, String> t) {
                ((TodoItem) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setDescription(t.getNewValue());

                db.editDescription(activeItemsTable.getItems().get(activeItemsTable.getSelectionModel().getSelectedIndex()).getId(), activeItemsTable.getItems().get(activeItemsTable.getSelectionModel().getSelectedIndex()).getDescription());
                listTasks(onlyActive, onlyStarred);
            }
        }
        );
        //tblColDesc.isEditable();

        //tblColDate = new TableColumn("Date");
        activeItemsTableColDate.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("date"));
        //tblColDate.setStyle("-fx-alignment: CENTER;");

        //tblColStat = new TableColumn("Status");
        activeItemsTableColStat.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("status"));
        //set tblColStar to button
        Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>> cellDoneFactory;
        cellDoneFactory = new Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>>() {
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
                            if (item == 0) {
                                setGraphic(new ImageView("/todo/checkbox-done.png"));
                            } else {
                                setGraphic(new ImageView("/todo/checkbox-empty.png"));
                            }
                        }
                    }
                };

                cell.setOnMouseClicked((event) -> {
                    if (cell.getItem() == 0) {
                        cell.setItem(1);
                    } else {
                        cell.setItem(0);
                    }
                    String musicFile = "button-done.mp3";

                    Media sound = new Media(new File(musicFile).toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                    mediaPlayer.play();
                    db.changeItemStatus(activeItemsTable.getItems().get(activeItemsTable.getSelectionModel().getSelectedIndex()).getId(), cell.getItem());
                    listTasks(onlyActive, onlyStarred);
                });

                return cell;
            }
        };

        activeItemsTableColStat.setCellFactory(cellDoneFactory);

        //tblColStat.setStyle("-fx-alignment: CENTER;");
        //tblColStat = new TableColumn("Rank");
        activeItemsTableColRank.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("rank"));
        //tblColStat = new TableColumn("Star");
        activeItemsTableColStar.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("star"));
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

                    db.changeStarred(activeItemsTable.getItems().get(activeItemsTable.getSelectionModel().getSelectedIndex()).getId(), cell.getItem());
                    listTasks(onlyActive, onlyStarred);
                });

                return cell;
            }
        };

        activeItemsTableColStar.setCellFactory(cellFactory);

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
        //allow drag and drop only for full view of all active items
        //not favorites, not period => show all
        if (!onlyStarred && showDateStart == null) {
            activeItemsTable.setRowFactory(tv -> {
                TableRow<TodoItem> row = new TableRow<>();

                row.setOnDragDetected(event -> {
                    if (!row.isEmpty()) {
                        Integer index = row.getIndex();
                        Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                        dragboard.setDragView(row.snapshot(null, null));
                        ClipboardContent cc = new ClipboardContent();
                        cc.put(SERIALIZED_MIME_TYPE, index);
                        dragboard.setContent(cc);
                        event.consume();
                    }
                });

                row.setOnDragOver(event -> {
                    Dragboard dragboard = event.getDragboard();
                    if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                        if (row.getIndex() != ((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                            event.consume();
                        }
                    }
                });
                //  if (!onlyStarred) {
                row.setOnDragDropped(event -> {
                    Dragboard dragboard = event.getDragboard();
                    if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                        int draggedIndex = (Integer) dragboard.getContent(SERIALIZED_MIME_TYPE);
                        int forDragged = activeItemsTable.getItems().get(draggedIndex).getRank();
                        TodoItem draggedTodoItem = activeItemsTable.getItems().remove(draggedIndex);

                        int dropIndex;
                        int forDropped;

                        if (row.isEmpty()) {
                            dropIndex = activeItemsTable.getItems().size();
                            forDropped = activeItemsTable.getItems().get(dropIndex - 1).getRank() + 1;
                        } else {
                            dropIndex = row.getIndex();
                            forDropped = activeItemsTable.getItems().get(dropIndex).getRank();
                        }

                        activeItemsTable.getItems().add(dropIndex, draggedTodoItem);

                        //debugging
                        System.out.println("Dragged index=" + draggedIndex + "\t dropped index=" + dropIndex + "\t dragged rank=" + forDragged + "\t dropped rank=" + forDropped);

                        //selectedIndex - start
                        //dropIndex - end/drop        
                        event.setDropCompleted(true);
                        activeItemsTable.getSelectionModel().select(dropIndex);
                        if (forDragged < forDropped) {
                            for (int i = 0; i < allItems.size(); i++) {

                                if (allItems.get(i).getRank() == forDragged) {
                                    db.changeItemRank(allItems.get(i).getId(), forDropped - 1);
                                } else if (allItems.get(i).getRank() > forDragged && allItems.get(i).getRank() < forDropped) {
                                    db.changeItemRank(allItems.get(i).getId(), allItems.get(i).getRank() - 1);
                                }

                            }
                        } else {
                            for (int i = 0; i < allItems.size(); i++) {

                                if (allItems.get(i).getRank() == forDragged) {
                                    db.changeItemRank(allItems.get(i).getId(), forDropped);
                                } else if (allItems.get(i).getRank() > forDropped - 1 && allItems.get(i).getRank() < forDragged) {
                                    db.changeItemRank(allItems.get(i).getId(), allItems.get(i).getRank() + 1);
                                }

                            }
                        }

                        listTasks(onlyActive, onlyStarred);
                        event.consume();
                    }
                });

                return row;
            });
        }
        activeItemsTable.getColumns().setAll(activeItemsTableColStat, activeItemsTableColId, activeItemsTableColDesc, activeItemsTableColDate, activeItemsTableColStar, activeItemsTableColRank);
        activeItemsTableColRank.setSortType(TableColumn.SortType.ASCENDING);
        //tblColStat.setSortType(TableColumn.SortType.DESCENDING);
        activeItemsTable.getSortOrder().setAll(activeItemsTableColRank);
        //tblitems.getStyleClass().add("strike");
        activeItemsTable.setEditable(true);
        activeItemsTable.setFixedCellSize(30);
        activeItemsTable.prefHeightProperty().bind(activeItemsTable.fixedCellSizeProperty().multiply(activeItemsTable.getItems().size() + 1).add(1.01));
        activeItemsTable.minHeightProperty().bind(activeItemsTable.prefHeightProperty());
        activeItemsTable.maxHeightProperty().bind(activeItemsTable.prefHeightProperty());
    }

    //buildDoneTable
    //defines all properties for cells, rows, columns and table itself
    //add any change here...
    private void buildDoneTable() {
        //set properties of cells
        //tblColId = new TableColumn("Id");
        doneItemsTableColId.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("id"));

        //tblColDesc = new TableColumn("Description");
        doneItemsTableColDesc.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("description"));
        //tblColDesc.setStyle("-fx-alignment: LEFT;");
        doneItemsTableColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        doneItemsTableColDesc.setOnEditCommit(
                new EventHandler<CellEditEvent<TodoItem, String>>() {
            @Override
            public void handle(CellEditEvent<TodoItem, String> t) {
                ((TodoItem) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setDescription(t.getNewValue());

                db.editDescription(doneItemsTable.getItems().get(doneItemsTable.getSelectionModel().getSelectedIndex()).getId(), doneItemsTable.getItems().get(doneItemsTable.getSelectionModel().getSelectedIndex()).getDescription());
                listTasks(onlyActive, onlyStarred);
            }
        }
        );
        //tblColDesc.isEditable();

        //tblColDate = new TableColumn("Date");
        doneItemsTableColDate.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("date"));
        //tblColDate.setStyle("-fx-alignment: CENTER;");

        //tblColStat = new TableColumn("Status");
        doneItemsTableColStat.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("status"));
        Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>> cellDoneDoneFactory;
        cellDoneDoneFactory = new Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>>() {
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
                            if (item == 0) {
                                setGraphic(new ImageView("/todo/checkbox-done.png"));
                            } else {
                                setGraphic(new ImageView("/todo/checkbox-empty.png"));
                            }
                        }
                    }
                };

                cell.setOnMouseClicked((event) -> {
                    if (cell.getItem() == 0) {
                        cell.setItem(1);
                    } else {
                        cell.setItem(0);
                    }

                    db.changeItemStatus(doneItemsTable.getItems().get(doneItemsTable.getSelectionModel().getSelectedIndex()).getId(), cell.getItem());
                    listTasks(onlyActive, onlyStarred);
                });

                return cell;
            }
        };
        doneItemsTableColStat.setCellFactory(cellDoneDoneFactory);
        //tblColStat.setStyle("-fx-alignment: CENTER;");
        //tblColStat = new TableColumn("Rank");
        doneItemsTableColRank.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("rank"));
        //tblColStat = new TableColumn("Star");
        doneItemsTableColStar.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("star"));
        /*
        //set tblColStar to button
        Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>> cellDoneFactory;
        cellDoneFactory = new Callback<TableColumn<TodoItem, Integer>, TableCell<TodoItem, Integer>>() {
            @Override
            public TableCell<TodoItem, Integer> call(final TableColumn<TodoItem, Integer> param) {
                final TableCell<TodoItem, Integer> doneCell = new TableCell<TodoItem, Integer>() {

                    private ImageView doneStar = new ImageView();

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

                doneCell.setOnMouseClicked((event) -> {
                    if (doneCell.getItem() == 1) {
                        doneCell.setItem(0);
                    } else {
                        doneCell.setItem(1);
                    }

                    db.changeStarred(doneItemsTable.getItems().get(doneItemsTable.getSelectionModel().getSelectedIndex()).getId(), doneCell.getItem());
                    listTasks(onlyActive, onlyStarred);
                });

                return doneCell;
            }
        };

        
        doneItemsTable.setRowFactory(tv -> {
            TableRow<TodoItem> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                    dragboard.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    dragboard.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer) dragboard.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) dragboard.getContent(SERIALIZED_MIME_TYPE);
                    int forDragged = doneItemsTable.getItems().get(draggedIndex).getRank();
                    TodoItem draggedTodoItem = doneItemsTable.getItems().remove(draggedIndex);

                    int dropIndex;
                    int forDropped;

                    if (row.isEmpty()) {
                        dropIndex = doneItemsTable.getItems().size();
                        forDropped = doneItemsTable.getItems().get(dropIndex - 1).getRank() + 1;
                    } else {
                        dropIndex = row.getIndex();
                        forDropped = doneItemsTable.getItems().get(dropIndex).getRank();
                    }

                    doneItemsTable.getItems().add(dropIndex, draggedTodoItem);

                    //debugging
                    System.out.println("Dragged index=" + draggedIndex + "\t dropped index=" + dropIndex + "\t dragged rank=" + forDragged + "\t dropped rank=" + forDropped);

                    //selectedIndex - start
                    //dropIndex - end/drop        
                    event.setDropCompleted(true);
                    doneItemsTable.getSelectionModel().select(dropIndex);
                    if (forDragged < forDropped) {
                        for (int i = 0; i < allItems.size(); i++) {

                            if (allItems.get(i).getRank() == forDragged) {
                                db.changeItemRank(allItems.get(i).getId(), forDropped - 1);
                            } else if (allItems.get(i).getRank() > forDragged && allItems.get(i).getRank() < forDropped) {
                                db.changeItemRank(allItems.get(i).getId(), allItems.get(i).getRank() - 1);
                            }

                        }
                    } else {
                        for (int i = 0; i < allItems.size(); i++) {

                            if (allItems.get(i).getRank() == forDragged) {
                                db.changeItemRank(allItems.get(i).getId(), forDropped);
                            } else if (allItems.get(i).getRank() > forDropped-1 && allItems.get(i).getRank() < forDragged) {
                                db.changeItemRank(allItems.get(i).getId(), allItems.get(i).getRank() + 1);
                            }

                        }
                    }
                    
                    listTasks(onlyActive, onlyStarred);
                    event.consume();
                }
            });
        
            return row;
        });
         */
        doneItemsTable.getColumns().setAll(doneItemsTableColStat, doneItemsTableColId, doneItemsTableColDesc, doneItemsTableColDate, doneItemsTableColStar, doneItemsTableColRank);
        doneItemsTableColRank.setSortType(TableColumn.SortType.ASCENDING);
        //tblColStat.setSortType(TableColumn.SortType.DESCENDING);
        doneItemsTable.getSortOrder().setAll(doneItemsTableColRank);
        //tblitems.getStyleClass().add("strike");
        doneItemsTable.setEditable(true);
        doneItemsTable.setFixedCellSize(30);
        doneItemsTable.prefHeightProperty().bind(doneItemsTable.fixedCellSizeProperty().multiply(doneItemsTable.getItems().size() + 1).add(1.01));
        doneItemsTable.minHeightProperty().bind(doneItemsTable.prefHeightProperty());
        doneItemsTable.maxHeightProperty().bind(doneItemsTable.prefHeightProperty());
    }

    //disable menu options based on items parameters
    private void dynamicContextMenu(int row) {
        //disable options according to items parameters
        TodoItem currentRow = activeItemsTable.getItems().get(row);
        if (currentRow.getStatus() == 0) {
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

    //disable menu options based on items parameters
    private void dynamicDoneContextMenu(int row) {
        //disable options according to items parameters
        TodoItem currentRow = doneItemsTable.getItems().get(row);
        if (currentRow.getStatus() == 0) {
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
        listTasks(onlyActive, onlyStarred);

    }

}
