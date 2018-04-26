package RunwayRedeclarationTool.Controllers;

import RunwayRedeclarationTool.Exceptions.AttributeNotAssignedException;
import RunwayRedeclarationTool.Exceptions.NoRedeclarationNeededException;
import RunwayRedeclarationTool.Logger.Logger;
import RunwayRedeclarationTool.Models.*;
import RunwayRedeclarationTool.Models.config.Configuration;
import RunwayRedeclarationTool.Models.db.DB_controller;
import RunwayRedeclarationTool.Models.xml.XML_Export;
import RunwayRedeclarationTool.Models.xml.XML_File_Loader;
import RunwayRedeclarationTool.View.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    private final DB_controller controller;
    private final Configuration config;
    @FXML
    private FlowPane topDownViewContainer, sideOnViewContainer;
    @FXML
    private ComboBox<Airport> airportComboBox;
    @FXML
    private ComboBox<Runway> runwayComboBox;
    @FXML
    private ComboBox<VirtualRunway> virtualRunwayComboBox;
    @FXML
    private ComboBox<Obstacle> obstructionComboBox;
    @FXML
    private ComboBox<RunwaySide> runwaySideComboBox;
    @FXML
    private TextFlow declaredDistances, calculationsBreakdown;
    @FXML
    private Button calculateButton, clearAllButton;
    @FXML
    private TextField distanceFromTHRLeft, distanceFromTHRRight, distanceFromCL, obstacleWidth;
    private TopDownView topDownView;
    private SideOnView sideOnView;
    private ObstaclePosition obstaclePosition;

    public MainWindowController(Configuration config, DB_controller controller) {
        this.config = config;
        this.controller = controller;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        runwaySideComboBox.getItems().addAll(RunwaySide.LEFT, RunwaySide.RIGHT, RunwaySide.CENTER);

        distanceFromTHRLeft.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    distanceFromTHRLeft.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        distanceFromTHRRight.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    distanceFromTHRRight.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        distanceFromCL.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    distanceFromCL.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        obstacleWidth.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    obstacleWidth.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        if (controller.get_airports().length > 0) {
            Logger.Log("Loaded airports: c" + controller.get_airports().toString());
            refresh_airports();
            drawRunway();
        }
    }


    /**
     * Add top-down and side-on views to the main window.
     */
    public void drawRunway() {
        topDownViewContainer.getChildren().clear();
        sideOnViewContainer.getChildren().clear();

        Runway runway = runwayComboBox.getValue();
        VirtualRunway virtualRunway = virtualRunwayComboBox.getValue();

        try {
            if (runway == null) {
                runway = runwayComboBox.getItems().get(0);
                // Default to the left virtual runway
                virtualRunway = runway.leftRunway;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // There is no runway yet
            return;
        }

        topDownView = new TopDownView(virtualRunway, obstaclePosition);
        topDownView.widthProperty().bind(topDownViewContainer.widthProperty());
        topDownView.heightProperty().bind(topDownViewContainer.heightProperty());
        topDownViewContainer.getChildren().add(topDownView);

        sideOnView = new SideOnView(virtualRunway, obstaclePosition);
        sideOnView.widthProperty().bind(sideOnViewContainer.widthProperty());
        sideOnView.heightProperty().bind(sideOnViewContainer.heightProperty());
        sideOnViewContainer.getChildren().add(sideOnView);

        topDownView.drawObstacle();
        sideOnView.drawObstacle();
    }

    public void updateDeclaredDistancesTextfield(){
        try {
            Runway runway = runwayComboBox.getValue();
            declaredDistances.getChildren().clear();
            declaredDistances.getChildren().add(new Text("Runway " + runway.leftRunway.getDesignator() + ":\nTORA: " + runway.leftRunway.getOrigParams().getTORA() + "m\nTODA: " + runway.leftRunway.getOrigParams().getTODA() + "m\nASDA: " + runway.leftRunway.getOrigParams().getASDA() + "m\nLDA:  " + runway.leftRunway.getOrigParams().getLDA() + "m\n\n"));
            declaredDistances.getChildren().add(new Text("Runway " + runway.rightRunway.getDesignator() + ":\nTORA: " + runway.rightRunway.getOrigParams().getTORA() + "m\nTODA: " + runway.rightRunway.getOrigParams().getTODA() + "m\nASDA: " + runway.rightRunway.getOrigParams().getASDA() + "m\nLDA:  " + runway.rightRunway.getOrigParams().getLDA() + "m\n"));
        } catch (NullPointerException e){
            // The runway is not set.
        }
}

    // TODO - Clear the text fields

    /**
     * Clear the current object and additional markings.
     */
    @FXML
    public void clearFields() {
        this.obstaclePosition = null;
        drawRunway();
    }

    /**
     * Export the current top-down view as an image.
     */
    @FXML
    public void handleTopDownImageExport(){
        Logger.Log("Running Image Export for top down view.");
        new ImageExport().export(topDownView);
    }

    /**
     * Export the current side-on view as an image.
     */
    @FXML
    public void handleSideOnImageExport(){
        Logger.Log("Running Image Export for side on view.");
        new ImageExport().export(sideOnView);
    }

    /**
     * Export all runway and obstacle information as text.
     */
    @FXML
    public void handleExportAsText(){
        String outputString = "";

        for(Node n : declaredDistances.getChildren()){
            if(n instanceof Text){
                outputString += ((Text) n).getText();
            }
        }

        for(Node n : calculationsBreakdown.getChildren()){
            if( n instanceof Text){
                outputString += ((Text) n).getText();
            }
        }

        outputString +=  "\n\nCurrent Obstacle: \n" + obstructionComboBox.getSelectionModel().getSelectedItem();
        outputString += "\n" + obstaclePosition;
        ExportToTextWindow.display(outputString);
    }

    /**
     * Recalculate and display the declared distances given the obstacle on the runway.
     */
    @FXML
    public void recalculateDistances() {

        Logger.Log("Attempting to recalculate distances.");
        Calculator calculator = Calculator.getInstance();
        try {

            // TODO - When calculate is clicked with empty values, nothing is shown to the user. Catch NumberFormatExcpetion and throw a popup
            // Can copy code from NewRunwayWindow probs.
            Runway runway = runwayComboBox.getValue();
            Obstacle obstacle = obstructionComboBox.getValue();

            // TODO - Don't need this.
            int distFromCL;
            if (runwaySideComboBox.getValue() == RunwaySide.CENTER) {
                distFromCL = 0;
            } else {
                distFromCL = Integer.parseInt(distanceFromCL.getText());
            }
            obstaclePosition = new ObstaclePosition(obstacle, Integer.parseInt(distanceFromTHRLeft.getText()), Integer.parseInt(distanceFromTHRRight.getText()), Integer.parseInt(obstacleWidth.getText()), distFromCL, runwaySideComboBox.getValue());

            declaredDistances.getChildren().clear();
            declaredDistances.getChildren().add(new Text("Original distances:\n"));
            declaredDistances.getChildren().add(new Text("Runway " + runway.leftRunway.getDesignator() + ":\nTORA: " + runway.leftRunway.getOrigParams().getTORA() + "m\nTODA: " + runway.leftRunway.getOrigParams().getTODA() + "m\nASDA: " + runway.leftRunway.getOrigParams().getASDA() + "m\nLDA:  " + runway.leftRunway.getOrigParams().getLDA() + "m\n\n"));
            declaredDistances.getChildren().add(new Text("Runway " + runway.rightRunway.getDesignator() + ":\nTORA: " + runway.rightRunway.getOrigParams().getTORA() + "m\nTODA: " + runway.rightRunway.getOrigParams().getTODA() + "m\nASDA: " + runway.rightRunway.getOrigParams().getASDA() + "m\nLDA:  " + runway.rightRunway.getOrigParams().getLDA() + "m\n\n"));

            calculator.calculate(obstaclePosition, runway);

            declaredDistances.getChildren().add(new Text("Recalculated distances:\n"));
            declaredDistances.getChildren().add(new Text("Runway " + runway.leftRunway.getDesignator() + ":\nTORA: " + runway.leftRunway.getRecalcParams().getTORA() + "m\nTODA: " + runway.leftRunway.getRecalcParams().getTODA() + "m\nASDA: " + runway.leftRunway.getRecalcParams().getASDA() + "m\nLDA:  " + runway.leftRunway.getRecalcParams().getLDA() + "m\n\n"));
            declaredDistances.getChildren().add(new Text("Runway " + runway.rightRunway.getDesignator() + ":\nTORA: " + runway.rightRunway.getRecalcParams().getTORA() + "m\nTODA: " + runway.rightRunway.getRecalcParams().getTODA() + "m\nASDA: " + runway.rightRunway.getRecalcParams().getASDA() + "m\nLDA:  " + runway.rightRunway.getRecalcParams().getLDA() + "m\n"));

            calculationsBreakdown.getChildren().clear();
            calculationsBreakdown.getChildren().add(new Text(runway.leftRunway.getRecalcBreakdown() + "\n\n"));
            calculationsBreakdown.getChildren().add(new Text(runway.rightRunway.getRecalcBreakdown()));

            drawRunway();

        } catch (NoRedeclarationNeededException e) {
            Logger.Log(Logger.Level.ERROR, e.getStackTrace().toString());
            declaredDistances.getChildren().add(new Text("\n\n" + e.getMessage()));
        } catch (AttributeNotAssignedException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error recalculating runway parameters.\nEnsure that the Obstacle position fields are correct.", ButtonType.CLOSE);
            alert.setTitle("Calculation failed");
            alert.showAndWait();
            return;
        } catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to parse values in Obstacle Position boxes.\n, Please ensure the values are valid.", ButtonType.CLOSE);
            alert.setTitle("Failed to parse values");
            alert.showAndWait();
        }
    }

    /**
     * Add a new airport to the database.
     */
    @FXML
    public void handleNewAirport() {
        try {
            Airport newAirport = NewAirportPopup.display();
            // This stops select an airport being added to the combobox.
            if (newAirport == null) {
                return;
            }
            airportComboBox.getItems().add(newAirport);
            airportComboBox.setValue(newAirport);
            controller.add_airport(newAirport);
        } catch (NullPointerException e) {
        }
    }

    /**
     * Add a new runway to the database.
     */
    @FXML
    public void handleNewRunway() {
        try {
            Airport currentAirport = airportComboBox.getValue();
            if (currentAirport == null) {
                JOptionPane.showMessageDialog(null, "You need to add or import an airport prior to adding a runway.");
                return;
            }
            Runway newRunway = NewRunwayPopup.display("Add a new runway to " + currentAirport.toString());
            // This stops select a runway being added to the combo box.
            if (newRunway == null) {
                return;
            }
            runwayComboBox.getItems().addAll(newRunway);
            runwayComboBox.setValue(newRunway);
            controller.add_Runway(newRunway, currentAirport.getAirport_id());
        } catch (NullPointerException e) {
        }

    }

    /**
     * Add a new obstacle to the database.
     */
    @FXML
    public void handleNewObstacle() {
        try {
            Obstacle newObstacle;
            newObstacle = NewObstaclePopup.display("Add a New Obstacle");
            // This stops 'select an obstacle' being added as an option on the combo box.
            if (newObstacle == null) {
                return;
            }
            obstructionComboBox.getItems().add(newObstacle);
            obstructionComboBox.setValue(newObstacle);
            controller.add_obstacle(newObstacle);
        } catch (NullPointerException e) {
        }
    }

    /**
     * Load an XML file.
     */
    @FXML
    public void handleImportFile() {
        new XML_File_Loader(controller).load_file();
        refresh_airports();
    }

    /**
     * Load a directory.
     */
    @FXML
    void handleImportFolder() {
        new XML_File_Loader(controller).load_directory();
        refresh_airports();
    }

    /**
     * Remove an airport from the database.
     */
    @FXML
    public void handleRemoveAirport() {
        for(Airport a : SelectAirportPopup.display(controller, "Select Airports to Remove")){
            controller.remove_Airport(a);
        }
        refresh_airports();
    }

    /**
     * Refresh the combo boxes to reflect changes to .
     */
    private void refresh_airports() {
        airportComboBox.getItems().clear();
        Airport[] airports = controller.get_airports();
        airportComboBox.getItems().addAll(airports);
        if(airports.length > 0){
            airportComboBox.setValue(airports[0]);
            refresh_runways();
        }

        refresh_obstacles();
    }

    @FXML
    private void refresh_runways(){
        if (airportComboBox.getItems().size() > 0) {
            Runway[] runways = controller.get_runways(airportComboBox.getValue().getAirport_id());
            if (runways.length > 0) {
                runwayComboBox.getItems().clear();
                runwayComboBox.getItems().addAll(runways);
                runwayComboBox.setValue(runwayComboBox.getItems().get(0));
                refresh_virtual_runways();
            }
        } else {
            runwayComboBox.getItems().clear();
        }
    }

    private void refresh_obstacles(){
        obstructionComboBox.getItems().clear();
        obstructionComboBox.getItems().addAll(controller.get_obstacles());
        if (obstructionComboBox.getItems().size() > 0) {
            obstructionComboBox.setValue(obstructionComboBox.getItems().get(0));
        }
    }

    @FXML
    private void refresh_virtual_runways(){
        Runway runway = runwayComboBox.getValue();
        if (runway == null) {
            return;
        }
        ArrayList<VirtualRunway> virtualRunways = new ArrayList<>();
        Collections.addAll(virtualRunways, runway.leftRunway, runway.rightRunway);
        ObservableList<VirtualRunway> observableList = FXCollections.observableList(virtualRunways);
        virtualRunwayComboBox.setItems(observableList);
        virtualRunwayComboBox.setValue(virtualRunwayComboBox.getItems().get(0));
        updateDeclaredDistancesTextfield();
    }

    /**
     * Prevents user from entering a value for distance to C/L if they specify the object is in the center.
     */
    @FXML
    private void runwaySideComboBoxHandler(){
        if(runwaySideComboBox.getValue() == RunwaySide.CENTER){
            distanceFromCL.setText("0");
            distanceFromCL.setEditable(false);
        } else {
            distanceFromCL.clear();
            distanceFromCL.setEditable(true);
        }
    }

    /**
     * Show the About popup display.
     */
    @FXML
    private void showAbout() {
        AboutPopup.display();
    }


    @FXML
    public void handleExportXML(){
        new XML_Export(controller, obstaclePosition);
        refresh_airports();
    }


    @FXML
    public void handleRemoveObstacle(){
        for(Obstacle o : RemoveObstaclePopup.display(controller)){
            controller.remove_obstacle(o);
        }
        refresh_airports();
    }
}