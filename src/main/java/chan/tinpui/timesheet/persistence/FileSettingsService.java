package chan.tinpui.timesheet.persistence;

import chan.tinpui.timesheet.zoho.domain.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

public class FileSettingsService implements SettingsService {

    private final String fileStorePath;

    public FileSettingsService(String fileStorePath) throws IOException {
        if (fileStorePath.contains(File.separator)) {
            Files.createDirectories(new File(fileStorePath.substring(0, fileStorePath.lastIndexOf(File.separatorChar))).toPath());
        }
        this.fileStorePath = fileStorePath;
    }

    @Override
    public Optional<Settings> loadSettings() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileStorePath))) {
            return Optional.of(new Settings(bufferedReader.readLine()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void saveSettings(Settings settings) throws IOException {
        Path filePath = Paths.get(fileStorePath);
        Files.write(filePath, Collections.singleton(settings.toString()), StandardCharsets.UTF_8);
    }
}
