import { Router } from 'express';
import { authenticate } from '../middlewares/authMiddleware';
import { upload } from '../middlewares/uploadMiddleware';

const router = Router();

const cpUpload = upload.fields([
  { name: 'images', maxCount: 5 },
  { name: 'video', maxCount: 1 }
]);

router.post('/', authenticate, cpUpload, (req, res) => {
  try {
    const files = req.files as { [fieldname: string]: Express.Multer.File[] };
    if (!files || (Object.keys(files).length === 0)) {
      return res.status(400).json({ message: 'Lütfen dosya seçin.' });
    }

    const imageUrls = files['images']?.map(f => `/uploads/${f.filename}`) || [];
    const videoUrl = files['video']?.[0] ? `/uploads/${files['video'][0].filename}` : null;

    res.status(200).json({ urls: imageUrls, videoUrl });
  } catch (error: any) {
    res.status(500).json({ message: 'Yükleme hatası', error: error.message });
  }
});

export default router;
