import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate, useLocation } from 'react-router-dom';
import {
  CheckSquare, LayoutDashboard, LogOut, Package, Settings, Users, Tag, Anchor,
  ShoppingCart, FileText, AlertTriangle, Bell, MessageSquare
} from 'lucide-react';
import { tokenStorageKey, api } from './api';
import type { AuthUser } from './api';
import Dashboard from './pages/Dashboard';
import Login from './pages/Login';
import Approvals from './pages/Approvals';
import AdsPage from './pages/Ads';
import UsersPage from './pages/Users';
import PackagesPage from './pages/Packages';
import OrdersPage from './pages/Orders';
import SettingsPage from './pages/Settings';
import CategoriesPage from './pages/Categories';
import ReportsPage from './pages/Reports';
import NotificationsPage from './pages/Notifications';
import MessagesPage from './pages/Messages';
import './index.css';

const Sidebar = ({ onLogout, pendingCount, reportCount }: { onLogout: () => void; pendingCount: number; reportCount: number }) => {
  const location = useLocation();
  const a = (path: string) => location.pathname === path ? 'active' : '';
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-icon"><Anchor size={20} color="white" /></div>
        <span>Avorium</span>
      </div>
      <nav className="nav-links">
        <div className="nav-label">Yonetim</div>
        <Link to="/" className={`nav-link ${a('/')}`}><LayoutDashboard size={18}/>Dashboard</Link>
        <Link to="/approvals" className={`nav-link ${a('/approvals')}`}>
          <CheckSquare size={18}/>Ilan Onaylari
          {pendingCount > 0 && <span className="nav-badge">{pendingCount}</span>}
        </Link>
        <Link to="/ads" className={`nav-link ${a('/ads')}`}><FileText size={18}/>Tum Ilanlar</Link>
        <Link to="/reports" className={`nav-link ${a('/reports')}`}>
          <AlertTriangle size={18}/>Sikayetler
          {reportCount > 0 && <span className="nav-badge" style={{background:'#e74c3c'}}>{reportCount}</span>}
        </Link>
        <Link to="/users" className={`nav-link ${a('/users')}`}><Users size={18}/>Kullanicilar</Link>
        <Link to="/messages" className={`nav-link ${a('/messages')}`}><MessageSquare size={18}/>Mesajlar</Link>
        <Link to="/notifications" className={`nav-link ${a('/notifications')}`}><Bell size={18}/>Bildirimler</Link>
        <Link to="/orders" className={`nav-link ${a('/orders')}`}><ShoppingCart size={18}/>Siparisler</Link>
        <div className="nav-label" style={{marginTop:8}}>Icerik</div>
        <Link to="/packages" className={`nav-link ${a('/packages')}`}><Package size={18}/>Paketler</Link>
        <Link to="/categories" className={`nav-link ${a('/categories')}`}><Tag size={18}/>Kategoriler</Link>
        <div className="nav-label" style={{marginTop:8}}>Sistem</div>
        <Link to="/settings" className={`nav-link ${a('/settings')}`}><Settings size={18}/>Ayarlar</Link>
      </nav>
      <button className="nav-link logout-button" onClick={onLogout}><LogOut size={18}/>Cikis Yap</button>
    </aside>
  );
};

function App() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [authError, setAuthError] = useState('');
  const [pendingCount, setPendingCount] = useState(0);
  const [reportCount, setReportCount] = useState(0);

  useEffect(() => {
    const storedUser = localStorage.getItem('avorium_admin_user');
    if (storedUser && localStorage.getItem(tokenStorageKey)) {
      setUser(JSON.parse(storedUser) as AuthUser);
    }
  }, []);

  useEffect(() => {
    if (user && user.role !== 'ADMIN') {
      localStorage.removeItem(tokenStorageKey);
      localStorage.removeItem('avorium_admin_user');
      setUser(null);
      setAuthError('Bu panele sadece admin kullanicilar erisebilir.');
    }
  }, [user]);

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      api.get<{stats:{pendingAds:number}}>('/admin/dashboard')
        .then(r => setPendingCount(r.data.stats.pendingAds))
        .catch(() => {});
      api.get<{id:string;isReviewed:boolean}[]>('/admin/reports')
        .then(r => setReportCount(r.data.filter(x => !x.isReviewed).length))
        .catch(() => {});
    }
  }, [user]);

  const handleLogout = () => {
    localStorage.removeItem(tokenStorageKey);
    localStorage.removeItem('avorium_admin_user');
    setAuthError('');
    setUser(null);
  };

  if (!user) {
    return <Login onLogin={(nextUser) => { setAuthError(''); setUser(nextUser); }} initialError={authError} />;
  }
  if (user.role !== 'ADMIN') return null;

  return (
    <BrowserRouter>
      <div className="admin-layout">
        <Sidebar onLogout={handleLogout} pendingCount={pendingCount} reportCount={reportCount} />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/approvals" element={<Approvals />} />
            <Route path="/ads" element={<AdsPage />} />
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/messages" element={<MessagesPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/packages" element={<PackagesPage />} />
            <Route path="/categories" element={<CategoriesPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;