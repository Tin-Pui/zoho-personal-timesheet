package chan.tinpui.timesheet;

import chan.tinpui.timesheet.controller.HelpInfoBox;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


public class TestApplication  extends Application {

    public static void main(String[] args) {
        Application.launch(TestApplication.class, args);
    }
    private Stage helpInfoStage;

    @Override
    public void start(Stage stage) throws Exception {
        Hyperlink hyperlink = new Hyperlink();
        hyperlink.setText("Need help?");
        hyperlink.setTextFill(Color.BLUE);
        hyperlink.setBorder(Border.EMPTY);
        Font font = hyperlink.getFont();
        hyperlink.setFont(Font.font(font.getName(), FontWeight.BOLD, font.getSize()));
        hyperlink.setOnAction(actionEvent -> {
            synchronized (this) {
                if (this.helpInfoStage == null) {
                    this.helpInfoStage = new Stage();
                    ScrollPane scrollPane = new ScrollPane();
                    scrollPane.setFitToWidth(true);
                    scrollPane.setContent(HelpInfoBox.INSTANCE);
                    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    Scene scene = new Scene(scrollPane, 900, 670); // Manage scene size
                    helpInfoStage.setTitle("How to use Zoho Personal Timesheet");
                    helpInfoStage.setMinWidth(880);
                    helpInfoStage.setMinHeight(200);
                    helpInfoStage.setScene(scene);
                    helpInfoStage.initOwner(stage);
                }
                helpInfoStage.show();
            }
        });

        Scene scene = new Scene(hyperlink, 450, 400); // Manage scene size
        stage.setScene(scene);
        stage.setTitle("Zoho Personal Timesheet");
        stage.show();
    }
}
