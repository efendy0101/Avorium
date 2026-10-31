# Avorium

Avorium, tekne ve balıkçılık ekipmanları için ilan, mesajlaşma ve yönetim akışlarını bir araya getiren örnek bir pazar yeri uygulamasıdır. Bu depo Android istemcisini, React yönetim panelini ve Express API'sini içerir.

## Proje yapısı

- `Avorium-Android/` — Kotlin ve Jetpack Compose ile Android uygulaması
- `Avorium-Admin/` — React, TypeScript ve Vite yönetim paneli
- `Avorium-Backend/` — Express, Prisma ve MySQL API'si

## Gereksinimler

- Node.js 24 veya üzeri ve npm
- Docker Desktop ve çalışan Docker Engine (alternatif olarak MySQL 8)
- Android uygulaması için Android Studio, Android SDK 36 ve JDK 17

## Yerel demo kurulumu

Aşağıdaki komutları depo kökünden bir terminalde çalıştırın. Windows'ta `cp` yerine `Copy-Item` da kullanılabilir. Örnek verileri gerçek veya paylaşılan bir veritabanına yüklemeyin.

```bash
cd Avorium-Backend
cp .env.example .env
npm ci
docker compose up -d db
```

Backend .env dosyasındaki JWT_SECRET değerini benzersiz bir rastgele değerle değiştirin. Örneğin Node.js ile `node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"` komutunu çalıştırın ve çıktıyı .env dosyasına koyun. Örnek yer tutucu ile sunucu başlamaz.

MySQL'in hazır olmasını bekleyin, ardından:

```bash
npm run db:push
npm run seed
npm run seed:demo
npm run dev
```

API sağlık kontrolü: http://localhost:5000/api/health. `db:push` şemayı veritabanına uygular; demo için yeni, boş bir veritabanı kullanın. `seed` kategorileri ekler. `seed:demo` yalnızca `localhost` veya `127.0.0.1` üzerindeki veritabanına örnek kullanıcılar, iki aktif ilan, bir onay bekleyen ilan ve bir paket ekler; mevcut kayıtları üzerine yazmaz. Aynı e-posta ile farklı bir kullanıcı varsa hata verir. Örnek ilanlara görsel eklenmez.

İkinci terminalde:

```bash
cd Avorium-Admin
cp .env.example .env
npm ci
npm run dev
```

Vite'ın gösterdiği adresi açın (genellikle http://localhost:5173). Demo yönetici: **admin@example.com** / **AvoriumDemo2026!**. Demo kullanıcıları `deniz@example.com` ve `ada@example.com` aynı parolayı kullanır. Bunlar yalnızca yerel demo hesaplarıdır. Tekrar seed çalıştırmak mevcut demo hesaplarının parolalarını veya değiştirilen kayıtlarını sıfırlamaz.

## Android

Android Studio ile `Avorium-Android` klasörünü açın, Gradle eşitlemesini tamamlayın ve bir emülatörde çalıştırın. Varsayılan API adresi emülatörden bilgisayara erişen `http://10.0.2.2:5000/api/` adresidir. Fiziksel cihazda bilgisayarın yerel ağ IP adresini `AVORIUM_API_BASE_URL` Gradle özelliği olarak belirtin; adres `/api/` ile bitmelidir. API'nin aynı ağdan erişilebilir olması gerekir.

## Yapılandırma ve üretim

- Backend `.env.example` yerel MySQL, API ve CORS ayarlarını içerir. Yönetim panelinde `VITE_API_URL` API adresini belirler. Bunları ortamınıza göre değiştirin.
- Demo parolalarını ve örnek JWT anahtarını üretimde kullanmayın. Üretim için yeni sırlar, uygun erişim kuralları ve güvenli dağıtım ayarları gereklidir.
- Dosya yüklemeleri `Avorium-Backend/uploads/` altında tutulur ve Git'e eklenmez. Yerel ortam dosyaları, bağımlılıklar ve derleme çıktıları da depoya dahil edilmez.
- Paket/ödeme entegrasyonu ve diğer üretim özellikleri canlı kullanım öncesinde ayrıca yapılandırılmalı ve doğrulanmalıdır.
- Sunucuya dağıtım için [cPanel notlarına](DEPLOYMENT_CPANEL.md) bakın; bu belge yerel demo kurulumundan ayrıdır.

## Kontroller

```bash
cd Avorium-Backend && npm run typecheck && npm run build
cd ../Avorium-Admin && npm run lint && npm run build
```

## Satın alma güvenliği

Google Play tarafından sunucuda doğrulama ve kullanıcıyla güvenli eşleştirme uygulanana kadar satın alma doğrulama uç noktası 503 döndürür; paket hakkı vermez. Canlı ödemeleri bu özellik tamamlanana kadar etkinleştirmeyin.
