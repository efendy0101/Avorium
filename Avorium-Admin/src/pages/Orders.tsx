import { useCallback, useEffect, useState } from 'react';
import { AlertTriangle, Eye, X } from 'lucide-react';
import { api, getErrorMessage } from '../api';
import type { AdminOrder, OrderStatus } from '../api';

const fmt = (v: number) => new Intl.NumberFormat('tr-TR',{style:'currency',currency:'TRY'}).format(v);
const statusLabel: Record<OrderStatus,string> = {
  PENDING:'Bekliyor', COMPLETED:'Tamamlandı', FAILED:'Başarısız'
};
const statusClass: Record<OrderStatus,string> = {
  PENDING:'pending', COMPLETED:'active', FAILED:'sold'
};

const OrdersPage = () => {
  const [orders, setOrders] = useState<AdminOrder[]>([]);
  const [filter, setFilter] = useState<OrderStatus|'ALL'>('ALL');
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [detail, setDetail] = useState<AdminOrder|null>(null);

  const load = useCallback(async () => {
    setIsLoading(true); setError('');
    try {
      const path = filter==='ALL'?'/admin/orders':`/admin/orders?status=${filter}`;
      const { data } = await api.get<AdminOrder[]>(path);
      setOrders(data);
    } catch(e){ setError(getErrorMessage(e)); }
    finally { setIsLoading(false); }
  }, [filter]);

  useEffect(()=>{ void load(); },[load]);

  const updateStatus = async (id: string, status: OrderStatus) => {
    setError(''); setSuccess('');
    try {
      await api.patch(`/admin/orders/${id}`, {status});
      setDetail(null);
      setSuccess('Sipariş güncellendi.');
      setTimeout(() => setSuccess(''), 3000);
      await load();
    } catch(e){ setError(getErrorMessage(e)); }
  };

  const filtered = orders.filter(o =>
    o.user.name.toLowerCase().includes(search.toLowerCase()) ||
    o.user.email.toLowerCase().includes(search.toLowerCase()) ||
    o.package.name.toLowerCase().includes(search.toLowerCase()) ||
    false
  );

  return (
    <div>
      <div className="page-header">
        <div><h1>Siparişler</h1><p>İlan paketi satın alımlarını görüntüleyin ve yönetin.</p></div>
        <div style={{display:'flex',gap:10}}>
          <input className="table-search" value={search} onChange={e=>setSearch(e.target.value)} placeholder="Kullanıcı, paket veya sipariş no ara..."/>
          <select className="table-select" value={filter} onChange={e=>setFilter(e.target.value as OrderStatus|'ALL')}>
            <option value="ALL">Tümü</option>
            <option value="PENDING">Bekleyen</option>
            <option value="COMPLETED">Tamamlanan</option>
            <option value="FAILED">Başarısız</option>
          </select>
        </div>
      </div>

      {error&&<div className="alert danger"><AlertTriangle size={16}/>{error}</div>}
      {success&&<div className="alert success">{success}</div>}
      {isLoading&&<div className="loading">Yükleniyor</div>}

      <div className="table-container">
        <div className="table-header">
          <h2>Sipariş Listesi</h2>
          <span className="muted">{filtered.length} sonuç</span>
        </div>
        <table>
          <thead><tr><th>Sipariş No</th><th>Kullanıcı</th><th>E-posta</th><th>Paket</th><th>Tutar</th><th>Durum</th><th>Tarih</th><th>İşlem</th></tr></thead>
          <tbody>
            {filtered.map(order=>(
              <tr key={order.id}>
                <td style={{fontFamily:'monospace',fontSize:11,color:'var(--text-muted)'}}>{order.id.slice(0,8)}</td>
                <td><strong>{order.user.name}</strong></td>
                <td style={{color:'var(--text-muted)'}}>{order.user.email}</td>
                <td>{order.package.name}</td>
                <td><strong>{fmt(order.amount)}</strong></td>
                <td><span className={`badge ${statusClass[order.status]}`}>{statusLabel[order.status]}</span></td>
                <td style={{color:'var(--text-muted)',fontSize:12}}>{new Date(order.createdAt).toLocaleDateString('tr-TR')}</td>
                <td>
                  <div className="actions">
                    <button className="btn btn-ghost btn-sm" onClick={()=>setDetail(order)}><Eye size={13}/>Detay</button>
                  </div>
                </td>
              </tr>
            ))}
            {filtered.length===0&&!isLoading&&<tr><td colSpan={8} className="empty">Kayıt bulunamadı.</td></tr>}
          </tbody>
        </table>
      </div>

      {/* Detail Modal */}
      {detail&&(
        <div className="modal-overlay" onClick={()=>setDetail(null)}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h2>Sipariş Detayı</h2>
                <span className={`badge ${statusClass[detail.status]}`}>{statusLabel[detail.status]}</span>
              </div>
              <button className="modal-close" onClick={()=>setDetail(null)}><X size={16}/></button>
            </div>

            <div className="detail-section">
              <h4>Sipariş Bilgileri</h4>
              <div className="detail-grid">
                <div className="detail-item"><div className="di-label">Sipariş No</div><div className="di-value" style={{fontFamily:'monospace'}}>{detail.id}</div></div>
                <div className="detail-item"><div className="di-label">Tutar</div><div className="di-value">{fmt(detail.amount)}</div></div>
                <div className="detail-item"><div className="di-label">Tarih</div><div className="di-value">{new Date(detail.createdAt).toLocaleString('tr-TR')}</div></div>
                <div className="detail-item"><div className="di-label">Son Güncelleme</div><div className="di-value">{new Date(detail.updatedAt).toLocaleString('tr-TR')}</div></div>
              </div>
            </div>

            <div className="detail-section">
              <h4>Kullanıcı Bilgileri</h4>
              <div className="detail-grid">
                <div className="detail-item"><div className="di-label">Ad Soyad</div><div className="di-value">{detail.user.name}</div></div>
                <div className="detail-item"><div className="di-label">E-posta</div><div className="di-value">{detail.user.email}</div></div>
                <div className="detail-item"><div className="di-label">Telefon</div><div className="di-value">{detail.user.phone??'-'}</div></div>
              </div>
            </div>

            <div className="detail-section">
              <h4>Paket Bilgileri</h4>
              <div className="detail-grid">
                <div className="detail-item"><div className="di-label">Paket</div><div className="di-value">{detail.package.name}</div></div>
                <div className="detail-item"><div className="di-label">İlan Hakkı</div><div className="di-value">{detail.package.adLimit} adet</div></div>
                <div className="detail-item"><div className="di-label">Paket Fiyatı</div><div className="di-value">{fmt(detail.package.price)}</div></div>
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={()=>setDetail(null)}>Kapat</button>
              {detail.status==='PENDING' && (
                <>
                  <button className="btn btn-danger" onClick={()=>void updateStatus(detail.id,'FAILED')}>Başarısız İşaretle</button>
                  <button className="btn primary-button" onClick={()=>void updateStatus(detail.id,'COMPLETED')}>Tamamlandı İşaretle</button>
                </>
              )}
              {detail.status==='FAILED' && (
                <button className="btn primary-button" onClick={()=>void updateStatus(detail.id,'COMPLETED')}>Tamamlandı İşaretle</button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrdersPage;
