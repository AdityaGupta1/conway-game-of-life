import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    private GridPane grid = new GridPane();

    private final int gridWidth = 64;
    private final int gridHeight = 64;
    private final int buttonPadding = 1;
    private final int menuHeight = 48;
    private int buttonWidth = 8;
    private int buttonHeight = 8;

    private boolean[][] cells = new boolean[gridHeight][gridWidth];

    private boolean paused = true;

    private int generation = 0;
    private Label generationLabel = new Label("0");

    private Button pause = new Button("Resume (P)");

    private final int maxSpeed = 30;
    private int tps = maxSpeed;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Conway's Game of Life");

        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));

        stage.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox();
        root.getStyleClass().add("background");

        Button clearGrid = new Button("Clear Grid (C)");
        clearGrid.setOnAction(event -> {
            clearGrid();
            resetGenerations();
            pause();
        });

        Button randomGrid = new Button("Random Grid (G)");
        randomGrid.setOnAction(event -> {
            randomGrid();
            resetGenerations();
            pause();
        });

        pause.setOnAction(event -> togglePause());

        Button nextGeneration = new Button("Next (N)");
        nextGeneration.setOnAction(event -> {
            updateButtons();
            pause();
        });

        Label speedLabel = new Label("Max FPS:");
        speedLabel.getStyleClass().add("speed-label");

        TextField speedTextField = new TextField(String.valueOf(tps));
        speedTextField.setPromptText(String.valueOf(maxSpeed));
        speedTextField.setMaxWidth(50);
        speedTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            int speed = maxSpeed;

            if (newValue.equals("")) {
                speed = maxSpeed;
                tps = speed;
                return;
            }

            try {
                speed = Integer.parseInt(newValue);
            } catch (NumberFormatException exception) {
                speedTextField.setText(newValue.replaceAll("[^0-9]", ""));
            }

            if (speed > maxSpeed) {
                speed = maxSpeed;
                speedTextField.setText(String.valueOf(maxSpeed));
            }
            if (speed < 1) {
                speed = 1;
                speedTextField.setText("1");
            }

            tps = speed;
        });

        HBox menu = new HBox(5, clearGrid, randomGrid, pause, nextGeneration, speedLabel, speedTextField);
        menu.setMinHeight(menuHeight);
        menu.setMaxHeight(menuHeight);
        menu.setAlignment(Pos.CENTER_LEFT);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10, 1);
        generationLabel.setMinWidth(Button.USE_PREF_SIZE);
        generationLabel.getStyleClass().add("generation-label");
        Button resetGenerationButton = new Button("Reset (R)");
        resetGenerationButton.setMinWidth(Button.USE_PREF_SIZE);
        resetGenerationButton.setOnAction(event -> {
            generation = 0;
            generationLabel.setText(String.valueOf(generation));
            pause();
        });

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(event -> exit());

        menu.getChildren().addAll(spacer, generationLabel, resetGenerationButton, exitButton);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double availableSpace = visualBounds.getMaxY() - menuHeight;
        availableSpace -= 2 * gridHeight * buttonPadding;
        // background
        availableSpace -= 6;
        buttonHeight = buttonWidth = ((int) Math.floor(availableSpace / (double) gridHeight));

        addButtons();

        root.getChildren().addAll(grid, menu);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

        scene.setOnKeyPressed(event -> {
            switch(event.getCode()) {
                case P:
                    pause.fire();
                    break;
                case N:
                    nextGeneration.fire();
                    break;
                case R:
                    resetGenerationButton.fire();
                    break;
                case C:
                    clearGrid.fire();
                    break;
                case G:
                    randomGrid.fire();
                    break;
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

        boolean[][] newCells = new boolean[gridHeight][gridWidth];

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

    private void resetGenerations() {
        generation = 0;
        generationLabel.setText("0");
    }

    private int getLiveNeighbors(int x, int y) {
        int neighbors = 0;

        String type = "normal";

        if (x == 0 && y == 0) {
            type = "top left";
        } else if (x == gridWidth - 1 && y == 0) {
            type = "top right";
        } else if (x == 0 && y == gridHeight - 1) {
            type = "bottom left";
        } else if (x == gridWidth - 1 && y == gridHeight - 1) {
            type = "bottom right";
        } else if (x == 0) {
            type = "left";
        } else if (y == 0) {
            type = "top";
        } else if (x == gridWidth - 1) {
            type = "right";
        } else if (y == gridHeight - 1) {
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
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
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

    private void randomGrid() {
        clearGrid();

        for (Node node : grid.getChildren()) {
            if (!(node instanceof Button)) {
                return;
            }

            if (Math.random() > 0.5) {
                toggleState((Button) node);
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

    private void exit() {
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
