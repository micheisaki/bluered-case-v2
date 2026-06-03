package red.theblue.sutcheck.model;

/** Raporda/recetede yer alan tek bir kalem (ilac veya tibbi malzeme). */
public class PrescribedItem {
    public String code;          // SUT etken madde/malzeme kodu, orn. "SGKFUV"
    public String name;          // orn. "VALGANSIKLOVIR"
    public ItemType type = ItemType.DRUG;
    public DrugClass drugClass = DrugClass.OTHER;
    public String form;          // orn. "ORAL"
    public String dailyDose;     // orn. "900 mg" (serbest metin, bilgi amacli)

    // Sadece tibbi malzemeler icin (orn. seker olcum cubugu): gunluk adet.
    public Integer stripsPerDay;

    public PrescribedItem() { }

    /** Kalem adinda (buyuk/kucuk harf duyarsiz) bir anahtar kelime geciyor mu? */
    public boolean nameContains(String keyword) {
        return name != null && normalize(name).contains(normalize(keyword));
    }

    /** Buyuk harfe cevirir ve Turkce harfleri ASCII'ye indirger (AI ciktilarindaki I/i farki icin). */
    private static String normalize(String s) {
        return s.toUpperCase(java.util.Locale.ROOT)
                .replace('İ', 'I').replace('Ş', 'S').replace('Ç', 'C')
                .replace('Ğ', 'G').replace('Ö', 'O').replace('Ü', 'U');
    }

    @Override
    public String toString() {
        return (code != null ? code + " - " : "") + name;
    }
}
