# Kapsül 📮

Geleceğe mühürlü notlar yazan, **%100 çevrimdışı** çalışan bir zaman kapsülü uygulaması.
Material 3 Expressive tasarım diliyle geliştirilmiştir.

## Ne yapıyor?

Bir kapsül yazarsın — mektup, hedef, kendine hatırlatma — ve vaktini seçersin.
Mühür o ana kadar açılmaz; sen bile göremezsin. Zamanı gelince mührü kırar,
notunu okursun.

## Özellikler

- **Mühürlü kapsüller** — geri sayım listede canlı işler, kartlar sıralanır;
  mühür etiketini saran **ilerleme halkası** beklenen süreyi dolmaya başlar
- **Mühür kırma animasyonu** — balmumu mührü titrer, kırılınca büyüyerek soler;
  içerik yay (spatial spring) ile açılır. Kırılma anında **titreşim** ve
  mührün üstünden fışkıran **konfeti kutlaması** vardır
- **Üç türlü açılma vakti**
  - Hazır süreler: 1 saat / 1 gün / 1 hafta / 1 ay
  - **Özel süre**: miktar + birim (dakika / saat / gün) — örn. "45 dakika", "6 gün"
  - Takvimden tarih seçimi (DatePicker)
- **Kapsülü düzenleme** — başlık, sembol, renk ve (mühürlüyken) süre
  değiştirilebilir; **mühürlüyken not alanı gizlenir** ve yalnızca mühür
  kırıldıktan sonra düzenlenebilir. Süre alanına dokunulmadıysa vakt aynen
  korunur
- **Sembol ve renk** — 12 emoji, 6 Expressive konteyner rengi
- **Filtreler** — Tümü / Mühürlü / Açıldı (ButtonGroup)
- **İstatistik rozetleri** — başlıkta mühürlü / açılmış kapsül sayısı
- **Hakkında** — uygulama açıklaması, sürüm bilgisi ve GitHub deposunu açan buton
- **%100 çevrimdışı** — veriler `capsules.json` dosyasında saklanır; hiçbir veri
  cihazdan çıkmaz, hesap gerekmez, reklam yoktur
- İlk açılışta 3 dakika içinde açılacak bir örnek kapsül eklenir

## Teknoloji

| Katman | Seçim |
| --- | --- |
| UI | Jetpack Compose, Material 3 Expressive (`material3` 1.5.0-rc01) |
| Motion | `MaterialExpressiveTheme` + `MotionScheme.expressive()` |
| Mimar | MVVM (`KapsulViewModel`) + tek kaynak repository |
| Kalıcılık | Gson + JSON dosyası (Room/KSP yok — bilinçli tercih) |
| Navigasyon | `navigation-compose` (liste ↔ detay) |
| Dil | Kotlin 2.2, AGP 9.5.1, compileSdk 37, minSdk 26 |

Kullanılan expressive bileşenler: `ButtonGroup`, `FloatingActionButtonMenu`,
`LoadingIndicator`, büyük yuvarlak `Shapes` ve yay tabanlı animasyonlar.

## Derleme

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Release derlemesi için `./gradlew assembleRelease` (imzalama yapılandırması gerekir).

## Gizlilik

Kapsüller yalnızca cihazında yaşar. Uygulama ağ erişimi kullanmaz; notların,
başlıkların ve açılış zamanların hiçbir sunucuya gönderilmez. Uygulamanın
kendisi analiz, izleme veya reklam yazılımı içermez.

## Lisans

[MIT](LICENSE) — özgürce kullanabilir, değiştirebilir ve dağıtabilirsin.
