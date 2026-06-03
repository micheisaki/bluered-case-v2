package red.theblue.sutcheck.rules;

import red.theblue.sutcheck.engine.Rule;
import red.theblue.sutcheck.engine.RuleResult;
import red.theblue.sutcheck.model.DrugClass;
import red.theblue.sutcheck.model.PrescribedItem;
import red.theblue.sutcheck.model.Report;
import red.theblue.sutcheck.model.Specialty;

import java.util.Set;

/**
 * SUT 4.2.38 (4) ve (6) - DPP-4 antagonistleri ve SGLT2 inhibitorleri.
 * Kosul: metformin ve/veya sulfonilurelerin maksimum tolere edilebilir dozlarinda
 *        yeterli glisemik kontrol saglanamamis olmasi.
 * Rapor: endokrinoloji veya ic hastaliklari uzmani (ya da bu rapora dayanarak tum hekimler).
 * Not: SUT, DPP-4'un GLP-1 ile birlikte kullanimini kapsam disi birakir;
 *      DPP-4 + SGLT2 birlikteligi icin acik bir yasak yoktur.
 */
public class DiabetesDppSglt2Rule implements Rule {

    private static final Set<Specialty> ALLOWED = Set.of(
            Specialty.ENDOKRINOLOJI, Specialty.IC_HASTALIKLARI);

    private static final String REF = "SUT 4.2.38 (4)/(6)";

    @Override
    public boolean appliesTo(PrescribedItem item, Report r) {
        return item.drugClass == DrugClass.DPP4
                || item.drugClass == DrugClass.SGLT2
                || item.nameContains("GLIPTIN")
                || item.nameContains("GLIFLOZIN");
    }

    @Override
    public RuleResult evaluate(PrescribedItem item, Report r) {
        if (!r.metforminSulfonylureaFailed) {
            return RuleResult.nonCompliant(item,
                    "Metformin ve/veya sulfonilurelerin maksimum dozda yetersiz kaldigi raporda "
                            + "belgelenmemis; SUT 4.2.38 on kosulu saglanmiyor.", REF);
        }
        if (!ALLOWED.contains(r.doctorSpecialty)) {
            return RuleResult.nonCompliant(item,
                    "Metformin/SU yetersizligi belgelenmis ancak raporu duzenleyen uzmanlik ("
                            + r.doctorSpecialty + ") uygun degil; endokrinoloji veya ic hastaliklari gerekir.", REF);
        }
        return RuleResult.compliant(item,
                "Metformin/SU max dozda yetersiz glisemik kontrol belgeli ve uzmanlik uygun.", REF);
    }

    @Override
    public String name() { return "Diyabet DPP-4 / SGLT2 - 4.2.38"; }
}
