package org.aditya;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private GridPane grid = new GridPane();

    private int width = 20;
    private int height = 20;
    private int buttonWidth = 25;
    private int buttonHeight = 25;
    private int buttonPadding = 1;

    List<Button> buttons = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Conway's Game of Life");

        addButtons();

        Scene scene = new Scene(grid);
        scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void addButtons() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Button button = new Button();
                button.setOnAction(actionEvent -> {
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
}
