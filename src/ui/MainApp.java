package ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.transformation.SortedList;
import java.io.FileWriter;
import java.io.IOException;
import javafx.stage.FileChooser;
import java.io.File;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        Label title = new Label("Student Grade Tracker");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        final StudentRow[] selectedRow = {null};

        TextField nameField = new TextField();
        nameField.setPromptText("Enter student name");

        TextField marksField = new TextField();
        marksField.setPromptText("Enter marks");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by name...");

        ObservableList<StudentRow> masterData =
                FXCollections.observableArrayList();

        FilteredList<StudentRow> filteredData =
                new FilteredList<>(masterData, p -> true);


        TableView<StudentRow> table = new TableView<>();
        SortedList<StudentRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<StudentRow, String> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(data -> data.getValue().marksProperty());

        TableColumn<StudentRow, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> data.getValue().gradeProperty());

        table.getColumns().addAll(nameCol, marksCol, gradeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(250);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(student -> {

                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return student.nameProperty().get().toLowerCase().contains(lowerCaseFilter);
            });
        });
        Label averageLabel = new Label("Average: 0");
        Label highestLabel = new Label("Highest: 0");
        Label lowestLabel = new Label("Lowest: 0");

        HBox statsBox = new HBox(20, averageLabel, highestLabel, lowestLabel);
        Button addButton = new Button("Add Student");
        Button deleteButton = new Button("Delete Selected");
        Button clearAllButton = new Button("Clear All");
        Button exportButton = new Button("Export CSV");

        addButton.setOnAction(e -> {

            String name = nameField.getText();
            String marksText = marksField.getText();

            if (name.isEmpty() || marksText.isEmpty()) return;

            double marks;

            try {
                marks = Double.parseDouble(marksText);
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numeric marks.");
                return;
            }

            if (marks < 0 || marks > 100) {
                showAlert("Invalid Range", "Marks must be between 0 and 100.");
                return;
            }

            String grade;
            if (marks >= 90) grade = "A";
            else if (marks >= 75) grade = "B";
            else if (marks >= 60) grade = "C";
            else grade = "D";

            if (selectedRow[0] != null) {
                selectedRow[0].nameProperty().set(name);
                selectedRow[0].marksProperty().set(marksText);
                selectedRow[0].gradeProperty().set(grade);

                selectedRow[0] = null;
                addButton.setText("Add Student");

            } else {

                masterData.add(new StudentRow(name, marksText, grade));
            }

            updateStats(table, averageLabel, highestLabel, lowestLabel);

            nameField.clear();
            marksField.clear();
        });
        table.setRowFactory(tv -> {
            TableRow<StudentRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    StudentRow rowData = row.getItem();
                    selectedRow[0] = rowData;

                    nameField.setText(rowData.nameProperty().get());
                    marksField.setText(rowData.marksProperty().get());

                    addButton.setText("Update Student");
                }
            });
            return row;
        });

        deleteButton.setOnAction(e -> {
            StudentRow selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                masterData.remove(selected);
                updateStats(table, averageLabel, highestLabel, lowestLabel);
            }
        });

        clearAllButton.setOnAction(e -> {
            masterData.clear();
            selectedRow[0] = null;
            addButton.setText("Add Student");
            nameField.clear();
            marksField.clear();
            updateStats(table, averageLabel, highestLabel, lowestLabel);
        });

        exportButton.setOnAction(e -> {

            if (masterData.isEmpty()) {
                showAlert("No Data", "There are no students to export.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Student Data");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("students.csv");

            File file = fileChooser.showSaveDialog(stage);

            if (file == null) {
                return; // user cancelled
            }

            try (FileWriter writer = new FileWriter(file)) {

                writer.write("Name,Marks,Grade\n");

                for (StudentRow row : masterData) {
                    writer.write(
                            row.nameProperty().get() + "," +
                                    row.marksProperty().get() + "," +
                                    row.gradeProperty().get() + "\n"
                    );
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Export Successful");
                alert.setContentText("File saved successfully!");
                alert.showAndWait();

            } catch (IOException ex) {
                showAlert("Error", "Failed to save file.");
            }
        });

        HBox formRow = new HBox(10, nameField, marksField);
        HBox buttonRow = new HBox(10, addButton, deleteButton, clearAllButton, exportButton);

        VBox root = new VBox(20,
                title,
                formRow,
                buttonRow,
                searchField,
                table,
                statsBox);

        root.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(root, 700, 500);

        stage.setTitle("Student Grade Tracker");
        stage.setScene(scene);
        stage.show();
    }

    private void updateStats(TableView<StudentRow> table,
                             Label averageLabel,
                             Label highestLabel,
                             Label lowestLabel) {

        if (table.getItems().isEmpty()) {
            averageLabel.setText("Average: 0");
            highestLabel.setText("Highest: 0");
            lowestLabel.setText("Lowest: 0");
            return;
        }

        double total = 0;
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;

        for (StudentRow row : table.getItems()) {
            double m = Double.parseDouble(row.marksProperty().get());
            total += m;
            highest = Math.max(highest, m);
            lowest = Math.min(lowest, m);
        }

        double average = total / table.getItems().size();

        averageLabel.setText("Average: " + String.format("%.2f", average));
        highestLabel.setText("Highest: " + highest);
        lowestLabel.setText("Lowest: " + lowest);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}