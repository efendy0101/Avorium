import { Request, Response } from 'express';
import { Prisma } from '@prisma/client';
import { prisma } from '../prisma';
import { AuthRequest } from '../middlewares/authMiddleware';

const BASE_URL = process.env.BASE_URL || 'http://localhost:5000';

const getSingleQueryValue = (value: unknown): string | undefined => {
  if (Array.isArray(value)) return typeof value[0] === 'string' ? value[0] : undefined;
  return typeof value === 'string' ? value : undefined;
};

const getPositiveNumber = (value: unknown, fallback: number): number => {
  const parsed = Number(getSingleQueryValue(value) ?? value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
};

const resolveUrl = (url: string): string => {
  if (!url) return url;
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return `${BASE_URL}${url.startsWith('/') ? '' : '/'}${url}`;
};

const resolveAdUrls = (ad: any): any => {
  if (!ad) return ad;
  return {
    ...ad,
    images: ad.images?.map((img: any) => ({ ...img, url: resolveUrl(img.url) })) ?? [],
    videoUrl: ad.videoUrl ? resolveUrl(ad.videoUrl) : null,
  };
};

export const createAd = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });

    const { title, description, price, categoryId, type, boatDetails, fishingGearDetails, images, videoUrl, showPhone, isUrgent, city, district } = req.body;

    if (!title || !description || !price || !categoryId || (type !== 'BOAT' && type !== 'FISHING_GEAR')) {
      return res.status(400).json({ message: 'Lütfen zorunlu ilan alanlarını doldurun.' });
    }

    const numericPrice = Number(price);
    if (!Number.isFinite(numericPrice) || numericPrice <= 0) {
      return res.status(400).json({ message: 'Geçerli bir fiyat girin.' });
    }

    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user) return res.status(404).json({ message: 'Kullanıcı bulunamadı.' });

    if (user.role !== 'ADMIN' && user.remainingAdLimit <= 0) {
      return res.status(403).json({ message: 'İlan yayınlama limitiniz dolmuştur. Lütfen yeni paket satın alın.' });
    }

    const settings = await prisma.appSettings.upsert({
      where: { id: 'singleton' },
      update: {},
      create: { id: 'singleton' }
    });

    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + (settings.adExpiryDays || 15));

    const adData: Prisma.AdCreateInput = {
      title,
      description,
      price: numericPrice,
      expiresAt,
      status: 'PENDING_REVIEW',
      videoUrl: typeof videoUrl === 'string' && videoUrl.trim().length > 0 ? videoUrl.trim() : null,
      showPhone: Boolean(showPhone),
      isUrgent: Boolean(isUrgent),
      city: city ? String(city) : null,
      district: district ? String(district) : null,
      user: { connect: { id: userId } },
      category: { connect: { id: categoryId } }
    };

    if (type === 'BOAT' && boatDetails) adData.boatDetails = { create: boatDetails };
    if (type === 'FISHING_GEAR' && fishingGearDetails) adData.fishingGearDetails = { create: fishingGearDetails };

    if (Array.isArray(images) && images.length > 0) {
      adData.images = {
        create: images
          .filter((url): url is string => typeof url === 'string' && url.trim().length > 0)
          .map((url, index) => ({ url, isMain: index === 0 }))
      };
    }

    const ad = await prisma.ad.create({
      data: adData,
      include: { boatDetails: true, fishingGearDetails: true, images: true, category: true }
    });

    if (user.role !== 'ADMIN') {
      await prisma.user.update({
        where: { id: userId },
        data: { remainingAdLimit: user.remainingAdLimit - 1 }
      });
    }

    res.status(201).json({ message: 'İlan başarıyla eklendi, onay bekliyor.', ad: resolveAdUrls(ad) });
  } catch (error: any) {
    console.error('Create Ad error:', error);
    res.status(500).json({ message: 'İlan eklenirken hata oluştu.', error: error.message });
  }
};

export const updateMyAd = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });

    const id = getSingleQueryValue(req.params.id);
    if (!id) return res.status(400).json({ message: 'İlan id zorunludur.' });

    const user = await prisma.user.findUnique({ where: { id: userId } });
    const ad = await prisma.ad.findFirst({ where: { id, userId } });
    if (!ad) return res.status(404).json({ message: 'İlan bulunamadı.' });

    if (user?.role !== 'ADMIN' && ad.editCount >= 2) {
      return res.status(403).json({ message: 'Bu ilan için düzenleme hakkınız dolmuştur.' });
    }

    const { title, description, price, isUrgent, showPhone, city, district, categoryId, type, boatDetails, fishingGearDetails, images } = req.body;
    const data: Prisma.AdUpdateInput = {};

    if (user?.role !== 'ADMIN') data.editCount = { increment: 1 };

    if (title !== undefined) data.title = String(title);
    if (description !== undefined) data.description = String(description);
    if (price !== undefined) {
      const p = Number(price);
      if (!Number.isFinite(p) || p <= 0) return res.status(400).json({ message: 'Geçerli bir fiyat girin.' });
      data.price = p;
    }
    if (isUrgent !== undefined) data.isUrgent = Boolean(isUrgent);
    if (showPhone !== undefined) data.showPhone = Boolean(showPhone);
    if (city !== undefined) data.city = city ? String(city) : null;
    if (district !== undefined) data.district = district ? String(district) : null;
    if (categoryId !== undefined) data.category = { connect: { id: categoryId } };

    if (type === 'BOAT' && boatDetails) {
      data.boatDetails = { upsert: { create: boatDetails, update: boatDetails } };
    } else if (type === 'FISHING_GEAR' && fishingGearDetails) {
      data.fishingGearDetails = { upsert: { create: fishingGearDetails, update: fishingGearDetails } };
    }

    if (Array.isArray(images) && images.length > 0) {
      // Simplistic: replace all images
      await prisma.adImage.deleteMany({ where: { adId: id } });
      data.images = {
        create: images.map((url: string, index: number) => ({ url, isMain: index === 0 }))
      };
    }

    const updated = await prisma.ad.update({
      where: { id },
      data,
      include: { images: true, category: true, boatDetails: true, fishingGearDetails: true }
    });

    res.json({ message: 'İlan güncellendi.', ad: resolveAdUrls(updated) });
  } catch (error: any) {
    res.status(500).json({ message: 'İlan güncellenemedi.', error: error.message });
  }
};

export const getAds = async (req: Request, res: Response) => {
  try {
    const type = getSingleQueryValue(req.query.type);
    const categoryId = getSingleQueryValue(req.query.categoryId);
    const minPrice = getSingleQueryValue(req.query.minPrice);
    const maxPrice = getSingleQueryValue(req.query.maxPrice);
    const searchRaw = getSingleQueryValue(req.query.search);
    const page = getPositiveNumber(req.query.page, 1);
    const limit = Math.min(getPositiveNumber(req.query.limit, 20), 100);

    const whereClause: Prisma.AdWhereInput = { status: 'ACTIVE' };

    // Search: min 3 chars, also search by adNumber
    if (searchRaw && searchRaw.trim().length >= 3) {
      const search = searchRaw.trim();
      const adNum = parseInt(search, 10);
      if (!isNaN(adNum)) {
        whereClause.OR = [
          { title: { contains: search } },
          { description: { contains: search } },
          { adNumber: adNum }
        ];
      } else {
        whereClause.OR = [
          { title: { contains: search } },
          { description: { contains: search } }
        ];
      }
    }

    if (categoryId) {
      whereClause.categoryId = categoryId;
    } else if (type === 'BOAT' || type === 'FISHING_GEAR') {
      whereClause.category = { type };
    }

    const priceFilter: Prisma.FloatFilter = {};
    if (minPrice) priceFilter.gte = Number(minPrice);
    if (maxPrice) priceFilter.lte = Number(maxPrice);
    if (Object.keys(priceFilter).length > 0) whereClause.price = priceFilter;

    const skip = (page - 1) * limit;

    const [ads, total] = await Promise.all([
      prisma.ad.findMany({
        where: whereClause,
        skip,
        take: limit,
        include: {
          images: true,
          boatDetails: true,
          fishingGearDetails: true,
          category: true,
          user: { select: { id: true, name: true, isPremium: true } }
        },
        orderBy: [
          { user: { isPremium: 'desc' } },
          { isUrgent: 'desc' },
          { createdAt: 'desc' }
        ]
      }),
      prisma.ad.count({ where: whereClause })
    ]);

    res.json({ ads: ads.map(resolveAdUrls), total, page, pages: Math.ceil(total / limit) });
  } catch (error: any) {
    console.error('Get Ads error:', error);
    res.status(500).json({ message: 'İlanlar getirilirken hata oluştu.', error: error.message });
  }
};

export const getMyAds = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });

    const ads = await prisma.ad.findMany({
      where: { userId },
      include: { images: true, boatDetails: true, fishingGearDetails: true, category: true },
      orderBy: { createdAt: 'desc' }
    });

    res.json(ads.map(resolveAdUrls));
  } catch (error: any) {
    res.status(500).json({ message: 'İlanlarınız getirilirken hata oluştu.', error: error.message });
  }
};

export const updateMyAdStatus = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });

    const id = getSingleQueryValue(req.params.id);
    const { status } = req.body;
    if (!id || (status !== 'PASSIVE' && status !== 'SOLD')) {
      return res.status(400).json({ message: 'Geçerli ilan id ve durum bilgisi zorunludur.' });
    }

    const ad = await prisma.ad.findFirst({ where: { id, userId } });
    if (!ad) return res.status(404).json({ message: 'İlan bulunamadı.' });

    const updatedAd = await prisma.ad.update({
      where: { id },
      data: { status },
      include: { images: true, category: true }
    });

    res.json({ message: 'İlan güncellendi.', ad: resolveAdUrls(updatedAd) });
  } catch (error: any) {
    res.status(500).json({ message: 'İlan güncellenirken hata oluştu.', error: error.message });
  }
};

export const getAdDetails = async (req: Request, res: Response) => {
  try {
    const id = getSingleQueryValue(req.params.id);
    if (!id) return res.status(400).json({ message: 'İlan id bilgisi zorunludur.' });

    const ad = await prisma.ad.findUnique({
      where: { id },
      include: {
        images: true,
        boatDetails: true,
        fishingGearDetails: true,
        category: { include: { parent: true } },
        user: {
          select: {
            id: true,
            name: true,
            phone: true,
            avatar: true,
            isPremium: true,
            memberNumber: true,
            createdAt: true
          }
        }
      }
    });

    if (!ad) return res.status(404).json({ message: 'İlan bulunamadı.' });

    const adResponse: any = resolveAdUrls({ ...ad });
    if (!ad.showPhone && adResponse.user) {
      adResponse.user = { ...adResponse.user, phone: null };
    }

    await prisma.ad.update({ where: { id }, data: { views: { increment: 1 } } });

    res.json(adResponse);
  } catch (error: any) {
    res.status(500).json({ message: 'İlan detayları getirilirken hata oluştu.', error: error.message });
  }
};