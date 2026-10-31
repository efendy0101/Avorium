import { Request, Response } from 'express';
import { prisma } from '../prisma';

export const getCategories = async (req: Request, res: Response) => {
  try {
    const categories = await prisma.category.findMany({
      include: {
        children: {
          orderBy: { name: 'asc' }
        }
      },
      orderBy: [{ type: 'asc' }, { name: 'asc' }]
    });

    // Return flat list for compatibility but include parentId and children
    res.json(categories);
  } catch (error: any) {
    res.status(500).json({ message: 'Kategoriler getirilirken hata oluştu.', error: error.message });
  }
};
