package chan.tinpui.timesheet.zoho.domain;

import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HolidayHoursToLog implements HoursToLog {

    private Record leaveJobId;
    private double jobHours;

    public HolidayHoursToLog(Record leaveJobId, double jobHours) {
        this.leaveJobId = leaveJobId;
        this.jobHours = jobHours;
    }

    @Override
    public Map<Record, Double> getJobIdToHours() {
        if (leaveJobId != null && !isEmpty(leaveJobId.getId()) && jobHours > 0) {
            return Collections.singletonMap(leaveJobId, jobHours);
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public void addLeaveJobHoursToLog(Record leaveJobId, double hoursToAdd) {

    }

    @Override
    public void reduceHoursToLog(Record jobId, double hoursToReduce) {
        jobHours -= hoursToReduce;
    }
}
