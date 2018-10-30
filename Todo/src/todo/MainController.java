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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
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
    private long hoveredId = -1;
    private int folderFolderId = 1; //folder Id from DB, folder table

    private ObservableList<TodoItem> activeItems = FXCollections.observableArrayList(); //ObservableList of active items
    private ObservableList<TodoItem> doneItems = FXCollections.observableArrayList(); //ObservableList of done items
    private ObservableList<Label> menuItems = FXCollections.observableArrayList(); //ObservableList of left menu items
    private ObservableList<FolderItem> folderItems = FXCollections.observableArrayList(); //ObservableList of folder items
    private ObservableList<String> folderNames = FXCollections.observableArrayList();
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

    private final String trashBucketSVG = "M16.588,3.411h-4.466c0.042-0.116,0.074-0.236,0.074-0.366c0-0.606-0.492-1.098-1.099-1.098H8.901c-0.607,0-1.098,0.492-1.098,1.098c0,0.13,0.033,0.25,0.074,0.366H3.41c-0.606,0-1.098,0.492-1.098,1.098c0,0.607,0.492,1.098,1.098,1.098h0.366V16.59c0,0.808,0.655,1.464,1.464,1.464h9.517c0.809,0,1.466-0.656,1.466-1.464V5.607h0.364c0.607,0,1.1-0.491,1.1-1.098C17.688,3.903,17.195,3.411,16.588,3.411z M8.901,2.679h2.196c0.202,0,0.366,0.164,0.366,0.366S11.3,3.411,11.098,3.411H8.901c-0.203,0-0.366-0.164-0.366-0.366S8.699,2.679,8.901,2.679z M15.491,16.59c0,0.405-0.329,0.731-0.733,0.731H5.241c-0.404,0-0.732-0.326-0.732-0.731V5.607h10.983V16.59z M16.588,4.875H3.41c-0.203,0-0.366-0.164-0.366-0.366S3.208,4.143,3.41,4.143h13.178c0.202,0,0.367,0.164,0.367,0.366S16.79,4.875,16.588,4.875zM6.705,14.027h6.589c0.202,0,0.366-0.164,0.366-0.366s-0.164-0.367-0.366-0.367H6.705c-0.203,0-0.366,0.165-0.366,0.367S6.502,14.027,6.705,14.027z M6.705,11.83h6.589c0.202,0,0.366-0.164,0.366-0.365c0-0.203-0.164-0.367-0.366-0.367H6.705c-0.203,0-0.366,0.164-0.366,0.367C6.339,11.666,6.502,11.83,6.705,11.83z M6.705,9.634h6.589c0.202,0,0.366-0.164,0.366-0.366c0-0.202-0.164-0.366-0.366-0.366H6.705c-0.203,0-0.366,0.164-0.366,0.366C6.339,9.47,6.502,9.634,6.705,9.634z";
    private final String envelopeSVG = "M17.388,4.751H2.613c-0.213,0-0.389,0.175-0.389,0.389v9.72c0,0.216,0.175,0.389,0.389,0.389h14.775c0.214,0,0.389-0.173,0.389-0.389v-9.72C17.776,4.926,17.602,4.751,17.388,4.751 M16.448,5.53L10,11.984L3.552,5.53H16.448zM3.002,6.081l3.921,3.925l-3.921,3.925V6.081z M3.56,14.471l3.914-3.916l2.253,2.253c0.153,0.153,0.395,0.153,0.548,0l2.253-2.253l3.913,3.916H3.56z M16.999,13.931l-3.921-3.925l3.921-3.925V13.931z";
    private final String backToActiveSVG = "M3.24,7.51c-0.146,0.142-0.146,0.381,0,0.523l5.199,5.193c0.234,0.238,0.633,0.064,0.633-0.262v-2.634c0.105-0.007,0.212-0.011,0.321-0.011c2.373,0,4.302,1.91,4.302,4.258c0,0.957-0.33,1.809-1.008,2.602c-0.259,0.307,0.084,0.762,0.451,0.572c2.336-1.195,3.73-3.408,3.73-5.924c0-3.741-3.103-6.783-6.916-6.783c-0.307,0-0.615,0.028-0.881,0.063V2.575c0-0.327-0.398-0.5-0.633-0.261L3.24,7.51 M4.027,7.771l4.301-4.3v2.073c0,0.232,0.21,0.409,0.441,0.366c0.298-0.056,0.746-0.123,1.184-0.123c3.402,0,6.172,2.709,6.172,6.041c0,1.695-0.718,3.24-1.979,4.352c0.193-0.51,0.293-1.045,0.293-1.602c0-2.76-2.266-5-5.046-5c-0.256,0-0.528,0.018-0.747,0.05C8.465,9.653,8.328,9.81,8.328,9.995v2.074L4.027,7.771z";
    private final String calendarSVG = "M16.557,4.467h-1.64v-0.82c0-0.225-0.183-0.41-0.409-0.41c-0.226,0-0.41,0.185-0.41,0.41v0.82H5.901v-0.82c0-0.225-0.185-0.41-0.41-0.41c-0.226,0-0.41,0.185-0.41,0.41v0.82H3.442c-0.904,0-1.64,0.735-1.64,1.639v9.017c0,0.904,0.736,1.64,1.64,1.64h13.114c0.904,0,1.64-0.735,1.64-1.64V6.106C18.196,5.203,17.461,4.467,16.557,4.467 M17.377,15.123c0,0.453-0.366,0.819-0.82,0.819H3.442c-0.453,0-0.82-0.366-0.82-0.819V8.976h14.754V15.123z M17.377,8.156H2.623V6.106c0-0.453,0.367-0.82,0.82-0.82h1.639v1.23c0,0.225,0.184,0.41,0.41,0.41c0.225,0,0.41-0.185,0.41-0.41v-1.23h8.196v1.23c0,0.225,0.185,0.41,0.41,0.41c0.227,0,0.409-0.185,0.409-0.41v-1.23h1.64c0.454,0,0.82,0.367,0.82,0.82V8.156z";
    private final String setAlarmSVG = "M14.38,3.467l0.232-0.633c0.086-0.226-0.031-0.477-0.264-0.559c-0.229-0.081-0.48,0.033-0.562,0.262l-0.234,0.631C10.695,2.38,7.648,3.89,6.616,6.689l-1.447,3.93l-2.664,1.227c-0.354,0.166-0.337,0.672,0.035,0.805l4.811,1.729c-0.19,1.119,0.445,2.25,1.561,2.65c1.119,0.402,2.341-0.059,2.923-1.039l4.811,1.73c0,0.002,0.002,0.002,0.002,0.002c0.23,0.082,0.484-0.033,0.568-0.262c0.049-0.129,0.029-0.266-0.041-0.377l-1.219-2.586l1.447-3.932C18.435,7.768,17.085,4.676,14.38,3.467 M9.215,16.211c-0.658-0.234-1.054-0.869-1.014-1.523l2.784,0.998C10.588,16.215,9.871,16.447,9.215,16.211 M16.573,10.27l-1.51,4.1c-0.041,0.107-0.037,0.227,0.012,0.33l0.871,1.844l-4.184-1.506l-3.734-1.342l-4.185-1.504l1.864-0.857c0.104-0.049,0.188-0.139,0.229-0.248l1.51-4.098c0.916-2.487,3.708-3.773,6.222-2.868C16.187,5.024,17.489,7.783,16.573,10.27";
    private final String starSVG = "M15.94,10.179l-2.437-0.325l1.62-7.379c0.047-0.235-0.132-0.458-0.372-0.458H5.25c-0.241,0-0.42,0.223-0.373,0.458l1.634,7.376L4.06,10.179c-0.312,0.041-0.446,0.425-0.214,0.649l2.864,2.759l-0.724,3.947c-0.058,0.315,0.277,0.554,0.559,0.401l3.457-1.916l3.456,1.916c-0.419-0.238,0.56,0.439,0.56-0.401l-0.725-3.947l2.863-2.759C16.388,10.604,16.254,10.22,15.94,10.179M10.381,2.778h3.902l-1.536,6.977L12.036,9.66l-1.655-3.546V2.778z M5.717,2.778h3.903v3.335L7.965,9.66L7.268,9.753L5.717,2.778zM12.618,13.182c-0.092,0.088-0.134,0.217-0.11,0.343l0.615,3.356l-2.938-1.629c-0.057-0.03-0.122-0.048-0.184-0.048c-0.063,0-0.128,0.018-0.185,0.048l-2.938,1.629l0.616-3.356c0.022-0.126-0.019-0.255-0.11-0.343l-2.441-2.354l3.329-0.441c0.128-0.017,0.24-0.099,0.295-0.215l1.435-3.073l1.435,3.073c0.055,0.116,0.167,0.198,0.294,0.215l3.329,0.441L12.618,13.182z";
    //private final String crossedStarSVG = "M0,20l20,-20z M15.94,10.179l-2.437-0.325l1.62-7.379c0.047-0.235-0.132-0.458-0.372-0.458H5.25c-0.241,0-0.42,0.223-0.373,0.458l1.634,7.376L4.06,10.179c-0.312,0.041-0.446,0.425-0.214,0.649l2.864,2.759l-0.724,3.947c-0.058,0.315,0.277,0.554,0.559,0.401l3.457-1.916l3.456,1.916c-0.419-0.238,0.56,0.439,0.56-0.401l-0.725-3.947l2.863-2.759C16.388,10.604,16.254,10.22,15.94,10.179M10.381,2.778h3.902l-1.536,6.977L12.036,9.66l-1.655-3.546V2.778z M5.717,2.778h3.903v3.335L7.965,9.66L7.268,9.753L5.717,2.778zM12.618,13.182c-0.092,0.088-0.134,0.217-0.11,0.343l0.615,3.356l-2.938-1.629c-0.057-0.03-0.122-0.048-0.184-0.048c-0.063,0-0.128,0.018-0.185,0.048l-2.938,1.629l0.616-3.356c0.022-0.126-0.019-0.255-0.11-0.343l-2.441-2.354l3.329-0.441c0.128-0.017,0.24-0.099,0.295-0.215l1.435-3.073l1.435,3.073c0.055,0.116,0.167,0.198,0.294,0.215l3.329,0.441L12.618,13.182z";
    private final String unstarSVG = "M16.85,7.275l-3.967-0.577l-1.773-3.593c-0.208-0.423-0.639-0.69-1.11-0.69s-0.902,0.267-1.11,0.69L7.116,6.699L3.148,7.275c-0.466,0.068-0.854,0.394-1,0.842c-0.145,0.448-0.023,0.941,0.314,1.27l2.871,2.799l-0.677,3.951c-0.08,0.464,0.112,0.934,0.493,1.211c0.217,0.156,0.472,0.236,0.728,0.236c0.197,0,0.396-0.048,0.577-0.143l3.547-1.864l3.548,1.864c0.18,0.095,0.381,0.143,0.576,0.143c0.256,0,0.512-0.08,0.729-0.236c0.381-0.277,0.572-0.747,0.492-1.211l-0.678-3.951l2.871-2.799c0.338-0.329,0.459-0.821,0.314-1.27C17.705,7.669,17.316,7.343,16.85,7.275z M13.336,11.754l0.787,4.591l-4.124-2.167l-4.124,2.167l0.788-4.591L3.326,8.5l4.612-0.67l2.062-4.177l2.062,4.177l4.613,0.67L13.336,11.754z";
    private final String clockSVG = "M10.25,2.375c-4.212,0-7.625,3.413-7.625,7.625s3.413,7.625,7.625,7.625s7.625-3.413,7.625-7.625S14.462,2.375,10.25,2.375M10.651,16.811v-0.403c0-0.221-0.181-0.401-0.401-0.401s-0.401,0.181-0.401,0.401v0.403c-3.443-0.201-6.208-2.966-6.409-6.409h0.404c0.22,0,0.401-0.181,0.401-0.401S4.063,9.599,3.843,9.599H3.439C3.64,6.155,6.405,3.391,9.849,3.19v0.403c0,0.22,0.181,0.401,0.401,0.401s0.401-0.181,0.401-0.401V3.19c3.443,0.201,6.208,2.965,6.409,6.409h-0.404c-0.22,0-0.4,0.181-0.4,0.401s0.181,0.401,0.4,0.401h0.404C16.859,13.845,14.095,16.609,10.651,16.811 M12.662,12.412c-0.156,0.156-0.409,0.159-0.568,0l-2.127-2.129C9.986,10.302,9.849,10.192,9.849,10V5.184c0-0.221,0.181-0.401,0.401-0.401s0.401,0.181,0.401,0.401v4.651l2.011,2.008C12.818,12.001,12.818,12.256,12.662,12.412";
    private final String doSVG = "M10.219,1.688c-4.471,0-8.094,3.623-8.094,8.094s3.623,8.094,8.094,8.094s8.094-3.623,8.094-8.094S14.689,1.688,10.219,1.688 M10.219,17.022c-3.994,0-7.242-3.247-7.242-7.241c0-3.994,3.248-7.242,7.242-7.242c3.994,0,7.241,3.248,7.241,7.242C17.46,13.775,14.213,17.022,10.219,17.022 M15.099,7.03c-0.167-0.167-0.438-0.167-0.604,0.002L9.062,12.48l-2.269-2.277c-0.166-0.167-0.437-0.167-0.603,0c-0.166,0.166-0.168,0.437-0.002,0.603l2.573,2.578c0.079,0.08,0.188,0.125,0.3,0.125s0.222-0.045,0.303-0.125l5.736-5.751C15.268,7.466,15.265,7.196,15.099,7.03";
    private final String undoSVG = "M14.776,10c0,0.239-0.195,0.434-0.435,0.434H5.658c-0.239,0-0.434-0.195-0.434-0.434s0.195-0.434,0.434-0.434h8.684C14.581,9.566,14.776,9.762,14.776,10 M18.25,10c0,4.558-3.693,8.25-8.25,8.25c-4.557,0-8.25-3.691-8.25-8.25c0-4.557,3.693-8.25,8.25-8.25C14.557,1.75,18.25,5.443,18.25,10 M17.382,10c0-4.071-3.312-7.381-7.382-7.381C5.929,2.619,2.619,5.93,2.619,10c0,4.07,3.311,7.382,7.381,7.382C14.07,17.383,17.382,14.07,17.382,10";
    private final String editSVG = "M19.404,6.65l-5.998-5.996c-0.292-0.292-0.765-0.292-1.056,0l-2.22,2.22l-8.311,8.313l-0.003,0.001v0.003l-0.161,0.161c-0.114,0.112-0.187,0.258-0.21,0.417l-1.059,7.051c-0.035,0.233,0.044,0.47,0.21,0.639c0.143,0.14,0.333,0.219,0.528,0.219c0.038,0,0.073-0.003,0.111-0.009l7.054-1.055c0.158-0.025,0.306-0.098,0.417-0.211l8.478-8.476l2.22-2.22C19.695,7.414,19.695,6.941,19.404,6.65z M8.341,16.656l-0.989-0.99l7.258-7.258l0.989,0.99L8.341,16.656z M2.332,15.919l0.411-2.748l4.143,4.143l-2.748,0.41L2.332,15.919z M13.554,7.351L6.296,14.61l-0.849-0.848l7.259-7.258l0.423,0.424L13.554,7.351zM10.658,4.457l0.992,0.99l-7.259,7.258L3.4,11.715L10.658,4.457z M16.656,8.342l-1.517-1.517V6.823h-0.003l-0.951-0.951l-2.471-2.471l1.164-1.164l4.942,4.94L16.656,8.342z";
    private final String removeAlarmSVG = "M3.401,13.367h0.959l1.56-1.56H4.181v-4.07h3.177c0.207,0,0.405-0.084,0.553-0.23l3.608-3.633V6.21l1.56-1.56V1.983c0-0.315-0.192-0.602-0.485-0.721c-0.29-0.122-0.624-0.055-0.85,0.171L7.032,6.178h-3.63c-0.433,0-0.78,0.349-0.78,0.78v5.629C2.621,13.018,2.968,13.367,3.401,13.367z M11.519,15.674l-2.416-2.418L8,14.358l3.745,3.753c0.149,0.149,0.349,0.228,0.553,0.228c0.1,0,0.201-0.019,0.297-0.059c0.291-0.12,0.483-0.405,0.483-0.72V9.28l-1.56,1.56V15.674z M19.259,0.785c-0.167-0.168-0.387-0.25-0.606-0.25s-0.438,0.082-0.606,0.25l-4.968,4.968l-1.56,1.56l-4.496,4.494l-1.56,1.56L0.83,18.001c-0.335,0.335-0.335,0.877,0,1.213c0.167,0.167,0.386,0.251,0.606,0.251c0.22,0,0.439-0.084,0.606-0.251l5.407-5.407l1.105-1.104l2.965-2.966l1.56-1.56l6.18-6.181C19.594,1.664,19.594,1.12,19.259,0.785z";
    private final String showAllSVG = "M2.25,12.584c-0.713,0-1.292,0.578-1.292,1.291s0.579,1.291,1.292,1.291c0.713,0,1.292-0.578,1.292-1.291S2.963,12.584,2.25,12.584z M2.25,14.307c-0.238,0-0.43-0.193-0.43-0.432s0.192-0.432,0.43-0.432c0.238,0,0.431,0.193,0.431,0.432S2.488,14.307,2.25,14.307z M5.694,6.555H18.61c0.237,0,0.431-0.191,0.431-0.43s-0.193-0.431-0.431-0.431H5.694c-0.238,0-0.43,0.192-0.43,0.431S5.457,6.555,5.694,6.555z M2.25,8.708c-0.713,0-1.292,0.578-1.292,1.291c0,0.715,0.579,1.292,1.292,1.292c0.713,0,1.292-0.577,1.292-1.292C3.542,9.287,2.963,8.708,2.25,8.708z M2.25,10.43c-0.238,0-0.43-0.192-0.43-0.431c0-0.237,0.192-0.43,0.43-0.43c0.238,0,0.431,0.192,0.431,0.43C2.681,10.238,2.488,10.43,2.25,10.43z M18.61,9.57H5.694c-0.238,0-0.43,0.192-0.43,0.43c0,0.238,0.192,0.431,0.43,0.431H18.61c0.237,0,0.431-0.192,0.431-0.431C19.041,9.762,18.848,9.57,18.61,9.57z M18.61,13.443H5.694c-0.238,0-0.43,0.193-0.43,0.432s0.192,0.432,0.43,0.432H18.61c0.237,0,0.431-0.193,0.431-0.432S18.848,13.443,18.61,13.443z M2.25,4.833c-0.713,0-1.292,0.578-1.292,1.292c0,0.713,0.579,1.291,1.292,1.291c0.713,0,1.292-0.578,1.292-1.291C3.542,5.412,2.963,4.833,2.25,4.833z M2.25,6.555c-0.238,0-0.43-0.191-0.43-0.43s0.192-0.431,0.43-0.431c0.238,0,0.431,0.192,0.431,0.431S2.488,6.555,2.25,6.555z";
    private final String showDateSVG = "M16.254,3.399h-0.695V3.052c0-0.576-0.467-1.042-1.041-1.042c-0.576,0-1.043,0.467-1.043,1.042v0.347H6.526V3.052c0-0.576-0.467-1.042-1.042-1.042S4.441,2.476,4.441,3.052v0.347H3.747c-0.768,0-1.39,0.622-1.39,1.39v11.813c0,0.768,0.622,1.39,1.39,1.39h12.507c0.768,0,1.391-0.622,1.391-1.39V4.789C17.645,4.021,17.021,3.399,16.254,3.399z M14.17,3.052c0-0.192,0.154-0.348,0.348-0.348c0.191,0,0.348,0.156,0.348,0.348v0.347H14.17V3.052z M5.136,3.052c0-0.192,0.156-0.348,0.348-0.348S5.831,2.86,5.831,3.052v0.347H5.136V3.052z M16.949,16.602c0,0.384-0.311,0.694-0.695,0.694H3.747c-0.384,0-0.695-0.311-0.695-0.694V7.568h13.897V16.602z M16.949,6.874H3.052V4.789c0-0.383,0.311-0.695,0.695-0.695h12.507c0.385,0,0.695,0.312,0.695,0.695V6.874z M5.484,11.737c0.576,0,1.042-0.467,1.042-1.042c0-0.576-0.467-1.043-1.042-1.043s-1.042,0.467-1.042,1.043C4.441,11.271,4.908,11.737,5.484,11.737z M5.484,10.348c0.192,0,0.347,0.155,0.347,0.348c0,0.191-0.155,0.348-0.347,0.348s-0.348-0.156-0.348-0.348C5.136,10.503,5.292,10.348,5.484,10.348z M14.518,11.737c0.574,0,1.041-0.467,1.041-1.042c0-0.576-0.467-1.043-1.041-1.043c-0.576,0-1.043,0.467-1.043,1.043C13.475,11.271,13.941,11.737,14.518,11.737z M14.518,10.348c0.191,0,0.348,0.155,0.348,0.348c0,0.191-0.156,0.348-0.348,0.348c-0.193,0-0.348-0.156-0.348-0.348C14.17,10.503,14.324,10.348,14.518,10.348z M14.518,15.212c0.574,0,1.041-0.467,1.041-1.043c0-0.575-0.467-1.042-1.041-1.042c-0.576,0-1.043,0.467-1.043,1.042C13.475,14.745,13.941,15.212,14.518,15.212z M14.518,13.822c0.191,0,0.348,0.155,0.348,0.347c0,0.192-0.156,0.348-0.348,0.348c-0.193,0-0.348-0.155-0.348-0.348C14.17,13.978,14.324,13.822,14.518,13.822z M10,15.212c0.575,0,1.042-0.467,1.042-1.043c0-0.575-0.467-1.042-1.042-1.042c-0.576,0-1.042,0.467-1.042,1.042C8.958,14.745,9.425,15.212,10,15.212z M10,13.822c0.192,0,0.348,0.155,0.348,0.347c0,0.192-0.156,0.348-0.348,0.348s-0.348-0.155-0.348-0.348C9.653,13.978,9.809,13.822,10,13.822z M5.484,15.212c0.576,0,1.042-0.467,1.042-1.043c0-0.575-0.467-1.042-1.042-1.042s-1.042,0.467-1.042,1.042C4.441,14.745,4.908,15.212,5.484,15.212z M5.484,13.822c0.192,0,0.347,0.155,0.347,0.347c0,0.192-0.155,0.348-0.347,0.348s-0.348-0.155-0.348-0.348C5.136,13.978,5.292,13.822,5.484,13.822z M10,11.737c0.575,0,1.042-0.467,1.042-1.042c0-0.576-0.467-1.043-1.042-1.043c-0.576,0-1.042,0.467-1.042,1.043C8.958,11.271,9.425,11.737,10,11.737z M10,10.348c0.192,0,0.348,0.155,0.348,0.348c0,0.191-0.156,0.348-0.348,0.348s-0.348-0.156-0.348-0.348C9.653,10.503,9.809,10.348,10,10.348z";
    private final String activeSVG = "M9.917,0.875c-5.086,0-9.208,4.123-9.208,9.208c0,5.086,4.123,9.208,9.208,9.208s9.208-4.122,9.208-9.208\n"
            + "C19.125,4.998,15.003,0.875,9.917,0.875z M9.917,18.141c-4.451,0-8.058-3.607-8.058-8.058s3.607-8.057,8.058-8.057\n"
            + "c4.449,0,8.057,3.607,8.057,8.057S14.366,18.141,9.917,18.141z M13.851,6.794l-5.373,5.372L5.984,9.672\n"
            + "c-0.219-0.219-0.575-0.219-0.795,0c-0.219,0.22-0.219,0.575,0,0.794l2.823,2.823c0.02,0.028,0.031,0.059,0.055,0.083\n"
            + "c0.113,0.113,0.263,0.166,0.411,0.162c0.148,0.004,0.298-0.049,0.411-0.162c0.024-0.024,0.036-0.055,0.055-0.083l5.701-5.7\n"
            + "c0.219-0.219,0.219-0.575,0-0.794C14.425,6.575,14.069,6.575,13.851,6.794z";

    //private final String leftMenuControlSVG = "M5,0v10m0,5v10";
    private final String openSVG = "M20,0v100h1v-100h1v100h1v-100 M20,150v700h1v-700h1v700h1v-700 M15,105l10,10l-10,10 M15,125l10,10l-10,10";

    //declarations of Menu Items
    MenuItem deleteMenuItem = new MenuItem("Delete Item");
    MenuItem setDoneMenuItem = new MenuItem("Set Item to Done");
    MenuItem setActiveItem = new MenuItem("Set Item to Pending");
    Menu setDueDate = new Menu("Set Due Date");
    //Menu assignFolder = new Menu("Assign to Folder...");
    //MenuItem setDueToday = new MenuItem("today");
    //MenuItem setDueTomorrow = new MenuItem("tomorrow");
    DatePicker menuDatePicker = new DatePicker();
    MenuItem setDueDatePicker = new MenuItem();
    MenuItem editItem = new MenuItem("Edit");
    MenuItem starItem = new MenuItem("Set Favorite");
    MenuItem unstarItem = new MenuItem("Unfavor");
    MenuItem emailItem = new MenuItem("Send email");
    Menu setAlarm = new Menu("Set Alarm");
    Spinner<LocalTime> alarmHourSpinner = new Spinner(new SpinnerValueFactory() {

        {
            setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("HH"), DateTimeFormatter.ofPattern("HH")));
        }

        @Override
        public void decrement(int steps) {
            if (getValue() == null) {
                setValue(LocalTime.now());
            } else {
                LocalTime time = (LocalTime) getValue();
                setValue(time.minusHours(steps));
            }
        }

        @Override
        public void increment(int steps) {
            if (this.getValue() == null) {
                setValue(LocalTime.now());
            } else {
                LocalTime time = (LocalTime) getValue();
                setValue(time.plusHours(steps));
            }
        }
    });

    Spinner<LocalTime> alarmMinuteSpinner = new Spinner(new SpinnerValueFactory() {

        {
            setConverter(new LocalTimeStringConverter(DateTimeFormatter.ofPattern("mm"), DateTimeFormatter.ofPattern("mm")));
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
    Button confirmAlarm = new Button("Set");
    private HBox spinnerTimeHBox = new HBox(alarmHourSpinner, alarmMinuteSpinner, confirmAlarm);

    //spinner.setEditable(true);
    //Spinner minuteSpinner = new Spinner();
    MenuItem setAlarmSpinner = new MenuItem();
    MenuItem confirmAlarmSpinner = new MenuItem();

    MenuItem removeAlarm = new MenuItem("Remove Alarm");
    MenuItem menuFolderChoiceBox = new MenuItem();

    //menu items for folders
    MenuItem removeFolderAll = new MenuItem("Delete Folder and all Items");
    MenuItem removeFolderDefault = new MenuItem("Delete Folder but keep Items");
    MenuItem removeFolderItems = new MenuItem("Delete All Items from folder");

    private Popup popup = new Popup();

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
    private Label leftMenu = new Label();
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
            System.out.println(id + "\t" + index + "\t fav=" + activeItemsTable.getItems().get(index).getStar() + "\t rank=" + activeItemsTable.getItems().get(index).getRank() + "\t status=" + activeItemsTable.getItems().get(index).getStatus() + "\t folderId=" + activeItemsTable.getItems().get(index).getFolderId());

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
            listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
            listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
        listTasks(onlyActive, onlyStarred, false, folderFolderId);
    }

    //handleDatePicker
    @FXML
    private void handleDatePicker() {
        localDate = datePicker.getValue();
        if (localDate != null) {
            if (descriptionText != null) {
                //create new item
                db.insertToDoItem(descriptionText, 1, localDate.toString(), onlyStarred ? 1 : 0, activeItems.size(), folderFolderId);

                //clear description field, ready for next task
                description.clear();
                descriptionText = null;

                //reset prompt text of textField
                description.setPromptText("Add new todo task...");

                //reset date in DatePicker
                datePicker.setValue(null);

                //refresh list of tasks after every task
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true, folderFolderId);
                } else {
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                }
            } else if (hoveredId != -1) {
                db.setDueDate(hoveredId, localDate.toString());
                hoveredId = -1;
                datePicker.setValue(null);
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true, folderFolderId);
                } else {
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
                        listTasks(true, onlyStarred, true, folderFolderId);
                    } else {
                        listTasks(onlyActive, onlyStarred, true, folderFolderId);
                    }
                    listFolders();
                } else if (result.get() == noButton) {
                    event.consume();
                } else if (result.get() == cancelButton) {
                    event.consume();
                }

            }
        };

        //Delete Folder and move Items to default (name="All", id=1)
        EventHandler<ActionEvent> actionDeleteFolderDefault = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //db.deleteToDoItem(id);
                //listTasks(onlyActive, onlyStarred);
                //Warning Dialog Box for delete one task 
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Warning");
                alert.setHeaderText("Would You Like To Delete Folder?");
                alert.setContentText("Please choose an option.");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == yesButton) {
                    db.moveItemsDefault(folderFolderId);
                    dbFolders.deleteFolder(folderFolderId);
                    //refresh list of tasks
                    if (onlyStarred || showDateStart != null) {
                        listTasks(true, onlyStarred, true, folderFolderId);
                    } else {
                        listTasks(onlyActive, onlyStarred, true, folderFolderId);
                    }
                    listFolders();
                } else if (result.get() == noButton) {
                    event.consume();
                } else if (result.get() == cancelButton) {
                    event.consume();
                }

            }
        };

        //Delete Items from folder
        EventHandler<ActionEvent> actionDeleteFolderItems = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                //db.deleteToDoItem(id);
                //listTasks(onlyActive, onlyStarred);
                //Warning Dialog Box for delete one task 
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Warning");
                alert.setHeaderText("Would You Like To Delete All Item from Folder?");
                alert.setContentText("Please choose an option.");

                ButtonType yesButton = new ButtonType("Yes");
                ButtonType noButton = new ButtonType("No");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == yesButton) {
                    db.deleteFolderItems(folderFolderId);
                    //dbFolders.deleteFolder(folderFolderId);
                    //refresh list of tasks
                    if (onlyStarred || showDateStart != null) {
                        listTasks(true, onlyStarred, true, folderFolderId);
                    } else {
                        listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
        //removeFolderAll.setGraphic(new ImageView("/todo/delete.png"));

        removeFolderDefault.setOnAction(actionDeleteFolderDefault);

        removeFolderItems.setOnAction(actionDeleteFolderItems);

        ContextMenu folderContextMenu = new ContextMenu(removeFolderItems, removeFolderDefault, removeFolderAll);
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
                    listTasks(true, onlyStarred, true, folderFolderId);
                } else {
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                }

            }
        };

        //Set Active
        EventHandler<ActionEvent> actionSetActive = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeItemStatus(id, 1, activeItems.size());
                listTasks(onlyActive, onlyStarred, true, folderFolderId);
            }
        };

        //Set Starred
        EventHandler<ActionEvent> actionSetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 1);
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true, folderFolderId);
                } else {
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                }
            }
        };

        //UnStar
        EventHandler<ActionEvent> actionResetStarred = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                db.changeStarred(id, 0);
                if (onlyStarred || showDateStart != null) {
                    listTasks(true, onlyStarred, true, folderFolderId);
                } else {
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                }
            }
        };

        //Set Alarm
        EventHandler<ActionEvent> actionSetAlarm = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (alarmHourSpinner.getValue() == null) {
                    //do nothing
                } else if (alarmMinuteSpinner.getValue() == null) {
                    //do nothing
                } else {

                    String spinnerTime = alarmHourSpinner.getValue().format(DateTimeFormatter.ofPattern("HH")) + ":" + alarmMinuteSpinner.getValue().format(DateTimeFormatter.ofPattern("mm"));
                    db.setAlarm(id, spinnerTime);
                    if (popup.isShowing()) {
                        popup.hide();
                    }

                    if (onlyStarred || showDateStart != null) {
                        listTasks(true, onlyStarred, true, folderFolderId);
                    } else {
                        listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
                    listTasks(true, onlyStarred, true, folderFolderId);
                } else {
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                }

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
                        listTasks(true, onlyStarred, true, folderFolderId);
                    } else {
                        listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
                    listTasks(true, onlyStarred, true, folderFolderId);
                } else {
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
                            String body = "Todo%20Item%20'" + activeItemsTable.getItems().get(selectedRowIndex).getDescription().getText() + "'%20is%20due%20on%20" + activeItemsTable.getItems().get(selectedRowIndex).getDate();
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
        //setDoneMenuItem.setGraphic(new ImageView("/todo/done.png"));
        SVGPath setDoneIcon = new SVGPath();
        setDoneIcon.setContent(doSVG); //doSVG
        setDoneMenuItem.setGraphic(setDoneIcon);
        setDoneMenuItem.setAccelerator(cntrlD);

        setActiveItem.setOnAction(actionSetActive);
        //setActiveItem.setGraphic(new ImageView("/todo/undo.png"));
        SVGPath setActiveIcon = new SVGPath();
        setActiveIcon.setContent(backToActiveSVG);
        setActiveItem.setGraphic(setActiveIcon);
        setActiveItem.setAccelerator(cntrlS);

        deleteMenuItem.setOnAction(actionDelete);
        //deleteMenuItem.setGraphic(new ImageView("/todo/delete.png"));
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent(trashBucketSVG);
        deleteMenuItem.setGraphic(deleteIcon);
        deleteMenuItem.setAccelerator(buttonDelete);

        //setDueDate.setOnAction(actionSetDueDate);
        //setDueToday.setOnAction(actionSetDueDateToday);
        //setDueTomorrow.setOnAction(actionSetDueDateTomorrow);
        setDueDatePicker.setGraphic(menuDatePicker);
        setDueDatePicker.setOnAction(actionSetDueDate);
        setDueDate.getItems().addAll(setDueDatePicker);
        //setDueDate.setGraphic(new ImageView("/todo/calendar.png"));
        SVGPath setDateIcon = new SVGPath();
        setDateIcon.setContent(calendarSVG);
        setDueDate.setGraphic(setDateIcon);

        //editItem.setOnAction(actionEdit);
        //editItem.setGraphic(new ImageView("/todo/edit.png"));
        SVGPath editIcon = new SVGPath();
        editIcon.setContent(editSVG);
        editItem.setGraphic(editIcon);
        editItem.setAccelerator(cntrlE);

        starItem.setOnAction(actionSetStarred);
        //starItem.setGraphic(new ImageView("/todo/star.png"));
        SVGPath starIcon = new SVGPath();
        starIcon.setContent(starSVG);
        starItem.setGraphic(starIcon);
        starItem.setAccelerator(cntrlF);

        unstarItem.setOnAction(actionResetStarred);
        //unstarItem.setGraphic(new ImageView("/todo/unstar.png"));
        SVGPath unstarIcon = new SVGPath();
        unstarIcon.setContent(unstarSVG);
        unstarItem.setGraphic(unstarIcon);
        unstarItem.setAccelerator(cntrlU);

        emailItem.setOnAction(actionEmail);
        //emailItem.setGraphic(new ImageView("/todo/email.png"));
        SVGPath emailIcon = new SVGPath();
        emailIcon.setContent(envelopeSVG);
        emailItem.setGraphic(emailIcon);
        emailItem.setAccelerator(cntrlM);

        alarmHourSpinner.setPrefWidth(70.0);
        alarmHourSpinner.setTooltip(new Tooltip("Hours"));
        alarmMinuteSpinner.setPrefWidth(70.0);
        alarmMinuteSpinner.setTooltip(new Tooltip("Minutes"));
        spinnerTimeHBox.setSpacing(5.0);
        setAlarmSpinner.setGraphic(spinnerTimeHBox);
        //setAlarmSpinner.
        //setAlarmSpinner.setOnAction(actionSetAlarm);
        //confirmAlarmSpinner.setGraphic(confirmAlarm);
        //confirmAlarmSpinner.setOnAction(actionSetAlarm);
        confirmAlarm.setOnAction(actionSetAlarm);
        setAlarm.getItems().addAll(setAlarmSpinner/*, confirmAlarmSpinner*/);
        //setAlarm.setGraphic(new ImageView("/todo/alarm-on.png"));
        SVGPath setAlarmIcon = new SVGPath();
        setAlarmIcon.setContent(setAlarmSVG);
        setAlarm.setGraphic(setAlarmIcon);

        removeAlarm.setOnAction(actionRemoveAlarm);
        //removeAlarm.setGraphic(new ImageView("/todo/alarm-off.png"));
        SVGPath removeAlarmIcon = new SVGPath();
        removeAlarmIcon.setContent(removeAlarmSVG);
        removeAlarm.setGraphic(removeAlarmIcon);

        //context menu for TableView
        ContextMenu tableContextMenu = new ContextMenu(editItem, setAlarm, removeAlarm, emailItem, starItem, unstarItem, setDueDate, setDoneMenuItem, setActiveItem, deleteMenuItem);

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
            db.insertToDoItem(descriptionText, 1, localDate.toString(), onlyStarred ? 1 : 0, activeItems.size(), folderFolderId);
            //clear description field, ready for next task
            description.clear();
            descriptionText = null;

            //reset prompt text of textField
            description.setPromptText("Add a new Todo task...");

            //reset date in DatePicker
            datePicker.setValue(null);

            //refresh list of tasks after every task
            if (onlyStarred || showDateStart != null) {
                listTasks(true, onlyStarred, true, folderFolderId);
            } else {
                listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
        //inboxList.setGraphic(new ImageView("/todo/todo1_small.png"));
        SVGPath showAllIcon = new SVGPath();
        showAllIcon.setContent(showAllSVG);
        inboxList.setGraphic(showAllIcon);
        inboxList.setTooltip(new Tooltip("Show all items"));
        menuItems.add(inboxList);

        Label favsList = new Label("Show Favorites \t" + favs);
        //favsList.setGraphic(new ImageView("/todo/star.png"));
        SVGPath showFavsIcon = new SVGPath();
        showFavsIcon.setContent(starSVG);
        favsList.setGraphic(showFavsIcon);
        favsList.setTooltip(new Tooltip("Show all favorite items"));
        menuItems.add(favsList);

        Label todayList = new Label("Show Today \t\t" + today);
        //todayList.setGraphic(new ImageView("/todo/today.png"));
        SVGPath showTodayIcon = new SVGPath();
        showTodayIcon.setContent(showDateSVG);
        todayList.setGraphic(showTodayIcon);
        todayList.setTooltip(new Tooltip("Show all items for today"));
        menuItems.add(todayList);

        Label tomorrowList = new Label("Show Tomorrow \t" + tomorrow);
        //tomorrowList.setGraphic(new ImageView("/todo/tomorrow.jpg"));
        SVGPath showTomorrowIcon = new SVGPath();
        showTomorrowIcon.setContent(showDateSVG);
        tomorrowList.setGraphic(showTomorrowIcon);
        tomorrowList.setTooltip(new Tooltip("Show all items for tomorrow"));
        menuItems.add(tomorrowList);

        Label weekList = new Label("Show Week \t\t" + week);
        //weekList.setGraphic(new ImageView("/todo/week.png"));
        SVGPath showWeekIcon = new SVGPath();
        showWeekIcon.setContent(showDateSVG);
        weekList.setGraphic(showWeekIcon);
        weekList.setTooltip(new Tooltip("Show all items for upcoming week"));
        menuItems.add(weekList);

        Label monthList = new Label("Show Month \t\t" + month);
        //monthList.setGraphic(new ImageView("/todo/month.png"));
        SVGPath showMonthIcon = new SVGPath();
        showMonthIcon.setContent(showDateSVG);
        monthList.setGraphic(showMonthIcon);
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
        folderNames.clear();
        for (int i = 0; i < folderItems.size(); i++) {
            folderNames.add(folderItems.get(i).getFolderName());
        }
        folderList.setItems(folderNames);
        //menuFolders.clear();
        //for (int i = 0; i < folderItems.size(); i++) {
        //    menuFolders.add(new MenuItem(folderItems.get(i).getFolderName()));
        //}

    }

    //private method for listing tasks
    private void listTasks(
            boolean showOnlyActive, //show done items or not
            boolean showOnlyStarred, //show only favorite items or all
            boolean refreshData, //check with DB
            int folderId) //which folder to list (1-All)
    {

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
            allItems.get(i).setStatus(intStatus);
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

                //if default 'All' folder
                //check if only favorites
                if (showOnlyStarred) {
                    //check star
                    if (allItems.get(i).getStar() == 1) {
                        if (folderId == 1 || folderId == allItems.get(i).getFolderId()) {
                            activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                        }
                    }
                    //otherwise, check dates
                } else {
                    //check if showDate is set
                    if (showDateStart == null) {
                        //show all
                        if (folderId == 1 || folderId == allItems.get(i).getFolderId()) {
                            activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                        }
                    } else {
                        //show only for selected date
                        if (showDateStart.toString().compareTo(allItems.get(i).getDate()) <= 0) {
                            if (showDateEnd.toString().compareTo(allItems.get(i).getDate()) >= 0) {
                                if (folderId == 1 || folderId == allItems.get(i).getFolderId()) {
                                    activeItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                                }
                            }
                        }

                    }
                }

                //done items, all, no filter
            } else {
                //check if showDateStart is set
                if (showDateStart == null) {
                    if (folderId == 1 || folderId == allItems.get(i).getFolderId()) {
                        doneItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                    }
                } else //show only for selected date
                if (showDateStart.toString().compareTo(allItems.get(i).getDate()) <= 0) {
                    if (showDateEnd.toString().compareTo(allItems.get(i).getDate()) >= 0) {
                        doneItems.add(new TodoItem(allItems.get(i).getId(), allItems.get(i).getDescription(), allItems.get(i).getDate(), intStatus, allItems.get(i).getStar(), allItems.get(i).getRank(), allItems.get(i).getAlarm(), allItems.get(i).getFolderId()));
                    }
                }
            }

        }
        //populate tblitems (TableView<TodoItem>) to be displayed in App from ObservableList<TodoItem>

        activeItemsTable.setItems(activeItems);

        doneItemsTable.setItems(doneItems);

        doneItemsTable.setVisible(
                !showOnlyActive);

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
        activeItemsTableColDesc.setCellValueFactory(new PropertyValueFactory<TodoItem, Label>("description"));
        //tblColDesc.setStyle("-fx-alignment: LEFT;");

        //activeItemsTableColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        //activeItemsTableColDesc.setOnEditCommit(
        //activeItemsTableColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
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
        /*
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
                            SVGPath activeIcon = new SVGPath();
                            activeIcon.setContent(activeSVG);
                            setGraphic(activeIcon);
                            //setGraphic(new ImageView("/todo/checkbox-empty.png"));
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
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                });
                 
                return cell;
            }
        };

        activeItemsTableColStat.setCellFactory(cellDoneFactory);
         */
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
                                SVGPath starredIcon = new SVGPath();
                                starredIcon.setContent(starSVG);
                                setGraphic(starredIcon);
                                //setGraphic(new ImageView("/todo/star.png"));
                                setTooltip(new Tooltip("Item is marked as favorite"));

                            } //else {
                            //setGraphic(new ImageView("/todo/unstar.png"));
                            //setTooltip(new Tooltip("Press to add to favorites (ctrl+F)"));
                            //}
                        }
                    }
                };
                /*
                cell.setOnMouseClicked(
                        (event) -> {
                            if (cell.getItem() == 1) {
                                cell.setItem(0);
                            } else {
                                cell.setItem(1);
                            }

                            db.changeStarred(activeItemsTable.getItems().get(activeItemsTable.getSelectionModel().getSelectedIndex()).getId(), cell.getItem());
                            if (onlyStarred || showDateStart != null) {
                                listTasks(true, onlyStarred, true, folderFolderId);
                            } else {
                                listTasks(onlyActive, onlyStarred, true, folderFolderId);
                            }
                        }
                );
                 */
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
                        Dragboard dragboard = row.startDragAndDrop(TransferMode.ANY);
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
                            listTasks(true, onlyStarred, true, folderFolderId);
                        } else {
                            listTasks(onlyActive, onlyStarred, true, folderFolderId);
                        }
                        event.consume();
                    }
                });

                row.hoverProperty().addListener((observable) -> {
                    final TodoItem todoItem = row.getItem();

                    if (row.isHover() && todoItem != null) {

                        //prepare set to done icon
                        Label setDoneOption = new Label();
                        SVGPath setDoneOptionIcon = new SVGPath();
                        setDoneOptionIcon.setContent(doSVG);
                        setDoneOption.setGraphic(setDoneOptionIcon);
                        setDoneOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;"); // -fx-border-right: 2px solid black; -fx-border-top: 2px solid grey; -fx-border-left: 2px solid grey;");
                        setDoneOption.setOnMouseClicked((event) -> {
                            //if (cell.getItem() == 0) {
                            //    cell.setItem(1);
                            //} else {
                            todoItem.setStatus(0);
                            //}
                            String musicFile = "applause10.mp3";

                            Media sound = new Media(new File(musicFile).toURI().toString());
                            MediaPlayer mediaPlayer = new MediaPlayer(sound);
                            mediaPlayer.play();
                            db.changeItemStatus(todoItem.getId(), todoItem.getStatus(), activeItems.size());
                            updateRanks();
                            listTasks(onlyActive, onlyStarred, true, folderFolderId);
                        });
                        setDoneOption.setTooltip(new Tooltip("Press to set to Done (ctrl+D)"));
                        setDoneOption.setOnMouseEntered((event) -> {
                            setDoneOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });
                        setDoneOption.setOnMouseExited((event) -> {
                            setDoneOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });

                        //prepare edit icon
                        Label editOption = new Label();
                        SVGPath editOptionIcon = new SVGPath();
                        editOptionIcon.setContent(editSVG);
                        editOption.setGraphic(editOptionIcon);
                        editOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        editOption.setOnMouseClicked((event) -> {
                            editItem.fire();
                        });
                        editOption.setTooltip(new Tooltip("Press to Edit Item (ctrl+E)"));
                        editOption.setOnMouseEntered((event) -> {
                            editOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });
                        editOption.setOnMouseExited((event) -> {
                            editOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });

                        //prepare email icon
                        Label emailOption = new Label();
                        SVGPath emailOptionIcon = new SVGPath();
                        emailOptionIcon.setContent(envelopeSVG);
                        emailOption.setGraphic(emailOptionIcon);
                        emailOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        emailOption.setOnMouseClicked((event) -> {
                            emailItem.fire();
                        });
                        emailOption.setTooltip(new Tooltip("Press to send e-mail with Item (ctrl+M)"));
                        emailOption.setOnMouseEntered((event) -> {
                            emailOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });
                        emailOption.setOnMouseExited((event) -> {
                            emailOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });

                        //prepare set favorite icon
                        Label setFavOption = new Label();
                        SVGPath setFavOptionIcon = new SVGPath();
                        setFavOptionIcon.setContent(starSVG);
                        setFavOption.setGraphic(setFavOptionIcon);
                        setFavOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        setFavOption.setOnMouseClicked((event) -> {
                            if (todoItem.getStar() == 1) {
                                todoItem.setStar(0);
                            } else {
                                todoItem.setStar(1);
                            }

                            db.changeStarred(todoItem.getId(), todoItem.getStar());
                            if (onlyStarred || showDateStart != null) {
                                listTasks(true, onlyStarred, true, folderFolderId);
                            } else {
                                listTasks(onlyActive, onlyStarred, true, folderFolderId);
                            }
                        });
                        if (todoItem.getStar() == 1) {
                            setFavOption.setTooltip(new Tooltip("Press to remove from favorites (ctrl+U)"));

                        } else {
                            setFavOption.setTooltip(new Tooltip("Press to add to favorites (ctrl+F)"));
                        }
                        setFavOption.setOnMouseEntered((event) -> {
                            setFavOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });
                        setFavOption.setOnMouseExited((event) -> {
                            setFavOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });

                        //prepare delete icon
                        Label deleteOption = new Label();
                        SVGPath deleteOptionIcon = new SVGPath();
                        deleteOptionIcon.setContent(trashBucketSVG);
                        deleteOption.setGraphic(deleteOptionIcon);
                        deleteOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        deleteOption.setOnMouseClicked((event) -> {

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
                                db.deleteToDoItem(todoItem.getId());
                                //refresh list of tasks
                                if (onlyStarred || showDateStart != null) {
                                    listTasks(true, onlyStarred, true, folderFolderId);
                                } else {
                                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                                }
                            } else if (result.get() == noButton) {
                                event.consume();
                            } else if (result.get() == cancelButton) {
                                event.consume();
                            }

                        });
                        deleteOption.setTooltip(new Tooltip("Press to Delete (Delete)"));
                        deleteOption.setOnMouseEntered((event) -> {
                            deleteOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });
                        deleteOption.setOnMouseExited((event) -> {
                            deleteOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });

                        //prepare set Due Date icon
                        Label setDueDateOption = new Label();
                        SVGPath setDueDateOptionIcon = new SVGPath();
                        setDueDateOptionIcon.setContent(calendarSVG);
                        setDueDateOption.setGraphic(setDueDateOptionIcon);
                        setDueDateOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        setDueDateOption.setOnMouseClicked((event) -> {
                            hoveredId = todoItem.getId();
                            datePicker.show();
                        });
                        setDueDateOption.setTooltip(new Tooltip("Press to select new Due Date"));
                        setDueDateOption.setOnMouseEntered((event) -> {
                            setDueDateOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });
                        setDueDateOption.setOnMouseExited((event) -> {
                            setDueDateOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });

                        //prepare set Alarm icon
                        Label setAlarmOption = new Label();
                        SVGPath setAlarmOptionIcon = new SVGPath();
                        if (todoItem.getAlarm() != null) {
                            setAlarmOptionIcon.setContent(removeAlarmSVG);
                            setAlarmOption.setTooltip(new Tooltip("Press to Remove Alarm"));
                        } else {
                            if (todoItem.getStatus() == 2) { //status =2, item overdue, alarm cannot be set
                                setAlarmOptionIcon.setOpacity(0.5);
                                setAlarmOptionIcon.setContent(setAlarmSVG);
                                setAlarmOption.setTooltip(new Tooltip("Item is overdue, no alarm can be set"));
                            } else { //status = 1, item pending, not overdue, alarm can be set
                                setAlarmOptionIcon.setContent(setAlarmSVG);
                                setAlarmOption.setTooltip(new Tooltip("Press to Set Alarm"));
                            }

                        }
                        //setAlarmOptionIcon.setFill(Color.RED);
                        setAlarmOption.setGraphic(setAlarmOptionIcon);
                        setAlarmOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        setAlarmOption.setOnMouseClicked((event) -> {
                            if (todoItem.getAlarm() != null) {
                                db.setAlarm(todoItem.getId(), null);
                                if (onlyStarred || showDateStart != null) {
                                    listTasks(true, onlyStarred, true, folderFolderId);
                                } else {
                                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                                }
                            } else {
                                if (todoItem.getStatus() == 2) { //status =2, item overdue, alarm cannot be set
                                    //no action
                                } else { //status = 1, item pending, not overdue, alarm can be set
                                    popup.getContent().clear();
                                    popup.getContent().add(spinnerTimeHBox);
                                    popup.show((Node) event.getSource(), event.getScreenX(), event.getScreenY());
                                }

                            }
                        });
                        setAlarmOption.setOnMouseEntered((event) -> {
                            setAlarmOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });
                        setAlarmOption.setOnMouseExited((event) -> {
                            setAlarmOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                        });

                        //build HBox of all icons
                        HBox editOptionsHBox = new HBox(setDoneOption, editOption, emailOption, setFavOption, deleteOption, setDueDateOption, setAlarmOption);
                        editOptionsHBox.setSpacing(8.0);
                        editOptionsHBox.setAlignment(Pos.CENTER_RIGHT);

                        //set HBox to appear on hover over table's row
                        todoItem.getDescription().setContentDisplay(ContentDisplay.RIGHT);
                        todoItem.getDescription().setGraphic(editOptionsHBox);
                        double gapTextToIcon = activeItemsTableColDesc.getWidth() - todoItem.getDescription().getWidth() - 190.0;
                        if (gapTextToIcon > 0.0) {
                            todoItem.getDescription().setGraphicTextGap(gapTextToIcon);
                        } else {
                            todoItem.getDescription().setGraphicTextGap(0.0);
                        }

                    } else {
                        todoItem.getDescription().setGraphic(null);

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
                                SVGPath alarmOnIcon = new SVGPath();
                                alarmOnIcon.setContent(setAlarmSVG);
                                setGraphic(alarmOnIcon);
                                //setGraphic(new ImageView("/todo/alarm-on.png"));
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
        doneItemsTableColDesc.setCellValueFactory(new PropertyValueFactory<TodoItem, Label>("description"));
        //tblColDesc.setStyle("-fx-alignment: LEFT;");
        //doneItemsTableColDesc.setCellFactory(TextFieldTableCell.forTableColumn());
        /*doneItemsTableColDesc.setOnEditCommit(
                new EventHandler<CellEditEvent<TodoItem, String>>() {
            @Override
            public void handle(CellEditEvent<TodoItem, String> t) {
                ((TodoItem) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setDescription(t.getNewValue());

                db.editDescription(doneItemsTable.getItems().get(doneItemsTable.getSelectionModel().getSelectedIndex()).getId(), doneItemsTable.getItems().get(doneItemsTable.getSelectionModel().getSelectedIndex()).getDescription());
                listTasks(onlyActive, onlyStarred, true, folderFolderId);
            }
        }
        );*/
        //tblColDesc.isEditable();

        //tblColDate = new TableColumn("Date");
        doneItemsTableColDate.setCellValueFactory(new PropertyValueFactory<TodoItem, String>("date"));
        //tblColDate.setStyle("-fx-alignment: CENTER;");

        //tblColStat = new TableColumn("Status");
        doneItemsTableColStat.setCellValueFactory(new PropertyValueFactory<TodoItem, Integer>("status"));
        /*
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
                            SVGPath doneIcon = new SVGPath();
                            doneIcon.setContent(backToActiveSVG);
                            setGraphic(doneIcon);
                            //setGraphic(new ImageView("/todo/checkbox-done.png"));
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
                    listTasks(onlyActive, onlyStarred, true, folderFolderId);
                });

                return cell;
            }
        };
        doneItemsTableColStat.setCellFactory(cellDoneDoneFactory);
         */
        doneItemsTable.setRowFactory(tv -> {
            TableRow<TodoItem> row = new TableRow<>();

            row.hoverProperty().addListener((observable) -> {
                final TodoItem todoItem = row.getItem();

                if (row.isHover() && todoItem != null) {

                    //prepare set to pending icon
                    Label setActiveOption = new Label();
                    SVGPath setActiveOptionIcon = new SVGPath();
                    setActiveOptionIcon.setContent(backToActiveSVG);
                    setActiveOption.setGraphic(setActiveOptionIcon);
                    setActiveOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    setActiveOption.setOnMouseClicked((event) -> {
                        //if (cell.getItem() == 0) {
                        //    cell.setItem(1);
                        //} else {
                        todoItem.setStatus(1);
                        //}

                        db.changeItemStatus(todoItem.getId(), todoItem.getStatus(), activeItems.size());
                        listTasks(onlyActive, onlyStarred, true, folderFolderId);
                    });
                    setActiveOption.setTooltip(new Tooltip("Press to put item to active again (ctrl+A)"));
                    setActiveOption.setOnMouseEntered((event) -> {
                        setActiveOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    });
                    setActiveOption.setOnMouseExited((event) -> {
                        setActiveOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    });

                    //prepare edit icon
                    Label editOption = new Label();
                    SVGPath editOptionIcon = new SVGPath();
                    editOptionIcon.setContent(editSVG);
                    editOption.setGraphic(editOptionIcon);
                    editOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    editOption.setOnMouseClicked((event) -> {
                        editItem.fire();
                    });
                    editOption.setTooltip(new Tooltip("Press to Edit Item (ctrl+E)"));
                    editOption.setOnMouseEntered((event) -> {
                        editOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    });
                    editOption.setOnMouseExited((event) -> {
                        editOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    });                    

                    //prepare delete icon
                    Label deleteOption = new Label();
                    SVGPath deleteOptionIcon = new SVGPath();
                    deleteOptionIcon.setContent(trashBucketSVG);
                    deleteOption.setGraphic(deleteOptionIcon);
                    deleteOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    deleteOption.setOnMouseClicked((event) -> {

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
                            db.deleteToDoItem(todoItem.getId());
                            //refresh list of tasks
                            if (onlyStarred || showDateStart != null) {
                                listTasks(true, onlyStarred, true, folderFolderId);
                            } else {
                                listTasks(onlyActive, onlyStarred, true, folderFolderId);
                            }
                        } else if (result.get() == noButton) {
                            event.consume();
                        } else if (result.get() == cancelButton) {
                            event.consume();
                        }

                    });
                    deleteOption.setTooltip(new Tooltip("Press to Delete (Delete)"));
                    deleteOption.setOnMouseEntered((event) -> {
                        deleteOption.setStyle("-fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    });
                    deleteOption.setOnMouseExited((event) -> {
                        deleteOption.setStyle("-fx-opacity: 0.5; -fx-border-color: grey; -fx-background-color: #ffff99; -fx-border-radius: 4px;");
                    });                     

                    //build HBox of all icons
                    HBox editOptionsHBox = new HBox(setActiveOption, editOption, deleteOption);
                    editOptionsHBox.setSpacing(8.0);
                    editOptionsHBox.setAlignment(Pos.CENTER_RIGHT);

                    //set HBox to appear on hover over table's row
                    todoItem.getDescription().setContentDisplay(ContentDisplay.RIGHT);
                    todoItem.getDescription().setGraphic(editOptionsHBox);
                    double gapTextToIcon = doneItemsTableColDesc.getWidth() - todoItem.getDescription().getWidth() - 75.0;
                    if (gapTextToIcon > 0.0) {
                        todoItem.getDescription().setGraphicTextGap(gapTextToIcon);
                    } else {
                        todoItem.getDescription().setGraphicTextGap(0.0);
                    }

                } else {
                    todoItem.getDescription().setGraphic(null);

                }
            });

            return row;
        });

        //
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
            listTasks(true, onlyStarred, true, folderFolderId);
        } else {
            listTasks(onlyActive, onlyStarred, true, folderFolderId);
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
        leftMenu.setPrefHeight(650.0);
        leftMenu.setPrefWidth(30.0);

        leftMenu.setAlignment(Pos.CENTER);
        leftMenu.setTooltip(new Tooltip("Open side menu"));
        SVGPath leftMenuIcon = new SVGPath();
        leftMenuIcon.setContent(openSVG);
        leftMenu.setGraphic(leftMenuIcon);

        anchorPane.getChildren().add(0, leftMenu);
        //anchorPane.setH
        //description.setLayoutX(75);
        //rightEdit.setGraphic(new ImageView("/todo/edit.png"));
        //editItem.setGraphic(rightEdit);

        folderList.setOnMouseClicked((event) -> {
            int indexLocal = folderList.getSelectionModel().getSelectedIndex();
            folderFolderId = folderItems.get(indexLocal).getFolderId();
            System.out.println("folder list, index=" + indexLocal + "\tFolderId=" + folderFolderId);
            if (folderFolderId == 1) {
                removeFolderAll.setDisable(true);
                removeFolderDefault.setDisable(true);
                removeFolderItems.setDisable(true);
            } else {
                removeFolderAll.setDisable(false);
                removeFolderDefault.setDisable(false);
                removeFolderItems.setDisable(false);
            }
            listTasks(onlyActive, onlyStarred, false, folderFolderId);

            /*switch (indexLocal) {
                case -1:
                    break;
                case 0:
                    break;
                default:
                    break;

            }*/
        });

        folderList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                }
            };
            cell.setOnDragOver(e -> {
                Dragboard dragboard = e.getDragboard();
                if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                    e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    e.consume();

                }
            });

            cell.setOnDragDropped(e -> {
                Dragboard dragboard = e.getDragboard();
                if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) dragboard.getContent(SERIALIZED_MIME_TYPE);
                    int dropIndex = cell.getIndex();
                    System.out.println("Dragged item index=" + draggedIndex + "\t dropped folder index=" + dropIndex);
                    //update item's folder in DB
                    db.moveItem(folderItems.get(dropIndex).getFolderId(), activeItems.get(draggedIndex).getId());

                    e.setDropCompleted(true);

                    //call updateRanks(drag,drop)
                    //updateRanks();
                    if (onlyStarred || showDateStart != null) {
                        listTasks(true, onlyStarred, true, folderFolderId);
                    } else {
                        listTasks(onlyActive, onlyStarred, true, folderFolderId);
                    }
                    e.consume();
                }

            });

            return cell;
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
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - INTRACOM TELECOM - All");
                    //tableLabel.setText("Todo Items");
                    description.setPromptText("Add new todo task...");
                    //set visibility of show done button
                    buttonShowOptions.setVisible(true);
                    if (onlyActive) {
                        buttonDeleteDone.setVisible(false);
                    } else {
                        buttonDeleteDone.setVisible(true);
                    }
                    listTasks(onlyActive, onlyStarred, false, folderFolderId);
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
                    listTasks(true, onlyStarred, false, folderFolderId);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - INTRACOM TELECOM - Favorites");
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
                    listTasks(true, onlyStarred, false, folderFolderId);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - INTRACOM TELECOM - Today");
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
                    listTasks(true, onlyStarred, false, folderFolderId);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - INTRACOM TELECOM - Tomorrow");
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
                    listTasks(true, onlyStarred, false, folderFolderId);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - INTRACOM TELECOM - Upcoming Week");
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
                    listTasks(true, onlyStarred, false, folderFolderId);
                    //index = -1;
                    ((Stage) ((ListView) event.getSource()).getScene().getWindow()).setTitle("Todo Items - INTRACOM TELECOM - Upcoming Month");
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

        listTasks(onlyActive, onlyStarred, true, folderFolderId);
        String currentDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            //final Calendar cal = Calendar.getInstance();
            LocalTime lt = LocalTime.now();
            String currentTime = lt.format(DateTimeFormatter.ofPattern("HH:mm"));
            clock.setText(currentTime);
            //

            if (anyAlarm) {
                for (int i = 0; i < allItems.size(); i++) {
                    if (allItems.get(i).getAlarm() != null && allItems.get(i).getDate().equals(currentDay) && allItems.get(i).getAlarm().equals(currentTime)) {
                        //play alarm and continue scanning
                        anyAlarm = false; //will be updated in listTask anyway
                        executePlayAlarm(allItems.get(i).getId(), allItems.get(i).getDescription().getText());

                    }
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);

        timeline.play();

        SVGPath clockIcon = new SVGPath();
        clockIcon.setContent(clockSVG);
        clock.setGraphic(clockIcon);
        clock.setTooltip(new Tooltip("Current Local Time")); //+ currentTime 
    }

}
