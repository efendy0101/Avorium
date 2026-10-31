import { useEffect, useState } from 'react';
import { api, getErrorMessage } from '../api';
import { CheckCircle, Trash2, Eye } from 'lucide-react';

interface Report {
  id: string; reason: string; detail: string|null; isReviewed: boolean; createdAt: string;
  user: { id: string; name: string; email: string };
  ad: { id: string; title: string; adNumber: number; status: string; images: {url:string}[]; user: { name: string; email: string } };
}

const reasonLabel: Record<string,string> = {
  FAKE_AD:'Sahte İlan', WRONG_INFO:'Yanlış Bilgi', SPAM:'Spam',
  INAPPROPRIATE:'Uygunsuz İçerik', FRAUD:'Dolandırıcılık', OTHER:'Diğer'
};

export default function ReportsPage() {
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState<Report|null>(null);

  const load = () => {
    setLoading(true);
    api.get<Report[]>('/admin/reports')
      .then(r => setReports(r.data))
      .catch(e => setError(getErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const markReviewed = async (id: string) => {
    try { await api.patch(`/admin/reports/${id}/review`); load(); }
    catch(e) { alert(getErrorMessage(e)); }
  };

  const deleteAd = async (adId: string) => {
    if (!confirm('Bu ilan silinsin mi?')) return;
    try { await api.delete(`/admin/ads/${adId}`); load(); }
    catch(e) { alert(getErrorMessage(e)); }
  };

  const unreviewed = reports.filter(r => !r.isReviewed).length;

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Şikayet Edilen İlanlar</h1>
        {unreviewed > 0 && <span className="badge" style={{background:'rgba(231,76,60,0.2)',color:'#e74c3c'}}>{unreviewed} yeni şikayet</span>}
      </div>
      {error && <div className="error-banner">{error}</div>}
      <div className="card">
        <table className="data-table">
          <thead><tr><th>Şikayetçi</th><th>İlan</th><th>Neden</th><th>Açıklama</th><th>Tarih</th><th>Durum</th><th>İşlem</th></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={7} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>Yükleniyor...</td></tr>
            : reports.length === 0 ? <tr><td colSpan={7} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>Şikayet bulunamadı.</td></tr>
            : reports.map(r => (
              <tr key={r.id} style={{opacity: r.isReviewed ? 0.5 : 1}}>
                <td><div style={{fontSize:13}}>{r.user.name}</div><div style={{fontSize:11,color:'var(--text-muted)'}}>{r.user.email}</div></td>
                <td><div style={{fontWeight:600,fontSize:13}}>{r.ad.title}</div><div style={{fontSize:11,color:'var(--text-muted)'}}>#{r.ad.adNumber} — {r.ad.user.name}</div></td>
                <td><span style={{background:'rgba(231,76,60,0.15)',color:'#e74c3c',padding:'2px 8px',borderRadius:12,fontSize:12}}>{reasonLabel[r.reason]??r.reason}</span></td>
                <td style={{fontSize:12,color:'var(--text-muted)',maxWidth:200}}>{r.detail || '-'}</td>
                <td style={{fontSize:12,color:'var(--text-muted)'}}>{new Date(r.createdAt).toLocaleDateString('tr-TR')}</td>
                <td>{r.isReviewed ? <span style={{color:'#2ecc71',fontSize:12}}>İncelendi</span> : <span style={{color:'#f39c12',fontSize:12}}>Bekliyor</span>}</td>
                <td>
                  <div style={{display:'flex',gap:4}}>
                    <button className="icon-btn" title="Detay" onClick={()=>setSelected(r)}><Eye size={14}/></button>
                    {!r.isReviewed && <button className="icon-btn success" title="İncelendi işaretle" onClick={()=>markReviewed(r.id)}><CheckCircle size={14}/></button>}
                    <button className="icon-btn danger" title="İlanı sil" onClick={()=>deleteAd(r.ad.id)}><Trash2 size={14}/></button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {selected && (
        <div className="modal-overlay" onClick={()=>setSelected(null)}>
          <div className="modal" onClick={e=>e.stopPropagation()} style={{maxWidth:560}}>
            <div className="modal-header"><h3>Şikayet Detayı</h3><button onClick={()=>setSelected(null)} className="modal-close">✕</button></div>
            <div className="modal-body">
              {selected.ad.images[0] && <img src={selected.ad.images[0].url} alt="" style={{width:'100%',height:160,objectFit:'cover',borderRadius:8,marginBottom:12}}/>}
              <div className="detail-grid">
                <div className="detail-item"><div className="di-label">İlan</div><div className="di-value">{selected.ad.title} (#{selected.ad.adNumber})</div></div>
                <div className="detail-item"><div className="di-label">İlan Sahibi</div><div className="di-value">{selected.ad.user.name} — {selected.ad.user.email}</div></div>
                <div className="detail-item"><div className="di-label">Şikayetçi</div><div className="di-value">{selected.user.name} — {selected.user.email}</div></div>
                <div className="detail-item"><div className="di-label">Neden</div><div className="di-value">{reasonLabel[selected.reason]??selected.reason}</div></div>
                <div className="detail-item"><div className="di-label">Açıklama</div><div className="di-value">{selected.detail || '-'}</div></div>
              </div>
              <div style={{marginTop:12,display:'flex',gap:8}}>
                {!selected.isReviewed && <button className="btn btn-success" onClick={()=>{markReviewed(selected.id);setSelected(null);}}>İncelendi İşaretle</button>}
                <button className="btn btn-danger" onClick={()=>{deleteAd(selected.ad.id);setSelected(null);}}>İlanı Sil</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}