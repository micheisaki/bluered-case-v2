package red.theblue.sutcheck.model;

/**
 * Ilacin SUT acisindan onemli farmakolojik sinifi.
 * Kurallar genelde etken madde adindan cok bu sinifa gore isler.
 */
public enum DrugClass {
    SGLT2,            // sodyum-glukoz ko-transporter 2 inhibitoru (empagliflozin...)
    DPP4,             // DPP-4 antagonisti (vildagliptin...)
    NON_STATIN_LIPID, // statin disi lipid dusurucu (fenofibrat...)
    ANTIVIRAL,        // valgansiklovir vb.
    OTHER;
}
