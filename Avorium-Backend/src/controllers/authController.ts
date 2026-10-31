import { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import { prisma } from '../prisma';
import { AuthRequest } from '../middlewares/authMiddleware';

import { JWT_SECRET } from '../jwtSecret';

const safeUser = (user: any) => ({
  id: user.id,
  memberNumber: user.memberNumber,
  email: user.email,
  name: user.name,
  phone: user.phone,
  tcNo: user.tcNo,
  city: user.city,
  district: user.district,
  avatar: user.avatar,
  avatarStatus: user.avatarStatus,
  pendingAvatar: user.pendingAvatar,
  isTcVerified: user.isTcVerified,
  isPhoneVerified: user.isPhoneVerified,
  role: user.role,
  isPremium: user.isPremium,
  remainingAdLimit: user.remainingAdLimit,
  createdAt: user.createdAt
});

export const register = async (req: Request, res: Response) => {
  try {
    const email = String(req.body.email ?? '').trim().toLowerCase();
    const password = String(req.body.password ?? '');
    const name = String(req.body.name ?? '').trim();
    const phone = req.body.phone ? String(req.body.phone).trim() : undefined;

    if (!email || !password || !name) {
      return res.status(400).json({ message: 'Lütfen zorunlu alanları doldurun: e-posta, şifre ve isim.' });
    }
    if (password.length < 6) {
      return res.status(400).json({ message: 'Şifre en az 6 karakter olmalıdır.' });
    }

    const existingUser = await prisma.user.findUnique({ where: { email } });
    if (existingUser) return res.status(400).json({ message: 'Bu e-posta adresi zaten kullanımda.' });

    const hashedPassword = await bcrypt.hash(password, 12);
    const user = await prisma.user.create({
      data: { email, password: hashedPassword, name, ...(phone ? { phone } : {}) }
    });

    const token = jwt.sign({ userId: user.id, role: user.role }, JWT_SECRET, { expiresIn: '30d' });
    res.status(201).json({ message: 'Kayıt başarılı', token, user: safeUser(user) });
  } catch (error: any) {
    console.error('Register error:', error);
    res.status(500).json({ message: 'Sunucu hatası', error: error.message });
  }
};

export const login = async (req: Request, res: Response) => {
  try {
    const email = String(req.body.email ?? '').trim().toLowerCase();
    const password = String(req.body.password ?? '');

    if (!email || !password) return res.status(400).json({ message: 'Lütfen e-posta ve şifrenizi girin.' });

    const user = await prisma.user.findUnique({ where: { email } });
    if (!user || !user.password) return res.status(400).json({ message: 'Geçersiz e-posta veya şifre.' });

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) return res.status(400).json({ message: 'Geçersiz e-posta veya şifre.' });

    const token = jwt.sign({ userId: user.id, role: user.role }, JWT_SECRET, { expiresIn: '30d' });
    res.json({ message: 'Giriş başarılı', token, user: safeUser(user) });
  } catch (error: any) {
    console.error('Login error:', error);
    res.status(500).json({ message: 'Sunucu hatası', error: error.message });
  }
};

export const me = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });

    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true, memberNumber: true, email: true, name: true, phone: true,
        tcNo: true, city: true, district: true, avatar: true,
        avatarStatus: true, pendingAvatar: true, isTcVerified: true, isPhoneVerified: true,
        role: true, isPremium: true, remainingAdLimit: true, createdAt: true
      }
    });

    if (!user) return res.status(404).json({ message: 'Kullanıcı bulunamadı.' });
    res.json({ user });
  } catch (error: any) {
    res.status(500).json({ message: 'Kullanıcı bilgileri alınamadı.', error: error.message });
  }
};

export const updateProfile = async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user?.userId;
    if (!userId) return res.status(401).json({ message: 'Yetkisiz' });

    const { name, phone, tcNo, city, district, avatar } = req.body;
    const data: any = {};

    if (name !== undefined) {
      const trimmedName = String(name).trim();
      if (trimmedName.length === 0) return res.status(400).json({ message: 'İsim boş olamaz.' });
      data.name = trimmedName;
    }
    if (phone !== undefined) data.phone = phone ? String(phone).trim() : null;
    if (city !== undefined) data.city = city ? String(city) : null;
    if (district !== undefined) data.district = district ? String(district) : null;
    if (tcNo !== undefined) {
      if (tcNo && !/^\d{11}$/.test(String(tcNo))) {
        return res.status(400).json({ message: 'TC kimlik numarası 11 haneli olmalıdır.' });
      }
      data.tcNo = tcNo ? String(tcNo) : null;
    }
    if (avatar !== undefined) {
      const currentUser = await prisma.user.findUnique({ where: { id: userId } });
      if (currentUser?.role === 'ADMIN') {
        data.avatar = avatar;
        data.avatarStatus = 'APPROVED';
      } else {
        data.pendingAvatar = avatar;
        data.avatarStatus = 'PENDING';
      }
    }

    if (Object.keys(data).length === 0) return res.status(400).json({ message: 'Güncellenecek alan gönderilmelidir.' });

    const user = await prisma.user.update({
      where: { id: userId },
      data,
      select: {
        id: true, memberNumber: true, email: true, name: true, phone: true,
        tcNo: true, avatar: true, avatarStatus: true, pendingAvatar: true,
        isTcVerified: true, isPhoneVerified: true,
        role: true, isPremium: true, remainingAdLimit: true, createdAt: true
      }
    });

    res.json({ message: 'Profil güncellendi.', user });
  } catch (error: any) {
    if (error.code === 'P2002') return res.status(400).json({ message: 'Bu TC kimlik numarası veya telefon zaten kullanımda.' });
    res.status(500).json({ message: 'Profil güncellenemedi.', error: error.message });
  }
};