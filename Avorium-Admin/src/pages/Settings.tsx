import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Settings, AlertTriangle, Bell, Globe, Clock } from 'lucide-react';
import { api, getErrorMessage } from '../api';
import type { AppSettings } from '../api';

const SettingsPage = () => {
  const [settings, setSettings] = useState<AppSettings | null>(null);
  const [form, setForm] = useState<Partial<AppSettings>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    api.get<AppSettings>('/admin/settings')
      .then(r => { setSettings(r.data); setForm(r.data); })
      .catch(e => setError(getErrorMessage(e)));
  }, []);

  const save = async (e: FormEvent) => {
    e.preventDefault();
    setIsLoading(true); setError(''); setSuccess('');
    try {
      const { data } = await api.patch<{ settings: AppSettings }>('/admin/settings', form);
      setSettings(data.settings);
      setSuccess('Ayarlar kaydedildi.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) { setError(getErrorMessage(err)); }
    finally { setIsLoading(false); }
  };

  if (!settings && !error) return <div className="loading">Yukleniyor</div>;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Ayarlar</h1>
          <p>Uygulama genelindeki yapilandirmalari buradan yonetin.</p>
        </div>
        <button className="btn primary-button" onClick={save} disabled={isLoading}>
          <Settings size={16} />
          {isLoading ? 'Kaydediliyor...' : 'Kaydet'}
        </button>
      </div>

      {error && <div className="alert danger"><AlertTriangle size={16}/>{error}</div>}
      {success && <div className="alert success">{success}</div>}

      <form onSubmit={save}>
        <div className="settings-grid">
          <div className="settings-card">
            <h3><Globe size={18} style={{color:'var(--primary-light)'}}/>Genel Ayarlar</h3>
            <p className="settings-desc">Platformun temel yapilandirma secenekleri</p>
            <div className="settings-form">
              <label>Site Adi<input value={form.siteName??''} onChange={e=>setForm(f=>({...f,siteName:e.target.value}))}/></label>
              <label>Yeni Kullanici Ucretsiz Ilan Limiti<input type="number" min="0" value={form.defaultAdLimit??2} onChange={e=>setForm(f=>({...f,defaultAdLimit:Number(e.target.value)}))}/></label>
              <label>Ilan Gecerlilik Suresi (Gun)<input type="number" min="1" value={form.adExpiryDays??15} onChange={e=>setForm(f=>({...f,adExpiryDays:Number(e.target.value)}))}/></label>
            </div>
          </div>

          <div className="settings-card">
            <h3><Bell size={18} style={{color:'var(--warning)'}}/>Duyuru ve Bakim</h3>
            <p className="settings-desc">Kullanicilara gosterilecek bildirim ve mod ayarlari</p>
            <div className="settings-form">
              <label>Duyuru Banner Mesaji<textarea rows={3} value={form.announcementBanner??''} placeholder="Bos birakilabilir..." onChange={e=>setForm(f=>({...f,announcementBanner:e.target.value||null}))}/></label>
              <div className="toggle-row">
                <div className="toggle-info"><h4>Bakim Modu</h4><p>Acikken uygulama bakim mesaji gosterir</p></div>
                <label className="toggle"><input type="checkbox" checked={form.maintenanceMode??false} onChange={e=>setForm(f=>({...f,maintenanceMode:e.target.checked}))}/><span className="toggle-slider"/></label>
              </div>
            </div>
          </div>

          <div className="settings-card">
            <h3><Clock size={18} style={{color:'var(--accent)'}}/>Sistem Bilgisi</h3>
            <p className="settings-desc">Ayarlarin son guncelleme bilgisi</p>
            <div className="detail-grid">
              <div className="detail-item"><div className="di-label">Son Guncelleme</div><div className="di-value">{settings?.updatedAt?new Date(settings.updatedAt).toLocaleString('tr-TR'):'-'}</div></div>
              <div className="detail-item"><div className="di-label">Aktif Ilan Suresi</div><div className="di-value">{settings?.adExpiryDays} Gun</div></div>
              <div className="detail-item"><div className="di-label">Kayit Ilan Limiti</div><div className="di-value">{settings?.defaultAdLimit} Ilan</div></div>
              <div className="detail-item"><div className="di-label">Bakim Modu</div><div className="di-value" style={{color:settings?.maintenanceMode?'var(--danger)':'var(--success)'}}>{settings?.maintenanceMode?'Aktif':'Kapali'}</div></div>
            </div>
          </div>
        </div>
      </form>
    </div>
  );
};

export default SettingsPage;