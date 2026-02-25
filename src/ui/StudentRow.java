package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StudentRow {

    private final StringProperty name;
    private final StringProperty marks;
    private final StringProperty grade;

    public StudentRow(String name, String marks, String grade) {
        this.name = new SimpleStringProperty(name);
        this.marks = new SimpleStringProperty(marks);
        this.grade = new SimpleStringProperty(grade);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty marksProperty() {
        return marks;
    }

    public StringProperty gradeProperty() {
        return grade;
    }
}