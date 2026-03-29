package ui;

import javafx.beans.property.*;

public class StudentRow {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty dob;
    private final StringProperty department;
    private final StringProperty marks;
    private final StringProperty grade;

    // Use this single constructor for everything
    public StudentRow(int id, String name, String dob, String department, String marks, String grade) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.dob = new SimpleStringProperty(dob);
        this.department = new SimpleStringProperty(department);
        this.marks = new SimpleStringProperty(marks);
        this.grade = new SimpleStringProperty(grade);
    }

    // Property getters for JavaFX TableView
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty dobProperty() { return dob; }
    public StringProperty departmentProperty() { return department; }
    public StringProperty marksProperty() { return marks; }
    public StringProperty gradeProperty() { return grade; }
}