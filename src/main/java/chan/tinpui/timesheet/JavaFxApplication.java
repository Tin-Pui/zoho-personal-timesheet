package chan.tinpui.timesheet;

import chan.tinpui.timesheet.controller.Controller;
import chan.tinpui.timesheet.controller.HelpInfoBox;
import chan.tinpui.timesheet.controller.UserInterface;
import chan.tinpui.timesheet.persistence.FileSettingsService;
import chan.tinpui.timesheet.persistence.FileTokenService;
import chan.tinpui.timesheet.persistence.SettingsService;
import chan.tinpui.timesheet.persistence.TokenService;
import chan.tinpui.timesheet.zoho.PeopleZohoService;
import chan.tinpui.timesheet.zoho.ZohoService;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class JavaFxApplication extends Application {

    public static final String APP_DIRECTORY = System.getProperty("user.home") + File.separatorChar + "zoho-personal-timesheet" + File.separatorChar;
    private UserInterface userInterface;
    private Stage helpInfoStage;

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ProgressIndicator loadingScreen = new ProgressIndicator();
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
        this.userInterface = new UserInterface(loadingScreen);
        userInterface.getChildren().add(hyperlink);
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(loadingScreen);
        vBox.getChildren().add(userInterface);
        Scene scene = new Scene(vBox, 635, 600);
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(560);
        stage.setTitle("Zoho Personal Timesheet");
        stage.show();
        CompletableFuture.runAsync(() -> {
            try {
                TokenService tokenService = new FileTokenService(APP_DIRECTORY + "zoho_token.csv");
                SettingsService settingsService = new FileSettingsService(APP_DIRECTORY + "settings.json");
                ZohoService zohoService = new PeopleZohoService();
                Controller controller = new Controller(tokenService, settingsService, zohoService);
                userInterface.setController(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        if (userInterface != null) {
            userInterface.saveToken();
        }
    }
}
