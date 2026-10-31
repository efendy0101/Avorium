import { prisma } from '../src/prisma';



const FISHING_GEAR_CATEGORIES = [
  {
    name: 'Olta Kamışları',
    children: ['Surf Kamışı', 'Spin Kamışı', 'Olta Kamışı', 'Jigging Kamışı', 'Casting Kamışı', 'Tekne Kamışı', 'Fly Kamışı']
  },
  {
    name: 'Olta Makineleri',
    children: ['Spin Makine', 'Baitcasting Makine', 'Surf Makine', 'Çıkrık', 'Olta Makinesi', 'Fly Makine']
  },
  {
    name: 'Yapay Yemler',
    children: ['Kaşık', 'Silikon Yem', 'Rapala', 'Metal Jig', 'Tırıvırı', 'Sahte Yem', 'Popper', 'Minnow']
  },
  {
    name: 'Oltacı Aksesuarları',
    children: ['İğne/Kanca', 'Fırdöndü', 'Klips', 'Kurşun/Ağırlık', 'Şamandıra', 'Misina', 'Örgü İp', 'Fluorocarbon', 'Takım']
  },
  {
    name: 'Ağ ve Tuzaklar',
    children: ['Serpme Ağ', 'Pinter', 'Balık Tuzağı', 'Saçma Ağ']
  },
  {
    name: 'Balıkçı Giyim',
    children: ['Yağmurluk', 'Bot/Çizme', 'Şapka', 'Yelek', 'Eldiven', 'Güneş Gözlüğü']
  },
  {
    name: 'Elektronik ve Navigasyon',
    children: ['Balık Bulucu', 'GPS', 'El Feneri/Kafa Lambası']
  },
  {
    name: 'Balıkçı Çantaları',
    children: ['Takım Çantası', 'Malzeme Kutusu', 'Balık Taşıma Çantası']
  },
  {
    name: 'Diğer Balıkçılık Aksesuarları',
    children: ['Kepçe', 'Fileto Bıçağı', 'Balık Tartısı', 'Canlı Yem Kovası', 'Olta Çamurluk', 'Olta Tutucu']
  }
];

const BOAT_CATEGORIES = [
  {
    name: 'Motorlu Tekneler',
    children: ['Sürat Teknesi', 'Fiber Tekne', 'Trawler', 'Kabin Kruzer', 'Center Console', 'Daycruiser']
  },
  {
    name: 'Yelkenli Tekneler',
    children: ['Yelkenli', 'Katamaran', 'Gulet', 'Trimaran']
  },
  {
    name: 'Şişme Botlar',
    children: ['Şişme Bot', 'RIB Bot', 'Zodiac']
  },
  {
    name: 'Kano ve Kayık',
    children: ['Kano', 'Kayık', 'SUP', 'Kürek Teknesi']
  },
  {
    name: 'Jet Ski',
    children: ['Jet Ski', 'Su Motosikleti']
  },
  {
    name: 'Tekne Motorları',
    children: ['Dıştan Takma Motor', 'İçten Takma Motor', 'Elektrikli Motor', 'Yedek Parça']
  },
  {
    name: 'Tekne Ekipmanları',
    children: ['Çapa', 'Halat', 'Can Yeleği', 'Fender', 'Dümenci', 'Pala/Kürek', 'Merdiven', 'Baba']
  },
  {
    name: 'Tekne Elektroniği',
    children: ['Sonar', 'VHF Telsiz', 'Radar', 'Oto Pilot', 'Deniz Haritası', 'AIS']
  },
  {
    name: 'Tekne Bakım',
    children: ['Antifouling Boya', 'Zımpara', 'Reçine', 'Macun', 'Cila', 'Yağ/Bakım Seti']
  },
  {
    name: 'Tekne Römorkları',
    children: ['Tekne Römorku', 'Römork Aksesuarları']
  }
];

async function seed() {
  console.log('🌱 Seeding categories...');

  // Seed Fishing Gear categories
  for (const parent of FISHING_GEAR_CATEGORIES) {
    const existing = await prisma.category.findFirst({
      where: { name: parent.name, type: 'FISHING_GEAR', parentId: null }
    });

    const parentRecord = existing || await prisma.category.create({
      data: { name: parent.name, type: 'FISHING_GEAR' }
    });

    for (const childName of parent.children) {
      const childExists = await prisma.category.findFirst({
        where: { name: childName, type: 'FISHING_GEAR', parentId: parentRecord.id }
      });
      if (!childExists) {
        await prisma.category.create({
          data: { name: childName, type: 'FISHING_GEAR', parentId: parentRecord.id }
        });
      }
    }

    console.log(`  ✅ FISHING_GEAR: ${parent.name} (${parent.children.length} alt kategori)`);
  }

  // Seed Boat categories
  for (const parent of BOAT_CATEGORIES) {
    const existing = await prisma.category.findFirst({
      where: { name: parent.name, type: 'BOAT', parentId: null }
    });

    const parentRecord = existing || await prisma.category.create({
      data: { name: parent.name, type: 'BOAT' }
    });

    for (const childName of parent.children) {
      const childExists = await prisma.category.findFirst({
        where: { name: childName, type: 'BOAT', parentId: parentRecord.id }
      });
      if (!childExists) {
        await prisma.category.create({
          data: { name: childName, type: 'BOAT', parentId: parentRecord.id }
        });
      }
    }

    console.log(`  ✅ BOAT: ${parent.name} (${parent.children.length} alt kategori)`);
  }

  console.log('\n🎉 Category seeding completed!');
}

seed()
  .catch((error) => { console.error(error); process.exitCode = 1; })
  .finally(() => prisma.$disconnect());
