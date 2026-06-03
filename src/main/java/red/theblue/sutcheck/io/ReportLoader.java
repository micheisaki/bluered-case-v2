package red.theblue.sutcheck.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import red.theblue.sutcheck.model.Report;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** JSON dosyasini Report nesnesine cevirir (Faz 1 girdi katmani). */
public class ReportLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Report fromFile(String path) throws IOException {
        try (Reader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, Report.class);
        }
    }
}
