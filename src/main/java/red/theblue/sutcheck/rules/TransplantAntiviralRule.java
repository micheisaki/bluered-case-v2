package red.theblue.sutcheck.rules;

import red.theblue.sutcheck.engine.Rule;
import red.theblue.sutcheck.engine.RuleResult;
import red.theblue.sutcheck.model.PrescribedItem;
import red.theblue.sutcheck.model.Report;

/**
 * Valgansiklovir/gansiklovir - nakil sonrasi CMV profilaksisi.
 * SUT'ta bu etken maddeye ozel bir 4.2 kisitlamasi YOKTUR; genel rapor kurallari
 * (SUT 4.1.3) gecerlidir: gecerli bir uzman hekim raporu ve uygun tani yeterlidir.
 * Dikkat: 4.2.12.A(3)'teki CMV immunglobulini kisitlamasi FARKLI bir ilactir,
 * burada uygulanmaz.
 */
public class TransplantAntiviralRule implements Rule {

    private static final String REF = "SUT 4.1.3 (genel rapor) — valgansiklovir icin ozel kisit yok";

    @Override
    public boolean appliesTo(PrescribedItem item, Report r) {
        return item.nameContains("VALGANSIKLOVIR") || item.nameContains("GANSIKLOVIR");
    }

    @Override
    public RuleResult evaluate(PrescribedItem item, Report r) {
        if (!r.hasSpecialistReport()) {
            return RuleResult.nonCompliant(item,
                    "Gecerli bir uzman hekim raporu/uzmanlik bilgisi yok.", REF);
        }
        if (!r.hasDiagnosisStartingWith("Z94")) {
            return RuleResult.needsReview(item,
                    "Nakil tanisi (Z94.x) raporda gorunmuyor; endikasyon dogrulanmali.", REF);
        }
        return RuleResult.compliant(item,
                "Nakil tanisi (Z94.x) ve uzman hekim raporu mevcut; valgansiklovir icin ozel SUT kisiti yok.",
                REF);
    }

    @Override
    public String name() { return "Transplant antiviral (valgansiklovir)"; }
}
