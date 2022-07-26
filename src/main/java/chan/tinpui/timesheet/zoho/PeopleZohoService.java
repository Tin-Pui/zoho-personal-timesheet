package chan.tinpui.timesheet.zoho;

import chan.tinpui.timesheet.exception.ZohoException;
import chan.tinpui.timesheet.zoho.domain.*;
import com.zoho.api.authenticator.OAuthToken;
import com.zoho.crm.api.exception.SDKException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PeopleZohoService extends AbstractZohoService {

    private static final Logger LOG = LoggerFactory.getLogger(PeopleZohoService.class);
    private static final String ACCESS_TOKEN_URL = "/oauth/v2/token";
    private static final String LOCAL_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT_PATTERN);
    private static final DateTimeFormatter LEAVE_FORM_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-LLL-yyyy");

    private final RestTemplate restTemplate;

    public PeopleZohoService() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    private JSONObject parseAuthResponse(ResponseEntity<String> responseEntity) throws ZohoException {
        if (responseEntity.hasBody()) {
            LOG.info(responseEntity.getBody());
            JSONObject response = new JSONObject(responseEntity.getBody());
            if (response.has("error")) {
                throw new ZohoException(response.getString("error"));
            }
            return response;
        } else {
            throw new ZohoException("Zoho response did not have a body");
        }
    }

    @Override
    public OAuthToken generateAuthToken(String clientId, String clientSecret, String grantTokenCode, ZohoDomain domain) throws ZohoException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", grantTokenCode);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        String url = domain.accountsUrl + ACCESS_TOKEN_URL;
        LOG.info("POST: " + url);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        try {
            JSONObject response = parseAuthResponse(responseEntity);
            OAuthToken token = new OAuthToken.Builder().clientID(clientId).clientSecret(clientSecret).grantToken(grantTokenCode).build();
            if (!response.has("access_token") || !response.has("refresh_token")) {
                throw new ZohoException("Zoho response did not have the token");
            }
            token.setExpiresIn("" + response.getBigDecimal("expires_in"));
            token.setAccessToken(response.getString("access_token"));
            token.setRefreshToken(response.getString("refresh_token"));
            return token;
        } catch (SDKException sdkException) {
            throw new ZohoException(sdkException);
        }
    }

    @Override
    public OAuthToken refreshAuthToken(OAuthToken authToken, ZohoDomain domain) throws ZohoException {
        String url = domain.accountsUrl + ACCESS_TOKEN_URL + "?refresh_token=" + authToken.getRefreshToken() + "&client_id=" + authToken.getClientId() + "&client_secret=" + authToken.getClientSecret() + "&grant_type=refresh_token";
        LOG.info("POST: " + url);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, null, String.class);
        JSONObject response = parseAuthResponse(responseEntity);
        if (!response.has("access_token")) {
            throw new ZohoException("Zoho response did not have the token");
        }
        authToken.setExpiresIn("" + response.getBigDecimal("expires_in"));
        authToken.setAccessToken(response.getString("access_token"));
        return authToken;
    }

    @Override
    public List<Record> getJobsForUser(OAuthToken authToken) throws ZohoException {
        String url = "https://people.zoho.com/people/api/timetracker/getjobs?assignedTo=" + authToken.getUserMail();
        JSONArray response = extractResultFrom(getRequest(url, authToken));
        Set<Record> jobs = new TreeSet<>(Comparator.comparing((Record::toString)));
        response.forEach(object -> {
            JSONObject job = (JSONObject) object;
            if (job.has("jobId") && job.has("clientName") && job.has("projectName") && job.has("jobName")) {
                jobs.add(new Record(job.getString("jobId"), job.getString("clientName") + "; " + job.getString("projectName") + "; " + job.getString("jobName")));
            }
        });
        return new ArrayList<>(jobs);
    }

    @Override
    public List<Record> getLeaveTypesForUser(OAuthToken authToken) throws ZohoException {
        String url = "https://people.zoho.com/people/api/leave/getLeaveTypeDetails?userId=" + authToken.getUserMail();
        JSONArray response = extractResultFrom(getRequest(url, authToken));
        List<Record> leaveTypes = new ArrayList<>();
        response.forEach(object -> {
            JSONObject leaveType = (JSONObject) object;
            leaveTypes.add(new Record(leaveType.getString("Id"), leaveType.getString("Name")));
        });
        return leaveTypes;
    }

    @Override
    public Map<LocalDate, HoursToLog> getApprovedLeavesForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate, Settings settings) throws ZohoException {
        String url = "https://people.zoho.com/people/api/forms/P_ApplyLeave/getRecords?searchColumn=EMPLOYEEMAILALIAS&searchValue=" + authToken.getUserMail();
        JSONArray response = extractResultFrom(getRequest(url, authToken));
        List<String> leaveFormIdsToCheck = new ArrayList<>();
        response.forEach(object -> {
            JSONObject jsonObject = (JSONObject) object;
            jsonObject.keys().forEachRemaining(leaveFormId -> {
                JSONArray jsonArray = jsonObject.getJSONArray(leaveFormId);
                jsonArray.forEach(formObject -> {
                    JSONObject formEntry = (JSONObject) formObject;
                    String approvalStatus = formEntry.getString("ApprovalStatus");
                    LocalDate formFrom = LocalDate.parse(formEntry.getString("From"), LEAVE_FORM_DATE_FORMATTER);
                    LocalDate formTo = LocalDate.parse(formEntry.getString("To"), LEAVE_FORM_DATE_FORMATTER);
                    if ("Approved".equals(approvalStatus) && fromDate.compareTo(formTo) <= 0 && toDate.compareTo(formFrom) >= 0) {
                        leaveFormIdsToCheck.add(leaveFormId);
                    }
                });
            });
        });

        Map<LocalDate, HoursToLog> approvedLeavesForUser = new HashMap<>();
        for (String leaveFormId : leaveFormIdsToCheck) {
            String leaveFormUrl = "https://people.zoho.com/people/api/forms/P_ApplyLeave/getDataByID?recordId=" + leaveFormId;
            JSONArray leaveFormResults = extractResultFrom(getRequest(leaveFormUrl, authToken));
            leaveFormResults.forEach(result -> {
                JSONObject jsonObject = (JSONObject) result;
                Record leaveType = new Record(jsonObject.getString("Leavetype.ID"), jsonObject.getString("Leavetype"));
                JSONObject dayDetails = jsonObject.getJSONObject("DayDetails");
                dayDetails.keys().forEachRemaining(date -> {
                    double leaveCount = Double.parseDouble(dayDetails.getJSONObject(date).getString("LeaveCount"));
                    LocalDate localDate = LocalDate.parse(date, LEAVE_FORM_DATE_FORMATTER);
                    LOG.info("Found approved leave for " + localDate + " with count " + leaveCount);
                    int hoursInDay = settings.getHoursForDay(localDate.getDayOfWeek());
                    approvedLeavesForUser.computeIfAbsent(localDate, d -> new WorkdayHoursToLog(new Record("", ""), 0))
                            .addLeaveJobHoursToLog(leaveType, hoursInDay * leaveCount);
                });
            });
        }
        return approvedLeavesForUser;
    }

    @Override
    public Map<LocalDate, HoursToLog> getExistingTimeLogsForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate) throws ZohoException {
        String url = "http://people.zoho.com/people/api/timetracker/gettimelogs?user=" + authToken.getUserMail() +
                "&fromDate=" + fromDate.format(LOCAL_DATE_FORMATTER) +
                "&toDate=" + toDate.format(LOCAL_DATE_FORMATTER) +
                "&dateFormat=" + LOCAL_DATE_FORMAT_PATTERN;
        JSONArray response = extractResultFrom(getRequest(url, authToken));
        Map<LocalDate, HoursToLog> existingTimeLogsForUser = new HashMap<>();
        response.forEach(result -> {
            JSONObject timeLog = (JSONObject) result;
            double hours = timeLog.getInt("hoursInMins") / 60.0;
            LocalDate workDate = LocalDate.parse(timeLog.getString("workDate"), LOCAL_DATE_FORMATTER);
            Record timeLogJob = new Record(timeLog.getString("jobId"), timeLog.getString("projectName") + "; " + timeLog.getString("jobName"));
            existingTimeLogsForUser.computeIfAbsent(workDate, localDate -> new WorkdayHoursToLog(new Record("", ""), 0))
                    .addLeaveJobHoursToLog(timeLogJob, hours);
        });
        return existingTimeLogsForUser;
    }

    @Override
    public Set<LocalDate> getHolidaysForUser(OAuthToken authToken, LocalDate fromDate, LocalDate toDate) throws ZohoException {
        LOG.info("Fetching from " + fromDate + " to " + toDate);
        String url = "https://people.zoho.com/people/api/leave/v2/holidays/get?employee=" + authToken.getUserMail() +
                "&from=" + fromDate.format(LOCAL_DATE_FORMATTER) +
                "&to=" + toDate.format(LOCAL_DATE_FORMATTER) +
                "&dateFormat=" + LOCAL_DATE_FORMAT_PATTERN;
        JSONArray response = new JSONObject(getRequest(url, authToken)).getJSONArray("data");
        Set<LocalDate> holidays = new HashSet<>();
        response.forEach(object -> {
            JSONObject holiday = (JSONObject) object;
            holidays.add(LocalDate.parse(holiday.getString("Date"), LOCAL_DATE_FORMATTER));
        });
        return holidays;
    }

    @Override
    public void addTimeLog(OAuthToken authToken, LocalDate workDate, String jobId, String hours) throws ZohoException {
        String url = "https://people.zoho.com/people/api/timetracker/addtimelog?user=" + authToken.getUserMail() +
                "&workDate=" + workDate.format(LOCAL_DATE_FORMATTER) +
                "&jobId=" + jobId +
                "&hours=" + hours +
                "&dateFormat=" + LOCAL_DATE_FORMAT_PATTERN;
        LOG.info("POST: " + url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + authToken.getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        LOG.info(responseEntity.getBody());
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new ZohoException(responseEntity.getBody());
        }
    }
}