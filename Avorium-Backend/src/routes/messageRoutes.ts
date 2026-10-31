import { Router } from 'express';
import { sendMessage, getMessages, getConversation, deleteMessage, getAdminMessages, deleteAdminMessage } from '../controllers/messageController';
import { authenticate } from '../middlewares/authMiddleware';

const router = Router();
router.use(authenticate);
router.post('/', sendMessage);
router.get('/', getMessages);
router.get('/conversation/:userId', getConversation);
router.delete('/:id', deleteMessage);
export default router;