package red.theblue.sutcheck.engine;

/** Bir kalemin SUT'a gore degerlendirme sonucu. */
public enum ComplianceStatus {
    COMPLIANT,       // SUT'a uygun, bedeli karsilanir
    NON_COMPLIANT,   // SUT'a aykiri, kesinti/red beklenir
    NEEDS_REVIEW;    // karar verecek veri eksik veya tanimli kural yok -> manuel inceleme
}
