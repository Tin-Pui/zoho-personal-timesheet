package chan.tinpui.timesheet.controller;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HelpInfoBox extends VBox {

    public static final HelpInfoBox INSTANCE = new HelpInfoBox();

    private static double computeTextWidth(Font font, String text) {
        Text helper = new Text();
        helper.setText(text);
        helper.setFont(font);
        helper.setWrappingWidth(0.0D);
        helper.setLineSpacing(0.0D);
        helper.setWrappingWidth((int) Math.ceil(Math.min(helper.prefWidth(-1.0D), 0.0d)));
        return Math.ceil(helper.getLayoutBounds().getWidth());
    }

    private HelpInfoBox() {
        addCollapsiblePane("First Time Setup",
                createTextFlow("Log into ", hyperlink("https://api-console.zoho.com/")),
                createTextFlow("Add a new client and select ", bold("Self Client"), " from the list of client types to create your new client."),
                createTextFlow("The client ID and client secret are displayed under the Client Secret tab for the new client."),
                createTextFlow("Click the Generate Code tab and enter ", copyable("ZOHOPEOPLE.employee.ALL,ZOHOPEOPLE.forms.ALL,ZOHOPEOPLE.timetracker.ALL,ZOHOPEOPLE.leave.ALL"), " for scope, select a duration and enter any scope description."),
                createTextFlow("The grant token code can now be copied. This will be valid for the duration selected."),
                createTextFlow("Enter the client ID, client secret, grant token and your email, then click the ", bold("Update Job List"), " button."),
                createTextFlow("If successful, the access token will be displayed, the refresh token will be populated and used next time."),
                createTextFlow("If you can see the access token but cannot select any jobs, ensure that you have entered the correct email then click the ", bold("Update Job List"), " button again.")
        );
        addCollapsiblePane("Settings for Creating Time Logs", createTextFlow(bold("Job"), " - Select the main job that you are doing while at work."),
                createTextFlow(bold("Holiday"), " - Select the job to use for adding time logs for public holidays."),
                createTextFlow("You can configure how many total hours of logs each day of the week should have."),
                createTextFlow("For each leave type, you may also select the job to use when adding time logs for days in which you have an approved leave."),
                createTextFlow("Clicking the ", bold("Create Time Logs"), " button will save the above settings for next time and start creating time logs in Zoho for the current month.")
        );
        addCollapsiblePane("More Information", createTextFlow("The client ID, client secret, grant token, refresh token and email will be saved for future use when the access token is successfully refreshed. These will be used automatically next time on start up."),
                createTextFlow("The token details and settings will be saved in the ", bold("zoho-personal-timesheet"), " folder in your home directory."),
                createTextFlow("Clicking the ", bold("Create Time Logs"), " button only submits time logs without creating or submitting the timesheet, please log into Zoho to check and submit your timesheet."),
                createTextFlow("Zoho Personal Timesheet will factor in any existing time logs already entered when creating time logs.")
        );
    }

    private void addCollapsiblePane(String title, Node... nodes) {
        VBox paneContent = new VBox();
        paneContent.getChildren().addAll(nodes);
        TitledPane titledPane = new TitledPane(title, paneContent);
        this.getChildren().addAll(titledPane);
    }

    private TextFlow createTextFlow(Object... nodes) {
        List<Node> nodeList = new ArrayList<>();
        for (Object obj : nodes) {
            if (obj instanceof Node) {
                Node node = (Node) obj;
                if (node instanceof Pane) {
                    node.setTranslateY(8);
                }
                nodeList.add(node);
            } else {
                nodeList.add(new Text(obj.toString()));
            }
        }
        TextFlow textFlow = new TextFlow(nodeList.toArray(new Node[0]));
        textFlow.setLineSpacing(4);
        textFlow.setPadding(new Insets(6, 0, 6, 0));
        return textFlow;
    }

    private Hyperlink hyperlink(String url) {
        Hyperlink link = new Hyperlink();
        link.setText(url);
        link.setBorder(Border.EMPTY);
        link.setPadding(Insets.EMPTY);
        link.setOnAction((actionEvent) -> {
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URL(url).toURI());
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
        return link;
    }

    private Text bold(String text) {
        Text textNode = new Text();
        Font font = textNode.getFont();
        textNode.setFont(Font.font(font.getName(), FontWeight.BOLD, font.getSize()));
        textNode.setText(text);
        return textNode;
    }

    private HBox copyable(String text) {
        HBox hBox = new HBox();

        TextField textField = new TextField(text);
        textField.setPrefWidth(computeTextWidth(textField.getFont(), text) + 15);
        textField.setEditable(false);
        hBox.getChildren().add(textField);

        Button button = new Button();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("copy.png");
        if (inputStream == null) {
            button.setText("Copy");
        } else {
            Image imageOk = new Image(inputStream);
            button.setGraphic(new ImageView(imageOk));
        }
        button.setOnAction(actionEvent -> {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(text);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        hBox.getChildren().add(button);

        return hBox;
    }

}
