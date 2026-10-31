import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Search, UserCheck, AlertTriangle, X, Trash2, Edit3, Crown } from 'lucide-react';
import { api, getErrorMessage } from '../api';
import type { AdminUser, AdminUserInput, Role } from '../api';

const UsersPage = () => {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [savingUserId, setSavingUserId] = useState<string|null>(null);
  const [search, setSearch] = useState('');
  const [editUser, setEditUser] = useState<AdminUser|null>(null);
  const [editForm, setEditForm] = useState<AdminUserInput>({});
  const [deleteConfirm, setDeleteConfirm] = useState<AdminUser|null>(null);

  useEffect(() => {
    api.get<AdminUser[]>('/admin/users')
      .then(r => setUsers(r.data))
      .catch(e => setError(getErrorMessage(e)));
  }, []);

  const updateAccess = async (userId: string, payload: {role?:Role; remainingAdLimit?:number}) => {
    setError('');
    setSavingUserId(userId);
    try {
      const { data } = await api.patch<{user:AdminUser}>(`/admin/users/${userId}/access`, payload);
      setUsers(u => u.map(x => x.id===userId ? data.user : x));
    } catch(e){ setError(getErrorMessage(e)); }
    finally { setSavingUserId(null); }
  };

  const openEdit = (user: AdminUser) => {
    setEditUser(user);
    setEditForm({
      name: user.name,
      email: user.email,
      phone: user.phone,
      role: user.role,
      remainingAdLimit: user.remainingAdLimit,
      isPremium: user.isPremium,
      tcNo: user.tcNo
    });
  };

  const saveEdit = async (e: FormEvent) => {
    e.preventDefault();
    if (!editUser) return;
    setError(''); setSuccess('');
    try {
      const { data } = await api.put<{user:AdminUser}>(`/admin/users/${editUser.id}`, editForm);
      setUsers(u => u.map(x => x.id===editUser.id ? data.user : x));
      setEditUser(null);
      setSuccess('Kullanici guncellendi.');
      setTimeout(() => setSuccess(''), 3000);
    } catch(e){ setError(getErrorMessage(e)); }
  };

  const confirmDelete = async () => {
    if (!deleteConfirm) return;
    setError(''); setSuccess('');
    try {
      await api.delete(`/admin/users/${deleteConfirm.id}`);
      setUsers(u => u.filter(x => x.id !== deleteConfirm.id));
      setDeleteConfirm(null);
      setSuccess('Kullanici silindi.');
      setTimeout(() => setSuccess(''), 3000);
    } catch(e){ setError(getErrorMessage(e)); }
  };

  const filtered = users.filter(u =>
    u.name.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <div><h1>Kullanicilar</h1><p>Kullanici profilleri, ilan haklari ve yonetici yetkileri.</p></div>
        <span className="info-chip"><UserCheck size={14}/>{users.length} kullanici</span>
      </div>
      {error&&<div className="alert danger"><AlertTriangle size={16}/>{error}</div>}
      {success&&<div className="alert success">{success}</div>}
      <div style={{marginBottom:16}}>
        <div style={{position:'relative',maxWidth:320}}>
          <Search size={15} style={{position:'absolute',left:12,top:'50%',transform:'translateY(-50%)',color:'var(--text-muted)'}}/>
          <input style={{paddingLeft:36}} className="table-search" value={search} onChange={e=>setSearch(e.target.value)} placeholder="Kullanici veya email ara..."/>
        </div>
      </div>
      <div className="table-container">
        <div className="table-header">
          <h2>Kullanici Listesi</h2>
          <span className="muted">{filtered.length} sonuc</span>
        </div>
        <table>
          <thead>
            <tr>
              <th>No</th><th>Ad Soyad</th><th>E-posta</th><th>Telefon</th><th>TC No</th>
              <th>Rol</th><th>Premium</th><th>Kalan Ilan</th><th>Ilan</th><th>Siparis</th><th>Kayit</th><th>Islem</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(user=>(
              <tr key={user.id}>
                <td style={{color:'var(--text-muted)',fontSize:12}}>#{user.memberNumber}</td>
                <td><strong>{user.name}</strong></td>
                <td style={{color:'var(--text-muted)'}}>{user.email}</td>
                <td style={{color:'var(--text-muted)'}}>{user.phone??'-'}</td>
                <td style={{color:'var(--text-muted)',fontSize:12}}>{user.tcNo??'-'}</td>
                <td>
                  <select className="table-select" value={user.role} disabled={savingUserId===user.id}
                    onChange={e=>void updateAccess(user.id,{role:e.target.value as Role})}>
                    <option value="USER">Kullanici</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </td>
                <td>
                  {user.isPremium
                    ? <span className="info-chip" style={{background:'var(--warning)',color:'#000'}}><Crown size={12}/> Premium</span>
                    : <span style={{color:'var(--text-muted)',fontSize:12}}>-</span>
                  }
                </td>
                <td>
                  <input className="table-number-input" type="number" min="0" defaultValue={user.remainingAdLimit}
                    disabled={savingUserId===user.id}
                    onBlur={e=>{
                      const v=Number(e.currentTarget.value);
                      if(Number.isInteger(v)&&v!==user.remainingAdLimit) void updateAccess(user.id,{remainingAdLimit:v});
                    }}/>
                </td>
                <td><span className="info-chip">{user._count.ads}</span></td>
                <td><span className="info-chip">{user._count.orders}</span></td>
                <td style={{color:'var(--text-muted)',fontSize:12}}>{new Date(user.createdAt).toLocaleDateString('tr-TR')}</td>
                <td>
                  <div className="actions">
                    <button className="btn btn-ghost btn-sm" onClick={()=>openEdit(user)}><Edit3 size={13}/>Duzenle</button>
                    <button className="btn btn-danger btn-sm" onClick={()=>setDeleteConfirm(user)}><Trash2 size={13}/>Sil</button>
                  </div>
                </td>
              </tr>
            ))}
            {filtered.length===0&&<tr><td colSpan={12} className="empty">Kayit bulunamadi</td></tr>}
          </tbody>
        </table>
      </div>

      {/* Edit User Modal */}
      {editUser&&(
        <div className="modal-overlay" onClick={()=>setEditUser(null)}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-header">
              <h2>Kullanici Duzenle</h2>
              <button className="modal-close" onClick={()=>setEditUser(null)}><X size={16}/></button>
            </div>
            <form onSubmit={saveEdit}>
              <div className="settings-form" style={{padding:'16px 0'}}>
                <label>Ad Soyad<input value={editForm.name??''} onChange={e=>setEditForm(f=>({...f,name:e.target.value}))} required/></label>
                <label>E-posta<input type="email" value={editForm.email??''} onChange={e=>setEditForm(f=>({...f,email:e.target.value}))} required/></label>
                <label>Telefon<input value={editForm.phone??''} onChange={e=>setEditForm(f=>({...f,phone:e.target.value||null}))}/></label>
                <label>TC Kimlik No<input value={editForm.tcNo??''} placeholder="11 haneli TC kimlik no" onChange={e=>setEditForm(f=>({...f,tcNo:e.target.value||null}))}/></label>
                <label>Rol
                  <select value={editForm.role??'USER'} onChange={e=>setEditForm(f=>({...f,role:e.target.value as Role}))}>
                    <option value="USER">Kullanici</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </label>
                <label>Kalan Ilan Hakki<input type="number" min="0" value={editForm.remainingAdLimit??0} onChange={e=>setEditForm(f=>({...f,remainingAdLimit:Number(e.target.value)}))}/></label>
                <div className="toggle-row">
                  <div className="toggle-info"><h4>Premium Uye</h4><p>Premium uyeler ilanlarini uste cikartir</p></div>
                  <label className="toggle">
                    <input type="checkbox" checked={editForm.isPremium??false} onChange={e=>setEditForm(f=>({...f,isPremium:e.target.checked}))}/>
                    <span className="toggle-slider"/>
                  </label>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={()=>setEditUser(null)}>Iptal</button>
                <button type="submit" className="btn primary-button">Kaydet</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {deleteConfirm&&(
        <div className="modal-overlay" onClick={()=>setDeleteConfirm(null)}>
          <div className="modal" onClick={e=>e.stopPropagation()}>
            <div className="modal-header">
              <h2>Kullanici Sil</h2>
              <button className="modal-close" onClick={()=>setDeleteConfirm(null)}><X size={16}/></button>
            </div>
            <div style={{padding:'20px 0'}}>
              <p style={{color:'var(--danger)',fontWeight:600,marginBottom:8}}>Bu islem geri alinamaz!</p>
              <p><strong>{deleteConfirm.name}</strong> ({deleteConfirm.email}) kullanicisi ve tum iliskili verileri (ilanlari, mesajlari, siparisleri) silinecektir.</p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={()=>setDeleteConfirm(null)}>Iptal</button>
              <button className="btn btn-danger" onClick={()=>void confirmDelete()}>Evet, Sil</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UsersPage;