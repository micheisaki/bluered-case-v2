package red.theblue.sutcheck.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bir SGK saglik raporunun yapilandirilmis hali.
 * AI/OCR katmani (Faz 2) bir rapor goruntusunu bu nesneye cevirir;
 * Faz 1'de ayni nesne JSON'dan okunur. Kural motoru yalnizca bu modele bakar.
 */
public class Report {

    // --- Hak sahibi / rapor ust bilgileri ---
    public String patientName;
    public String tckn;
    public String gender;          // "M" / "F"
    public String birthDate;       // "yyyy-MM-dd"
    public String reportNo;
    public String reportDate;      // "yyyy-MM-dd"
    public String validFrom;       // "yyyy-MM-dd"
    public String validTo;         // "yyyy-MM-dd"

    public Specialty doctorSpecialty = Specialty.DIGER;
    public String explanation;     // raporun "Aciklamalar" metni

    public List<Diagnosis> diagnoses = new ArrayList<>();

    // --- Laboratuvar / ilave deger bilgileri ---
    // Anahtar ornekleri: "trigliserit", "hba1c", "aclikKanSekeri" (mg/dL, %)
    public Map<String, Double> labValues = new HashMap<>();

    // --- Klinik bayraklar (raporun acikladigi/MEDULA'dan gelen durumlar) ---
    public boolean diabetesMellitus;
    public boolean oralAntidiabeticOnly;       // yalnizca oral antidiyabetik kullaniyor
    public boolean insulinUser;                // insulin kullaniyor
    public boolean metforminSulfonylureaFailed;// metformin/SU max dozda yetersiz glisemik kontrol
    public boolean transplant;                 // organ nakli hastasi
    public boolean pregnant;

    public List<PrescribedItem> items = new ArrayList<>();

    // ---------- Yardimci metodlar ----------

    /** Verilen ICD/SUT kod onekiyle baslayan bir tani var mi? (orn. "Z94", "E11") */
    public boolean hasDiagnosisStartingWith(String prefix) {
        for (Diagnosis d : diagnoses) {
            if (d.code != null && d.code.toUpperCase(java.util.Locale.ROOT).startsWith(prefix.toUpperCase(java.util.Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /** Laboratuvar degerini dondurur; yoksa null. */
    public Double lab(String key) {
        return labValues.get(key);
    }

    public boolean hasSpecialistReport() {
        return doctorSpecialty != null && doctorSpecialty != Specialty.DIGER;
    }
}
