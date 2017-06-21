package org.aditya;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main extends Application {
    private GridPane grid = new GridPane();

    private final int width = 64;
    private final int height = 64;
    private final int buttonWidth = 9;
    private final int buttonHeight = 9;
    private final int buttonPadding = 1;

    private int tps = 60;

    private boolean[][] cells = new boolean[height][width];

    private boolean paused = true;

    private int generation = 0;
    private Label generationLabel = new Label("0");

    private Button pause = new Button("Resume (P)");

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Conway's Game of Life");

        VBox root = new VBox();

        Button clearGrid = new Button("Clear Grid");
        clearGrid.setOnAction(event -> {
            clearGrid();
            pause();
        });

        pause.setOnAction(event -> togglePause());

        Button nextGeneration = new Button("Next (N)");
        nextGeneration.setOnAction(event -> {
            updateButtons();
            pause();
        });

        Label speedLabel = new Label("FPS:");
        speedLabel.getStyleClass().add("speed-label");

        TextField speedTextField = new TextField();
        speedTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            int speed = 60;

            try {
                speed = Integer.parseInt(newValue);
            } catch (NumberFormatException exception) {
                speedTextField.setText(newValue.replaceAll("[^0-9]", ""));
            }

            if (speed > 60) {
                speed = 60;
                speedTextField.setText("60");
            }
            if (speed < 1) {
                speed = 1;
                speedTextField.setText("1");
            }

            tps = speed;
        });

        HBox menu = new HBox(5, clearGrid, pause, nextGeneration, speedLabel, speedTextField);
        menu.setAlignment(Pos.CENTER_LEFT);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10, 1);
        generationLabel.setMinWidth(Button.USE_PREF_SIZE);
        generationLabel.getStyleClass().add("generation-label");
        Button resetGenerationLabel = new Button("Reset");
        resetGenerationLabel.setMinWidth(Button.USE_PREF_SIZE);
        resetGenerationLabel.setOnAction(event -> {
            generation = 0;
            generationLabel.setText(String.valueOf(generation));
            pause();
        });

        menu.getChildren().addAll(spacer, generationLabel, resetGenerationLabel);

        addButtons();

        root.getChildren().addAll(grid, menu);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.P) {
                pause.fire();
            }

            if (event.getCode() == KeyCode.N) {
                nextGeneration.fire();
            }
        });

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        startLoop();
    }

    private boolean isCellAlive(Node node) {
        return (node.getStyleClass().get(0).equals("alive") || node.getStyleClass().get(0).equals("structure"));
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

                updateButtons();
            }
        };
        animator.start();
    }

    private void updateButtons() {
        // read values from buttons; creates initial cells array
        for (Node node : grid.getChildren()) {
            if (!(node instanceof Button)) {
                continue;
            }

            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);

            cells[y][x] = isCellAlive(node);
        }

        boolean[][] newCells = new boolean[height][width];

        // apply rules to each button and populate new cells array
        for (Node node : grid.getChildren()) {
            if (!(node instanceof Button)) {
                continue;
            }

            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);

            if (isCellAlive(node)) {
                switch (getLiveNeighbors(x, y)) {
                    case 2:
                    case 3:
                        newCells[y][x] = true;
                        break;
                    default:
                        newCells[y][x] = false;
                        break;
                }
            } else {
                switch (getLiveNeighbors(x, y)) {
                    case 3:
                        newCells[y][x] = true;
                        break;
                    default:
                        newCells[y][x] = false;
                        break;
                }
            }
        }

        // update button values
        cells = newCells;
        updateCellsArray();

        // update generation number
        generation++;
        generationLabel.setText(String.valueOf(generation));
    }

    private void updateCellsArray() {
        for (Node node : grid.getChildren()) {
            if (!(node instanceof Button)) {
                continue;
            }

            int x = GridPane.getColumnIndex(node);
            int y = GridPane.getRowIndex(node);

            Button button = (Button) node;

            if (isCellAlive(button) != cells[y][x]) {
                toggleState(button);
            }
        }
    }

    private int getLiveNeighbors(int x, int y) {
        int neighbors = 0;

        String type = "normal";

        if (x == 0 && y == 0) {
            type = "top left";
        } else if (x == width - 1 && y == 0) {
            type = "top right";
        } else if (x == 0 && y == height - 1) {
            type = "bottom left";
        } else if (x == width - 1 && y == height - 1) {
            type = "bottom right";
        } else if (x == 0) {
            type = "left";
        } else if (y == 0) {
            type = "top";
        } else if (x == width - 1) {
            type = "right";
        } else if (y == height - 1) {
            type = "bottom";
        }

        if (!type.equals("top left") && !type.equals("top") && !type.equals("top right") && !type.equals("left") && !type.equals("bottom left")) {
            neighbors += cells[y - 1][x - 1] ? 1 : 0;
        }
        if (!type.equals("top right") && !type.equals("top") && !type.equals("top left") && !type.equals("right") && !type.equals("bottom right")) {
            neighbors += cells[y - 1][x + 1] ? 1 : 0;
        }
        if (!type.equals("bottom left") && !type.equals("bottom") && !type.equals("bottom right") && !type.equals("left") && !type.equals("top left")) {
            neighbors += cells[y + 1][x - 1] ? 1 : 0;
        }
        if (!type.equals("bottom right") && !type.equals("bottom") && !type.equals("bottom left") && !type.equals("right") && !type.equals("top right")) {
            neighbors += cells[y + 1][x + 1] ? 1 : 0;
        }
        if (!type.equals("top") && !type.equals("top left") && !type.equals("top right")) {
            neighbors += cells[y - 1][x] ? 1 : 0;
        }
        if (!type.equals("bottom") && !type.equals("bottom left") && !type.equals("bottom right")) {
            neighbors += cells[y + 1][x] ? 1 : 0;
        }
        if (!type.equals("left") && !type.equals("top left") && !type.equals("bottom left")) {
            neighbors += cells[y][x - 1] ? 1 : 0;
        }
        if (!type.equals("right") && !type.equals("top right") && !type.equals("bottom right")) {
            neighbors += cells[y][x + 1] ? 1 : 0;
        }

        return neighbors;
    }

    private void addButtons() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Button button = new Button();
                button.setOnAction(event -> {
                    toggleState(button);
                });
                button.setPrefSize(buttonWidth, buttonHeight);
                button.setMinHeight(button.getPrefHeight());
                button.setMaxHeight(button.getPrefHeight());
                button.setMinWidth(button.getPrefWidth());
                button.setMaxWidth(button.getPrefWidth());
                GridPane.setMargin(button, new Insets(buttonPadding, buttonPadding, buttonPadding, buttonPadding));
                button.getStyleClass().clear();
                button.getStyleClass().add("dead");
                grid.add(button, x, y);
            }
        }
    }

    private void toggleState(Button button) {
        if (!isCellAlive(button)) {
            button.getStyleClass().clear();
            button.getStyleClass().add("alive");
        } else {
            button.getStyleClass().clear();
            button.getStyleClass().add("dead");
        }
    }

    private void clearGrid() {
        for (Node node : grid.getChildren()) {
            if (!(node instanceof Button)) {
                continue;
            }

            Button button = (Button) node;

            if (isCellAlive(button)) {
                toggleState(button);
            }
        }
    }

    private void togglePause() {
        paused = !paused;
        pause.setText(paused ? "Resume (P)" : "Pause (P)");
    }

    private void pause() {
        paused = true;
        pause.setText("Resume (P)");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
