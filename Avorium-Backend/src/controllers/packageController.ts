import { Request, Response } from 'express';
import { prisma } from '../prisma';

export const getPackages = async (req: Request, res: Response) => {
  try {
    const packages = await prisma.package.findMany({
      orderBy: { price: 'asc' }
    });

    res.json(packages);
  } catch (error: any) {
    res.status(500).json({ message: 'Paketler getirilirken hata oluştu.', error: error.message });
  }
};
