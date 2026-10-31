import { Response } from 'express';
import { prisma } from '../prisma';
import { AuthRequest } from '../middlewares/authMiddleware';

export const createReport = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });
    const { adId, reason, detail } = req.body;
    const validReasons = ['FAKE_AD','WRONG_INFO','SPAM','INAPPROPRIATE','FRAUD','OTHER'];
    if (!adId || !validReasons.includes(reason)) return res.status(400).json({ message: 'adId ve gecerli reason zorunludur.' });
    const ad = await prisma.ad.findUnique({ where: { id: adId } });
    if (!ad) return res.status(404).json({ message: 'Ilan bulunamadi.' });
    const existing = await prisma.report.findFirst({ where: { adId, userId } });
    if (existing) return res.status(409).json({ message: 'Bu ilani zaten sikayet ettiniz.' });
    const report = await prisma.report.create({ data: { adId, userId, reason, detail: detail || null } });
    res.status(201).json({ message: 'Sikayet iletildi.', report });
  } catch (e: any) { res.status(500).json({ message: 'Sikayet gonderilemedi.', error: e.message }); }
};

export const getMyNotifications = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });
    const notifications = await prisma.notification.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      take: 50
    });
    res.json(notifications);
  } catch (e: any) { res.status(500).json({ message: 'Bildirimler alinamadi.', error: e.message }); }
};

export const markNotificationRead = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });
    const id = req.params.id as string;
    if (id === 'all') {
      await prisma.notification.updateMany({ where: { userId }, data: { isRead: true } });
      return res.json({ message: 'Tum bildirimler okundu.' });
    }
    await prisma.notification.updateMany({ where: { id, userId }, data: { isRead: true } });
    res.json({ message: 'Bildirim okundu.' });
  } catch (e: any) { res.status(500).json({ message: 'Hata.', error: e.message }); }
};

export const getUnreadCount = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });
    const count = await prisma.notification.count({ where: { userId, isRead: false } });
    res.json({ count });
  } catch (e: any) { res.status(500).json({ message: 'Hata.', error: e.message }); }
};

export const sendNotificationToAll = async (req: AuthRequest, res: Response) => {
  try {
    if (req.user?.role !== 'ADMIN') return res.status(403).json({ message: 'Yetkisiz' });
    const { title, body, type } = req.body;
    if (!title || !body) return res.status(400).json({ message: 'title ve body zorunludur.' });
    const users = await prisma.user.findMany({ select: { id: true } });
    await prisma.notification.createMany({
      data: users.map(u => ({ userId: u.id, type: (type || 'ANNOUNCEMENT') as any, title, body, data: null }))
    });
    res.json({ message: `${users.length} kullaniciya bildirim gonderildi.` });
  } catch (e: any) { res.status(500).json({ message: 'Bildirim gonderilemedi.', error: e.message }); }
};

export const createNotification = async (userId: string, type: string, title: string, body: string, data?: string) => {
  try {
    await prisma.notification.create({ data: { userId, type: type as any, title, body, data: data ?? null } });
  } catch (_) {}
};