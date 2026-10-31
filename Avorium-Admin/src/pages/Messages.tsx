import { useEffect, useState } from 'react';
import { api, getErrorMessage } from '../api';
import { Trash2, Search } from 'lucide-react';

interface Msg {
  id: string; content: string; isRead: boolean; createdAt: string;
  sender: { id: string; name: string; email: string };
  receiver: { id: string; name: string; email: string };
  ad: { id: string; title: string };
}

export default function MessagesPage() {
  const [messages, setMessages] = useState<Msg[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');

  const load = () => {
    setLoading(true);
    api.get<Msg[]>('/admin/messages')
      .then(r => setMessages(r.data))
      .catch(e => setError(getErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const deleteMsg = async (id: string) => {
    if (!confirm('Bu mesaj silinsin mi?')) return;
    try { await api.delete(`/admin/messages/${id}`); load(); }
    catch(e) { alert(getErrorMessage(e)); }
  };

  const filtered = messages.filter(m =>
    m.content.toLowerCase().includes(search.toLowerCase()) ||
    m.sender.name.toLowerCase().includes(search.toLowerCase()) ||
    m.receiver.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Mesaj Yönetimi</h1>
        <span className="badge" style={{background:'#112240',color:'#3ea8e0'}}>{messages.length} mesaj</span>
      </div>
      {error && <div className="error-banner">{error}</div>}
      <div className="card" style={{marginBottom:16,padding:'12px 16px'}}>
        <div className="search-box">
          <Search size={16}/><input placeholder="İçerik, gönderen veya alıcı..." value={search} onChange={e=>setSearch(e.target.value)} className="search-input"/>
        </div>
      </div>
      <div className="card">
        <table className="data-table">
          <thead><tr><th>Gönderen</th><th>Alıcı</th><th>İlan</th><th>İçerik</th><th>Okundu</th><th>Tarih</th><th>İşlem</th></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={7} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>Yükleniyor...</td></tr>
            : filtered.length===0 ? <tr><td colSpan={7} style={{textAlign:'center',padding:32,color:'var(--text-muted)'}}>Mesaj bulunamadı.</td></tr>
            : filtered.map(m => (
              <tr key={m.id}>
                <td><div style={{fontSize:13}}>{m.sender.name}</div><div style={{fontSize:11,color:'var(--text-muted)'}}>{m.sender.email}</div></td>
                <td><div style={{fontSize:13}}>{m.receiver.name}</div><div style={{fontSize:11,color:'var(--text-muted)'}}>{m.receiver.email}</div></td>
                <td style={{fontSize:12,color:'var(--text-muted)'}}>{m.ad.title}</td>
                <td style={{fontSize:13,color:'var(--text-primary)',maxWidth:250}}>{m.content}</td>
                <td>{m.isRead ? <span style={{color:'#2ecc71',fontSize:12}}>Okundu</span> : <span style={{color:'#f39c12',fontSize:12}}>Okunmadı</span>}</td>
                <td style={{fontSize:12,color:'var(--text-muted)'}}>{new Date(m.createdAt).toLocaleDateString('tr-TR')}</td>
                <td><button className="icon-btn danger" onClick={()=>deleteMsg(m.id)}><Trash2 size={14}/></button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}