import { Router, Request, Response } from 'express';
import { prisma } from '../prisma';

const router = Router();

router.get('/maintenance', async (_req: Request, res: Response) => {
  try {
    const setting = await (prisma as any).setting.findUnique({ where: { key: 'maintenance' } });
    const msgSetting = await (prisma as any).setting.findUnique({ where: { key: 'maintenanceMessage' } });
    const isUnderMaintenance = setting?.value === 'true';
    const message = msgSetting?.value || null;
    res.json({ isUnderMaintenance, message });
  } catch {
    res.json({ isUnderMaintenance: false, message: null });
  }
});

export default router;