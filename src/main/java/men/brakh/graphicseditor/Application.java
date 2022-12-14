package men.brakh.graphicseditor;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import men.brakh.graphicseditor.config.Configuration;
import men.brakh.graphicseditor.controller.Controller;

import java.util.Objects;

public class Application extends javafx.application.Application {

    private Configuration config = Configuration.getInstance();


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("view1.fxml")));
        primaryStage.setTitle("Graphics editor");

        int height = config.getMinFrameHeight();
        int width = config.getMinFrameWidth();

        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.setMinHeight(height);
        primaryStage.setMinWidth(width);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}