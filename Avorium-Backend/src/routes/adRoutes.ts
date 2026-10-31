import { Router } from 'express';
import { createAd, getAds, getAdDetails, getMyAds, updateMyAdStatus, updateMyAd } from '../controllers/adController';
import { authenticate } from '../middlewares/authMiddleware';

const router = Router();

router.get('/', getAds);
router.get('/my/list', authenticate, getMyAds);
router.patch('/my/:id/status', authenticate, updateMyAdStatus);
router.put('/my/:id', authenticate, updateMyAd);
router.get('/:id', getAdDetails);
router.post('/', authenticate, createAd);

export default router;