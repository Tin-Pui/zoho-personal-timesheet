package chan.tinpui.timesheet.zoho.domain;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class WorkdayHoursToLog implements HoursToLog {

    private Record mainJobId;
    private double jobHours;
    private Map<Record, Double> leaveJobToHours;

    public WorkdayHoursToLog(Record mainJobId, double totalHours) {
        this.mainJobId = mainJobId;
        this.jobHours = totalHours;
        this.leaveJobToHours = new HashMap<>();
    }

    @Override
    public Map<Record, Double> getJobIdToHours() {
        Map<Record, Double> jobIdToHours = new HashMap<>();
        if (mainJobId != null && !isEmpty(mainJobId.getId()) && jobHours > 0) {
            jobIdToHours.put(mainJobId, jobHours);
        }
        jobIdToHours.putAll(leaveJobToHours);
        return jobIdToHours;
    }

    @Override
    public void addLeaveJobHoursToLog(Record leaveJobId, double hoursToAdd) {
        if (!mainJobId.equals(leaveJobId)) {
            leaveJobToHours.merge(leaveJobId, hoursToAdd, Double::sum);
            jobHours -= hoursToAdd;
        }
    }

    @Override
    public void reduceHoursToLog(Record jobId, double hoursToReduce) {
        if (jobId != null && leaveJobToHours.containsKey(jobId) && !mainJobId.equals(jobId)) {
            double hoursRemainingToLog = leaveJobToHours.get(jobId);
            if (hoursToReduce >= hoursRemainingToLog) {
                leaveJobToHours.remove(jobId);
                jobHours -= (hoursToReduce - hoursRemainingToLog);
            } else {
                leaveJobToHours.put(jobId, hoursRemainingToLog - hoursToReduce);
            }
        } else {
            jobHours -= hoursToReduce;
        }
    }
}
