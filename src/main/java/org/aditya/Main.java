package org.aditya;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    GridPane grid = new GridPane();

    int width = 10;
    int height = 10;

    List<Button> buttons = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("game-of-life.fxml"));
        stage.setTitle("Conway's Game of Life");

        addButtons();

        stage.setScene(new Scene(grid, 500, 500));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void addButtons(){
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Button button = new Button("x: " + x + ", y: " + y);
                button.setOnAction(actionEvent -> {
                    System.out.println(button.getText());
                });
                grid.add(button, x, y);
            }
        }
    }
}
