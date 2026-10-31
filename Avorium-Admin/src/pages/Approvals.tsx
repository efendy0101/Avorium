import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Eye, X, CheckCircle, XCircle, Video, AlertTriangle, Save } from 'lucide-react';
import { api, getErrorMessage } from '../api';
import type { AdminAd, AdStatus } from '../api';

const fmt = (v: number) => new Intl.NumberFormat('tr-TR',{style:'currency',currency:'TRY'}).format(v);
const statusLabel: Record<AdStatus,string> = {
  ACTIVE:'Aktif', PASSIVE:'Pasif', PENDING_REVIEW:'Onay Bekliyor', SOLD:'Satıldı'
};

const Approvals = () => {
  const [ads, setAds] = useState<AdminAd[]>([]);
  const [filter, setFilter] = useState<AdStatus|'ALL'>('PENDING_REVIEW');
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [detail, setDetail] = useState<AdminAd|null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState<{title:string;description:string;price:string;status:AdStatus}>({title:'',description:'',price:'',status:'PENDING_REVIEW'});

  const load = useCallback(async () => {
    setIsLoading(true); setError('');
    try {
      const path = filter==='ALL'?'/admin/ads':`/admin/ads?status=${filter}`;
      const { data } = await api.get<AdminAd[]>(path);
      setAds(data);
    } catch(e){ setError(getErrorMessage(e)); }
    finally { setIsLoading(false); }
  }, [filter]);

  useEffect(()=>{ void load(); },[load]);

  const update = async (id: string, status: AdStatus) => {
    setError(''); setSuccess('');
    try {
      await api.patch(`/admin/ads/${id}/status`, {status});
      setDetail(null);
      setSuccess('İlan durumu güncellendi.');
      setTimeout(() => setSuccess(''), 3000);
      await load();
    } catch(e){ setError(getErrorMessage(e)); }
  };

  const openEdit = (ad: AdminAd) => {
    setEditForm({
      title: ad.title,
      description: ad.description,
      price: String(ad.price),
      status: ad.status
    });
    setIsEditing(true);
  };

  const saveEdit = async (e: FormEvent) => {
    e.preventDefault();
    if (!detail) return;
    setError(''); setSuccess('');
    try {
      await api.put(`/admin/ads/${detail.id}`, {
        title: editForm.title,
        description: editForm.description,
        price: Number(editForm.price),
        status: editForm.status
      });
      setDetail(null);
      setIsEditing(false);
      setSuccess('İlan güncellendi.');
      setTimeout(() => setSuccess(''), 3000);
      await load();
    } catch(e){ setError(getErrorMessage(e)); }
  };

  const filtered = ads.filter(a =>
    a.title.toLowerCase().includes(search.toLowerCase()) ||
    (a.user?.name??'').toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <div><h1>İlan Onayları</h1><p>Yeni ilanları inceleyin, yayına alın veya pasife çekin.</p></div>
        <div style={{display:'flex',gap:10}}>
          <input className="table-search" value={search} onChange={e=>setSearch(e.target.value)} placeholder="İlan veya satıcı ara..."/>
          <select className="table-select" value={filter} onChange={e=>setFilter(e.target.value as AdStatus|'ALL')}>
            <option value="PENDING_REVIEW">Onay Bekleyen</option>
            <option value="ACTIVE">Aktif</option>
            <option value="PASSIVE">Pasif</option>
            <option value="SOLD">Satıldı</option>
            <option value="ALL">Tümü</option>
          </select>
        </div>
      </div>

      {error&&<div className="alert danger"><AlertTriangle size={16}/>{error}</div>}
      {success&&<div className="alert success">{success}</div>}
      {isLoading&&<div className="loading">Yükleniyor</div>}

      <div className="table-container">
        <div className="table-header">
          <h2>İlanlar</h2>
          <span className="muted">{filtered.length} sonuç</span>
        </div>
        <table>
          <thead><tr><th>İlan</th><th>Satıcı</th><th>Kategori</th><th>Fiyat</th><th>Durum</th><th>İşlem</th></tr></thead>
          <tbody>
            {filtered.map(ad=>(
              <tr key={ad.id}>
                <td>
                  <div className="td-with-thumb">
                    {ad.images?.[0]
                      ?<img className="table-thumb" src={ad.images[0].url} alt={ad.title}/>
                      :<div className="table-thumb-placeholder">IMG</div>
                    }
                    <div><strong>{ad.title}</strong><div className="table-description">{ad.description}</div></div>
                  </div>
                </td>
                <td>{ad.user?.name??'-'}</td>
                <td>{ad.category?.name??'-'}</td>
                <td><strong>{fmt(ad.price)}</strong></td>
                <td><span className={`badge ${ad.status.toLowerCase().replace('_','-')}`}>{statusLabel[ad.status]}</span></td>
                <td>
                  <div className="actions">
                    <button className="btn btn-ghost btn-sm" onClick={()=>{setDetail(ad);setIsEditing(false);}}><Eye size={13}/>Detay</button>
                    <button className="btn btn-success btn-sm" onClick={()=>void update(ad.id,'ACTIVE')}><CheckCircle size={13}/>Onayla</button>
                    <button className="btn btn-danger btn-sm" onClick={()=>void update(ad.id,'PASSIVE')}><XCircle size={13}/>Pasif</button>
                  </div>
                </td>
              </tr>
            ))}
            {filtered.length===0&&!isLoading&&<tr><td colSpan={6} className="empty">Kayıt bulunamadı.</td></tr>}
          </tbody>
        </table>
      </div>

      {detail&&(
        <div className="modal-overlay" onClick={()=>{setDetail(null);setIsEditing(false);}}>
          <div className="modal modal-lg" onClick={e=>e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <h2>{isEditing ? 'İlan Düzenle' : detail.title}</h2>
                {!isEditing && <span className={`badge ${detail.status.toLowerCase().replace('_','-')}`}>{statusLabel[detail.status]}</span>}
              </div>
              <button className="modal-close" onClick={()=>{setDetail(null);setIsEditing(false);}}><X size={16}/></button>
            </div>

            {isEditing ? (
              <form onSubmit={saveEdit}>
                <div className="settings-form" style={{padding:'16px 0'}}>
                  <label>İlan Başlığı<input value={editForm.title} onChange={e=>setEditForm(f=>({...f,title:e.target.value}))} required/></label>
                  <label>Açıklama<textarea rows={4} value={editForm.description} onChange={e=>setEditForm(f=>({...f,description:e.target.value}))} required/></label>
                  <label>Fiyat (TL)<input type="number" min="0" step="0.01" value={editForm.price} onChange={e=>setEditForm(f=>({...f,price:e.target.value}))} required/></label>
                  <label>Durum
                    <select value={editForm.status} onChange={e=>setEditForm(f=>({...f,status:e.target.value as AdStatus}))}>
                      <option value="PENDING_REVIEW">Onay Bekliyor</option>
                      <option value="ACTIVE">Aktif</option>
                      <option value="PASSIVE">Pasif</option>
                      <option value="SOLD">Satıldı</option>
                    </select>
                  </label>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost" onClick={()=>setIsEditing(false)}>İptal</button>
                  <button type="submit" className="btn primary-button"><Save size={14}/>Kaydet</button>
                </div>
              </form>
            ) : (
              <>
                {detail.images?.length>0&&(
                  <div className="detail-section">
                    <h4>Fotoğraflar</h4>
                    <div className="image-gallery">
                      {detail.images.map(img=><img key={img.id} src={img.url} alt="İlan foto"/>)}
                    </div>
                  </div>
                )}

                {detail.videoUrl&&(
                  <div className="detail-section">
                    <h4>Video</h4>
                    <div style={{background:'rgba(0,0,0,0.3)',borderRadius:8,padding:'10px 14px',display:'flex',alignItems:'center',gap:10}}>
                      <Video size={18} style={{color:'var(--accent)'}}/>
                      <a href={detail.videoUrl} target="_blank" rel="noreferrer" style={{color:'var(--accent)',fontSize:13}}>Video linkini aç</a>
                    </div>
                  </div>
                )}

                <div className="detail-section">
                  <h4>Açıklama</h4>
                  <p style={{fontSize:14,lineHeight:1.7,color:'var(--text-sub)'}}>{detail.description}</p>
                </div>

                <div className="detail-section">
                  <h4>İlan Bilgileri</h4>
                  <div className="detail-grid">
                    <div className="detail-item"><div className="di-label">Fiyat</div><div className="di-value">{fmt(detail.price)}</div></div>
                    <div className="detail-item"><div className="di-label">Kategori</div><div className="di-value">{detail.category?.name}</div></div>
                    <div className="detail-item"><div className="di-label">Satıcı</div><div className="di-value">{detail.user?.name}</div></div>
                    <div className="detail-item"><div className="di-label">E-posta</div><div className="di-value">{detail.user?.email}</div></div>
                    <div className="detail-item"><div className="di-label">Görüntülenme</div><div className="di-value">{detail.views??0}</div></div>
                    <div className="detail-item"><div className="di-label">Tarih</div><div className="di-value">{new Date(detail.createdAt).toLocaleDateString('tr-TR')}</div></div>
                  </div>
                </div>

                {detail.boatDetails&&(
                  <div className="detail-section">
                    <h4>Tekne Detayları</h4>
                    <div className="detail-grid">
                      <div className="detail-item"><div className="di-label">Marka</div><div className="di-value">{detail.boatDetails.make}</div></div>
                      <div className="detail-item"><div className="di-label">Model</div><div className="di-value">{detail.boatDetails.model}</div></div>
                      <div className="detail-item"><div className="di-label">Yıl</div><div className="di-value">{detail.boatDetails.year}</div></div>
                      <div className="detail-item"><div className="di-label">Boy (m)</div><div className="di-value">{detail.boatDetails.length}</div></div>
                      <div className="detail-item"><div className="di-label">Gövde</div><div className="di-value">{detail.boatDetails.hullMaterial}</div></div>
                      <div className="detail-item"><div className="di-label">Motor Gücü</div><div className="di-value">{detail.boatDetails.enginePower} HP</div></div>
                    </div>
                  </div>
                )}

                {detail.fishingGearDetails&&(
                  <div className="detail-section">
                    <h4>Malzeme Detayları</h4>
                    <div className="detail-grid">
                      <div className="detail-item"><div className="di-label">Marka</div><div className="di-value">{detail.fishingGearDetails.brand}</div></div>
                      <div className="detail-item"><div className="di-label">Malzeme Türü</div><div className="di-value">{detail.fishingGearDetails.gearType}</div></div>
                      <div className="detail-item"><div className="di-label">Durum</div><div className="di-value">{detail.fishingGearDetails.condition}</div></div>
                    </div>
                  </div>
                )}

                <div className="modal-footer">
                  <button className="btn btn-ghost" onClick={()=>{setDetail(null);setIsEditing(false);}}>Kapat</button>
                  <button className="btn btn-success" onClick={()=>openEdit(detail)}>Düzenle</button>
                  <button className="btn btn-danger" onClick={()=>void update(detail.id,'PASSIVE')}><XCircle size={14}/>Pasife Al</button>
                  <button className="btn primary-button" onClick={()=>void update(detail.id,'ACTIVE')}><CheckCircle size={14}/>Onayla</button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Approvals;
