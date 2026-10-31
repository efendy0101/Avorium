import express from 'express';
import type { Router } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import path from 'path';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;
const corsOrigins = process.env.CORS_ORIGIN?.split(',')
  .map((o) => o.trim())
  .filter(Boolean);

// Validate before accepting any requests.
require('./jwtSecret');

let startupError: string | null = null;

app.use(cors({ origin: corsOrigins && corsOrigins.length > 0 ? corsOrigins : true }));
app.use(express.json({ limit: '10mb' }));
app.use('/uploads', express.static(path.resolve(process.cwd(), 'uploads')));

app.get('/api/health', (_req, res) => {
  res.json({ status: startupError ? 'DEGRADED' : 'OK', message: 'Avorium API is running' });
});

const loadRoute = (routePath: string): Router => require(routePath).default as Router;

try {
  app.use('/api/auth', loadRoute('./routes/authRoutes'));
  app.use('/api/ads', loadRoute('./routes/adRoutes'));
  app.use('/api/upload', loadRoute('./routes/uploadRoutes'));
  app.use('/api/messages', loadRoute('./routes/messageRoutes'));
  app.use('/api/packages', loadRoute('./routes/packageRoutes'));
  app.use('/api/categories', loadRoute('./routes/categoryRoutes'));
  app.use('/api/admin', loadRoute('./routes/adminRoutes'));
  app.use('/api/notifications', loadRoute('./routes/notificationRoutes'));
  app.use('/api/reports', loadRoute('./routes/reportRoutes'));
  app.use('/api/settings', loadRoute('./routes/settingsRoutes'));
} catch (error) {
  startupError = error instanceof Error ? `${error.name}: ${error.message}` : String(error);
  console.error('Route startup error:', startupError);
}

app.listen(PORT, () => console.log(`Server running on port ${PORT}`));