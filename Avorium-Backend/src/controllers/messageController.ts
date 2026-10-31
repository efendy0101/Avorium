import { Response } from 'express';
import { prisma } from '../prisma';
import { AuthRequest } from '../middlewares/authMiddleware';

export const sendMessage = async (req: AuthRequest, res: Response) => {
  try {
    const senderId = req.user?.userId;
    if (!senderId) return res.status(401).json({ message: 'Yetkisiz' });

    const { receiverId, adId, content } = req.body;
    if (!receiverId || !adId || !content) {
      return res.status(400).json({ message: 'Alıcı, ilan ve mesaj içeriği zorunludur.' });
    }

    const message = await prisma.message.create({
      data: {
        senderId,
        receiverId,
        adId,
        content
      }
    });

    res.status(201).json({ message: 'Mesaj gönderildi', data: message });
  } catch (error: any) {
    res.status(500).json({ message: 'Mesaj gönderilirken hata oluştu', error: error.message });
  }
};

export const getMessages = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });

    const messages = await prisma.message.findMany({
      where: {
        OR: [
          { senderId: userId },
          { receiverId: userId }
        ]
      },
      include: {
        sender: { select: { name: true, avatar: true } },
        receiver: { select: { name: true, avatar: true } },
        ad: { select: { title: true, images: true } }
      },
      orderBy: { createdAt: 'desc' }
    });

    res.json(messages);
  } catch (error: any) {
    res.status(500).json({ message: 'Mesajlar getirilirken hata oluştu', error: error.message });
  }
};

export const getConversation = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });
    const otherId = req.params.userId as string;
    const messages = await prisma.message.findMany({
      where: {
        OR: [
          { senderId: userId, receiverId: otherId },
          { senderId: otherId, receiverId: userId }
        ]
      },
      include: {
        sender: { select: { id: true, name: true, avatar: true } },
        receiver: { select: { id: true, name: true, avatar: true } },
        ad: { select: { id: true, title: true } }
      },
      orderBy: { createdAt: 'asc' }
    });
    await prisma.message.updateMany({
      where: { senderId: otherId, receiverId: userId, isRead: false },
      data: { isRead: true }
    });
    res.json(messages);
  } catch (e: any) { res.status(500).json({ message: 'Hata', error: e.message }); }
};

export const deleteMessage = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });
    const id = req.params.id as string;
    const msg = await prisma.message.findUnique({ where: { id } });
    if (!msg || msg.senderId !== userId) return res.status(403).json({ message: 'Bu mesaji silemezsiniz.' });
    await prisma.message.delete({ where: { id } });
    res.json({ message: 'Mesaj silindi.' });
  } catch (e: any) { res.status(500).json({ message: 'Hata', error: e.message }); }
};

export const getAdminMessages = async (_req: any, res: Response) => {
  try {
    const messages = await prisma.message.findMany({
      include: {
        sender: { select: { id: true, name: true, email: true } },
        receiver: { select: { id: true, name: true, email: true } },
        ad: { select: { id: true, title: true } }
      },
      orderBy: { createdAt: 'desc' },
      take: 200
    });
    res.json(messages);
  } catch (e: any) { res.status(500).json({ message: 'Mesajlar alinamadi.', error: e.message }); }
};

export const deleteAdminMessage = async (req: any, res: Response) => {
  try {
    const id = req.params.id as string;
    await prisma.message.delete({ where: { id } });
    res.json({ message: 'Mesaj silindi.' });
  } catch (e: any) { res.status(500).json({ message: 'Hata', error: e.message }); }
};