package ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Optional;

public class MainApp extends Application {

    private PieChart gradeChart;
    private StudentRow selectedStudent = null;

    @Override
    public void start(Stage stage) {
        DatabaseHandler.setupDatabase();

        Label title = new Label("Student Tracker Pro Dashboard");
        title.getStyleClass().add("title-label");

        TextField nameField = new TextField(); nameField.setPromptText("Name");
        TextField dobField = new TextField(); dobField.setPromptText("DOB (YYYY-MM-DD)");
        TextField deptField = new TextField(); deptField.setPromptText("Department");
        TextField marksField = new TextField(); marksField.setPromptText("Marks");
        TextField searchField = new TextField(); searchField.setPromptText("🔍 Search students...");

        ObservableList<StudentRow> masterData = FXCollections.observableArrayList(DatabaseHandler.getAllStudents());
        FilteredList<StudentRow> filteredData = new FilteredList<>(masterData, p -> true);
        TableView<StudentRow> table = new TableView<>();
        SortedList<StudentRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        TableColumn<StudentRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        TableColumn<StudentRow, String> dobCol = new TableColumn<>("DOB");
        dobCol.setCellValueFactory(data -> data.getValue().dobProperty());
        TableColumn<StudentRow, String> deptCol = new TableColumn<>("Dept");
        deptCol.setCellValueFactory(data -> data.getValue().departmentProperty());
        TableColumn<StudentRow, String> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(data -> data.getValue().marksProperty());
        TableColumn<StudentRow, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(data -> data.getValue().gradeProperty());

        table.getColumns().setAll(nameCol, dobCol, deptCol, marksCol, gradeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        gradeChart = new PieChart();
        gradeChart.setTitle("Performance Overview");
        Label avgLabel = new Label(); Label highLabel = new Label(); Label lowLabel = new Label();
        avgLabel.getStyleClass().add("stats-label"); highLabel.getStyleClass().add("stats-label"); lowLabel.getStyleClass().add("stats-label");

        Button addButton = new Button("Add Student");
        Button clearFieldsButton = new Button("Clear Fields");
        Button deleteButton = new Button("Delete Selected");
        deleteButton.getStyleClass().add("button-danger");

        // --- DOUBLE CLICK TO EDIT ---
        table.setRowFactory(tv -> {
            TableRow<StudentRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    selectedStudent = row.getItem();
                    nameField.setText(selectedStudent.nameProperty().get());
                    dobField.setText(selectedStudent.dobProperty().get());
                    deptField.setText(selectedStudent.departmentProperty().get());
                    marksField.setText(selectedStudent.marksProperty().get());
                    addButton.setText("Update Student");
                    addButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                }
            });
            return row;
        });

        // --- SEARCH FILTER ---
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(s -> newVal == null || newVal.isEmpty() ||
                    s.nameProperty().get().toLowerCase().contains(newVal.toLowerCase()));
            updateDashboard(table, avgLabel, highLabel, lowLabel);
        });

        // --- CLEAR BUTTON ---
        clearFieldsButton.setOnAction(e -> {
            selectedStudent = null;
            addButton.setText("Add Student");
            addButton.setStyle("");
            nameField.clear(); dobField.clear(); deptField.clear(); marksField.clear();
            nameField.requestFocus();
        });

        // --- ADD / UPDATE WITH VALIDATION ---
        addButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String dob = dobField.getText().trim();
            String dept = deptField.getText().trim();
            String mTxt = marksField.getText().trim();

            if (name.isEmpty() || dob.isEmpty() || dept.isEmpty() || mTxt.isEmpty()) {
                showAlert("Input Error", "All fields are required.");
                return;
            }

            if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                showAlert("Date Error", "Please use YYYY-MM-DD format.");
                return;
            }

            try {
                double m = Double.parseDouble(mTxt);
                if (m < 0 || m > 100) {
                    showAlert("Range Error", "Marks must be 0-100.");
                    return;
                }

                String g = (m >= 90) ? "A" : (m >= 75) ? "B" : (m >= 60) ? "C" : "D";

                if (selectedStudent != null) {
                    DatabaseHandler.updateStudent(selectedStudent.idProperty().get(), name, dob, dept, mTxt);
                } else {
                    DatabaseHandler.addStudent(name, dob, dept, mTxt, g);
                }

                masterData.setAll(DatabaseHandler.getAllStudents());
                updateDashboard(table, avgLabel, highLabel, lowLabel);
                clearFieldsButton.fire();

            } catch (NumberFormatException ex) {
                showAlert("Format Error", "Marks must be a number.");
            }
        });

        // --- DELETE WITH CONFIRMATION ---
        deleteButton.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Confirmation");
                confirm.setHeaderText("Delete " + s.nameProperty().get() + "?");
                confirm.setContentText("This action cannot be undone.");

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    DatabaseHandler.deleteStudent(s.idProperty().get());
                    masterData.setAll(DatabaseHandler.getAllStudents());
                    updateDashboard(table, avgLabel, highLabel, lowLabel);
                }
            }
        });

        VBox root = new VBox(20, title, new HBox(10, nameField, dobField, deptField, marksField),
                new HBox(10, addButton, clearFieldsButton, deleteButton),
                searchField, new HBox(20, table, gradeChart),
                new HBox(20, avgLabel, highLabel, lowLabel));
        root.getStyleClass().add("main-container");

        Scene scene = new Scene(root, 1150, 750);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> { if (ev.getCode() == KeyCode.ENTER) addButton.fire(); });

        try { scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); } catch (Exception ignored) {}

        updateDashboard(table, avgLabel, highLabel, lowLabel);
        stage.setTitle("Student Tracker Pro - Final Version");
        stage.setScene(scene);
        stage.show();
    }

    private void updateDashboard(TableView<StudentRow> table, Label avg, Label high, Label low) {
        double total = 0, h = -1, l = 101;
        int count = 0, aCount = 0, bCount = 0, cCount = 0, dCount = 0;

        for (StudentRow s : table.getItems()) {
            double m = Double.parseDouble(s.marksProperty().get());
            total += m; h = Math.max(h, m); l = Math.min(l, m); count++;
            String g = s.gradeProperty().get();
            if (g.equals("A")) aCount++; else if (g.equals("B")) bCount++;
            else if (g.equals("C")) cCount++; else dCount++;
        }
        avg.setText("Avg: " + (count > 0 ? String.format("%.2f", total / count) : "0"));
        high.setText("High: " + (h == -1 ? "0" : h));
        low.setText("Low: " + (l == 101 ? "0" : l));
        gradeChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("A", aCount), new PieChart.Data("B", bCount),
                new PieChart.Data("C", cCount), new PieChart.Data("D", dCount)));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}