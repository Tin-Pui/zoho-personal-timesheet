package chan.tinpui.timesheet.zoho;

import chan.tinpui.timesheet.exception.ZohoException;
import chan.tinpui.timesheet.zoho.domain.*;
import com.zoho.api.authenticator.OAuthToken;
import com.zoho.crm.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class DummyZohoService implements ZohoService {

    private static final Logger LOG = LoggerFactory.getLogger(DummyZohoService.class);
    private static final Random RANDOM = new Random();

    @Override
    public OAuthToken generateAuthToken(String clientId, String clientSecret, String grantTokenCode, ZohoDomain domain) throws ZohoException {
        try {
            simulateResponseTime(400);
            OAuthToken token = new OAuthToken.Builder().clientID(clientId).clientSecret(clientSecret).grantToken(grantTokenCode).build();
            token.setAccessToken(generateRandomString(20));
            token.setRefreshToken(generateRandomString(20));
            return token;
        } catch (SDKException sdkException) {
            throw new ZohoException(sdkException);
        }
    }

    @Override
    public OAuthToken refreshAuthToken(OAuthToken authToken, ZohoDomain domain) {
        simulateResponseTime(400);
        authToken.setAccessToken(generateRandomString(20));
        return authToken;
    }

    @Override
    public List<Record> getJobsForUser(OAuthToken authToken) throws ZohoException {
        checkEmail(authToken);
        simulateResponseTime(500);
        List<Record> jobs = new ArrayList<>();
        jobs.add(new Record("100001", "Internal Annual Leave"));
        jobs.add(new Record("100002", "Other Leave"));
        jobs.add(new Record("100003", "Public/Bank Holiday"));
        jobs.add(new Record("100004", "Day Client Work"));
        jobs.add(new Record("100005", "Night Client Work"));
        jobs.add(new Record("100006", "Weekend Client Work"));
        return jobs;
    }

    @Override
    public List<Record> getLeaveTypesForUser(OAuthToken authToken) throws ZohoException {
        checkEmail(authToken);
        simulateResponseTime(500);
        List<Record> leaveTypes = new ArrayList<>();
        leaveTypes.add(new Record("200001", "Sick Day"));
        leaveTypes.add(new Record("200002", "Annual Leave"));
        leaveTypes.add(new Record("200003", "Compensatory Off"));
        leaveTypes.add(new Record("200004", "Other Leave"));
        return leaveTypes;
    }

    @Override
    public Map<LocalDate, HoursToLog> getApprovedLeavesForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate, Settings settings) throws ZohoException {
        checkEmail(authToken);
        simulateResponseTime(100);
        Map<LocalDate, HoursToLog> approvedLeavesForUser = new HashMap<>();
        LocalDate localDate = fromDate.withDayOfMonth(15).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        Record leaveType = new Record("200002", "Annual Leave");
        int hoursInDay = settings.getHoursForDay(localDate.getDayOfWeek());
        approvedLeavesForUser.computeIfAbsent(localDate, d -> new WorkdayHoursToLog(new Record("", ""), 0))
                .addLeaveJobHoursToLog(leaveType, hoursInDay);
        return approvedLeavesForUser;
    }

    @Override
    public Map<LocalDate, HoursToLog> getExistingTimeLogsForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate) throws ZohoException {
        checkEmail(authToken);
        simulateResponseTime(100);
        return new HashMap<>();
    }

    @Override
    public Set<LocalDate> getHolidaysForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate) throws ZohoException {
        checkEmail(authToken);
        simulateResponseTime(100);
        return Collections.singleton( fromDate.with(TemporalAdjusters.lastDayOfMonth()).with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY)));
    }

    @Override
    public void addTimeLog(OAuthToken authToken, LocalDate workDate, String jobId, String hours) throws ZohoException {
        checkEmail(authToken);
        simulateResponseTime(300);
        LOG.info("Adding time log for " + jobId + " with " + hours + " hours.");
    }

    private void simulateResponseTime(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkEmail(OAuthToken authToken) throws ZohoException {
        if (!authToken.getUserMail().contains("@") || authToken.getUserMail().equals("login.example@email.com")) {
            throw new ZohoException("Invalid email");
        }
    }

    private String generateRandomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        return RANDOM.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
