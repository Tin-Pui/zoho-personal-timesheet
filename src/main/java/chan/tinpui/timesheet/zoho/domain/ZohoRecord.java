package chan.tinpui.timesheet.zoho.domain;

import java.util.Objects;

public class ZohoRecord {
    public static final String IGNORE_RECORD_ID = "";
    private String id;
    private String description;

    public ZohoRecord(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZohoRecord zohoRecord = (ZohoRecord) o;
        return Objects.equals(id, zohoRecord.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
