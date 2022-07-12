package chan.tinpui.timesheet.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.time.LocalDate;

public class DateRangeFormBox extends HBox {

    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;

    public DateRangeFormBox(LocalDate defaultFrom, LocalDate defaultTo) {
        this.fromDatePicker = new DatePicker(defaultFrom);
        this.toDatePicker = new DatePicker(defaultTo);
        fromDatePicker.setShowWeekNumbers(false);
        toDatePicker.setShowWeekNumbers(false);
        fromDatePicker.setDayCellFactory(datePicker ->
                new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isAfter(toDatePicker.getValue())) {
                            setDisable(true);
                        }
                    }
                });
        toDatePicker.setDayCellFactory(datePicker ->
                new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(fromDatePicker.getValue())) {
                            setDisable(true);
                        }
                    }
                });
        this.setPadding(new Insets(0, 0, 2, 0));
        this.setAlignment(Pos.CENTER);
        this.getChildren().add(new Label("Create time logs from "));
        this.getChildren().add(fromDatePicker);
        this.getChildren().add(new Label(" to "));
        this.getChildren().add(toDatePicker);
    }

    public LocalDate selectedFromDate() {
        return fromDatePicker.getValue();
    }

    public LocalDate selectedToDate() {
        return toDatePicker.getValue();
    }
}
