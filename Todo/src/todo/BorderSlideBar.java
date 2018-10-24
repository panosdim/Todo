/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * Animates a node on and off screen to the top, right, bottom or left side.
 */
public class BorderSlideBar extends VBox {

    //private final String CSS = "/" + this.getClass().getSimpleName() + ".css";
    private double expandedSize;
    private Pos flapbarLocation;
    private final String openSVG = "M20,0v100h1v-100h1v100h1v-100 M20,150v700h1v-700h1v700h1v-700 M15,105l10,10l-10,10 M15,125l10,10l-10,10";
    private final String closeSVG = "M20,0v100h1v-100h1v100h1v-100 M20,150v700h1v-700h1v700h1v-700 M25,105l-10,10l10,10 M25,125l-10,10l10,10";
    private SVGPath leftMenuIcon = new SVGPath();

    /**
     * Creates a side bar panel in a BorderPane, containing an horizontal
     * alignment of the given nodes.
     *
     * <pre>
     * <code>
     *  Example:
     *
     *  BorderSlideBar topFlapBar = new BorderSlideBar(
     *                  100, button, Pos.TOP_LEFT, new contentController());
     *  mainBorderPane.setTop(topFlapBar);
     * </code>
     * </pre>
     *
     * @param expandedSize The size of the panel.
     * @param controlLabel
     * @param controlButton The button responsible to open/close slide bar.
     * @param location The location of the panel (TOP_LEFT, BOTTOM_LEFT,
     * BASELINE_RIGHT, BASELINE_LEFT).
     * @param nodes Nodes inside the panel.
     */
    public BorderSlideBar(double expandedSize,
            Label controlLabel, Pos location, Node... nodes) {

        //getStyleClass().add("sidebar");
        //getStylesheets().add(CSS);        
        setExpandedSize(expandedSize);
        setVisible(false);

        // Set location
        if (location == null) {
            flapbarLocation = Pos.BASELINE_LEFT; // Set default location
        } else {
            flapbarLocation = location;
        }

        initPosition();
        setSpacing(15);

        // Add nodes in the vbox
        getChildren().addAll(nodes);

        //controlLabel.setGraphic(new ImageView("/todo/left_menu.png"));
        controlLabel.setOnMouseClicked((event) -> {

            // Create an animation to hide the panel.
            final Animation hidePanel = new Transition() {
                {
                    setCycleDuration(Duration.millis(250));
                }

                @Override
                protected void interpolate(double frac) {
                    final double size = getExpandedSize() * (1.0 - frac);
                    translateByPos(size);
                }
            };

            hidePanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    setVisible(false);
                    //controlLabel.setText(">>");
                    leftMenuIcon.setContent(openSVG);
                    controlLabel.setGraphic(leftMenuIcon);
                    controlLabel.setTooltip(new Tooltip("Open side menu"));
                }
            });

            // Create an animation to show the panel.
            final Animation showPanel = new Transition() {
                {
                    setCycleDuration(Duration.millis(500));
                }

                @Override
                protected void interpolate(double frac) {
                    final double size = getExpandedSize() * frac;
                    translateByPos(size);
                }
            };

            showPanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    //controlLabel.setText("<<");
                    leftMenuIcon.setContent(closeSVG);
                    controlLabel.setGraphic(leftMenuIcon);
                    controlLabel.setTooltip(new Tooltip("Close side menu"));
                }
            });

            if (showPanel.statusProperty().get() == Animation.Status.STOPPED && hidePanel.statusProperty().get() == Animation.Status.STOPPED) {

                if (isVisible()) {
                    hidePanel.play();

                } else {
                    setVisible(true);
                    showPanel.play();
                    //controlLabel.setVisible(false);

                }
            }

        });
        /*
        setOnMouseExited((event) -> {

            // Create an animation to hide the panel.
            final Animation hidePanel = new Transition() {
                {
                    setCycleDuration(Duration.millis(500));
                }

                @Override
                protected void interpolate(double frac) {
                    final double size = getExpandedSize() * (1.0 - frac);
                    translateByPos(size);
                }
            };

            hidePanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    setVisible(false);
                }
            });
            /*
                // Create an animation to show the panel.
                final Animation showPanel = new Transition() {
                    {
                        setCycleDuration(Duration.millis(250));
                    }

                    @Override
                    protected void interpolate(double frac) {
                        final double size = getExpandedSize() * frac;
                        translateByPos(size);
                    }
                };

                showPanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                    }
                });
             
            if (/*showPanel.statusProperty().get() == Animation.Status.STOPPED
                        &&hidePanel.statusProperty().get() == Animation.Status.STOPPED) {

                if (isVisible()) {
                    hidePanel.play();
                    controlLabel.setVisible(true);

                }
                else {
                        setVisible(true);
                        showPanel.play();
                    }
                             }

        });
         */
    }

    /**
     * Initialize position orientation.
     */
    private void initPosition() {
        switch (flapbarLocation) {
            case TOP_LEFT:
                setPrefHeight(0);
                setMinHeight(0);
                break;
            case BOTTOM_LEFT:
                setPrefHeight(0);
                setMinHeight(0);
                break;
            case BASELINE_RIGHT:
                setPrefWidth(0);
                setMinWidth(0);
                break;
            case BASELINE_LEFT:
                setPrefWidth(0);
                setMinWidth(0);
                break;
        }
    }

    /**
     * Translate the VBox according to location Pos.
     *
     * @param size
     */
    private void translateByPos(double size) {
        switch (flapbarLocation) {
            case TOP_LEFT:
                setPrefHeight(size);
                setTranslateY(-getExpandedSize() + size);
                break;
            case BOTTOM_LEFT:
                setPrefHeight(size);
                break;
            case BASELINE_RIGHT:
                setPrefWidth(size);
                break;
            case BASELINE_LEFT:
                setPrefWidth(size);
                break;
        }
    }

    /**
     * @return the expandedSize
     */
    public double getExpandedSize() {
        return expandedSize;
    }

    /**
     * @param expandedSize the expandedSize to set
     */
    public void setExpandedSize(double expandedSize) {
        this.expandedSize = expandedSize;
    }

}
