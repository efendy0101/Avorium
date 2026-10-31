import { Router } from 'express';
import { createReport } from '../controllers/notificationController';
import { authenticate } from '../middlewares/authMiddleware';

const router = Router();
router.post('/', authenticate, createReport);
export default router;