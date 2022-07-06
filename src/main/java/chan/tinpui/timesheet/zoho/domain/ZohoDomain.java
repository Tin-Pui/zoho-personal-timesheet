package chan.tinpui.timesheet.zoho.domain;

public enum ZohoDomain {
    US("https://accounts.zoho.com"),
    AU("https://accounts.zoho.com.au"),
    EU("https://accounts.zoho.eu"),
    IN("https://accounts.zoho.in"),
    CN("https://accounts.zoho.com.cn"),
    JP("https://accounts.zoho.jp");

    public final String accountsUrl;

    ZohoDomain(String accountsUrl) {
        this.accountsUrl = accountsUrl;
    }
}