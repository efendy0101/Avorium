import { Router } from 'express';
import { verifyPurchase, getPackages, getMyOrders } from '../controllers/iapController';
import { authenticate } from '../middlewares/authMiddleware';

const router = Router();

router.get('/', getPackages);
router.post('/verify', authenticate, verifyPurchase);
router.get('/my-orders', authenticate, getMyOrders);

export default router;