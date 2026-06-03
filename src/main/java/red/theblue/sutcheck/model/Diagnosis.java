package red.theblue.sutcheck.model;

/** Rapordaki bir tani satiri (ICD-10 / SUT tani kodu + aciklama). */
public class Diagnosis {
    public String code;        // orn. "Z94.0", "E11.9", "E78.4"
    public String description; // orn. "BOBREK NAKLI"

    public Diagnosis() { }

    public Diagnosis(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
