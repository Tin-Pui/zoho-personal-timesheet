package chan.tinpui.timesheet.controller;

import chan.tinpui.timesheet.zoho.domain.Settings;
import chan.tinpui.timesheet.zoho.domain.ZohoRecord;
import com.zoho.api.authenticator.OAuthToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static chan.tinpui.timesheet.controller.StatusLabel.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class UserInterface extends VBox {

    private static final double BUTTON_HEIGHT = 50.0;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Controller controller;
    private Node loadingScreen;

    private TextField clientIdField;
    private TextField clientSecretField;
    private TextField grantTokenField;
    private TextField refreshTokenField;
    private TextField userEmailField;
    private StatusLabel updateJobsStatusLabel;
    private Label accessTokenLabel;
    private ComboBox<ZohoRecord> selectedJobDropdown;
    private ComboBox<ZohoRecord> holidayJobDropdown;
    private Map<Integer, Spinner<Integer>> dayHourFields;
    private FormGridPane leaveMapForm;
    private Map<ZohoRecord, ComboBox<ZohoRecord>> leaveMapControls;
    private TextArea infoDisplayArea;

    public UserInterface(Node loadingScreen) {
        this.clientIdField = new TextField();
        this.clientSecretField = new PasswordField();
        this.grantTokenField = new TextField();
        this.refreshTokenField = new TextField();
        this.userEmailField = new TextField();
        this.updateJobsStatusLabel = new StatusLabel();
        this.accessTokenLabel = new Label("Access Token");
        this.selectedJobDropdown = new ComboBox<>();
        selectedJobDropdown.setMaxWidth(Double.MAX_VALUE);
        selectedJobDropdown.setTooltip(new Tooltip("Select the main job that you are doing while at work"));
        this.holidayJobDropdown = new ComboBox<>();
        holidayJobDropdown.setMaxWidth(Double.MAX_VALUE);
        holidayJobDropdown.setTooltip(new Tooltip("Select the job to use for adding time logs for public holidays"));
        this.dayHourFields = new HashMap<>(10);
        this.leaveMapForm = new FormGridPane();
        this.leaveMapControls = new HashMap<>();
        this.infoDisplayArea = new TextArea();
        infoDisplayArea.setEditable(false);
        this.setPadding(new Insets(5, 5, 5, 5));
        this.setAlignment(Pos.CENTER);

        this.loadingScreen = loadingScreen;
        loadingScreen.managedProperty().bind(loadingScreen.visibleProperty());
        loadingScreen.setVisible(true);
        managedProperty().bind(visibleProperty());
        setVisible(false);
    }

    public void setController(Controller controller) {
        this.controller = controller;
        this.infoDisplayArea.setText(controller.getDisplayLogs());
        controller.addAccessTokenListener(accessToken -> Platform.runLater(() -> accessTokenLabel.setText(accessToken)));
        controller.addLogListener(log -> Platform.runLater(() -> infoDisplayArea.appendText("\n" + log)));
        updateFields();
        boolean jobsUpdated = populateSettings();
        addComponents();
        Platform.runLater(() -> {
            if (jobsUpdated) {
                updateJobsStatusLabel.setState(FINISHED_OK);
            }
            VBox.setVgrow(this, Priority.ALWAYS);
            this.loadingScreen.setVisible(false);
            setVisible(true);
        });
    }

    public void close() {
        if (controller != null) {
            controller.saveToken(userEmailField.getText());
        }
        executorService.shutdown();
    }

    private void updateFields() {
        if (controller.isTokenActive()) {
            OAuthToken token = controller.getToken();
            Platform.runLater(() -> {
                clientIdField.setText(token.getClientId());
                clientSecretField.setText(token.getClientSecret());
                grantTokenField.setText(token.getGrantToken());
                refreshTokenField.setText(token.getRefreshToken());
                userEmailField.setText(token.getUserMail());
                accessTokenLabel.setText("Access Token: " + (isEmpty(token.getAccessToken()) ? "--" : token.getAccessToken()));
            });
        }
    }

    private boolean populateSettings() {
        if (controller.isTokenActive()) {
            OAuthToken token = controller.getToken();
            Settings settings = controller.getSettings();
            List<ZohoRecord> jobs = controller.findJobs(token.getUserMail(), true);
            List<ZohoRecord> holidayJobOptions = new ArrayList<>();
            holidayJobOptions.add(new ZohoRecord(ZohoRecord.IGNORE_RECORD_ID, "{ No selection: Omit time logs for public/bank holidays }"));
            holidayJobOptions.addAll(jobs);
            Platform.runLater(() -> {
                updateJobDropdown(selectedJobDropdown, jobs, settings.getDefaultJobId());
                updateJobDropdown(holidayJobDropdown, holidayJobOptions, settings.getHolidayJobId());
                populateLeaveMapForm(settings.getLeaveToJobMap(), controller.findLeaveTypes(token.getUserMail(), true), jobs);
            });
            return !jobs.isEmpty();
        } else {
            return false;
        }
    }

    private void populateLeaveMapForm(Map<String, String> leaveToJobMap, List<ZohoRecord> leaveTypes, List<ZohoRecord> jobs) {
        leaveMapForm.removeAllFormFields();
        leaveMapControls.clear();
        List<ZohoRecord> selectOptions = new ArrayList<>();
        selectOptions.add(new ZohoRecord(ZohoRecord.IGNORE_RECORD_ID, "{ No selection: Omit time logs for this leave type }"));
        selectOptions.addAll(jobs);
        for (ZohoRecord leaveType : leaveTypes) {
            ComboBox<ZohoRecord> jobDropdown = new ComboBox<>();
            jobDropdown.setMaxWidth(Double.MAX_VALUE);
            updateJobDropdown(jobDropdown, selectOptions, leaveToJobMap.getOrDefault(leaveType.getId(), ZohoRecord.IGNORE_RECORD_ID));
            leaveMapForm.addFormField(leaveType.getDescription(), jobDropdown);
            leaveMapControls.put(leaveType, jobDropdown);
        }
    }

    private void updateJobDropdown(ComboBox<ZohoRecord> jobDropdown, List<ZohoRecord> jobs, String defaultJobId) {
        ObservableList<ZohoRecord> observableList = FXCollections.observableArrayList(jobs);
        jobDropdown.setItems(observableList);
        if (!observableList.isEmpty()) {
            int defaultSelectedIndex = 0;
            if (defaultJobId != null) {
                int index = 0;
                for (ZohoRecord job : observableList) {
                    if (defaultJobId.equals(job.getId())) {
                        defaultSelectedIndex = index;
                        break;
                    }
                    index++;
                }
            }
            jobDropdown.getSelectionModel().select(defaultSelectedIndex);
        }
    }

    private void addComponents() {
        FormGridPane connectionForm = new FormGridPane();
        connectionForm.addFormField("Client ID", clientIdField);
        connectionForm.addFormField("Client Secret", clientSecretField);
        connectionForm.addFormField("Grant Token", grantTokenField);
        connectionForm.addFormField("Refresh Token", refreshTokenField);
        connectionForm.addFormField("Email", userEmailField);
        addNode(connectionForm);

        Button updateJobListButton = new Button("Update Job List");
        Button createTimeLogsButton = new Button("Create Time Logs");
        StatusLabel createTimeLogsStatusLabel = new StatusLabel();

        addButton(updateJobListButton, updateJobsStatusLabel, (actionEvent) -> handleButtonClick(() -> {
            Platform.runLater(() -> updateJobsStatusLabel.setState(IN_PROGRESS));
            controller.updateAccessToken(clientIdField.getText(), clientSecretField.getText(), grantTokenField.getText(), refreshTokenField.getText());
            controller.updateUserEmail(userEmailField.getText());
            updateFields();
            boolean jobsUpdated = populateSettings();
            Platform.runLater(() -> updateJobsStatusLabel.setState(jobsUpdated ? FINISHED_OK : FINISHED_NOT_OK));
        }, updateJobListButton, createTimeLogsButton));

        addNode(accessTokenLabel);

        FormGridPane mainJobsForm = new FormGridPane();
        mainJobsForm.addFormField("Job", selectedJobDropdown);
        mainJobsForm.addFormField("Holiday", holidayJobDropdown);
        addNode(mainJobsForm);

        addDayHoursSetting(controller.getSettings());

        addNode(leaveMapForm);

        DateRangeFormBox dateRangeFormBox = new DateRangeFormBox( LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()));
        addNode(dateRangeFormBox);

        CheckBox ignoreExistingTimeLogsCheckBox = new CheckBox();
        Label ignoreExistingTimeLogsLabel = new Label("Ignore existing time logs");
        Tooltip ignoreExistingTimeLogsTooltip = new Tooltip("The number of hours for new time logs will be reduced based on existing time logs unless this option is enabled.");
        ignoreExistingTimeLogsTooltip.setShowDelay(Duration.millis(500));
        ignoreExistingTimeLogsTooltip.setShowDuration(Duration.seconds(10));
        ignoreExistingTimeLogsLabel.setTooltip(ignoreExistingTimeLogsTooltip);
        ignoreExistingTimeLogsLabel.setPadding(new Insets(0, 0, 2, 0));
        ignoreExistingTimeLogsLabel.setGraphic(ignoreExistingTimeLogsCheckBox);
        ignoreExistingTimeLogsLabel.setContentDisplay(ContentDisplay.RIGHT);
        addNode(ignoreExistingTimeLogsLabel);

        addButton(createTimeLogsButton, createTimeLogsStatusLabel, (actionEvent) -> handleButtonClick(() -> {
            Platform.runLater(() -> createTimeLogsStatusLabel.setState(IN_PROGRESS));
            Map<String, String> leaveToJobMap = new HashMap<>();
            for (Map.Entry<ZohoRecord, ComboBox<ZohoRecord>> leaveJobEntry : leaveMapControls.entrySet()) {
                ComboBox<ZohoRecord> leaveJobSelection = leaveJobEntry.getValue();
                if (leaveJobSelection.getValue() != null && !leaveJobSelection.getValue().getId().equals(ZohoRecord.IGNORE_RECORD_ID)) {
                    leaveToJobMap.put(leaveJobEntry.getKey().getId(), leaveJobSelection.getValue().getId());
                }
            }
            controller.saveSettings(
                    selectedJobDropdown.getValue() == null ? "" : selectedJobDropdown.getValue().getId(),
                    holidayJobDropdown.getValue() == null ? "" : holidayJobDropdown.getValue().getId(),
                    leaveToJobMap,
                    dayHourFields.get(1).getValue(),
                    dayHourFields.get(2).getValue(),
                    dayHourFields.get(3).getValue(),
                    dayHourFields.get(4).getValue(),
                    dayHourFields.get(5).getValue(),
                    dayHourFields.get(6).getValue(),
                    dayHourFields.get(7).getValue());
            boolean success = controller.addTimeLogs(userEmailField.getText(), dateRangeFormBox.selectedFromDate(), dateRangeFormBox.selectedToDate(), ignoreExistingTimeLogsCheckBox.isSelected());
            Platform.runLater(() -> createTimeLogsStatusLabel.setState(success ? FINISHED_OK : FINISHED_NOT_OK));
        }, updateJobListButton, createTimeLogsButton));

        addSeparator();

        addDisplayArea("Log", infoDisplayArea);
    }

    private void handleButtonClick(Runnable runnable, Button... disableButtons) {
        Platform.runLater(() -> {
            for (Button button : disableButtons) {
                button.setDisable(true);
            }
        });
        executorService.execute(runnable);
        executorService.execute(() -> {
            Platform.runLater(() -> {
                for (Button button : disableButtons) {
                    button.setDisable(false);
                }
            });
        });
    }

    private void addButton(Button button, StatusLabel statusLabel, EventHandler<ActionEvent> eventHandler) {
        statusLabel.translateXProperty().bind(button.heightProperty().divide(1.5).add(button.widthProperty().divide(2)));
        StackPane stackPane = new StackPane();
        button.setOnAction(eventHandler);
        button.setPrefHeight(BUTTON_HEIGHT);
        button.setMinWidth(300);
        stackPane.getChildren().addAll(statusLabel, button);
        addNode(stackPane);
    }

    private void addDayHoursSetting(Settings settings) {
        GridPane dayHoursPane = new GridPane();
        dayHoursPane.setHgap(2);
        dayHoursPane.setAlignment(Pos.CENTER);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setMinWidth(32);
        dayHoursPane.getColumnConstraints().add(column1);
        dayHoursPane.add(new Label(), 0, 0);
        ColumnConstraints dayConstraints = new ColumnConstraints();
        dayConstraints.setHalignment(HPos.CENTER);
        dayConstraints.setMinWidth(52);
        dayConstraints.setMaxWidth(100);
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            dayHoursPane.getColumnConstraints().add(dayConstraints);
            dayHoursPane.add(new Label(DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)), dayOfWeek, 0);
        }
        dayHoursPane.add(new Label("Hours"), 0, 1);
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            Spinner<Integer> spinner = new Spinner(0, 16, settings.getHoursForDay(DayOfWeek.of(dayOfWeek)), 1);
            dayHoursPane.add(spinner, dayOfWeek, 1);
            dayHourFields.put(dayOfWeek, spinner);
        }
        addNode(dayHoursPane);
    }

    private void addSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 0, 0));
        addNode(separator);
    }

    private void addDisplayArea(String label, TextArea textArea) {
        VBox.setVgrow(textArea, Priority.ALWAYS);
        addNode(new Label(label));
        addNode(textArea);
    }

    private void addNode(Node node) {
        Platform.runLater(() -> this.getChildren().add(node));
    }
}
