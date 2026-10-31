import { Router } from 'express';
import { getMyNotifications, markNotificationRead, getUnreadCount, sendNotificationToAll } from '../controllers/notificationController';
import { authenticate } from '../middlewares/authMiddleware';

const router = Router();
router.get('/', authenticate, getMyNotifications);
router.get('/unread-count', authenticate, getUnreadCount);
router.patch('/:id/read', authenticate, markNotificationRead);
router.post('/broadcast', authenticate, sendNotificationToAll);
export default router;