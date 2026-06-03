package red.theblue.sutcheck;

import red.theblue.sutcheck.engine.ComplianceStatus;
import red.theblue.sutcheck.engine.RuleEngine;
import red.theblue.sutcheck.engine.RuleResult;
import red.theblue.sutcheck.io.AiReportExtractor;
import red.theblue.sutcheck.io.ReportLoader;
import red.theblue.sutcheck.model.Report;
import red.theblue.sutcheck.rules.DiabetesDppSglt2Rule;
import red.theblue.sutcheck.rules.GlucoseStripRule;
import red.theblue.sutcheck.rules.LipidNonStatinRule;
import red.theblue.sutcheck.rules.TransplantAntiviralRule;

import java.util.List;

/**
 * Giris noktasi.
 * Kullanim:  java -jar sut-compliance-checker.jar <rapor1.json> [rapor2.json ...]
 * Arguman verilmezse paketteki iki ornek rapor calistirilir.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        RuleEngine engine = new RuleEngine()
                .register(new TransplantAntiviralRule())
                .register(new LipidNonStatinRule())
                .register(new DiabetesDppSglt2Rule())
                .register(new GlucoseStripRule());

        String[] paths = (args.length > 0)
                ? args
                : new String[]{
                    "src/main/resources/report1_transplant.json",
                    "src/main/resources/report2_diyabet.json"
                };

        for (String path : paths) {
            Report report = loadReport(path);
            printReport(report, engine.evaluate(report));
        }
    }

    /**
     * Girdiyi uzantisina gore yukler:
     *  - .json  -> dogrudan okunur (Faz 1)
     *  - .png/.jpg/.jpeg -> Gemini ile goruntuden cikarilir (Faz 2)
     */
    private static Report loadReport(String path) throws Exception {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            System.out.println("[AI] Goruntu cozumleniyor: " + path);
            return new AiReportExtractor().fromImage(path);
        }
        return ReportLoader.fromFile(path);
    }

    private static void printReport(Report r, List<RuleResult> results) {
        System.out.println("============================================================");
        System.out.println("Hasta      : " + r.patientName + "   (Rapor No: " + r.reportNo + ")");
        System.out.println("Uzmanlik   : " + r.doctorSpecialty);
        System.out.println("Gecerlilik : " + r.validFrom + " - " + r.validTo);
        System.out.println("------------------------------------------------------------");

        int ok = 0, bad = 0, review = 0;
        for (RuleResult res : results) {
            System.out.println(icon(res.status) + " " + res.item);
            System.out.println("      Durum    : " + res.status);
            System.out.println("      Gerekce  : " + res.reason);
            System.out.println("      SUT      : " + res.sutReference);
            if (res.status == ComplianceStatus.COMPLIANT) ok++;
            else if (res.status == ComplianceStatus.NON_COMPLIANT) bad++;
            else review++;
        }
        System.out.println("------------------------------------------------------------");
        System.out.printf("Ozet: %d uygun, %d uygun degil, %d manuel inceleme%n", ok, bad, review);
        System.out.println("============================================================\n");
    }

    private static String icon(ComplianceStatus s) {
        switch (s) {
            case COMPLIANT:     return "[UYGUN]   ";
            case NON_COMPLIANT: return "[UYGUNSUZ]";
            default:            return "[INCELE]  ";
        }
    }
}
