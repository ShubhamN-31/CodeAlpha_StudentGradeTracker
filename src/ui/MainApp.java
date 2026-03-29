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
                    addButton.setStyle("-fx-background-color: #f39c12;");
                }
            });
            return row;
        });

        searchField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(s -> newVal == null || newVal.isEmpty() ||
                    s.nameProperty().get().toLowerCase().contains(newVal.toLowerCase()));
        });

        addButton.setOnAction(e -> {
            String name = nameField.getText(), dob = dobField.getText(), dept = deptField.getText(), mTxt = marksField.getText();
            if (name.isEmpty() || mTxt.isEmpty()) {
                showAlert("Input Required", "Please fill in Name and Marks at least.");
                return;
            }
            try {
                double m = Double.parseDouble(mTxt);
                String g = (m >= 90) ? "A" : (m >= 75) ? "B" : (m >= 60) ? "C" : "D";

                if (selectedStudent != null) {
                    DatabaseHandler.updateStudent(selectedStudent.idProperty().get(), name, dob, dept, mTxt);
                } else {
                    DatabaseHandler.addStudent(name, dob, dept, mTxt, g);
                }

                masterData.setAll(DatabaseHandler.getAllStudents());
                updateDashboard(table, avgLabel, highLabel, lowLabel);

                selectedStudent = null;
                addButton.setText("Add Student");
                addButton.setStyle("");
                nameField.clear(); dobField.clear(); deptField.clear(); marksField.clear();
                nameField.requestFocus();
            } catch (Exception ex) { showAlert("Error", "Check your inputs (Marks must be a number)."); }
        });

        clearFieldsButton.setOnAction(e -> {
            selectedStudent = null;
            addButton.setText("Add Student"); addButton.setStyle("");
            nameField.clear(); dobField.clear(); deptField.clear(); marksField.clear();
        });

        deleteButton.setOnAction(e -> {
            StudentRow s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                DatabaseHandler.deleteStudent(s.idProperty().get());
                masterData.remove(s);
                updateDashboard(table, avgLabel, highLabel, lowLabel);
            }
        });

        HBox inputRow = new HBox(10, nameField, dobField, deptField, marksField);
        HBox buttonRow = new HBox(10, addButton, clearFieldsButton, deleteButton);
        HBox contentRow = new HBox(20, table, gradeChart);
        VBox root = new VBox(20, title, inputRow, buttonRow, searchField, contentRow, new HBox(20, avgLabel, highLabel, lowLabel));
        root.getStyleClass().add("main-container");

        Scene scene = new Scene(root, 1150, 750);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addButton.fire();
                event.consume();
            }
        });

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
            try {
                double m = Double.parseDouble(s.marksProperty().get());
                total += m; h = Math.max(h, m); l = Math.min(l, m); count++;
                String g = s.gradeProperty().get();
                if (g.equals("A")) aCount++; else if (g.equals("B")) bCount++;
                else if (g.equals("C")) cCount++; else dCount++;
            } catch (Exception ignored) {}
        }
        avg.setText("Avg: " + (count > 0 ? String.format("%.2f", total / count) : "0"));
        high.setText("High: " + (h == -1 ? "0" : h));
        low.setText("Low: " + (l == 101 ? "0" : l));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("A", aCount), new PieChart.Data("B", bCount),
                new PieChart.Data("C", cCount), new PieChart.Data("D", dCount)
        );
        gradeChart.setData(pieData);
    }

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(t); a.setContentText(m); a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
