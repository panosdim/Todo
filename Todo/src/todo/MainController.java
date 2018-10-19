/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import java.awt.Desktop;
import javafx.scene.paint.Color;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import static java.sql.JDBCType.NULL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.converter.LocalTimeStringConverter;

/**
 *
 * @author ckok
 */
public class MainController implements Initializable {

    DBHandler db;
    DBHandler dbFolders;
    private String descriptionText;
    private long id = -1; //id read from DB used to delete single task
    private int folderFolderId = -1; //folder Id from DB, folder table

    private ObservableList<TodoItem> activeItems = FXCollections.observableArrayList(); //ObservableList of active items
    private ObservableList<TodoItem> doneItems = FXCollections.observableArrayList(); //ObservableList of done items
    private ObservableList<Label> menuItems = FXCollections.observableArrayList(); //ObservableList of left menu items
    private ObservableList<FolderItem>  folderItems = FXCollections.observableArrayList(); //ObservableList of folder items
    private boolean onlyActive = true; //used to view all items or only active ones, default=true
    private boolean onlyStarred = false;
    private LocalDate localDate = null; //used to read from DatePicker 
    private LocalDate showDateStart = null; //used to filter items for one day only
    private LocalDate showDateEnd = null;
    private DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private ArrayList<TodoItem> allItems; //stores all DB in ArrayList of TodoItem type
    private ArrayList<MenuItem> menuFolders = new ArrayList<>();
    //private ArrayList<String> allFolders;
    private boolean anyAlarm = false;
    private String alarmDesc = null;
    //shortcut ctrl+F to mark selected item as favorite, ctrl+U unfavor
    private KeyCombination cntrlF = new KeyCodeCombination(KeyCode.F, KeyCodeCombination.CONTROL_DOWN);
    private KeyCombination cntrlU = new KeyCodeCombination(KeyCode.U, KeyCodeCombination.CONTROL_DOWN);

    //shortcut ctrl+DEL to delete selected item
    private KeyCombination buttonDelete = new KeyCodeCombination(KeyCode.DELETE);//, KeyCodeCombination.CONTROL_DOWN);

    //shortcut ctrl+D to set selected item to done, ctrl+A back to active
    private KeyCombination cntrlD = new KeyCodeCombination(KeyCode.D, KeyCodeCombination.CONTROL_DOWN);
    private KeyCombination cntrlS = new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN);

    //shortcut ctrl+E to edit selected item
    private KeyCombination cntrlE = new KeyCodeCombination(KeyCode.E, KeyCodeCombination.CONTROL_DOWN);

    //shortcut ctrl+M to email selected item
    private KeyCombination cntrlM = new KeyCodeCombination(KeyCode.M, KeyCodeCombination.CONTROL_DOWN);

    //declarations of Menu Items
    MenuItem deleteMenuItem = new MenuItem("Delete Item");
    MenuItem setDoneMenuItem = new MenuItem("Set Item to Done");
    MenuItem setActiveItem = new MenuItem("Set Item to Pending");
    Menu setDueDate = new Menu("Set Due Date");
    Menu assignFolder = new Menu("Assign to Folder...");
    //MenuItem setDueToday = new MenuItem("today");
    //MenuItem setDueTomorrow = new MenuItem("tomorrow");
    DatePicker menuDatePicker = new DatePicker();
    MenuItem setDueDatePicker = new MenuItem();
    MenuItem editItem = new MenuItem("Edit");
    MenuItem starItem = new MenuItem("Set Favorite");
    MenuItem unstarItem = new MenuItem("Unfavor");
    MenuItem emailItem = new MenuItem("Send email");
    Menu setAlarm = new Menu("Set Alarm");
    Spinner<LocalTime> alarmSpinner = new Spinner(new SpinnerValueFactory() {

        {
            setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH:mm"), DateTimeFormatter.ofPattern("HH:mm")));
        }

        @Override
        public void decrement(int steps) {
            if (getValue() == null) {
                setValue(LocalTime.now());
            } else {
                LocalTime time = (LocalTime) getValue();
                setValue(time.minusMinutes(steps));
            }
        }

        @Override
        public void increment(int steps) {
            if (this.getValue() == null) {
                setValue(LocalTime.now());
            } else {
                LocalTime time = (LocalTime) getValue();
                setValue(time.plusMinutes(steps));
            }
        }
    });
    //spinner.setEditable(true);
    //Spinner minuteSpinner = new Spinner();
    MenuItem setAlarmSpinner = new MenuItem();
    MenuItem confirmAlarmSpinner = new MenuItem();
    Button confirmAlarm = new Button("Set Alarm");
    MenuItem removeAlarm = new MenuItem("Remove Alarm");

    //menu items for folders
    MenuItem removeFolderAll = new MenuItem("Delete Folder and all Items");
    MenuItem removeFolderDefault = new MenuItem("Delete Folder but keep Items");
    MenuItem removeFolderItems = new MenuItem("Delete All Items from folder");

    //
    //private final String strikeThrough = getClass().getResource("sceneCSS.css").toExternalForm();
    @FXML
    private Button buttonShowOptions;
    @FXML
    private Button buttonDeleteDone;
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
    private TableColumn activeItemsTableColAlarm;

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

    private ListView menuList = new ListView<Label>();
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
    @FXML
    private Label clock;
    @FXML
    private BorderPane borderPane;
    @FXML
    private AnchorPane anchorPane;

    final DateFormat format = DateFormat.getInstance();
    Timeline timeline;

    //@FXML
    private Label leftMenu = new Label("<<");
    private Label folderLabel = new Label("Folders");
    private ListView folderList = new ListView<String>();
    private TextField folderAddText = new TextField();
    private Label folderAddLabel = new Label("Add new folder");

    private BorderSlideBar leftFlapBar = new BorderSlideBar(220, leftMenu, Pos.BASELINE_LEFT, menuList, folderLabel, folderList, folderAddLabel, folderAddText);
    //private ToolBar toolbar = new ToolBar();

    //Nodes to appear in new right pane
    private Label descLabel = new Label("Description");
    private Label statusLabel = new Label("Status");
    private Label dueDateLabel = new Label("Due Date");
    private TextField descEdit = new TextField();
    private TextField statusEdit = new TextField();
    private DatePicker dateEdit = new DatePicker();
    private Button updateButton = new Button("Update");

    //control MenuItem is editItem from context menu
    private BorderSlideBar2 rightFlapBar = new BorderSlideBar2(220, editItem, Pos.BASELINE_RIGHT, descLabel, descEdit, statusLabel, statusEdit, dueDateLabel, dateEdit, updateButton);

    //method handling tooltip in the left list via mouse 
    @FXML
    private void toolTipList(MouseEvent event) {
        //index from list of task is read via MouseEvent (any for now)
        int indexLocal = menuList.getSelectionModel().getSelectedIndex();
        //System.out.println(index);
        //if valid, it's used for menu actions (filtering items by dates)
        switch (indexLocal) {
            case -1:
                break;
            case 0:
                menuList.tooltipProperty();
                menuList.setTooltip(new Tooltip("Show all active Todo tasks"));
                //System.out.println("test0");
                indexLocal = -1;
                event.consume();
                break;
            case 1:
                menuList.tooltipProperty();
                menuList.setTooltip(new Tooltip("Show all favourite Todo tasks"));
                //System.out.println("test1");
                indexLocal = -1;
                event.consume();
                break;
            case 2:
                System.out.println("Show the active ToDo tasks for today");
                indexLocal = -1;
                event.consume();
                break;
            case 3:
                System.out.println("Show the active ToDo tasks for tomorrow");
                indexLocal = -1;
                event.consume();
                break;
            case 4:
                System.out.println("Show the active ToDo tasks for the upcoming week");
                indexLocal = -1;
                event.consume();
                break;
            case 5:
                System.out.println("Show the active ToDo tasks for the current month");
                indexLocal = -1;
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
            System.out.println(id + "\t" + index + "\t fav=" + activeItemsTable.getItems().get(index).getStar() + "\t rank=" + activeItemsTable.getItems().get(index).getRank() + "\t status=" + activeItemsTable.getItems().get(index).getStatus());

            //dynamic context menu
            dynamicContextMenu(activeItemsTable.getItems().get(index));

            //reset index
            //index = -1;
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
            dynamicContextMenu(doneItemsTable.getItems().get(index));

            //reset index
            //index = -1;
        } else {
            //clear any previous value of id
            id = -1;

        }
    }

    //method handling action on 'Clear list' button
    @FXML
    private void handleButtonDeleteAllAction(ActionEvent event) {
        //Warning Dialog Box for delete all tasks 
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
            listTasks(onlyActive, onlyStarred, true);
        } else if (result.get() == noButton) {
            event.consume();
        } else if (result.get() == cancelButton) {
            event.consume();
        }
    }

    //method handling action on 'Clear done list' button
    @FXML
    private void handleButtonDeleteDoneAction(ActionEvent event) {
        //Warning Dialog Box for delete all tasks 
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Would You Like To Delete All Done Items?");
        alert.setContentText("Please choose an option.");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yesButton) {
            for (int i = 0; i < doneItems.size(); i++) {
                db.deleteToDoItem(doneItems.get(i).getId());
            }

            //refresh list of tasks
            listTasks(onlyActive, onlyStarred, true);
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
            buttonShowOptions.setTooltip(new Tooltip("Hide completed ToDo tasks"));
            buttonDeleteDone.setVisible(true);

        } else {
            onlyActive = true;
            buttonShowOptions.setText("Show Done");
            buttonShowOptions.setTooltip(new Tooltip("Show completed ToDo tasks"));
            buttonDeleteDone.setVisible(false);
        }

        //refresh list of tasks
        listTasks(onlyActive, onlyStarred, false);
    }

    //handleDatePicker
    @FXML
    private void handleDatePicker() {
        localDate = datePicker.getValue();
        if (localDate != null) {
            if (descriptionText != null) {
                //create new item
                db.insertToDoItem(descriptionText, 1, localDate.toString(), onlyStarred ? 1 : 0, activeItems.size());

                //clear description field, ready for next task
                description.clear();
                descriptionText = null;

                //reset prompt text of textField
                description.setPromptText("Add new todo task...");

                //reset date in DatePicker
                datePicker.setValue(null);

                //refresh list of tasks after every task
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true);
                } else {
                    listTasks(onlyActive, onlyStarred, true);
                }
            } else {
                System.out.println(localDate.toString());
                description.setPromptText("Add new todo task for selected date");
            }
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

    //method to update all impacted ranks
    //called when item is set to done or removed or drag-dropped
    private void updateRanks() {
        for (int i = 0; i < activeItems.size(); i++) {
            db.changeItemRank(activeItems.get(i).getId(), i);
        }
    }

    //method to build context menu for folders
    private void buildFoldersContextMenu() {

        //Delete Folder and Items inside
        EventHandler<ActionEvent> actionDeleteFolderAll = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //db.deleteToDoItem(id);
                //listTasks(onlyActive, onlyStarred);
                //Warning Dialog Box for delete one task 
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Warning");
                alert.setHeaderText("Would You Like To Delete Folder and All Item inside?");
                alert.setContentText("Please choose an option.");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == yesButton) {
                    db.deleteFolderItems(folderFolderId);
                    dbFolders.deleteFolder(folderFolderId);
                    //refresh list of tasks
                    if (onlyStarred || showDateStart != null) {
                        listTasks(true, onlyStarred, true);
                    } else {
                        listTasks(onlyActive, onlyStarred, true);
                    }
                    listFolders();
                } else if (result.get() == noButton) {
                    event.consume();
                } else if (result.get() == cancelButton) {
                    event.consume();
                }

            }
        };

        removeFolderAll.setOnAction(actionDeleteFolderAll);
        removeFolderAll.setGraphic(new ImageView("/todo/delete.png"));
        //removeFolderDefault;
        //removeFolderItems;
        
        ContextMenu folderContextMenu = new ContextMenu(removeFolderAll);
        folderList.setContextMenu(folderContextMenu);
        
    }

    //method to build context menu
    private void buildTableContextMenu() {

        //declarations of actions for Menu Items
        //Set Done
        EventHandler<ActionEvent> actionSetDone = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeItemStatus(id, 0, activeItems.size());
                updateRanks();
                String musicFile = "applause10.mp3";

                Media sound = new Media(new File(musicFile).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.play();
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true);
                } else {
                    listTasks(onlyActive, onlyStarred, true);
                }

            }
        };

        //Set Active
        EventHandler<ActionEvent> actionSetActive = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeItemStatus(id, 1, activeItems.size());
                listTasks(onlyActive, onlyStarred, true);
            }
        };

        //Set Starred
        EventHandler<ActionEvent> actionSetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 1);
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true);
                } else {
                    listTasks(onlyActive, onlyStarred, true);
                }
            }
        };

        //UnStar
        EventHandler<ActionEvent> actionResetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 0);
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true);
                } else {
                    listTasks(onlyActive, onlyStarred, true);
                }
            }
        };

        //Set Alarm
        EventHandler<ActionEvent> actionSetAlarm = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (alarmSpinner.getValue() == null) {
                    //do nothing
                } else {
                    String spinnerTime = alarmSpinner.getValue().format(DateTimeFormatter.ofPattern("HH:mm"));
                    db.setAlarm(id, spinnerTime);

                    if (onlyStarred || showDateStart != null) {
                        listTasks(true, onlyStarred, true);
                    } else {
                        listTasks(onlyActive, onlyStarred, true);
                    }
                }

            }
        };

        //Delete Alarm
        EventHandler<ActionEvent> actionRemoveAlarm = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.setAlarm(id, null);
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true);
                } else {
                    listTasks(onlyActive, onlyStarred, true);
                }

            }
        };

        //assign Folder
        EventHandler<ActionEvent> actionAssignFolder = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //  db.assignFolder(folderName);
            }
        };

        //Delete
        EventHandler<ActionEvent> actionDelete = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //db.deleteToDoItem(id);
                //listTasks(onlyActive, onlyStarred);
                //Warning Dialog Box for delete one task 
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Warning");
                alert.setHeaderText("Would You Like To Delete This Item?");
                alert.setContentText("Please choose an option.");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == yesButton) {
                    db.deleteToDoItem(id);
                    //refresh list of tasks
                    if (onlyStarred || showDateStart != null) {
                        listTasks(true, onlyStarred, true);
                    } else {
                        listTasks(onlyActive, onlyStarred, true);
                    }
                } else if (result.get() == noButton) {
                    event.consume();
                } else if (result.get() == cancelButton) {
                    event.consume();
                }

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
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true);
                } else {
                    listTasks(onlyActive, onlyStarred, true);
                }

            }
        };

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

        //Edit description
        EventHandler<ActionEvent> actionEmail;
        actionEmail = new EventHandler<ActionEvent>() {
            @Override

            public void handle(ActionEvent event) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.MAIL)) {

                        try {
                            int selectedRowIndex = activeItemsTable.getSelectionModel().getSelectedIndex();
                            String email = "javagroupc@intracom-telecom.com";
                            String subject = "Todo%20item%20info";
                            String body = "Todo%20Item%20'" + activeItemsTable.getItems().get(selectedRowIndex).getDescription() + "'%20is%20due%20on%20" + activeItemsTable.getItems().get(selectedRowIndex).getDate();
                            body = body.replaceAll(" ", "%20");
                            URI mailto;
                            mailto = new URI("mailto:" + email + "?subject=" + subject + "&body=" + body);
                            desktop.mail(mailto);
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }

            }
        };

        //Assignment of actions to Menu Items
        setDoneMenuItem.setOnAction(actionSetDone);
        setDoneMenuItem.setGraphic(new ImageView("/todo/done.png"));
        setDoneMenuItem.setAccelerator(cntrlD);
        setActiveItem.setOnAction(actionSetActive);
        setActiveItem.setGraphic(new ImageView("/todo/undo.png"));
        setActiveItem.setAccelerator(cntrlS);
        deleteMenuItem.setOnAction(actionDelete);
        deleteMenuItem.setGraphic(new ImageView("/todo/delete.png"));
        deleteMenuItem.setAccelerator(buttonDelete);
        //setDueDate.setOnAction(actionSetDueDate);
        //setDueToday.setOnAction(actionSetDueDateToday);
        //setDueTomorrow.setOnAction(actionSetDueDateTomorrow);
        setDueDatePicker.setGraphic(menuDatePicker);
        setDueDatePicker.setOnAction(actionSetDueDate);
        setDueDate.getItems().addAll(setDueDatePicker);
        setDueDate.setGraphic(new ImageView("/todo/calendar.png"));
        //editItem.setOnAction(actionEdit);
        editItem.setGraphic(new ImageView("/todo/edit.png"));
        editItem.setAccelerator(cntrlE);

        starItem.setOnAction(actionSetStarred);
        starItem.setGraphic(new ImageView("/todo/star.png"));
        starItem.setAccelerator(cntrlF);
        unstarItem.setOnAction(actionResetStarred);
        unstarItem.setGraphic(new ImageView("/todo/unstar.png"));
        unstarItem.setAccelerator(cntrlU);
        emailItem.setOnAction(actionEmail);
        emailItem.setGraphic(new ImageView("/todo/email.png"));
        emailItem.setAccelerator(cntrlM);
        setAlarmSpinner.setGraphic(alarmSpinner);
        //setAlarmSpinner.
        //setAlarmSpinner.setOnAction(actionSetAlarm);
        confirmAlarmSpinner.setGraphic(confirmAlarm);
        confirmAlarmSpinner.setOnAction(actionSetAlarm);
        setAlarm.getItems().addAll(setAlarmSpinner, confirmAlarmSpinner);
        setAlarm.setGraphic(new ImageView("/todo/alarm-on.png"));

        removeAlarm.setOnAction(actionRemoveAlarm);
        removeAlarm.setGraphic(new ImageView("/todo/alarm-off.png"));

        //context menu for TableView
        ContextMenu tableContextMenu = new ContextMenu(editItem, assignFolder, setAlarm, removeAlarm, emailItem, starItem, unstarItem, setDueDate, setDoneMenuItem, setActiveItem, deleteMenuItem);

        //set context menu for tblitems TableView object
        activeItemsTable.setContextMenu(tableContextMenu);
        doneItemsTable.setContextMenu(tableContextMenu);

    }

    //method handling action on textField to add new task (ENTER press)
    @FXML

    private void insertTextField(ActionEvent event) {

        //get description from textField
        descriptionText = description.getText();
        //replace single quote with double single quote for SQL query
        descriptionText = descriptionText.replaceAll("'", "''");
        if (localDate == null) {
            //if DatePicker is empty (no date seleceted)
            //set focus on DatePicker to choose one
            datePicker.requestFocus();
            //datePicker.show();
        } else {
            //insert new task with description and status=1 and set date
            db.insertToDoItem(descriptionText, 1, localDate.toString(), onlyStarred ? 1 : 0, activeItems.size());
            //clear description field, ready for next task
            description.clear();
            descriptionText = null;

            //reset prompt text of textField
            description.setPromptText("Add a new Todo task...");

            //reset date in DatePicker
            datePicker.setValue(null);

            //refresh list of tasks after every task
            if (onlyStarred || showDateStart != null) {
                listTasks(true, onlyStarred, true);
            } else {
                listTasks(onlyActive, onlyStarred, true);
            }
        }

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
                db.changeItemStatus(id, outStatus, activeItems.size());
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
                db.changeItemStatus(id, outStatus, activeItems.size());
            }

        }

        return outStatus;
    }

    //build side menu
    private void buildSideMenu(int active, int favs, int today, int tomorrow, int week, int month) {
        menuItems.clear();
        Label inboxList = new Label("Show All \t\t\t" + active);
        inboxList.setGraphic(new ImageView("/todo/todo1_small.png"));
        inboxList.setTooltip(new Tooltip("Show all items"));
        menuItems.add(inboxList);
        Label favsList = new Label("Show Favorites \t" + favs);
        favsList.setGraphic(new ImageView("/todo/star.png"));
        favsList.setTooltip(new Tooltip("Show all favorite items"));
        menuItems.add(favsList);
        Label todayList = new Label("Show Today \t\t" + today);
        todayList.setGraphic(new ImageView("/todo/today.png"));
        todayList.setTooltip(new Tooltip("Show all items for today"));
        menuItems.add(todayList);
        Label tomorrowList = new Label("Show Tomorrow \t" + tomorrow);
        tomorrowList.setGraphic(new ImageView("/todo/tomorrow.jpg"));
        tomorrowList.setTooltip(new Tooltip("Show all items for tomorrow"));
        menuItems.add(tomorrowList);
        Label weekList = new Label("Show Week \t\t" + week);
        weekList.setGraphic(new ImageView("/todo/week.png"));
        weekList.setTooltip(new Tooltip("Show all items for upcoming week"));
        menuItems.add(weekList);
        Label monthList = new Label("Show Month \t\t" + month);
        monthList.setGraphic(new ImageView("/todo/month.png"));
        monthList.setTooltip(new Tooltip("Show all items for upcoming month"));
        menuItems.add(monthList);
        //menuItems.add("Show Month \t\t\t" + month);

        menuList.setItems(menuItems);
        menuList.setFixedCellSize(35);
        menuList.prefHeightProperty().bind(menuList.fixedCellSizeProperty().multiply(menuList.getItems().size()).add(1.02));
        menuList.minHeightProperty().bind(menuList.prefHeightProperty());
        menuList.maxHeightProperty().bind(menuList.prefHeightProperty());

    }

    //private method to list all folders
    private void listFolders() {
        folderItems = dbFolders.viewFolderTable();
        ObservableList<String> folderNames = FXCollections.observableArrayList();
        for(int i = 0; i < folderItems.size(); i++) {
            folderNames.add(folderItems.get(i).getFolderName());
        }
        folderList.setItems(folderNames);
        menuFolders.clear();
        for (int i = 0; i < folderItems.size(); i++) {
            menuFolders.add(new MenuItem(folderItems.get(i).getFolderName()));
            //menuFolders.get(i).setOnAction((event) -> {

            // db.assignFolder(id, folderList.getItems().get(i)  .get(i));
            //});
        }
        assignFolder.getItems().addAll(menuFolders);
    }

//private method for refreshing list of tasks
    private void listTasks(boolean showOnlyActive, boolean showOnlyStarred, boolean refreshData) {

        if (refreshData) {
            //read all DB and save to local variable 'toDoList'
            allItems = db.viewTable();
        }

        //clear ObservableList<TodoItem>
        activeItems.clear();
        doneItems.clear();
        anyAlarm = false;

        //declare counters
        int active = 0, favs = 0, today = 0, tomorrow = 0, week = 0, month = 0;
        LocalDate todayDate = LocalDate.now(); //.plus(1, ChronoUnit.DAYS);

        //scan all 'activetoDoList'
        for (int i = 0; i < allItems.size(); i++) {

            if (allItems.get(i).getAlarm() != null) {
                anyAlarm = true;
            }

            int intStatus = checkOverDue(allItems.get(i).getStatus(), allItems.get(i).getDate(), allItems.get(i).getId());
            //only active items
            if (intStatus != 0) {
                //update counters
                active++;
                if (allItems.get(i).getStar() == 1) {
                    favs++;
                }
                if (allItems.get(i).getDate().compareTo(todayDate.toString()) == 0) {
                    today++;
                }
                if (allItems.get(i).getDate().compareTo(todayDate.plus(1, ChronoUnit.DAYS).toString()) == 0) {
                    tomorrow++;
                }
                if (allItems.get(i).getDate().compareTo(todayDate.toString()) >= 0 && allItems.get(i).getDate().compareTo(todayDate.plus(7, ChronoUnit.DAYS).toString()) <= 0) {
                    week++;
                }
                if (allItems.get(i).getDate().compareTo(todayDate.toString()) >= 0 && allItems.get(i).getDate().compareTo(todayDate.plus(todayDate.lengthOfMonth(), ChronoUnit.DAYS).toString()) <= 0) {
                    month++;
                }
                //check if only favorites
                if (showOnlyStarred) {
                    //check star
                    if (allItems.get(i).getStar() == 1) {

                        activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                    }
                    //otherwise, check dates
                } else {
                    //check if showDate is set
                    if (showDateStart == null) {
                        //show all
                        activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                    } else {
                        //show only for selected date
                        if (showDateStart.toString().compareTo(allItems.get(i).getDate()) <= 0) {
                            if (showDateEnd.toString().compareTo(allItems.get(i).getDate()) >= 0) {
                                activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                            }
                        }

                    }
                }

                //done items, all, no filter
            } else {
                //check if showDateStart is set
                if (showDateStart == null) {
                    //show all
                    doneItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                } else {
                    //show only for selected date
                    if (showDateStart.toString().compareTo(allItems.get(i).getDate()) <= 0) {
                        if (showDateEnd.toString().compareTo(allItems.get(i).getDate()) >= 0) {
                            doneItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                        }
                    }
                }

            }

        }
        //populate tblitems (TableView<TodoItem>) to be displayed in App from ObservableList<TodoItem>
        activeItemsTable.setItems(activeItems);
        doneItemsTable.setItems(doneItems);

        doneItemsTable.setVisible(!showOnlyActive);

        //build table's properties
        buildTable();
        buildDoneTable();
        buildSideMenu(active, favs, today, tomorrow, week, month);
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

        //activeItemsTableColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        //activeItemsTableColDesc.setOnEditCommit(
        activeItemsTableColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        Label tblColDescLabel = new Label("Description");
        tblColDescLabel.setTooltip(new Tooltip("This column shows the decription of the ToDo tasks."));
        activeItemsTableColDesc.setGraphic(tblColDescLabel);
        //tblColDesc.isEditable();

        //tblColDate = new TableColumn("Date");
        activeItemsTableColDate.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("date"));
        //tblColDate.setStyle("-fx-alignment: CENTER;");
        Label tblColDateLabel = new Label("Due Date");
        tblColDateLabel.setTooltip(new Tooltip("This column shows the due date of the ToDo tasks."));
        activeItemsTableColDate.setGraphic(tblColDateLabel);

        Callback<TableColumn<TodoItem, String>, TableCell<TodoItem, String>> cellDateFactory;
        cellDateFactory = new Callback<TableColumn<TodoItem, String>, TableCell<TodoItem, String>>() {
            @Override
            public TableCell<TodoItem, String> call(final TableColumn<TodoItem, String> param) {
                final TableCell<TodoItem, String> cell = new TableCell<TodoItem, String>() {

                    //private ImageView star = new ImageView();
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            Date today = new Date();
                            String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);

                            //if today is more than dueDate, status is 'overdue', set text color to red
                            if (todayString.compareTo(item) > 0) {
                                this.setTextFill(Color.RED);
                                this.setText(item);
                                this.setTooltip(new Tooltip("this item is overdue!!!"));
                                //System.out.println(item);
                            } else {
                                this.setTextFill(Color.BLACK);
                                this.setTooltip(new Tooltip("due date"));
                                this.setText(item);
                                //System.out.println(item);

                            }
                        }
                    }
                };

                return cell;
            }
        };
        activeItemsTableColDate.setCellFactory(cellDateFactory);

        //tblColStat = new TableColumn("Status");
        activeItemsTableColStat.setCellValueFactory(
                new PropertyValueFactory<TodoItem, Integer>("status"));
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
                            //if (item == 0) {
                            //    setGraphic(new ImageView("/todo/checkbox-done.png"));
                            //    setTooltip(new Tooltip("Press to put item to active again"));
                            //} else {
                            setGraphic(new ImageView("/todo/checkbox-empty.png"));
                            setTooltip(new Tooltip("Press to put item to done (ctrl+D)"));
                            //}
                        }
                    }
                };

                cell.setOnMouseClicked((event) -> {
                    //if (cell.getItem() == 0) {
                    //    cell.setItem(1);
                    //} else {
                    cell.setItem(0);
                    //}
                    String musicFile = "applause10.mp3";

                    Media sound = new Media(new File(musicFile).toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                    mediaPlayer.play();
                    db.changeItemStatus(activeItemsTable.getItems().get(activeItemsTable.getSelectionModel().getSelectedIndex()).getId(), cell.getItem(), activeItems.size());
                    updateRanks();
                    listTasks(onlyActive, onlyStarred, true);
                });

                return cell;
            }
        };

        activeItemsTableColStat.setCellFactory(cellDoneFactory);

        //tblColStat.setStyle("-fx-alignment: CENTER;");
        //tblColStat = new TableColumn("Rank");
        activeItemsTableColRank.setCellValueFactory(
                new PropertyValueFactory<TodoItem, Integer>("rank"));
        //tblColStat = new TableColumn("Star");
        activeItemsTableColStar.setCellValueFactory(
                new PropertyValueFactory<TodoItem, Integer>("star"));

        activeItemsTableColStat.setCellValueFactory(
                new PropertyValueFactory<TodoItem, String>("status"));
        //tblColStat.setStyle("-fx-alignment: CENTER;");
        Label tblColStatLabel = new Label("Status");

        tblColStatLabel.setTooltip(
                new Tooltip("This column shows the status of the ToDo tasks. Types of status: Completed, Pending & Overdue"));
        activeItemsTableColStat.setGraphic(tblColStatLabel);

        //tblColStat = new TableColumn("Status");
        activeItemsTableColStar.setCellValueFactory(
                new PropertyValueFactory<TodoItem, Integer>("star"));
        Label tblColStarLabel = new Label("");

        tblColStarLabel.setTooltip(
                new Tooltip("This column shows the favourites ToDo tasks."));
        activeItemsTableColStar.setGraphic(tblColStarLabel);

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
                            if (item == 1) //{
                            {
                                setGraphic(new ImageView("/todo/star.png"));
                                setTooltip(new Tooltip("Press to remove from favorites (ctrl+U)"));

                            } else {
                                //setGraphic(new ImageView("/todo/unstar.png"));
                                setTooltip(new Tooltip("Press to add to favorites (ctrl+F)"));
                            }
                        }
                    }
                };

                cell.setOnMouseClicked(
                        (event) -> {
                            if (cell.getItem() == 1) {
                                cell.setItem(0);
                            } else {
                                cell.setItem(1);
                            }

                            db.changeStarred(activeItemsTable.getItems().get(activeItemsTable.getSelectionModel().getSelectedIndex()).getId(), cell.getItem());
                            if (onlyStarred || showDateStart != null) {
                                listTasks(true, onlyStarred, true);
                            } else {
                                listTasks(onlyActive, onlyStarred, true);
                            }
                        }
                );

                return cell;
            }
        };

        activeItemsTableColStar.setCellFactory(cellFactory);

        //allow drag and drop only for full view of all active items
        //not favorites, not period => show all
        if (!onlyStarred && showDateStart
                == null) {
            activeItemsTable.setRowFactory(tv -> {
                TableRow<TodoItem> row = new TableRow<>();

                row.setOnDragDetected(event -> {
                    if (!row.isEmpty()) {
                        Integer indexLocal = row.getIndex();
                        Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                        dragboard.setDragView(row.snapshot(null, null));
                        ClipboardContent cc = new ClipboardContent();
                        cc.put(SERIALIZED_MIME_TYPE, indexLocal);
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
                        //int forDragged = activeItemsTable.getItems().get(draggedIndex).getRank();
                        TodoItem draggedTodoItem = activeItemsTable.getItems().remove(draggedIndex);

                        int dropIndex;
                        //int forDropped;

                        if (row.isEmpty()) {
                            dropIndex = activeItemsTable.getItems().size();
                            //forDropped = activeItemsTable.getItems().get(dropIndex - 1).getRank() + 1;
                            activeItemsTable.getItems().add(draggedTodoItem);
                        } else {
                            dropIndex = row.getIndex();
                            //forDropped = activeItemsTable.getItems().get(dropIndex).getRank();
                            activeItemsTable.getItems().add(dropIndex, draggedTodoItem);
                        }

                        //debugging
                        System.out.println("Dragged index=" + draggedIndex + "\t dropped index=" + dropIndex);

                        //selectedIndex - start
                        //dropIndex - end/drop        
                        event.setDropCompleted(true);

                        //call updateRanks(drag,drop)
                        updateRanks();

                        if (onlyStarred || showDateStart != null) {
                            listTasks(true, onlyStarred, true);
                        } else {
                            listTasks(onlyActive, onlyStarred, true);
                        }
                        event.consume();
                    }
                });

                return row;
            });
        }

        //tblColStat = new TableColumn("Status");
        activeItemsTableColAlarm.setCellValueFactory(
                new PropertyValueFactory<TodoItem, String>("alarm"));
        //set tblColStar to button
        Callback<TableColumn<TodoItem, String>, TableCell<TodoItem, String>> cellAlarmFactory;
        cellAlarmFactory = new Callback<TableColumn<TodoItem, String>, TableCell<TodoItem, String>>() {
            @Override
            public TableCell<TodoItem, String> call(final TableColumn<TodoItem, String> param) {
                final TableCell<TodoItem, String> cell = new TableCell<TodoItem, String>() {

                    private ImageView star = new ImageView();

                    @Override
                    public void updateItem(String item, boolean empty) {

                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            if (item == null) {
                                setGraphic(null);
                            } else {
                                setGraphic(new ImageView("/todo/alarm-on.png"));
                                setTooltip(new Tooltip(item));
                            }
                        }
                    }
                };

                return cell;
            }
        };

        activeItemsTableColAlarm.setCellFactory(cellAlarmFactory);

        activeItemsTable.getColumns()
                .setAll(activeItemsTableColStat, activeItemsTableColId, activeItemsTableColDesc, activeItemsTableColDate, activeItemsTableColStar, activeItemsTableColRank, activeItemsTableColAlarm);
        activeItemsTableColRank.setSortType(TableColumn.SortType.ASCENDING);
        //tblColStat.setSortType(TableColumn.SortType.DESCENDING);

        activeItemsTable.getSortOrder()
                .setAll(activeItemsTableColRank);
        //tblitems.getStyleClass().add("strike");
        //activeItemsTable.setEditable(true);
        activeItemsTable.setFixedCellSize(
                30);
        if (activeItemsTable.getItems()
                .size() == 0) {
            activeItemsTable.prefHeightProperty().bind(activeItemsTable.fixedCellSizeProperty().multiply(activeItemsTable.getItems().size()));
        } else {
            activeItemsTable.prefHeightProperty().bind(activeItemsTable.fixedCellSizeProperty().multiply(activeItemsTable.getItems().size()).add(1.01));
        }

        activeItemsTable.minHeightProperty()
                .bind(activeItemsTable.prefHeightProperty());
        activeItemsTable.maxHeightProperty()
                .bind(activeItemsTable.prefHeightProperty());
        activeItemsTable.widthProperty()
                .addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> source, Number oldWidth,
                            Number newWidth
                    ) {

                        //Don't show header
                        Pane header = (Pane) activeItemsTable.lookup("TableHeaderRow");
                        if (header.isVisible()) {
                            header.setMaxHeight(0);
                            header.setMinHeight(0);
                            header.setPrefHeight(0);
                            header.setVisible(false);
                        }
                    }
                }
                );
        //activeItemsTable.setStyle("-fx-background-color: transparent; ");
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
                listTasks(onlyActive, onlyStarred, true);
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
                            //if (item == 0) {
                            setGraphic(new ImageView("/todo/checkbox-done.png"));
                            setTooltip(new Tooltip("Press to put item to active again (ctrl+A)"));
                            //} else {
                            //    setGraphic(new ImageView("/todo/checkbox-empty.png"));
                            //    setTooltip(new Tooltip("Press to put item to done"));
                            //}
                        }
                    }
                };

                cell.setOnMouseClicked((event) -> {
                    //if (cell.getItem() == 0) {
                    cell.setItem(1);
                    //} else {
                    //    cell.setItem(0);
                    //}

                    db.changeItemStatus(doneItemsTable.getItems().get(doneItemsTable.getSelectionModel().getSelectedIndex()).getId(), cell.getItem(), activeItems.size());
                    listTasks(onlyActive, onlyStarred, true);
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

        doneItemsTable.getColumns().setAll(doneItemsTableColStat, doneItemsTableColId, doneItemsTableColDesc, doneItemsTableColDate, doneItemsTableColStar, doneItemsTableColRank);
        doneItemsTableColRank.setSortType(TableColumn.SortType.ASCENDING);
        //tblColStat.setSortType(TableColumn.SortType.DESCENDING);
        doneItemsTable.getSortOrder().setAll(doneItemsTableColRank);
        //tblitems.getStyleClass().add("strike");
        //doneItemsTable.setEditable(true);
        doneItemsTable.setFixedCellSize(30);
        if (doneItemsTable.getItems()
                .size() == 0) {
            doneItemsTable.prefHeightProperty().bind(doneItemsTable.fixedCellSizeProperty().multiply(doneItemsTable.getItems().size()));
        } else {

            doneItemsTable.prefHeightProperty().bind(doneItemsTable.fixedCellSizeProperty().multiply(doneItemsTable.getItems().size()).add(1.01));

        }
        doneItemsTable.minHeightProperty().bind(doneItemsTable.prefHeightProperty());
        doneItemsTable.maxHeightProperty().bind(doneItemsTable.prefHeightProperty());
        doneItemsTable.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {

                //Don't show header
                Pane header = (Pane) doneItemsTable.lookup("TableHeaderRow");
                if (header.isVisible()) {
                    header.setMaxHeight(0);
                    header.setMinHeight(0);
                    header.setPrefHeight(0);
                    header.setVisible(false);
                }
            }
        });
    }

    //disable menu options based on items parameters
    private void dynamicContextMenu(TodoItem currentRow) {
        //restore all items to visible
        setDoneMenuItem.setDisable(false);
        setActiveItem.setDisable(false);
        emailItem.setDisable(false);
        setDueDate.setDisable(false);
        starItem.setDisable(false);
        unstarItem.setDisable(false);
        removeAlarm.setDisable(false);
        setAlarm.setDisable(false);

        //disable options according to items parameters
        //TodoItem currentRow = activeItemsTable.getItems().get(row);
        if (currentRow.getStatus() == 0) {
            //for 'done' items disable:
            setDoneMenuItem.setDisable(true);
            emailItem.setDisable(true);
            setDueDate.setDisable(true);
            starItem.setDisable(true);
            unstarItem.setDisable(true);
            removeAlarm.setDisable(true);
            setAlarm.setDisable(true);

        } else if (currentRow.getStatus() == 1) {
            //for 'pending' items disable:
            setActiveItem.setDisable(true);

            if (currentRow.getAlarm() == null) {
                removeAlarm.setDisable(true);
                //setAlarm.setDisable(false);
            } else {
                //removeAlarm.setDisable(false);
                setAlarm.setDisable(true);
            }

            if (currentRow.getStar() == 1) {
                starItem.setDisable(true);
                //unstarItem.setDisable(false);
            } else {
                //starItem.setDisable(false);
                unstarItem.setDisable(true);
            }

        } else {
            //for 'overdue' items disable:
            removeAlarm.setDisable(true);
            setAlarm.setDisable(true);
            setActiveItem.setDisable(true);

            if (currentRow.getStar() == 1) {
                starItem.setDisable(true);
                //unstarItem.setDisable(false);
            } else {
                //starItem.setDisable(false);
                unstarItem.setDisable(true);
            }
        }

    }

    private void executePlayAlarm(long itemId, String text) {

        //reset alarm in DB
        db.setAlarm(itemId, null);

        //refresh list of tasks
        if (onlyStarred || showDateStart != null) {
            listTasks(true, onlyStarred, true);
        } else {
            listTasks(onlyActive, onlyStarred, true);
        }

        //play alarm
        String musicFile = "alarm.mp3";
        Media sound = new Media(new File(musicFile).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        if (!mediaPlayer.getStatus().equals(Status.PLAYING)) {
            mediaPlayer.play();
        }

        //popup alert window
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Todo item's: \"" + text + "\" reminder!");

        Button okButton = new Button("Ok");

        EventHandler<ActionEvent> okButtonPressed = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                alert.hide();

            }
        };

        okButton.setOnAction(okButtonPressed);
        //alert.setGraphic(okButton);
        alert.show();

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //create new DBHandler object and establish connection to DB
        db = new DBHandler("tasks");
        dbFolders = new DBHandler("folders");
        listFolders();
        buildTableContextMenu();
        buildFoldersContextMenu();

        borderPane.setLeft(leftFlapBar);
        borderPane.setRight(rightFlapBar);
        //toolbar.getItems().addAll(leftMenu);
        leftMenu.setTooltip(new Tooltip("Open side menu"));
        clock.setTooltip(new Tooltip("Current time"));
        anchorPane.getChildren().add(0, leftMenu);
        //anchorPane.setH
        //description.setLayoutX(75);
        //rightEdit.setGraphic(new ImageView("/todo/edit.png"));
        //editItem.setGraphic(rightEdit);
        
        folderList.setOnMouseClicked((event) -> {
            int indexLocal = folderList.getSelectionModel().getSelectedIndex();
            folderFolderId = folderItems.get(indexLocal).getFolderId();
            System.out.println("folder list, index="+indexLocal+"\tFolderId="+folderFolderId);
            
            switch(indexLocal) {
                case -1:
                    break;
                case 0:
                    break;
                default:
                    break;
                    
            }
        });

        menuList.setOnMouseClicked((event) -> {

            //index from list of task is read via MouseEvent (any for now)
            int indexLocal = menuList.getSelectionModel().getSelectedIndex();

            //if valid, it's used for menu actions (filtering items by dates)
            switch (indexLocal) {
                case -1:
                    break;
                case 0:
                    showDateStart = null;
                    onlyStarred = false;
                    datePicker.setValue(null);
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - All");
                    //tableLabel.setText("Todo Items");
                    description.setPromptText("Add new todo task...");
                    //set visibility of show done button
                    buttonShowOptions.setVisible(true);
                    if (onlyActive) {
                        buttonDeleteDone.setVisible(false);
                    } else {
                        buttonDeleteDone.setVisible(true);
                    }
                    listTasks(onlyActive, onlyStarred, false);
                    //index = -1;

                    break;
                case 1:
                    showDateStart = null;
                    onlyStarred = true;
                    datePicker.setValue(null);

                    //tableLabel.setText("Favorite Items");
                    //set visibility of show done button
                    buttonShowOptions.setVisible(false);
                    buttonDeleteDone.setVisible(false);
                    description.setPromptText("Add new todo task...");
                    listTasks(true, onlyStarred, false);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - Favorites");
                    break;
                case 2:
                    showDateStart = showDateEnd = LocalDate.now();
                    //preset DatePicker to current view date
                    datePicker.setValue(showDateStart);
                    onlyStarred = false;

                    //tableLabel.setText("Todo Items for today");
                    //set visibility of show done button
                    buttonShowOptions.setVisible(false);
                    buttonDeleteDone.setVisible(false);
                    description.setPromptText("Add new todo task...");
                    listTasks(true, onlyStarred, false);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - Today");
                    break;
                case 3:
                    showDateStart = showDateEnd = LocalDate.now().plus(1, ChronoUnit.DAYS);
                    //preset DatePicker to current view date
                    datePicker.setValue(showDateStart);
                    onlyStarred = false;

                    //tableLabel.setText("Todo Items for tomorrow");
                    //set visibility of show done button
                    buttonShowOptions.setVisible(false);
                    buttonDeleteDone.setVisible(false);
                    description.setPromptText("Add new todo task...");
                    listTasks(true, onlyStarred, false);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - Tomorrow");
                    break;
                case 4:
                    //showDateStart = LocalDate.now().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
                    //showDateEnd = LocalDate.now().with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7);
                    showDateStart = LocalDate.now();
                    showDateEnd = LocalDate.now().plus(7, ChronoUnit.DAYS);
                    datePicker.setValue(null);
                    onlyStarred = false;

                    //tableLabel.setText("Todo Items for upcoming week");
                    //set visibility of show done button
                    buttonShowOptions.setVisible(false);
                    buttonDeleteDone.setVisible(false);
                    description.setPromptText("Add new todo task...");
                    listTasks(true, onlyStarred, false);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - Upcoming Week");
                    break;
                case 5:
                    //showDateStart = LocalDate.now().withDayOfMonth(1);
                    //showDateEnd = LocalDate.now().withDayOfMonth(showDateStart.lengthOfMonth());
                    showDateStart = LocalDate.now();
                    showDateEnd = LocalDate.now().plus(showDateStart.lengthOfMonth(), ChronoUnit.DAYS);
                    datePicker.setValue(null);
                    onlyStarred = false;

                    //tableLabel.setText("Todo Items for upcoming month");
                    //set visibility of show done button
                    buttonShowOptions.setVisible(false);
                    buttonDeleteDone.setVisible(false);
                    description.setPromptText("Add new todo task...");
                    listTasks(true, onlyStarred, false);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - Upcoming Month");
                    break;
            }
            //event.consume();

        });

        folderAddText.setOnAction((event) -> {
            String newFolderName = folderAddText.getText();
            dbFolders.insertFolderItem(newFolderName);
            //folderList.setItems(folderItems);
            listFolders();
        });

        listTasks(onlyActive, onlyStarred, true);
        String currentDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //final Calendar cal = Calendar.getInstance();

            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            clock.setText(currentTime);
            //

            if (anyAlarm) {
                for (int i = 0; i < allItems.size(); i++) {
                    if (allItems.get(i).getAlarm() != null && allItems.get(i).getDate().equals(currentDay) && allItems.get(i).getAlarm().equals(currentTime)) {
                        //play alarm and continue scanning
                        anyAlarm = false; //will be updated in listTask anyway
                        executePlayAlarm(allItems.get(i).getId(), allItems.get(i).getDescription());

                    }
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);

        timeline.play();

    }

}
