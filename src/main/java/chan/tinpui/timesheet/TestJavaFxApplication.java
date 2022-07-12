package chan.tinpui.timesheet;

import chan.tinpui.timesheet.controller.Controller;
import chan.tinpui.timesheet.persistence.FileSettingsService;
import chan.tinpui.timesheet.persistence.FileTokenService;
import chan.tinpui.timesheet.persistence.SettingsService;
import chan.tinpui.timesheet.persistence.TokenService;
import chan.tinpui.timesheet.zoho.DummyZohoService;
import chan.tinpui.timesheet.zoho.ZohoService;
import javafx.application.Application;


public class TestJavaFxApplication extends JavaFxApplication {

    public static void main(String[] args) {
        Application.launch(TestJavaFxApplication.class, args);
    }

    @Override
    protected Controller initController() throws Exception {
        TokenService tokenService = new FileTokenService(APP_DIRECTORY + "test_zoho_token.csv");
        SettingsService settingsService = new FileSettingsService(APP_DIRECTORY + "test_settings.json");
        ZohoService zohoService = new DummyZohoService();
        return new Controller(tokenService, settingsService, zohoService);
    }
}
