package red.theblue.sutcheck.engine;

import red.theblue.sutcheck.model.PrescribedItem;
import red.theblue.sutcheck.model.Report;

/**
 * Tek bir SUT kuralini temsil eder. Her SUT maddesi ayri bir sinif olur,
 * boylece yeni kural eklemek = yeni bir sinif yazmak (Open/Closed).
 */
public interface Rule {

    /** Bu kural verilen kaleme uygulanir mi? (orn. yalnizca SGLT2 ilaclarina) */
    boolean appliesTo(PrescribedItem item, Report report);

    /** Kalemi degerlendirir ve sonucu dondurur. */
    RuleResult evaluate(PrescribedItem item, Report report);

    /** Loglama/aciklama icin kuralin kisa adi. */
    String name();
}
