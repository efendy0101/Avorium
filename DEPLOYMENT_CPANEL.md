# Avorium cPanel / Linux Production Deployment

> Bu belgede `example.com`, `exampleuser` ve `example_` ile başlayan değerler yer tutucudur; dağıtımdan önce kendi alan adınızı ve sunucu bilgilerinizi kullanın.

Production hedefi:

```text
Android App / Admin Panel
        -> https://api.example.com/api
        -> Node.js Express Backend
        -> cPanel MySQL / MariaDB
```

## 1. Önemli Mimari Karar

Bu proje production için MySQL/MariaDB kullanacak şekilde ayarlandı.

cPanel sunucuda MySQL varsa PostgreSQL kurmaya çalışmak çoğu shared hosting paketinde doğru yol değildir. PostgreSQL sadece cPanel hesabında PostgreSQL özelliği açıksa veya WHM/root erişimi varsa kurulabilir. Bu nedenle Prisma datasource `mysql` yapıldı ve backend MariaDB/MySQL adapter kullanıyor.

## 2. cPanel MySQL Kurulumu

1. cPanel'e gir.
2. `MySQL Databases` bölümünü aç.
3. Yeni database oluştur:

```text
Database name: avorium
```

cPanel genelde prefix ekler. Örnek:

```text
example_avorium
```

4. Yeni database user oluştur:

```text
User: avorium_user
Password: güçlü bir parola
```

cPanel prefix ekleyebilir:

```text
example_avorium_user
```

5. User'ı database'e ekle.
6. `ALL PRIVILEGES` ver.

## 3. Backend Dosyalarını Yükleme

Sunucuda önerilen klasör:

```text
/home/CPANEL_USER/avorium-backend
```

`Avorium-Backend` içeriğini bu klasöre yükle.

Bu klasörü subdomain'in document root'u içine koyma. Önerilen yapı:

```text
/home/CPANEL_USER/avorium-backend
/home/CPANEL_USER/public_html
/home/CPANEL_USER/api.example.com
```

`avorium-backend` Node.js uygulama köküdür. `api.example.com` document root'u statik dosya klasörü gibi kalsa bile Node.js uygulaması cPanel Application Manager/Passenger üzerinden bu subdomain'e bağlanır.

`/api` adında ayrıca klasör açma. Bu projede `/api` fiziksel klasör değil, Express route prefix'idir. Örneğin kodda:

```ts
app.use('/api/auth', authRoutes)
app.get('/api/health', ...)
```

Bu nedenle canlı endpoint otomatik olarak şu olur:

```text
https://api.example.com/api/health
https://api.example.com/api/auth/login
https://api.example.com/api/auth/register
```

cPanel Application URL/Base URL ayarını `/api` yapma. Eğer `/api` yaparsan endpoint'ler büyük ihtimalle çift prefix olur:

```text
https://api.example.com/api/api/health
```

Doğru ayar:

```text
Application root: avorium-backend
Application URL / Deployment Domain: api.example.com
Base Application URL / App URI: / veya boş
Startup file: app.js
```

Yüklenmesi gerekenler:

```text
src/
prisma/
package.json
prisma.config.ts
tsconfig.json
app.js
```

`node_modules` yükleme. Sunucuda kurulacak.

## 4. Production .env

Sunucuda `Avorium-Backend/.env` dosyasını şu şekilde oluştur:

```env
DATABASE_URL="mysql://CPANEL_DB_USER:DB_PASSWORD@localhost:3306/CPANEL_DB_NAME"
JWT_SECRET="CHANGE_ME_TO_A_RANDOM_SECRET"
CORS_ORIGIN="https://api.example.com"
```

Örnek:

```env
DATABASE_URL="mysql://example_avorium_user:PAROLA@localhost:3306/example_avorium"
JWT_SECRET="replace-with-64-plus-random-characters"
CORS_ORIGIN="https://api.example.com"
```

Parolada `@`, `#`, `:` veya `/` varsa URL encode et:

```text
@ -> %40
# -> %23
: -> %3A
/ -> %2F
```

## 5. Node.js Uygulaması Oluşturma

cPanel'de:

1. `Setup Node.js App` aç.
2. `Create Application` seç.
3. Ayarlar:

```text
Node.js version: 24.x
Application mode: Production
Application root: avorium-backend
Application URL: api.example.com
Application startup file: app.js
```

Node 24 kurulu olmalı. WHM / EasyApache tarafında `ea-nodejs24` aktif değilse önce onu kur.

Sunucuda Node 24 yolunu doğrula:

```bash
/opt/cpanel/ea-nodejs24/bin/node -v
/opt/cpanel/ea-nodejs24/bin/npm -v
```

Beklenen:

```text
v24.x.x
```

Eğer bu dosya yoksa gerçek yolu bul:

```bash
which node
find /opt/cpanel -path "*node" -type f 2>/dev/null | grep nodejs24
```

## 6. Bağımlılık Kurulumu

cPanel terminal veya SSH:

```bash
cd ~/avorium-backend
npm install
```

`npm ci` kullanma. Projede `package-lock.json` tutulmuyor; sunucuda `npm install` ile paketler kurulmalıdır.

Kurulumdan sonra Prisma sürümünü kontrol et:

```bash
npx prisma -v
```

Beklenen ana sürüm:

```text
Prisma CLI Version : 6.x
```

Bu proje cPanel için Prisma 6 uyumlu ayarlandı. `prisma/schema.prisma` içinde datasource şu şekilde olmalı:

```prisma
datasource db {
  provider = "mysql"
  url      = env("DATABASE_URL")
}
```

Eğer `Argument "url" is missing in data source block "db"` hatası alırsan sunucuda eski schema dosyası vardır. Güncel dosyayı tekrar yükle veya bu `url` satırını ekle.

## 7. Prisma Client, Tablo Kurulumu ve Seed

```bash
npx prisma generate
npx prisma db push
npm run seed
```

Bu işlem:

- MySQL tablolarını oluşturur.
- Varsayılan kategorileri ekler.
- Varsayılan paketleri ekler.

Mevcut bir kullanıcıyı admin yapmak için önce kullanıcı uygulamadan veya API'den normal kayıt olsun. Sonra sunucuda:

```bash
cd ~/avorium-backend
npm run make:admin -- test@example.com
```

Bu komut `test@example.com` kullanıcısının rolünü `ADMIN` yapar. Admin paneline aynı e-posta ve şifreyle girilir.

Alternatif olarak seed ile yeni admin kullanıcı oluşturmak için `.env` içine geçici olarak şunları ekleyebilirsin:

```env
ADMIN_EMAIL="admin@example.com"
ADMIN_PASSWORD="çok_güçlü_admin_şifresi"
```

Sonra tekrar:

```bash
npm run seed
```

Admin oluşturduktan sonra `ADMIN_PASSWORD` değerini `.env` içinde bırakmaman önerilir.

Bir kullanıcı admin yapıldıktan sonra eski oturumu kapatıp tekrar giriş yapmalıdır. JWT token login anındaki kullanıcı bilgisini taşıdığı için en temiz test yeni giriş yapmaktır.

## 8. Build ve Başlatma

```bash
npm run build
```

cPanel Node.js ekranından:

```text
Restart
```

Uygulama çalışınca şu URL cevap vermeli:

```text
https://api.example.com/api/health
```

Beklenen cevap:

```json
{
  "status": "OK",
  "message": "Avorium API is running"
}
```

## 8.1. Domain Node'a Gitmiyor, Apache Statik Klasör Gösteriyorsa

Şu test başarılıysa backend kodu çalışıyor demektir:

```bash
PORT=5050 node app.js
curl http://127.0.0.1:5050/api/health
```

Beklenen:

```json
{"status":"OK","message":"Avorium API is running"}
```

Ama şu istek Apache HTML 500/403/autoindex dönüyorsa:

```bash
curl https://api.example.com/api/health
```

problem backend değil, Apache/cPanel yönlendirmesidir.

Apache loglarında şu tip kayıtlar bunu doğrular:

```text
Cannot serve directory /home/exampleuser/public_html/api.example.com/
Request exceeded the limit of 10 internal redirects
```

Bu durumda Apache subdomain document root'u statik klasör olarak servis etmeye çalışıyordur. Node.js app domain'e bağlanmamıştır veya `.htaccess` redirect loop vardır.

Kontrol et:

```bash
ls -la /home/exampleuser/public_html/api.example.com
cat /home/exampleuser/public_html/api.example.com/.htaccess
```

Bu klasörde rewrite/redirect varsa geçici olarak devre dışı bırak:

```bash
mv /home/exampleuser/public_html/api.example.com/.htaccess /home/exampleuser/public_html/api.example.com/.htaccess.bak
```

### Alternatif: WHM Root ile Apache Reverse Proxy

cPanel Application Manager sorun çıkarırsa en stabil yöntem:

1. Backend'i local portta systemd ile çalıştır.
2. Apache subdomain'i `127.0.0.1:5050` adresine proxy'le.

Örnek systemd dosyası projede:

```text
Avorium-Backend/deploy/avorium-backend.service
```

Sunucuda:

```bash
cp ~/avorium-backend/deploy/avorium-backend.service /etc/systemd/system/avorium-backend.service
systemctl daemon-reload
systemctl enable --now avorium-backend
systemctl status avorium-backend
curl http://127.0.0.1:5050/api/health
```

Apache proxy include örneği projede:

```text
Avorium-Backend/deploy/apache-proxy.conf
```

WHM'de şu bölümden subdomain vhost include ekle:

```text
WHM -> Service Configuration -> Apache Configuration -> Include Editor
```

`api.example.com` vhost'una şu içeriği ekle:

```apache
ProxyPreserveHost On
ProxyPass / http://127.0.0.1:5050/
ProxyPassReverse / http://127.0.0.1:5050/
RequestHeader set X-Forwarded-Proto "https" env=HTTPS
```

Sonra:

```bash
/scripts/rebuildhttpdconf
systemctl restart httpd
curl https://api.example.com/api/health
```

Apache modülleri kapalıysa WHM EasyApache 4'ten şunlar aktif olmalı:

```text
mod_proxy
mod_proxy_http
mod_headers
```

## 9. Android Production API Adresi

`Avorium-Android/gradle.properties`:

```properties
AVORIUM_API_BASE_URL=https://api.example.com/api/
```

Release build almadan önce bu değer production domain olmalı. `10.0.2.2`, `localhost`, `192.168.x.x` sadece geliştirme içindir.

## 10. Admin Panel API Adresi

Admin panel build alırken:

```env
VITE_API_URL=https://api.example.com/api
```

Admin panel ayrı bir subdomain'de yayınlanacaksa örnek:

```text
https://admin.example.com
```

Backend `.env` içindeki `CORS_ORIGIN` değeri buna göre olmalı:

```env
CORS_ORIGIN="https://admin.example.com"
```

Birden fazla origin gerekirse:

```env
CORS_ORIGIN="https://admin.example.com,https://example.com"
```

## 11. PostgreSQL Kullanmak İstersen

PostgreSQL için iki seçenek var:

1. cPanel hesabında `PostgreSQL Databases` bölümü varsa:
   - Database oluştur.
   - User oluştur.
   - User'ı database'e bağla.
   - Prisma provider tekrar `postgresql` yapılmalı.
   - Backend adapter tekrar `@prisma/adapter-pg` olmalı.

2. WHM/root erişimin varsa:
   - Hosting sağlayıcısı PostgreSQL servisini WHM tarafında kurmalı/aktif etmeli.
   - Shared hosting paketinde bunu sen kuramazsın.

Bu projede mevcut cPanel MySQL nedeniyle PostgreSQL önerilmiyor.

## 12. Production Kontrol Listesi

- `https://api.example.com/api/health` çalışıyor.
- MySQL tabloları oluşmuş.
- `User`, `Category`, `Package` tablolarında veri var.
- Android `AVORIUM_API_BASE_URL` production HTTPS adresini gösteriyor.
- Backend `.env` içinde güçlü `JWT_SECRET` var.
- cPanel Node.js app `Production` modunda.
- cPanel SSL sertifikası aktif.
- Upload klasörü yazılabilir.
