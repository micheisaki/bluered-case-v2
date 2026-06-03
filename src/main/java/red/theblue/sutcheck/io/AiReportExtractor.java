package red.theblue.sutcheck.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import red.theblue.sutcheck.model.Report;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * FAZ 2 - AI extraction katmani.
 * Bir rapor GORUNTUSUNU (png/jpg) alir, Google Gemini'nin vision modeline
 * gonderir ve donen yapilandirilmis JSON'u {@link Report} nesnesine cevirir.
 *
 * Onemli: Mimari degismez. Kural motoru hala yalnizca Report nesnesine bakar;
 * burada sadece "girdi kaynagi" JSON dosyasi yerine bir goruntu + AI olur.
 *
 * API anahtari koda yazilmaz; GEMINI_API_KEY ortam degiskeninden okunur.
 */
public class AiReportExtractor {

    private static final String MODEL = "gemini-2.5-flash";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private static final Gson GSON = new GsonBuilder().create();
    private final HttpClient http = HttpClient.newHttpClient();

    /** Gemini'den, raporu bizim Report semasina gore JSON dondurmesini isteyen yonerge. */
    private static final String PROMPT = String.join("\n",
        "Sen bir SGK saglik raporu okuma asistanisin. Verilen rapor goruntusunu incele",
        "ve ASAGIDAKI JSON semasina BIREBIR uyan, yalnizca JSON dondur (aciklama yazma):",
        "{",
        "  \"patientName\": string, \"tckn\": string, \"gender\": \"M|F\", \"birthDate\": \"yyyy-MM-dd\",",
        "  \"reportNo\": string, \"reportDate\": \"yyyy-MM-dd\", \"validFrom\": \"yyyy-MM-dd\", \"validTo\": \"yyyy-MM-dd\",",
        "  \"doctorSpecialty\": one of [ENDOKRINOLOJI, IC_HASTALIKLARI, KARDIYOLOJI, KALP_DAMAR_CERRAHISI, NOROLOJI, COCUK_SAGLIGI, AILE_HEKIMI, ENFEKSIYON_HASTALIKLARI, GOGUS_HASTALIKLARI, DIGER],",
        "  \"explanation\": string (raporun Aciklamalar metni),",
        "  \"diagnoses\": [ { \"code\": string (orn. Z94.0, E11.9), \"description\": string } ],",
        "  \"labValues\": { \"trigliserit\": number?, \"hba1c\": number?, \"aclikKanSekeri\": number? },",
        "  \"diabetesMellitus\": boolean, \"oralAntidiabeticOnly\": boolean, \"insulinUser\": boolean,",
        "  \"metforminSulfonylureaFailed\": boolean, \"transplant\": boolean, \"pregnant\": boolean,",
        "  \"items\": [ { \"code\": string, \"name\": string, \"type\": \"DRUG|SUPPLY\",",
        "               \"drugClass\": one of [SGLT2, DPP4, NON_STATIN_LIPID, ANTIVIRAL, OTHER],",
        "               \"form\": string, \"dailyDose\": string, \"stripsPerDay\": number? } ]",
        "}",
        "Cikarim kurallari:",
        "- drugClass: ad ...gliflozin -> SGLT2; ...gliptin -> DPP4; fenofibrat/gemfibrozil -> NON_STATIN_LIPID;",
        "  valgansiklovir/gansiklovir -> ANTIVIRAL; digerleri -> OTHER.",
        "- type: seker olcum cubugu gibi malzemeler SUPPLY, ilaclar DRUG.",
        "- diabetesMellitus: E10-E14 tanisi varsa true. transplant: Z94 tanisi varsa true.",
        "- insulinUser: kalemlerde insulin varsa true. oralAntidiabeticOnly: yalnizca agizdan",
        "  antidiyabetik kullaniyorsa (insulin yoksa) true.",
        "- metforminSulfonylureaFailed: aciklamada 'metformin ve/veya sulfonilure ... maksimum dozda",
        "  yeterli glisemik kontrol saglanamamis' benzeri ifade varsa true.",
        "- stripsPerDay: seker cubugu icin gunluk adet (orn. 'Gunde 4x1' -> 4).",
        "- Bir alani goruntude bulamazsan: metinlerde null/bos, boolean'larda false, listelerde [] kullan.");

    /**
     * Goruntu dosyasini AI ile cozumleyip Report dondurur.
     * @param imagePath png/jpg rapor goruntusu yolu
     */
    public Report fromImage(String imagePath) throws IOException, InterruptedException {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "GEMINI_API_KEY ortam degiskeni tanimli degil. Lutfen anahtarinizi ayarlayin.");
        }

        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mime = imagePath.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";

        String requestBody = buildRequestBody(base64, mime);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Gemini API hatasi (" + response.statusCode() + "): " + response.body());
        }

        String json = extractJsonFromResponse(response.body());
        return GSON.fromJson(json, Report.class);
    }

    /** Gemini generateContent istek govdesini (gorsel + prompt) olusturur. */
    private String buildRequestBody(String base64Image, String mime) {
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", mime);
        inlineData.addProperty("data", base64Image);

        JsonObject imgPart = new JsonObject();
        imgPart.add("inline_data", inlineData);

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", PROMPT);

        JsonArray parts = new JsonArray();
        parts.add(textPart);
        parts.add(imgPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        // Modelin duz JSON dondurmesini zorlamak icin:
        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("response_mime_type", "application/json");

        JsonObject root = new JsonObject();
        root.add("contents", contents);
        root.add("generationConfig", genConfig);
        return GSON.toJson(root);
    }

    /**
     * Gemini cevabindan asil JSON metnini cikarir:
     * candidates[0].content.parts[0].text. Olasi ```json ... ``` cercevesini de temizler.
     */
    private String extractJsonFromResponse(String responseBody) {
        JsonObject root = GSON.fromJson(responseBody, JsonObject.class);
        String text = root.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString()
                .trim();

        if (text.startsWith("```")) {
            text = text.replaceAll("^```[a-zA-Z]*", "").replaceAll("```$", "").trim();
        }
        return text;
    }
}
