import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcrypt';
const prisma = new PrismaClient();
async function main() {
  const db = new URL(process.env.DATABASE_URL || '');
  if (!['localhost', '127.0.0.1', '::1', '[::1]'].includes(db.hostname)) throw new Error('Demo verileri yalnızca yerel veritabanına yüklenebilir.');
  const password = await bcrypt.hash('AvoriumDemo2026!', 12);
  const users = [
    { id: 'demo-admin-avorium', email: 'admin@example.com', name: 'Demo Yönetici', role: 'ADMIN' as const },
    { id: 'demo-user-avorium', email: 'deniz@example.com', name: 'Deniz Yılmaz', role: 'USER' as const },
    { id: 'demo-user2-avorium', email: 'ada@example.com', name: 'Ada Demir', role: 'USER' as const },
  ];
  for (const user of users) {
    const existing = await prisma.user.findUnique({ where: { email: user.email } });
    if (existing && existing.id !== user.id) throw new Error(`${user.email} zaten kullanılıyor; mevcut hesap değiştirilmedi.`);
    await prisma.user.upsert({ where: { id: user.id }, update: {}, create: { ...user, password, city: 'İzmir' } });
  }
  const boat = await prisma.category.findFirst({ where: { name: 'Fiber Tekne', type: 'BOAT' } });
  const gear = await prisma.category.findFirst({ where: { name: 'Spin Kamışı', type: 'FISHING_GEAR' } });
  if (!boat || !gear) throw new Error('Önce npm run seed ile kategorileri oluşturun.');
  const expiresAt = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000);
  const ads = [
    { id: 'demo-ad-boat', title: 'Kıyı gezileri için fiber tekne', description: 'Demo ilan: Günlük gezilere uygun örnek tekne.', price: 285000, categoryId: boat.id, userId: 'demo-user-avorium', status: 'ACTIVE' as const, city: 'İzmir', district: 'Urla', boatDetails: { create: { make: 'Örnek Marin', model: 'Kıyı 450', year: 2021, length: 4.5, hullMaterial: 'Fiber', enginePower: 40 } } },
    { id: 'demo-ad-gear', title: 'Spin olta kamışı ve çantası', description: 'Demo ilan: Örnek balıkçılık ekipmanı.', price: 2400, categoryId: gear.id, userId: 'demo-user2-avorium', status: 'ACTIVE' as const, city: 'Muğla', district: 'Bodrum', fishingGearDetails: { create: { condition: 'İkinci El', brand: 'Örnek', gearType: 'Spin Kamışı' } } },
    { id: 'demo-ad-pending', title: 'Onay bekleyen örnek kamış', description: 'Demo ilan: Yönetim panelindeki onay akışını denemek için oluşturuldu.', price: 1750, categoryId: gear.id, userId: 'demo-user-avorium', status: 'PENDING_REVIEW' as const, city: 'İzmir' },
  ];
  for (const ad of ads) await prisma.ad.upsert({ where: { id: ad.id }, update: {}, create: { ...ad, expiresAt } });
  await prisma.package.upsert({ where: { id: 'demo-package' }, update: {}, create: { id: 'demo-package', name: 'Demo Paket', price: 99, adLimit: 5, features: '5 ilan hakkı' } });
  await prisma.appSettings.upsert({ where: { id: 'singleton' }, update: {}, create: { id: 'singleton', announcementBanner: 'Demo ortamına hoş geldiniz. İçerikler örnektir.' } });
  console.log('Demo hazır. Yönetici: admin@example.com / AvoriumDemo2026!');
}
main().catch((error) => { console.error(error); process.exitCode = 1; }).finally(() => prisma.$disconnect());
