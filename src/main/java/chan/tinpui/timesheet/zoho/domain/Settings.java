package chan.tinpui.timesheet.zoho.domain;

import org.json.JSONObject;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    
    private final JSONObject jsonObject;
    private static final int MINIMUM_HOURS = 0;
    private static final int MAXIMUM_HOURS = 16;
    
    public Settings() {
        this("{\"hoursForFriday\":8,\"defaultJobId\":\"\",\"leaveToJobMap\":{\"322556000000082013\":\"322556000000129615\",\"322556000011086199\":\"322556000000129615\",\"322556000000103001\":\"322556000000210267\",\"322556000000122013\":\"322556000003808247\",\"322556000000082152\":\"322556000000129615\",\"322556000006568001\":\"322556000006647300\",\"322556000011086217\":\"322556000000129615\"},\"hoursForThursday\":8,\"holidayJobId\":\"322556000001506017\",\"hoursForTuesday\":8,\"hoursForWednesday\":8,\"hoursForMonday\":8,\"hoursForSaturday\":0,\"hoursForSunday\":0}");
    }

    public Settings(String data) {
        Settings proxySettings = new Settings(new JSONObject(data));
        this.jsonObject = new JSONObject();
        setDefaultJobId(proxySettings.getDefaultJobId());
        setHolidayJobId(proxySettings.getHolidayJobId());
        setLeaveToJobMap(proxySettings.getLeaveToJobMap());
        setHoursForMonday(proxySettings.getHoursForMonday());
        setHoursForTuesday(proxySettings.getHoursForTuesday());
        setHoursForWednesday(proxySettings.getHoursForWednesday());
        setHoursForThursday(proxySettings.getHoursForThursday());
        setHoursForFriday(proxySettings.getHoursForFriday());
        setHoursForSaturday(proxySettings.getHoursForSaturday());
        setHoursForSunday(proxySettings.getHoursForSunday());
    }

    private Settings(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    public String getDefaultJobId() {
        return jsonObject.getString("defaultJobId");
    }

    public void setDefaultJobId(String defaultJobId) {
        jsonObject.put("defaultJobId", defaultJobId);
    }

    public String getHolidayJobId() {
        return jsonObject.getString("holidayJobId");
    }

    public void setHolidayJobId(String defaultJobId) {
        jsonObject.put("holidayJobId", defaultJobId);
    }
    
    public Map<String, String> getLeaveToJobMap() {
        JSONObject jsonMap = jsonObject.getJSONObject("leaveToJobMap");
        Map<String, String> leaveToJobMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonMap.toMap().entrySet()) {
            leaveToJobMap.put(entry.getKey(), entry.getValue().toString());
        }
        return leaveToJobMap;
    }

    public Map<Record, Record> getLeaveToJobMap(Map<String, Record> fetchedLeaveTypes, Map<String, Record> fetchedJobs) {
        JSONObject jsonMap = jsonObject.getJSONObject("leaveToJobMap");
        Map<Record, Record> leaveToJobMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonMap.toMap().entrySet()) {
            leaveToJobMap.put(fetchedLeaveTypes.get(entry.getKey()), fetchedJobs.get(entry.getValue().toString()));
        }
        return leaveToJobMap;
    }

    public void setLeaveToJobMap(Map<String, String> leaveToJobMap) {
        jsonObject.put("leaveToJobMap", leaveToJobMap);
    }

    public int getHoursForDay(DayOfWeek dayOfWeek) {
        switch(dayOfWeek) {
            case MONDAY: return getHoursForMonday();
            case TUESDAY: return getHoursForTuesday();
            case WEDNESDAY: return getHoursForWednesday();
            case THURSDAY: return getHoursForThursday();
            case FRIDAY: return getHoursForFriday();
            case SATURDAY: return getHoursForSaturday();
            case SUNDAY: return getHoursForSunday();
            default: return 0;
        }
    }

    public void setHoursForDay(DayOfWeek dayOfWeek, int hours) {
        switch(dayOfWeek) {
            case MONDAY: setHoursForMonday(hours); break;
            case TUESDAY: setHoursForTuesday(hours); break;
            case WEDNESDAY: setHoursForWednesday(hours); break;
            case THURSDAY: setHoursForThursday(hours); break;
            case FRIDAY: setHoursForFriday(hours); break;
            case SATURDAY: setHoursForSaturday(hours); break;
            case SUNDAY: setHoursForSunday(hours); break;
        }
    }
    
    public int getHoursForMonday() {
        return jsonObject.getInt("hoursForMonday");
    }
    
    public void setHoursForMonday(int hours) {
        jsonObject.put("hoursForMonday", validHours(hours));
    }

    public int getHoursForTuesday() {
        return jsonObject.getInt("hoursForTuesday");
    }

    public void setHoursForTuesday(int hours) {
        jsonObject.put("hoursForTuesday", validHours(hours));
    }

    public int getHoursForWednesday() {
        return jsonObject.getInt("hoursForWednesday");
    }

    public void setHoursForWednesday(int hours) {
        jsonObject.put("hoursForWednesday", validHours(hours));
    }

    public int getHoursForThursday() {
        return jsonObject.getInt("hoursForThursday");
    }

    public void setHoursForThursday(int hours) {
        jsonObject.put("hoursForThursday", validHours(hours));
    }

    public int getHoursForFriday() {
        return jsonObject.getInt("hoursForFriday");
    }

    public void setHoursForFriday(int hours) {
        jsonObject.put("hoursForFriday", validHours(hours));
    }

    public int getHoursForSaturday() {
        return jsonObject.getInt("hoursForSaturday");
    }

    public void setHoursForSaturday(int hours) {
        jsonObject.put("hoursForSaturday", validHours(hours));
    }

    public int getHoursForSunday() {
        return jsonObject.getInt("hoursForSunday");
    }

    public void setHoursForSunday(int hours) {
        jsonObject.put("hoursForSunday", validHours(hours));
    }

    private int validHours(int hours) {
        return Math.max(Math.min(hours, MAXIMUM_HOURS), MINIMUM_HOURS);
    }
}
