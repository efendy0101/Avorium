import { Request, Response } from 'express';
import { prisma } from '../prisma';
import { AuthRequest } from '../middlewares/authMiddleware';

// Disabled until server-side Google Play verification and account binding are configured.
export const verifyPurchase = async (_req: AuthRequest, res: Response) => {
  return res.status(503).json({ message: 'Satın alma doğrulaması henüz kullanılamıyor.' });
};

export const getPackages = async (req: Request, res: Response) => {
  try {
    const packages = await prisma.package.findMany({ orderBy: { price: 'asc' } });
    res.json(packages);
  } catch (error: any) {
    res.status(500).json({ message: 'Paketler getirilirken hata oluştu.', error: error.message });
  }
};
export const getMyOrders = async (req: any, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });
    const orders = await (prisma as any).iapOrder.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      select: { id: true, productId: true, amount: true, status: true, createdAt: true }
    });
    res.json(orders);
  } catch (error: any) {
    res.status(500).json({ message: 'Siparisler getirilemedi.', error: error.message });
  }
};