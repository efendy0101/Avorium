import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
import { PrismaMariaDb } from '@prisma/adapter-mariadb';
const url = process.env.DATABASE_URL;
if (!url) throw new Error('DATABASE_URL must be configured.');
export const prisma = new PrismaClient({ adapter: new PrismaMariaDb(url) });
