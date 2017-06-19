package org.aditya;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Arrays;

public class Main extends Application {
    private GridPane grid = new GridPane();

    private int width = 64;
    private int height = 64;
    private int buttonWidth = 16;
    private int buttonHeight = 16;
    private int buttonPadding = 1;

    private int tps = 60;

    private boolean[][] cells = new boolean[height][width];

    private boolean paused = true;
    private boolean gameRunning = true;


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Conway's Game of Life");

        addButtons();

        Scene scene = new Scene(grid);
        scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.P) {
                paused = !paused;
                System.out.println("Paused: " + paused);
            }
        });

        int windowWidth = (width * buttonWidth) + (2 * width * buttonPadding);
        int windowHeight = (height * buttonHeight) + (2 * height * buttonPadding);
        stage.setMinWidth(windowWidth);
        stage.setMaxWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.setMaxHeight(windowHeight);

        stage.setScene(scene);
        stage.show();

        startLoop();
    }

    private long nextUpdate = 0;

    private void startLoop() {
        AnimationTimer animator = new AnimationTimer() {
            public void handle(long now) {
                if (nextUpdate == 0) {
                    nextUpdate = now;
                }

                if (now >= nextUpdate) {
                    nextUpdate += 1_000_000_000 / tps;
                } else {
                    return;
                }

                if (paused) {
                    return;
                }

                updateCellsArray();
            }
        };
        animator.start();
    }

    private void updateCellsArray() {
        for (Node node : grid.getChildren()) {
            if (node instanceof Button) {
                cells[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] = ((Button) node).getStyleClass().get(0).equals("alive");
            }
        }\
    }

    private void addButtons() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Button button = new Button();
                button.setOnAction(event -> {
                    toggleState(button);
                });
                button.setPrefSize(buttonWidth, buttonHeight);
                GridPane.setMargin(button, new Insets(buttonPadding, buttonPadding, buttonPadding, buttonPadding));
                button.getStyleClass().clear();
                button.getStyleClass().add("dead");
                grid.add(button, x, y);
            }
        }
    }

    private void toggleState(Button button) {
        if (button.getStyleClass().get(0).equals("dead")) {
            button.getStyleClass().clear();
            button.getStyleClass().add("alive");
        } else {
            button.getStyleClass().clear();
            button.getStyleClass().add("dead");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
