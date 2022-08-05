package chan.tinpui.timesheet.zoho;

import chan.tinpui.timesheet.exception.ZohoException;
import chan.tinpui.timesheet.zoho.domain.HoursToLog;
import chan.tinpui.timesheet.zoho.domain.ZohoRecord;
import chan.tinpui.timesheet.zoho.domain.Settings;
import chan.tinpui.timesheet.zoho.domain.ZohoDomain;
import com.zoho.api.authenticator.OAuthToken;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ZohoService {
    OAuthToken generateAuthToken(String clientId, String clientSecret, String grantTokenCode, ZohoDomain domain) throws ZohoException;
    OAuthToken refreshAuthToken(OAuthToken authToken, ZohoDomain domain) throws ZohoException;
    List<ZohoRecord> getJobsForUser(OAuthToken authToken) throws ZohoException;
    List<ZohoRecord> getLeaveTypesForUser(OAuthToken authToken) throws ZohoException;
    Map<LocalDate, HoursToLog> getApprovedLeavesForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate, Settings settings) throws ZohoException;
    Map<LocalDate, HoursToLog> getExistingTimeLogsForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate) throws ZohoException;
    Set<LocalDate> getHolidaysForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate) throws ZohoException;
    void addTimeLog(OAuthToken authToken, LocalDate workDate, String jobId, String hours) throws ZohoException;

}