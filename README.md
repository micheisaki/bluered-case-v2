# theBlueRed – SUT Uygunluk Denetleyici (Case Çalışması)

Bir **SGK sağlık raporunu** girdi olarak alıp, içindeki ilaç ve tıbbi malzeme
kalemlerini **Sağlık Uygulama Tebliği (SUT)** kurallarına göre otomatik denetleyen
bir Java uygulaması. Her kalem için sonuç: **UYGUN / UYGUNSUZ / MANUEL İNCELEME**,
gerekçesi ve dayandığı **SUT madde referansı** ile birlikte.

---

## 1. Problemi nasıl yorumladım (varsayım)

Case e-postasında ayrı bir "şunu yap" talimatı yoktu; ekler **SUT metni** ve
**iki örnek SGK rapor ekran görüntüsüydü**. Bu malzemeler bir araya geldiğinde
işaret ettiği görev şu: *"SUT kurallarını bilen, bir raporu alıp uygunluğunu
denetleyen bir yazılım."* Bu yüzden çözümü bir **kural motoru** olarak kurdum.

Bu yorum, theBlueRed'in kendi ürünü **"SUT Kontrol"** tarifiyle de örtüşüyor:
rapordaki **etken maddeleri tespit edip**, SUT mevzuatına göre değerlendirip
**ödenme gerekçesini sonucu gerekçeleriyle birlikte** sunan; **Medula'ya
bağlanmadan, hasta/doktor bilgisi gerektirmeden** çalışan, **ceza ve kesintileri
önlemeye** yönelik bir analiz uygulaması. Tasarım bu hedeflere göre kuruldu:

- Sonuç çıktısı bir **ödenme gerekçesi** gibidir (UYGUN/UYGUNSUZ + neden + SUT madde).
- **Gizlilik:** Hasta adı/TC gibi kimlik alanları opsiyoneldir; değerlendirme yalnızca
  tanı, etken madde ve laboratuvar verisinden yürür — Medula bağlantısı gerekmez.

Örnek raporlardan çıkarılan kapsam (denetlenen 4 SUT kuralı):

| Kalem | SUT maddesi | Özet kural |
|-------|-------------|-----------|
| Valgansiklovir (nakil/CMV) | 4.1.3 (genel rapor) | Etken maddeye özel kısıt yok; geçerli uzman raporu + Z94.x tanısı yeterli |
| Fenofibrat (statin dışı lipid) | 4.2.28.B | TG>500, veya DM ile birlikte TG>200; uygun uzmanlık |
| Empagliflozin / Vildagliptin (SGLT2 / DPP-4) | 4.2.38 (4)/(6) | Metformin/SÜ max dozda yetersizlik belgeli; endokrinoloji/iç hastalıkları |
| Şeker ölçüm çubuğu | 3.3.2 | Yalnızca oral antidiyabetik: 3 ayda ≤100 adet; insülinli: ayda ≤150 |

## 2. Yaklaşım ve mimari

İki katmanlı, **kademeli** tasarım:

- **Faz 1 – Kural motoru (bu teslimde tamamlandı):** Rapor verisi yapılandırılmış
  bir `Report` nesnesi olarak (JSON'dan) okunur ve kurallar uygulanır.
- **Faz 2 – AI extraction (tamamlandı, bkz. §6):** Rapor *görüntüsünü* Gemini
  vision modeline gönderip aynı `Report` nesnesine çıkarma. Motor değişmez;
  sadece girdi kaynağı değişir.

Tasarımın kalbi **Open/Closed prensibi**: her SUT maddesi `Rule` arayüzünü
uygulayan ayrı bir sınıftır. Yeni bir kural eklemek = yeni bir sınıf yazıp
motora `register()` etmek. Mevcut kod değişmez.

```
red.theblue.sutcheck
├── model/      Report, PrescribedItem, Diagnosis + enum'lar (Specialty, ItemType, DrugClass)
├── engine/     Rule (arayüz), RuleEngine, RuleResult, ComplianceStatus
├── rules/      LipidNonStatinRule, DiabetesDppSglt2Rule, GlucoseStripRule, TransplantAntiviralRule
├── io/         ReportLoader (JSON -> Report, Gson)
└── Main        giriş noktası + konsol raporu
```

**Güvenli varsayılan:** Bir kaleme hiçbir kural uymazsa motor sessizce "uygun"
demez; `MANUEL İNCELEME` döner. Karar için veri eksikse (örn. trigliserid yok)
yine `MANUEL İNCELEME` üretilir — yanlış pozitif onay vermemek için.

## 3. Varsayımlar

- Rapor verisi Faz 1'de **JSON** olarak hazır kabul edilir (OCR Faz 2'nin işi).
- Klinik durumlar (örn. "metformin/SÜ max dozda yetersiz", "yalnızca oral
  antidiyabetik") raporun açıklama metninden çıkarılıp **boolean bayrak** olarak
  modele verilir. Bu çıkarımı Faz 2'de LLM otomatik yapacak şekilde tasarlandı.
- Uzmanlık dalı `Specialty` enum'una eşlenir; tanımsızlar `DIGER` olur.
- SUT'un, örnek raporları ilgilendiren maddeleri kapsanmıştır; tüm SUT değil.

## 4. Kullanılan araç ve kütüphaneler

- **Java 11** (daha yeni JDK'lerle de çalışır)
- **Gson 2.10.1** – JSON ayrıştırma (tek dış bağımlılık)
- **Maven** – derleme/çalıştırma

## 5. Çalıştırma adımları

### Maven ile (önerilen)
```bash
# proje kök dizininde
mvn -q exec:java                 # paketteki iki örnek raporu çalıştırır
mvn -q exec:java -Dexec.args="yol/rapor.json"   # kendi raporunuzla
```

### Tek jar olarak
```bash
mvn -q package
java -jar target/sut-compliance-checker.jar
java -jar target/sut-compliance-checker.jar src/main/resources/report2_diyabet.json
```

### Maven olmadan (manuel)
`gson-2.10.1.jar`'ı `lib/` altına koyun, sonra:
```bash
javac -encoding UTF-8 -cp lib/gson-2.10.1.jar -d out $(find src/main/java -name "*.java")
java  -cp out:lib/gson-2.10.1.jar:src/main/resources red.theblue.sutcheck.Main
```

### Beklenen çıktı (özet)
```
=== HUSEYIN ARSLANYUREKLI (Rapor 3292832) | ENFEKSIYON_HASTALIKLARI ===
[UYGUN]    SGKFUV - VALGANSIKLOVIR        -> Nakil tanısı + uzman raporu mevcut  [SUT 4.1.3 ...]

=== DIYABET ... (Rapor 10313) | IC_HASTALIKLARI ===
[UYGUN]    SGKF1D - FENOFIBRAT            -> TG 272 (>200) + DM  [SUT 4.2.28.B]
[UYGUN]    SGKGB1 - EMPAGLIFLOZIN         -> Metformin/SÜ yetersizliği belgeli  [SUT 4.2.38 ...]
[UYGUN]    SGKFWL - VILDAGLIPTIN+METFORMIN-> ...  [SUT 4.2.38 ...]
[UYGUNSUZ] SGKFOA - SEKER OLCUM CUBUGU    -> Günde 4 -> 360 adet/3 ay; limit 100  [SUT 3.3.2]
```

## 6. Faz 2 — AI extraction (tamamlandı)

`io/AiReportExtractor` sınıfı bir rapor **görüntüsünü** (png/jpg) Google Gemini'nin
vision modeline gönderir ve dönen yapılandırılmış JSON'u `Report` nesnesine çevirir.
Kural motoru ve kurallar **hiç değişmedi** — `Main` artık girdiyi uzantısına göre
yönlendiriyor: `.json` ise `ReportLoader`, görüntü ise `AiReportExtractor`.

Teknik notlar:
- Ek bağımlılık yok: HTTP için JDK'nin yerleşik `java.net.http.HttpClient`'ı,
  görüntü için `java.util.Base64` kullanıldı.
- Prompt, modele tam olarak bu README'deki `Report` şemasını döndürmesini ve
  açıklama metninden klinik bayrakları (örn. metformin/SÜ yetersizliği) türetmesini söyler.
- **Gizlilik / Medula'sız çalışma:** Analiz yalnızca görüntüden çıkarılan içerikle
  yapılır; Medula veya harici sisteme bağlanılmaz.

### API anahtarı (kullanıcı tarafından ayarlanır)
Anahtar koda yazılmaz, `GEMINI_API_KEY` ortam değişkeninden okunur.
Ücretsiz anahtar: https://aistudio.google.com/apikey

PowerShell (bu oturum için):
```powershell
$env:GEMINI_API_KEY = "AIza...buraya-anahtar..."
```

### Görüntüden çalıştırma
```powershell
.\run.bat rapor.png
```
`.json` verirsen Faz 1 gibi davranır, görüntü verirsen Faz 2 (AI) devreye girer.

## 7. Süre bilgisi

- Başlangıç: 1 Haziran 2026 (Pazartesi) ~14:00
- Bitiş: 3 Haziran 2026 (Çarşamba) ~17:00
- Toplam etkin çalışma: ~7 saat 

> Not: SUT'un tamamı yerine örnek raporların kapsamında hazırlandı.