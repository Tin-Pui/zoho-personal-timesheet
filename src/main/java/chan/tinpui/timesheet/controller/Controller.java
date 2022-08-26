package chan.tinpui.timesheet.controller;

import chan.tinpui.timesheet.exception.InvalidAuthTokenZohoException;
import chan.tinpui.timesheet.exception.ZohoException;
import chan.tinpui.timesheet.persistence.SettingsService;
import chan.tinpui.timesheet.persistence.TokenService;
import chan.tinpui.timesheet.zoho.ZohoService;
import chan.tinpui.timesheet.zoho.domain.*;
import com.zoho.api.authenticator.OAuthToken;
import com.zoho.crm.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class Controller {

    private static final ZohoDomain ZOHO_DOMAIN = ZohoDomain.US;
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final TokenService tokenService;
    private final SettingsService settingsService;
    private final ZohoService zohoService;
    private final Map<String, ZohoRecord> fetchedJobs;
    private final Map<String, ZohoRecord> fetchedLeaveTypes;
    private final List<String> logs;
    private final List<Consumer<String>> logListeners;
    private final List<Consumer<String>> accessTokenListeners;

    private Settings settings;
    private OAuthToken token;
    private volatile boolean tokenActive;

    public Controller(TokenService tokenService, SettingsService settingsService, ZohoService zohoService) {
        this.tokenService = tokenService;
        this.settingsService = settingsService;
        this.zohoService = zohoService;
        this.fetchedJobs = new HashMap<>();
        this.fetchedLeaveTypes = new HashMap<>();
        this.logs = new ArrayList<>();
        this.logListeners = new ArrayList<>();
        this.accessTokenListeners = new ArrayList<>();

        Optional<Settings> optionalSettings = settingsService.loadSettings();
        if (optionalSettings.isPresent()) {
            addLog("Existing settings loaded");
            this.settings = optionalSettings.get();
        } else {
            addLog("Using default settings");
            this.settings = new Settings();
        }
        try {
            Optional<OAuthToken> authToken = tokenService.loadAuthToken();
            if (authToken.isPresent()) {
                addLog("Existing token detected");
                this.token = authToken.get();
                refreshToken();
            } else {
                this.token = new OAuthToken.Builder().clientID("").clientSecret("").grantToken("").refreshToken("").build();
                this.tokenActive = false;
            }
        } catch (SDKException sdkException) {
            sdkException.printStackTrace();
            this.tokenActive = false;
        }
    }

    public void addLog(String message) {
        String log = LocalTime.now().format(LOG_TIME_FORMATTER) + " - " + message;
        LOG.info(log);
        logs.add(log);
        logListeners.forEach(listener -> listener.accept(log));
    }

    public void addLogListener(Consumer<String> listener) {
        logListeners.add(listener);
    }

    public void addAccessTokenListener(Consumer<String> listener) {
        accessTokenListeners.add(listener);
    }

    public String getDisplayLogs() {
        StringJoiner stringJoiner = new StringJoiner("\n");
        logs.forEach(stringJoiner::add);
        return stringJoiner.toString();
    }

    public Settings getSettings() {
        return settings;
    }

    public OAuthToken getToken() {
        return token;
    }

    public boolean isTokenActive() {
        return tokenActive;
    }

    public void updateAccessToken(String clientId, String clientSecret, String grantToken, String refreshToken) {
        try {
            if (isEmpty(refreshToken)) {
                generateToken(clientId, clientSecret, grantToken);
            } else {
                if (!refreshToken.equals(token.getRefreshToken())) {
                    token.setClientId(clientId);
                    token.setClientSecret(clientSecret);
                    token.setRefreshToken(refreshToken);
                    refreshToken();
                }
                if (!tokenActive) {
                    generateToken(clientId, clientSecret, grantToken);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            addLog(e.getMessage());
            tokenActive = false;
        }
    }

    private void generateToken(String clientId, String clientSecret, String grantToken) throws ZohoException {
        token = zohoService.generateAuthToken(clientId, clientSecret, grantToken, ZOHO_DOMAIN);
        tokenActive = true;
        accessTokenListeners.forEach(listener -> listener.accept(token.getAccessToken()));
        addLog("Access token successfully generated");
    }

    private void refreshToken() {
        try {
            if (token != null && !isEmpty(token.getRefreshToken())) {
                zohoService.refreshAuthToken(token, ZOHO_DOMAIN);
                tokenActive = true;
                accessTokenListeners.forEach(listener -> listener.accept(token.getAccessToken()));
                addLog("Access token successfully refreshed");
            } else {
                tokenActive = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            addLog(e.getMessage());
            tokenActive = false;
        }
    }

    public void updateUserEmail(String userEmail) {
        token.setUserMail(userEmail);
    }

    public void saveSettings(String selectedJobId, String holidayJobId, Map<String, String> leaveToJobMap, int hoursForMonday, int hoursForTuesday, int hoursForWednesday, int hoursForThursday, int hoursForFriday, int hoursForSaturday, int hoursForSunday) {
        settings.setDefaultJobId(selectedJobId);
        settings.setHolidayJobId(holidayJobId);
        settings.setLeaveToJobMap(leaveToJobMap);
        settings.setHoursForMonday(hoursForMonday);
        settings.setHoursForTuesday(hoursForTuesday);
        settings.setHoursForWednesday(hoursForWednesday);
        settings.setHoursForThursday(hoursForThursday);
        settings.setHoursForFriday(hoursForFriday);
        settings.setHoursForSaturday(hoursForSaturday);
        settings.setHoursForSunday(hoursForSunday);
        try {
            settingsService.saveSettings(settings);
            addLog("Settings saved");
        } catch (IOException e) {
            e.printStackTrace();
            addLog("Error while saving settings");
        }
    }

    public boolean addTimeLogs(String userEmail, LocalDate fromDate, LocalDate toDate, boolean ignoreExistingTimeLogs) {
        try {
            if (isEmpty(settings.getDefaultJobId())) {
                addLog("Cannot add time logs without a selected job");
                return false;
            } else {
                token.setUserMail(userEmail);
                // Fetch user's holidays
                Set<LocalDate> holidays = zohoService.getHolidaysForUser(token, fromDate, toDate);
                if (!holidays.isEmpty()) {
                    int numberOfHolidays = holidays.size();
                    addLog("Found " + numberOfHolidays + " public/bank holiday" + (numberOfHolidays == 1 ? "" : "s"));
                }
                // Fetch user's applied leaves that are approved, for each leave within the specified period, fetch exact days that the leave has been applied for
                Map<LocalDate, HoursToLog> approvedLeaves = zohoService.getApprovedLeavesForUser(token, fromDate, toDate, settings);
                if (!approvedLeaves.isEmpty()) {
                    addLog("Found approved leaves within date range from " + fromDate + " to " + toDate);
                }
                // Fetch existing time logs for the specified period
                Map<LocalDate, HoursToLog> existingTimeLogs = ignoreExistingTimeLogs ? Collections.emptyMap() : zohoService.getExistingTimeLogsForUser(token, fromDate, toDate);
                if (!existingTimeLogs.isEmpty()) {
                    int numberOfDaysWithTimeLogs = existingTimeLogs.size();
                    addLog("Found existing time logs for " + numberOfDaysWithTimeLogs + " day" + (numberOfDaysWithTimeLogs == 1 ? "" : "s"));
                }

                Map<ZohoRecord, ZohoRecord> leaveToJobMap = settings.getLeaveToJobMap(fetchedLeaveTypes, fetchedJobs);
                for (LocalDate dateToAddTimeLogsFor : fromDate.datesUntil(toDate.plusDays(1)).collect(Collectors.toList())) {
                    // For each date in the specified period, find hours to log from settings
                    int hoursForDay = settings.getHoursForDay(dateToAddTimeLogsFor.getDayOfWeek());
                    HoursToLog timeLogsForDay = holidays.contains(dateToAddTimeLogsFor) ? new HolidayHoursToLog(fetchedJobs.get(settings.getHolidayJobId()), hoursForDay) : new WorkdayHoursToLog(fetchedJobs.get(settings.getDefaultJobId()), hoursForDay);
                    // Split hours to log to determine how much should be on the main job and each leave job, or holidays
                    if (approvedLeaves.containsKey(dateToAddTimeLogsFor)) {
                        timeLogsForDay.addApprovedLeaveHoursToLog(approvedLeaves.get(dateToAddTimeLogsFor), leaveToJobMap);
                    }
                    // Reduce hours to log according to existing time logs
                    if (existingTimeLogs.containsKey(dateToAddTimeLogsFor)) {
                        // Reduce holidays hours to log for all existing time logs if date is a holiday
                        // Reduce leave job hours to log for matching existing time logs, reduce main job hours to log for all other existing time logs
                        timeLogsForDay.reduceHoursToLog(existingTimeLogs.get(dateToAddTimeLogsFor));
                    }
                    // Add new time log for hours remaining, do not log if time remaining is 0 or less.
                    for (Map.Entry<ZohoRecord, Double> timeLogToAdd : timeLogsForDay.getJobIdToHours().entrySet()) {
                        Double hours = roundTo2DecimalPlaces(timeLogToAdd.getValue());
                        if (hours != 0) {
                            addLog("Adding time log for " + dateToAddTimeLogsFor + " with " + hours.toString() + " hours on " + timeLogToAdd.getKey());
                            zohoService.addTimeLog(token, dateToAddTimeLogsFor, timeLogToAdd.getKey().getId(), roundTo2DecimalPlaces(timeLogToAdd.getValue()).toString());
                        }
                    }
                }
                addLog("Finished creating time logs for dates from " + fromDate + " to " + toDate);
                return true;
            }
        } catch (Exception e) {
            // TODO : What to do when catching InvalidAuthTokenZohoException
            LOG.error("Error occurred while adding time logs", e);
            addLog("Error occurred");
            return false;
        }
    }

    private static Double roundTo2DecimalPlaces(double d) {
        return Math.round(d * 100.0) / 100.0;
    }

    public void saveToken(String userEmail) {
        try {
            if (tokenActive) {
                token.setUserMail(userEmail);
                tokenService.saveAuthToken(token);
            }
        } catch (Exception e) {
            e.printStackTrace();
            addLog(e.getMessage());
        }
    }

    public List<ZohoRecord> findJobs(String userEmail, boolean refreshToken) {
        try {
            fetchedJobs.clear();
            token.setUserMail(userEmail);
            List<ZohoRecord> jobs = zohoService.getJobsForUser(token);
            jobs.forEach(job -> {
                fetchedJobs.put(job.getId(), job);
            });
            int numberOfJobs = jobs.size();
            addLog("Found " + numberOfJobs + " job" + (numberOfJobs == 1 ? "" : "s") + " for " + userEmail);
            return jobs;
        } catch (InvalidAuthTokenZohoException invalidTokenException) {
            tokenActive = false;
            refreshToken();
            if (tokenActive && refreshToken) {
                return findJobs(userEmail, false);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOG.error("Error occurred while finding jobs", e);
            addLog("Failed to find jobs for " + userEmail + " (" + e.getMessage() + ")");
            return new ArrayList<>();
        }
    }

    public List<ZohoRecord> findLeaveTypes(String userEmail, boolean refreshToken) {
        try {
            fetchedLeaveTypes.clear();
            token.setUserMail(userEmail);
            List<ZohoRecord> leaveTypes = zohoService.getLeaveTypesForUser(token);
            leaveTypes.forEach(leaveType -> {
                fetchedLeaveTypes.put(leaveType.getId(), leaveType);
            });
            int numberOfLeaveTypes = leaveTypes.size();
            addLog("Found " + numberOfLeaveTypes + " leave type" + (numberOfLeaveTypes == 1 ? "" : "s") + " for " + userEmail);
            return leaveTypes;
        } catch (InvalidAuthTokenZohoException invalidTokenException) {
            tokenActive = false;
            refreshToken();
            if (tokenActive && refreshToken) {
                return findLeaveTypes(userEmail, false);
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            LOG.error("Error occurred while finding leave types", e);
            addLog("Failed to find leave types for " + userEmail + " (" + e.getMessage() + ")");
            return new ArrayList<>();
        }
    }

}
