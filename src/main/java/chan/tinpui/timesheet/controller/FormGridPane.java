package chan.tinpui.timesheet.controller;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import static chan.tinpui.timesheet.controller.utils.UserInterfaceUtils.computeTextWidth;

public class FormGridPane extends GridPane {

    private int numberOfFields;

    public FormGridPane() {
        this.numberOfFields = 0;
        this.setPadding(new Insets(2, 0, 2, 0));
        this.setHgap(2);
        this.setAlignment(Pos.CENTER);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.RIGHT);
        this.getColumnConstraints().add(column1);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHalignment(HPos.LEFT);
        column2.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(column2);
    }

    public void addFormField(String label, Node formField) {
        Label labelComponent = new Label(label);
        labelComponent.setMinWidth(computeTextWidth(labelComponent.getFont(), label));
        this.add(labelComponent, 0, numberOfFields);
        this.add(formField, 1, numberOfFields);
        numberOfFields++;
    }

    public void removeAllFormFields() {
        this.getChildren().clear();
    }
}
