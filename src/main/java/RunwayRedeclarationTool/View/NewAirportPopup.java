package RunwayRedeclarationTool.View;

import RunwayRedeclarationTool.Logger.Logger;
import RunwayRedeclarationTool.Models.Airport;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class NewAirportPopup {

    static Airport airport = null;

    public Airport display() {
        final Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Add a new Airport");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));

        Scene scene = new Scene(grid, 700, 400);
        window.setScene(scene);

        Label name = new Label("Airport name:");
        grid.add(name, 0, 0);

        final TextField nameField = new TextField();
        grid.add(nameField, 1,0);

        Label id = new Label("Airport code:");
        grid.add(id, 0, 1);

        final TextField idField = new TextField();
        grid.add(idField, 1, 1);

        Button button = new Button("Submit");

        final Label errorLabel = new Label("You need to enter a value!");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        grid.add(errorLabel,0,2);

        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if(!nameField.getText().replaceAll("\\s+", "").equalsIgnoreCase("") && !idField.getText().replaceAll("\\s+", "").equalsIgnoreCase("")){
                    airport = new Airport(nameField.getText(), idField.getText().toUpperCase());
                    window.close();
                } else {
                    Logger.Log("Empty values entered. ");
                    errorLabel.setVisible(true);
                }
            }
        });
        grid.add(button, 0, 4);

        window.setScene(scene);
        window.showAndWait();
        return airport;
    }
}
