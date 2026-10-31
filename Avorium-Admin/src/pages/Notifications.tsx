import { useEffect, useState } from 'react';
import { api, getErrorMessage } from '../api';
import { Send, Bell } from 'lucide-react';

interface Notif {
  id: string; type: string; title: string; body: string; isRead: boolean; createdAt: string;
  user: { id: string; name: string; email: string };
}

export default function NotificationsPage() {
  const [notifs, setNotifs] = useState<Notif[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [type, setType] = useState('ANNOUNCEMENT');
  const [sending, setSending] = useState(false);
  const [success, setSuccess] = useState('');

  const load = () => {
    setLoading(true);
    api.get<Notif[]>('/admin/notifications')
      .then(r => setNotifs(r.data))
      .catch(e => setError(getErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const sendBroadcast = async () => {
    if (!title.trim() || !body.trim()) { setError('Başlık ve içerik zorunludur.'); return; }
    setSending(true); setError(''); setSuccess('');
    try {
      const r = await api.post<{message:string}>('/admin/notifications/broadcast', { title, body, type });
      setSuccess(r.data.message);
      setTitle(''); setBody('');
      load();
    } catch(e) { setError(getErrorMessage(e)); }
    finally { setSending(false); }
  };

  const typeLabel: Record<string,string> = {
    ANNOUNCEMENT:'Duyuru', SYSTEM:'Sistem', AD_APPROVED:'İlan Onayı',
    AD_REJECTED:'İlan Reddi', NEW_MESSAGE:'Yeni Mesaj', AD_EXPIRING:'İlan Süresi', PACKAGE_EXPIRING:'Paket Süresi'
  };

  return (
    <div className="page">
      <div className="page-header"><h1 className="page-title">Bildirim Yönetimi</h1></div>
      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}
      <div className="card" style={{marginBottom:24}}>
        <h3 style={{color:'var(--text-primary)',marginBottom:16,display:'flex',alignItems:'center',gap:8}}><Send size={18}/>Toplu Bildirim Gönder</h3>
        <div style={{display:'flex',flexDirection:'column',gap:12}}>
          <div style={{display:'flex',gap:12,flexWrap:'wrap'}}>
            <input className="form-input" placeholder="Başlık" value={title} onChange={e=>setTitle(e.target.value)} style={{flex:1,minWidth:200}}/>
            <select className="filter-select" value={type} onChange={e=>setType(e.target.value)}>
              <option value="ANNOUNCEMENT">Duyuru</option>
              <option value="SYSTEM">Sistem</option>
              <option value="AD_EXPIRING">İlan Süresi</option>
              <option value="PACKAGE_EXPIRING">Paket Süresi</option>
            </select>
          </div>
          <textarea className="form-textarea" placeholder="Bildirim içeriği..." value={body} onChange={e=>setBody(e.target.value)} rows={3}/>
          <button className="btn btn-primary" onClick={sendBroadcast} disabled={sending} style={{alignSelf:'flex-start'}}>
            {sending ? 'Gönderiliyor...' : 'Tüm Kullanıcılara Gönder'}
          </button>
        </div>
      </div>
      <div className="card">
        <h3 style={{color:'var(--text-primary)',marginBottom:16,display:'flex',alignItems:'center',gap:8}}><Bell size={18}/>Bildirim Geçmişi</h3>
        <table className="data-table">
          <thead><tr><th>Kullanıcı</th><th>Tür</th><th>Başlık</th><th>İçerik</th><th>Okundu</th><th>Tarih</th></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={6} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>Yükleniyor...</td></tr>
            : notifs.length===0 ? <tr><td colSpan={6} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>Bildirim bulunamadı.</td></tr>
            : notifs.map(n => (
              <tr key={n.id}>
                <td><div style={{fontSize:13}}>{n.user.name}</div><div style={{fontSize:11,color:'var(--text-muted)'}}>{n.user.email}</div></td>
                <td><span style={{background:'rgba(62,168,224,0.15)',color:'#3ea8e0',padding:'2px 8px',borderRadius:12,fontSize:12}}>{typeLabel[n.type]??n.type}</span></td>
                <td style={{fontWeight:600,fontSize:13,color:'var(--text-primary)'}}>{n.title}</td>
                <td style={{fontSize:12,color:'var(--text-muted)',maxWidth:200}}>{n.body}</td>
                <td>{n.isRead ? <span style={{color:'#2ecc71',fontSize:12}}>Okundu</span> : <span style={{color:'#f39c12',fontSize:12}}>Okunmadı</span>}</td>
                <td style={{fontSize:12,color:'var(--text-muted)'}}>{new Date(n.createdAt).toLocaleDateString('tr-TR')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}