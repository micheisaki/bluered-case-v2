package red.theblue.sutcheck.model;

/**
 * Raporu duzenleyen hekimin uzmanlik dali.
 * SUT bircok ilac/malzeme icin "hangi uzman raporu yazabilir" sartini koyar,
 * bu yuzden uzmanligi tipli (enum) tutuyoruz.
 */
public enum Specialty {
    ENDOKRINOLOJI,
    IC_HASTALIKLARI,
    KARDIYOLOJI,
    KALP_DAMAR_CERRAHISI,
    NOROLOJI,
    COCUK_SAGLIGI,
    AILE_HEKIMI,
    ENFEKSIYON_HASTALIKLARI,
    GOGUS_HASTALIKLARI,
    DIGER;
}
