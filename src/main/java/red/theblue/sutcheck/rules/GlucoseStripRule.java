package red.theblue.sutcheck.rules;

import red.theblue.sutcheck.engine.Rule;
import red.theblue.sutcheck.engine.RuleResult;
import red.theblue.sutcheck.model.ItemType;
import red.theblue.sutcheck.model.PrescribedItem;
import red.theblue.sutcheck.model.Report;
import red.theblue.sutcheck.model.Specialty;

import java.util.Set;

/**
 * SUT 3.3.2 - Seker olcum cubuklari (test stripleri) miktar limitleri.
 *   - Insulin kullanan Tip II (ve Tip I eriskin): ayda en fazla 150 adet
 *   - Yalnizca oral antidiyabetik kullanan diyabetli: 3 ayda en fazla 100 adet
 * Rapor: aile hekimi / endokrinoloji / ic hastaliklari / cocuk sagligi uzmani.
 */
public class GlucoseStripRule implements Rule {

    private static final Set<Specialty> ALLOWED = Set.of(
            Specialty.AILE_HEKIMI, Specialty.ENDOKRINOLOJI,
            Specialty.IC_HASTALIKLARI, Specialty.COCUK_SAGLIGI);

    private static final String REF = "SUT 3.3.2";

    @Override
    public boolean appliesTo(PrescribedItem item, Report r) {
        return item.type == ItemType.SUPPLY
                && (item.nameContains("CUBUK") || item.nameContains("CUBUG")
                    || item.nameContains("STRIP") || item.nameContains("OLCUM"));
    }

    @Override
    public RuleResult evaluate(PrescribedItem item, Report r) {
        if (!ALLOWED.contains(r.doctorSpecialty)) {
            return RuleResult.nonCompliant(item,
                    "Seker olcum cubugu raporu icin uzmanlik (" + r.doctorSpecialty + ") uygun degil; "
                            + "aile hekimi/endokrinoloji/ic hastaliklari/cocuk sagligi gerekir.", REF);
        }
        if (item.stripsPerDay == null) {
            return RuleResult.needsReview(item,
                    "Gunluk cubuk adedi belirtilmemis; miktar limiti degerlendirilemiyor.", REF);
        }

        // Hasta kategorisine gore donem limiti
        int allowed;          // donemdeki azami adet
        int prescribed;       // ayni donem icin recete edilen adet
        String period;
        if (r.insulinUser) {
            allowed = 150;
            prescribed = item.stripsPerDay * 30;
            period = "ay";
        } else if (r.oralAntidiabeticOnly) {
            allowed = 100;
            prescribed = item.stripsPerDay * 90;
            period = "3 ay";
        } else {
            return RuleResult.needsReview(item,
                    "Hasta kategorisi (insulin / yalnizca oral) belirsiz; SUT 3.3.2 limiti secilemiyor.", REF);
        }

        String detail = "Gunde " + item.stripsPerDay + " adet -> " + prescribed + " adet/" + period
                + "; SUT limiti " + allowed + " adet/" + period + ".";
        if (prescribed > allowed) {
            return RuleResult.nonCompliant(item,
                    detail + " Limit asiliyor; asan kisim Kurumca karsilanmaz.", REF);
        }
        return RuleResult.compliant(item, detail + " Limit icinde.", REF);
    }

    @Override
    public String name() { return "Seker olcum cubugu - 3.3.2"; }
}
