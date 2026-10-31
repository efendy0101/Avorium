import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Tag, Plus, Trash2, AlertTriangle, Fish, Anchor } from 'lucide-react';
import { api, getErrorMessage } from '../api';
import type { AdminCategory, CategoryType } from '../api';



const CategoriesPage = () => {
  const [categories, setCategories] = useState<AdminCategory[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [form, setForm] = useState({ name: '', type: 'FISHING_GEAR' as CategoryType });
  const [search, setSearch] = useState('');

  const load = useCallback(async () => {
    try {
      const { data } = await api.get<AdminCategory[]>('/admin/categories');
      setCategories(data);
    } catch (e) { setError(getErrorMessage(e)); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const create = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.name.trim()) return;
    setIsLoading(true); setError(''); setSuccess('');
    try {
      await api.post('/admin/categories', form);
      setForm({ name: '', type: 'FISHING_GEAR' });
      setSuccess('Kategori olusturuldu.');
      setTimeout(() => setSuccess(''), 3000);
      await load();
    } catch (err) { setError(getErrorMessage(err)); }
    finally { setIsLoading(false); }
  };

  const remove = async (id: string, name: string) => {
    if (!window.confirm(name + ' kategorisini silmek istiyor musunuz?')) return;
    setError('');
    try { await api.delete('/admin/categories/' + id); await load(); }
    catch (err) { setError(getErrorMessage(err)); }
  };

  const filtered = categories.filter(c =>
    c.name.toLowerCase().includes(search.toLowerCase())
  );

  const fishing = filtered.filter(c => c.type === 'FISHING_GEAR');
  const boats = filtered.filter(c => c.type === 'BOAT');

  return (
    <div>
      <div className="page-header">
        <div><h1>Kategoriler</h1><p>Ilan kategorilerini buradan yonetin ve ekleyin.</p></div>
        <span className="info-chip"><Tag size={14}/>{categories.length} kategori</span>
      </div>

      {error && <div className="alert danger"><AlertTriangle size={16}/>{error}</div>}
      {success && <div className="alert success">{success}</div>}

      <form className="form-grid" onSubmit={create} style={{marginBottom:24}}>
        <label>Kategori Adi<input value={form.name} onChange={e=>setForm(f=>({...f,name:e.target.value}))} placeholder="Ornek: Olta Kamislari" required/></label>
        <label>Tip<select value={form.type} onChange={e=>setForm(f=>({...f,type:e.target.value as CategoryType}))}><option value="FISHING_GEAR">Balikcilik Ekipmani</option><option value="BOAT">Tekne ve Denizcilik</option></select></label>
        <button className="btn primary-button" type="submit" disabled={isLoading}><Plus size={16}/>{isLoading?'Ekleniyor...':'Ekle'}</button>
      </form>

      <div style={{marginBottom:16}}>
        <input className="table-search" value={search} onChange={e=>setSearch(e.target.value)} placeholder="Kategori ara..."/>
      </div>

      <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:20}}>
        <div className="table-container">
          <div className="table-header">
            <h2 style={{display:'flex',alignItems:'center',gap:8}}><Fish size={18} style={{color:'var(--success)'}}/>Balikcilik ({fishing.length})</h2>
          </div>
          <table>
            <thead><tr><th>Kategori Adi</th><th>Ilan</th><th>Islem</th></tr></thead>
            <tbody>
              {fishing.map(cat=>(
                <tr key={cat.id}>
                  <td>{cat.name}</td>
                  <td><span className="info-chip">{cat._count.ads}</span></td>
                  <td>
                    <button className="btn btn-danger btn-sm" onClick={()=>void remove(cat.id,cat.name)} title="Sil" disabled={cat._count.ads>0}>
                      <Trash2 size={13}/>
                    </button>
                  </td>
                </tr>
              ))}
              {fishing.length===0&&<tr><td colSpan={3} className="empty">Kayit bulunamadi</td></tr>}
            </tbody>
          </table>
        </div>
        <div className="table-container">
          <div className="table-header">
            <h2 style={{display:'flex',alignItems:'center',gap:8}}><Anchor size={18} style={{color:'var(--accent)'}}/>Tekne ve Deniz ({boats.length})</h2>
          </div>
          <table>
            <thead><tr><th>Kategori Adi</th><th>Ilan</th><th>Islem</th></tr></thead>
            <tbody>
              {boats.map(cat=>(
                <tr key={cat.id}>
                  <td>{cat.name}</td>
                  <td><span className="info-chip">{cat._count.ads}</span></td>
                  <td>
                    <button className="btn btn-danger btn-sm" onClick={()=>void remove(cat.id,cat.name)} title="Sil" disabled={cat._count.ads>0}>
                      <Trash2 size={13}/>
                    </button>
                  </td>
                </tr>
              ))}
              {boats.length===0&&<tr><td colSpan={3} className="empty">Kayit bulunamadi</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
      <p className="muted" style={{marginTop:12}}>Not: Icinde ilan olan kategoriler silinemez.</p>
    </div>
  );
};

export default CategoriesPage;
