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

public class Main extends Application {
    private GridPane grid = new GridPane();

    private int width = 64;
    private int height = 64;
    private int buttonWidth = 16;
    private int buttonHeight = 16;
    private int buttonPadding = 1;

    private int tps = 20;

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

    private boolean isCellAlive(Node node) {
        // if shit goes down add a `(Button) node` cast
        return node.getStyleClass().get(0).equals("alive");
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
        System.out.println("Starting x: " + x + ", y: " + y);

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

        System.out.println("Type of x: " + x + ", y: " + y + " = " + type);

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

        System.out.println("Finished x: " + x + ", y: " + y);

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

    public static void main(String[] args) {
        launch(args);
    }
}
