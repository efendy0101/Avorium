import { prisma } from '../prisma';
import { Router } from 'express';
import {
  createPackage, deletePackage, getAdminAds, getAdminPackages, getDashboardStats,
  getUsers, updateAdStatus, updateAd, updateUserAccess, updateUser, deleteUser,
  updatePackage, getSettings, updateSettings, getAdminCategories, createCategory,
  deleteCategory, getAdminOrders, updateOrder, getAdminReports, reviewReport, getAdminNotifications
} from '../controllers/adminController';
import { authenticate, requireAdmin } from '../middlewares/authMiddleware';
import { sendNotificationToAll } from '../controllers/notificationController';
import { getAdminMessages, deleteAdminMessage } from '../controllers/messageController';

const router = Router();
router.use(authenticate, requireAdmin);

router.get('/dashboard', getDashboardStats);
router.get('/ads', getAdminAds);
router.patch('/ads/:id/status', updateAdStatus);
router.put('/ads/:id', updateAd);
router.delete('/ads/:id', async (req, res) => {
  try {
    const id = req.params.id;
    await prisma.report.deleteMany({ where: { adId: id } });
    await prisma.message.deleteMany({ where: { adId: id } });
    await prisma.adImage.deleteMany({ where: { adId: id } });
    await prisma.boatDetails.deleteMany({ where: { adId: id } });
    await prisma.fishingGearDetails.deleteMany({ where: { adId: id } });
    await prisma.ad.delete({ where: { id } });
    res.json({ message: 'Ilan silindi.' });
  } catch (e: any) { res.status(500).json({ message: 'Ilan silinemedi.', error: e.message }); }
});
router.get('/users', getUsers);
router.patch('/users/:id/access', updateUserAccess);
router.put('/users/:id', updateUser);
router.delete('/users/:id', deleteUser);
router.get('/packages', getAdminPackages);
router.post('/packages', createPackage);
router.patch('/packages/:id', updatePackage);
router.delete('/packages/:id', deletePackage);
router.get('/orders', getAdminOrders);
router.patch('/orders/:id', updateOrder);
router.get('/settings', getSettings);
router.patch('/settings', updateSettings);
router.get('/categories', getAdminCategories);
router.post('/categories', createCategory);
router.delete('/categories/:id', deleteCategory);
router.get('/reports', getAdminReports);
router.patch('/reports/:id/review', reviewReport);
router.get('/notifications', getAdminNotifications);

// Avatar management
router.get('/pending-avatars', async (_req, res) => {
  try {
    const users = await prisma.user.findMany({
      where: { avatarStatus: 'PENDING' },
      select: { id: true, name: true, email: true, pendingAvatar: true, avatar: true }
    });
    res.json(users);
  } catch (e: any) { res.status(500).json({ message: 'Hata', error: e.message }); }
});

router.patch('/users/:id/avatar-status', async (req, res) => {
  try {
    const id = req.params.id;
    const { avatarStatus } = req.body;
    if (!['APPROVED', 'REJECTED'].includes(avatarStatus)) return res.status(400).json({ message: 'Gecersiz durum' });

    const user = await prisma.user.findUnique({ where: { id } });
    if (!user) return res.status(404).json({ message: 'Kullanici bulunamadi' });

    const data: any = { avatarStatus };
    if (avatarStatus === 'APPROVED' && user.pendingAvatar) {
      data.avatar = user.pendingAvatar;
      data.pendingAvatar = null;
    } else if (avatarStatus === 'REJECTED') {
      data.pendingAvatar = null;
    }

    const updated = await prisma.user.update({
      where: { id },
      data,
      select: { id: true, name: true, email: true, avatar: true, avatarStatus: true, role: true }
    });
    res.json({ message: 'Resim durumu guncellendi', user: updated });
  } catch (e: any) { res.status(500).json({ message: 'Hata', error: e.message }); }
});

router.post('/notifications/broadcast', sendNotificationToAll);
router.get('/messages', getAdminMessages);
router.delete('/messages/:id', deleteAdminMessage);

export default router;