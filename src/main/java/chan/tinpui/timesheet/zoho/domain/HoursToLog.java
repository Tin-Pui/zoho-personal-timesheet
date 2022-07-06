package chan.tinpui.timesheet.zoho.domain;

import java.util.Map;

public interface HoursToLog {
    Map<Record, Double> getJobIdToHours();
    void addLeaveJobHoursToLog(Record leaveJobId, double hoursToAdd);
    void reduceHoursToLog(Record jobId, double hoursToReduce);

    default void addApprovedLeaveHoursToLog(HoursToLog approvedLeavesForDay, Map<Record, Record> leaveToJobMap) {
        for (Map.Entry<Record, Double> approvedLeave : approvedLeavesForDay.getJobIdToHours().entrySet()) {
            Record leaveTypeId = approvedLeave.getKey();
            Record leaveJobId = leaveToJobMap.get(leaveTypeId);
            double leaveHours = approvedLeave.getValue();
            if (leaveJobId == null || Record.IGNORE_RECORD_ID.equals(leaveJobId.getId())) {
                reduceHoursToLog(leaveJobId, leaveHours);
            } else {
                addLeaveJobHoursToLog(leaveJobId, leaveHours);
            }
        }
    }

    default void reduceHoursToLog(HoursToLog existingTimeLogs) {
        for (Map.Entry<Record, Double> existingTimeLog : existingTimeLogs.getJobIdToHours().entrySet()) {
            reduceHoursToLog(existingTimeLog.getKey(), existingTimeLog.getValue());
        }
    }
}
