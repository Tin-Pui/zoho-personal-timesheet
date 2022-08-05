package chan.tinpui.timesheet.zoho.domain;

import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HolidayHoursToLog implements HoursToLog {

    private ZohoRecord leaveJobId;
    private double jobHours;

    public HolidayHoursToLog(ZohoRecord leaveJobId, double jobHours) {
        this.leaveJobId = leaveJobId;
        this.jobHours = jobHours;
    }

    @Override
    public Map<ZohoRecord, Double> getJobIdToHours() {
        if (leaveJobId != null && !isEmpty(leaveJobId.getId()) && jobHours > 0) {
            return Collections.singletonMap(leaveJobId, jobHours);
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public void addLeaveJobHoursToLog(ZohoRecord leaveJobId, double hoursToAdd) {

    }

    @Override
    public void reduceHoursToLog(ZohoRecord jobId, double hoursToReduce) {
        jobHours -= hoursToReduce;
    }
}
