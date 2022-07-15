package chan.tinpui.timesheet.controller;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.InputStream;

public class StatusLabel extends Label {

    public static final int IN_PROGRESS = 1;
    public static final int FINISHED_OK = 2;
    public static final int FINISHED_NOT_OK = 3;
    private Node inProgressGraphic;
    private Node finishedOkGraphic;
    private Node finishedNotOkGraphic;

    public StatusLabel() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPadding(Insets.EMPTY);
        this.inProgressGraphic = progressIndicator;
        InputStream tickIcon = getClass().getClassLoader().getResourceAsStream("tick.png");
        if (tickIcon != null) {
            this.finishedOkGraphic = new ImageView(new Image(tickIcon));
        } else {
            this.finishedOkGraphic = new Text("✔");
        }
        InputStream warningIcon = getClass().getClassLoader().getResourceAsStream("warning.png");
        if (warningIcon != null) {
            this.finishedNotOkGraphic = new ImageView(new Image(warningIcon));
        } else {
            this.finishedNotOkGraphic = new Text("⚠");
        }
        this.setPadding(Insets.EMPTY);
    }

    public StatusLabel(Node inProgressGraphic, Node finishedOkGraphic, Node finishedNotOkGraphic) {
        this.inProgressGraphic = inProgressGraphic;
        this.finishedOkGraphic = finishedOkGraphic;
        this.finishedNotOkGraphic = finishedNotOkGraphic;
        this.setPadding(Insets.EMPTY);
    }

    public void setState(int state) {
        switch(state) {
            case IN_PROGRESS:
                this.setGraphic(inProgressGraphic);
                break;
            case FINISHED_OK:
                this.setGraphic(finishedOkGraphic);
                break;
            case FINISHED_NOT_OK:
                this.setGraphic(finishedNotOkGraphic);
                break;
        }
    }

}
