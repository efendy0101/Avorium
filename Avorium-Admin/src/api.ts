import axios from 'axios';

export type Role = 'USER' | 'ADMIN';
export type AdStatus = 'ACTIVE' | 'PASSIVE' | 'PENDING_REVIEW' | 'SOLD';
export type CategoryType = 'BOAT' | 'FISHING_GEAR';
export type OrderStatus = 'PENDING' | 'COMPLETED' | 'FAILED';

export interface AuthUser {
  id: string;
  email: string;
  name: string;
  role: Role;
  remainingAdLimit?: number;
}

export interface AuthResponse {
  message: string;
  token: string;
  user: AuthUser;
}

export interface Category {
  id: string;
  name: string;
  type: CategoryType;
  parentId?: string | null;
}

export interface AdminCategory {
  id: string;
  name: string;
  type: CategoryType;
  parentId?: string | null;
  _count: { ads: number };
}

export interface AdImage {
  id: string;
  url: string;
  isMain: boolean;
}

export interface AdminAd {
  id: string;
  adNumber: number;
  title: string;
  description: string;
  price: number;
  status: AdStatus;
  views: number;
  isUrgent: boolean;
  createdAt: string;
  videoUrl?: string | null;
  category: Category;
  user: Pick<AuthUser, 'id' | 'name' | 'email'>;
  images: AdImage[];
  boatDetails?: {
    make: string;
    model: string;
    year: number;
    length: number;
    hullMaterial: string;
    enginePower: number;
  } | null;
  fishingGearDetails?: {
    condition: string;
    brand: string;
    gearType: string;
  } | null;
}

export interface DashboardPayload {
  stats: {
    totalUsers: number;
    activeAds: number;
    pendingAds: number;
    totalAds: number;
    monthlyRevenue: number;
    completedOrders: number;
  };
  latestAds: AdminAd[];
}

export interface AdminUser {
  id: string;
  email: string;
  name: string;
  phone: string | null;
  role: Role;
  avatar: string | null;
  remainingAdLimit: number;
  isPremium: boolean;
  memberNumber: number;
  tcNo: string | null;
  createdAt: string;
  _count: {
    ads: number;
    orders: number;
  };
}

export interface AdminUserInput {
  name?: string;
  email?: string;
  phone?: string | null;
  role?: Role;
  remainingAdLimit?: number;
  isPremium?: boolean;
  tcNo?: string | null;
}

export interface PackageItem {
  id: string;
  name: string;
  price: number;
  adLimit: number;
  features: string | null;
  googleSkuId: string | null;
  _count?: {
    orders: number;
  };
}

export interface PackageInput {
  name: string;
  price: number;
  adLimit: number;
  features?: string;
  googleSkuId?: string;
}

export interface AdminOrder {
  id: string;
  amount: number;
  status: OrderStatus;
  createdAt: string;
  updatedAt: string;
  user: {
    id: string;
    name: string;
    email: string;
    phone: string | null;
  };
  package: {
    id: string;
    name: string;
    adLimit: number;
    price: number;
  };
}

export interface AppSettings {
  id: string;
  siteName: string;
  defaultAdLimit: number;
  adExpiryDays: number;
  announcementBanner: string | null;
  maintenanceMode: boolean;
  updatedAt: string;
}

export const tokenStorageKey = 'avorium_admin_token';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:5000/api'
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(tokenStorageKey);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const getErrorMessage = (error: unknown) => {
  if (axios.isAxiosError<{ message?: string }>(error)) {
    return error.response?.data?.message ?? error.message;
  }

  return error instanceof Error ? error.message : 'Beklenmeyen hata olustu.';
};