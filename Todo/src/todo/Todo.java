/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.StageStyle;

/**
 *
 * @author ckok
 */
public class Todo extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        Scene scene = new Scene(root);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("todo1.png")));
        stage.setTitle("ToDo v0.4");
        //set background photo
        scene.getStylesheets().addAll(this.getClass().getResource("sceneCSS.css").toExternalForm());
        //stage.isResizable( );
        stage.setScene(scene);
        //stage.initStyle(StageStyle.UNIFIED);
        //stage.setIconified(true);
        //stage.setOpacity(0);
        //stage.setAlwaysOnTop(true);
        
        
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
