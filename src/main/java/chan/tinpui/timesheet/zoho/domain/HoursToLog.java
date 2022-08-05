package chan.tinpui.timesheet.zoho.domain;

import java.util.Map;

public interface HoursToLog {
    Map<ZohoRecord, Double> getJobIdToHours();
    void addLeaveJobHoursToLog(ZohoRecord leaveJobId, double hoursToAdd);
    void reduceHoursToLog(ZohoRecord jobId, double hoursToReduce);

    default void addApprovedLeaveHoursToLog(HoursToLog approvedLeavesForDay, Map<ZohoRecord, ZohoRecord> leaveToJobMap) {
        for (Map.Entry<ZohoRecord, Double> approvedLeave : approvedLeavesForDay.getJobIdToHours().entrySet()) {
            ZohoRecord leaveTypeId = approvedLeave.getKey();
            ZohoRecord leaveJobId = leaveToJobMap.get(leaveTypeId);
            double leaveHours = approvedLeave.getValue();
            if (leaveJobId == null || ZohoRecord.IGNORE_RECORD_ID.equals(leaveJobId.getId())) {
                reduceHoursToLog(leaveJobId, leaveHours);
            } else {
                addLeaveJobHoursToLog(leaveJobId, leaveHours);
            }
        }
    }

    default void reduceHoursToLog(HoursToLog existingTimeLogs) {
        for (Map.Entry<ZohoRecord, Double> existingTimeLog : existingTimeLogs.getJobIdToHours().entrySet()) {
            reduceHoursToLog(existingTimeLog.getKey(), existingTimeLog.getValue());
        }
    }
}
