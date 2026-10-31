import { useEffect, useState } from 'react';
import { api, getErrorMessage } from '../api';
import { Search, Eye, Trash2, CheckCircle, XCircle, Clock } from 'lucide-react';

interface Ad {
  id: string; adNumber: number; title: string; price: number; status: string;
  isUrgent: boolean; city: string|null; district: string|null; createdAt: string;
  user: { id: string; name: string; email: string };
  category: { id: string; name: string } | null;
  images: { url: string }[];
}

export default function AdsPage() {
  const [ads, setAds] = useState<Ad[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [error, setError] = useState('');
  const [selected, setSelected] = useState<Ad|null>(null);

  const load = () => {
    setLoading(true);
    const params = statusFilter ? `?status=${statusFilter}` : '';
    api.get<Ad[]>(`/admin/ads${params}`)
      .then(r => setAds(r.data))
      .catch(e => setError(getErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [statusFilter]);

  const updateStatus = async (id: string, status: string) => {
    try { await api.patch(`/admin/ads/${id}/status`, { status }); load(); }
    catch(e) { alert(getErrorMessage(e)); }
  };

  const deleteAd = async (id: string) => {
    if (!confirm('Bu ilan silinsin mi?')) return;
    try { await api.delete(`/admin/ads/${id}`); load(); }
    catch(e) { alert(getErrorMessage(e)); }
  };

  const filtered = ads.filter(a =>
    a.title.toLowerCase().includes(search.toLowerCase()) ||
    a.user.name.toLowerCase().includes(search.toLowerCase()) ||
    String(a.adNumber).includes(search)
  );

  const statusLabel = (s: string) => ({ ACTIVE:'Aktif', PASSIVE:'Pasif', PENDING_REVIEW:'Bekliyor', SOLD:'Satildi' }[s] ?? s);
  const statusColor = (s: string) => ({ ACTIVE:'#2ecc71', PASSIVE:'#95a5a6', PENDING_REVIEW:'#f39c12', SOLD:'#3498db' }[s] ?? '#888');

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Yayinlanan İlanlar</h1>
        <span className="badge" style={{background:'#112240',color:'#3ea8e0'}}>{ads.length} ilan</span>
      </div>
      {error && <div className="error-banner">{error}</div>}
      <div className="card" style={{marginBottom:16,padding:'12px 16px',display:'flex',gap:12,flexWrap:'wrap',alignItems:'center'}}>
        <div className="search-box" style={{flex:1,minWidth:200}}>
          <Search size={16}/><input placeholder="Başlık, kullanıcı veya ilan no..." value={search} onChange={e=>setSearch(e.target.value)} className="search-input"/>
        </div>
        <select value={statusFilter} onChange={e=>setStatusFilter(e.target.value)} className="filter-select">
          <option value="">Tüm Durumlar</option>
          <option value="ACTIVE">Aktif</option>
          <option value="PENDING_REVIEW">Bekliyor</option>
          <option value="PASSIVE">Pasif</option>
          <option value="SOLD">Satıldı</option>
        </select>
      </div>
      <div className="card">
        <table className="data-table">
          <thead><tr><th>#</th><th>Başlık</th><th>Kullanıcı</th><th>Kategori</th><th>Fiyat</th><th>Durum</th><th>Tarih</th><th>İşlem</th></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={8} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>Yükleniyor...</td></tr>
            : filtered.length === 0 ? <tr><td colSpan={8} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>İlan bulunamadı.</td></tr>
            : filtered.map(ad => (
              <tr key={ad.id}>
                <td style={{fontFamily:'monospace',fontSize:11,color:'var(--text-muted)'}}>#{ad.adNumber}</td>
                <td>
                  <div style={{fontWeight:600,color:'var(--text-primary)'}}>{ad.title}</div>
                  {ad.isUrgent && <span style={{fontSize:10,color:'#e53935',background:'rgba(229,57,53,0.15)',padding:'1px 6px',borderRadius:4}}>ACİL</span>}
                  {(ad.city||ad.district) && <div style={{fontSize:11,color:'var(--text-muted)'}}>{[ad.city,ad.district].filter(Boolean).join(' / ')}</div>}
                </td>
                <td><div style={{fontSize:13}}>{ad.user.name}</div><div style={{fontSize:11,color:'var(--text-muted)'}}>{ad.user.email}</div></td>
                <td style={{fontSize:13,color:'var(--text-muted)'}}>{ad.category?.name ?? '-'}</td>
                <td style={{fontWeight:700,color:'#ffb300'}}>{ad.price.toLocaleString('tr-TR')} ₺</td>
                <td><span style={{background:statusColor(ad.status)+'22',color:statusColor(ad.status),padding:'2px 10px',borderRadius:20,fontSize:12,fontWeight:600}}>{statusLabel(ad.status)}</span></td>
                <td style={{fontSize:12,color:'var(--text-muted)'}}>{new Date(ad.createdAt).toLocaleDateString('tr-TR')}</td>
                <td>
                  <div style={{display:'flex',gap:4}}>
                    <button className="icon-btn" title="Detay" onClick={()=>setSelected(ad)}><Eye size={14}/></button>
                    {ad.status!=='ACTIVE' && <button className="icon-btn success" title="Yayına Al" onClick={()=>updateStatus(ad.id,'ACTIVE')}><CheckCircle size={14}/></button>}
                    {ad.status==='ACTIVE' && <button className="icon-btn warning" title="Yayından Kaldır" onClick={()=>updateStatus(ad.id,'PASSIVE')}><XCircle size={14}/></button>}
                    {ad.status!=='PENDING_REVIEW' && <button className="icon-btn" title="Bekletmeye Al" onClick={()=>updateStatus(ad.id,'PENDING_REVIEW')} style={{color:'#f39c12'}}><Clock size={14}/></button>}
                    <button className="icon-btn danger" title="Sil" onClick={()=>deleteAd(ad.id)}><Trash2 size={14}/></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {selected && (
        <div className="modal-overlay" onClick={()=>setSelected(null)}>
          <div className="modal" onClick={e=>e.stopPropagation()} style={{maxWidth:600}}>
            <div className="modal-header"><h3>İlan Detayı — #{selected.adNumber}</h3><button onClick={()=>setSelected(null)} className="modal-close">✕</button></div>
            <div className="modal-body">
              {selected.images[0] && <img src={selected.images[0].url} alt="" style={{width:'100%',height:200,objectFit:'cover',borderRadius:8,marginBottom:12}}/>}
              <div className="detail-grid">
                <div className="detail-item"><div className="di-label">Başlık</div><div className="di-value">{selected.title}</div></div>
                <div className="detail-item"><div className="di-label">Fiyat</div><div className="di-value" style={{color:'#ffb300',fontWeight:700}}>{selected.price.toLocaleString('tr-TR')} ₺</div></div>
                <div className="detail-item"><div className="di-label">Durum</div><div className="di-value"><span style={{color:statusColor(selected.status)}}>{statusLabel(selected.status)}</span></div></div>
                <div className="detail-item"><div className="di-label">Kategori</div><div className="di-value">{selected.category?.name ?? '-'}</div></div>
                <div className="detail-item"><div className="di-label">Konum</div><div className="di-value">{[selected.city,selected.district].filter(Boolean).join(' / ') || '-'}</div></div>
                <div className="detail-item"><div className="di-label">Kullanıcı</div><div className="di-value">{selected.user.name} ({selected.user.email})</div></div>
              </div>
              <div style={{marginTop:12,display:'flex',gap:8,flexWrap:'wrap'}}>
                {selected.status!=='ACTIVE' && <button className="btn btn-success" onClick={()=>{updateStatus(selected.id,'ACTIVE');setSelected(null);}}>Yayına Al</button>}
                {selected.status==='ACTIVE' && <button className="btn btn-warning" onClick={()=>{updateStatus(selected.id,'PASSIVE');setSelected(null);}}>Yayından Kaldır</button>}
                <button className="btn btn-danger" onClick={()=>{deleteAd(selected.id);setSelected(null);}}>Sil</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}