package chan.tinpui.timesheet.persistence;

import chan.tinpui.timesheet.zoho.domain.Settings;

import java.io.IOException;
import java.util.Optional;

public interface SettingsService {
    Optional<Settings> loadSettings();
    void saveSettings(Settings settings) throws IOException;
}
