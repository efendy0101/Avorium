import { useEffect, useState } from 'react';
import { Users, CheckSquare, BarChart2, TrendingUp, Eye } from 'lucide-react';
import { api, getErrorMessage } from '../api';
import type { DashboardPayload, AdStatus } from '../api';

const fmt = (v: number) => new Intl.NumberFormat('tr-TR',{style:'currency',currency:'TRY'}).format(v);

const statusLabel: Record<AdStatus, string> = {
  ACTIVE: 'Aktif', PASSIVE: 'Pasif', PENDING_REVIEW: 'Onay Bekliyor', SOLD: 'Satildi'
};

const Dashboard = () => {
  const [payload, setPayload] = useState<DashboardPayload | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get<DashboardPayload>('/admin/dashboard')
      .then(r => setPayload(r.data))
      .catch(e => setError(getErrorMessage(e)));
  }, []);

  if (error) return <div className="alert danger">{error}</div>;
  if (!payload) return <div className="loading">Yukleniyor</div>;

  const { stats, latestAds } = payload;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p>Platformdaki ilan, kullanici ve satis durumunu takip edin.</p>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon blue"><Users size={22}/></div>
          <span className="stat-title">Toplam Kullanici</span>
          <span className="stat-value">{stats.totalUsers.toLocaleString('tr-TR')}</span>
        </div>
        <div className="stat-card">
          <div className="stat-icon green"><BarChart2 size={22}/></div>
          <span className="stat-title">Aktif Ilanlar</span>
          <span className="stat-value">{stats.activeAds.toLocaleString('tr-TR')}</span>
        </div>
        <div className="stat-card">
          <div className="stat-icon yellow"><CheckSquare size={22}/></div>
          <span className="stat-title">Onay Bekleyen</span>
          <span className="stat-value" style={{color:'var(--warning)'}}>{stats.pendingAds}</span>
        </div>
        <div className="stat-card">
          <div className="stat-icon cyan"><TrendingUp size={22}/></div>
          <span className="stat-title">Toplam Gelir</span>
          <span className="stat-value" style={{fontSize:24,color:'var(--success)'}}>{fmt(stats.monthlyRevenue)}</span>
        </div>
      </div>

      <div className="table-container">
        <div className="table-header">
          <h2>Son Eklenen Ilanlar</h2>
          <span className="muted">{stats.totalAds} toplam ilan</span>
        </div>
        <table>
          <thead>
            <tr>
              <th>Ilan</th>
              <th>Kategori</th>
              <th>Satici</th>
              <th>Fiyat</th>
              <th>Goruntulenme</th>
              <th>Durum</th>
            </tr>
          </thead>
          <tbody>
            {latestAds.map(ad => (
              <tr key={ad.id}>
                <td>
                  <div className="td-with-thumb">
                    {ad.images?.[0]
                      ? <img className="table-thumb" src={ad.images[0].url} alt={ad.title}/>
                      : <div className="table-thumb-placeholder">IMG</div>
                    }
                    <div>
                      <strong>{ad.title}</strong>
                      <div className="table-description">{ad.description}</div>
                    </div>
                  </div>
                </td>
                <td>{ad.category?.name??'-'}</td>
                <td>{ad.user?.name??'-'}</td>
                <td><strong>{fmt(ad.price)}</strong></td>
                <td><span className="info-chip"><Eye size={11}/>{ad.views??0}</span></td>
                <td><span className={`badge ${ad.status.toLowerCase().replace('_','-')}`}>{statusLabel[ad.status]}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Dashboard;
