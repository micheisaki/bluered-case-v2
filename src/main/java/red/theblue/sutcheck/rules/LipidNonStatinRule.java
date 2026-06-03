package red.theblue.sutcheck.rules;

import red.theblue.sutcheck.engine.Rule;
import red.theblue.sutcheck.engine.RuleResult;
import red.theblue.sutcheck.model.DrugClass;
import red.theblue.sutcheck.model.PrescribedItem;
import red.theblue.sutcheck.model.Report;
import red.theblue.sutcheck.model.Specialty;

import java.util.Set;

/**
 * SUT 4.2.28.B - Statin disi lipid dusurucu ilaclar (fenofibrat, gemfibrozil, kolestiramin).
 * Kosul:
 *   a) Trigliserid > 500 mg/dL, VEYA
 *   b) Trigliserid > 200 mg/dL olup; diabetes mellitus / KAH / gecirilmis MI-inme vb. olanlarda
 * Rapor: kardiyoloji, KVC, endokrinoloji, ic hastaliklari veya noroloji uzmani.
 */
public class LipidNonStatinRule implements Rule {

    private static final Set<Specialty> ALLOWED = Set.of(
            Specialty.KARDIYOLOJI, Specialty.KALP_DAMAR_CERRAHISI,
            Specialty.ENDOKRINOLOJI, Specialty.IC_HASTALIKLARI, Specialty.NOROLOJI);

    private static final String REF = "SUT 4.2.28.B";

    @Override
    public boolean appliesTo(PrescribedItem item, Report r) {
        return item.drugClass == DrugClass.NON_STATIN_LIPID
                || item.nameContains("FENOFIBRAT")
                || item.nameContains("GEMFIBROZIL")
                || item.nameContains("KOLESTIRAMIN");
    }

    @Override
    public RuleResult evaluate(PrescribedItem item, Report r) {
        Double tg = r.lab("trigliserit");
        if (tg == null) {
            return RuleResult.needsReview(item,
                    "Trigliserid degeri raporda yok; SUT 4.2.28.B esigi degerlendirilemiyor.", REF);
        }

        boolean indicationMet;
        String indicationText;
        if (tg > 500) {
            indicationMet = true;
            indicationText = "Trigliserid " + tg + " mg/dL (>500).";
        } else if (tg > 200 && r.diabetesMellitus) {
            indicationMet = true;
            indicationText = "Trigliserid " + tg + " mg/dL (>200) ve diabetes mellitus mevcut.";
        } else {
            return RuleResult.nonCompliant(item,
                    "Trigliserid " + tg + " mg/dL; SUT 4.2.28.B baslangic kriterleri saglanmiyor "
                            + "(>500 veya DM/KAH ile birlikte >200 gerekir).", REF);
        }

        if (!ALLOWED.contains(r.doctorSpecialty)) {
            return RuleResult.nonCompliant(item,
                    indicationText + " Ancak raporu duzenleyen uzmanlik (" + r.doctorSpecialty
                            + ") uygun degil; kardiyoloji/KVC/endokrinoloji/ic hastaliklari/noroloji gerekir.", REF);
        }
        return RuleResult.compliant(item, indicationText + " Uzmanlik uygun.", REF);
    }

    @Override
    public String name() { return "Lipid (statin disi) - 4.2.28.B"; }
}
