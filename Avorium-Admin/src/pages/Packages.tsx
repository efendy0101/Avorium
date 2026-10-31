import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { api, getErrorMessage } from '../api';
import type { PackageInput, PackageItem } from '../api';

const emptyForm: PackageInput = {
  name: '',
  price: 0,
  adLimit: 1,
  features: '',
  googleSkuId: ''
};

const PackagesPage = () => {
  const [packages, setPackages] = useState<PackageItem[]>([]);
  const [form, setForm] = useState<PackageInput>(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [error, setError] = useState('');

  const loadPackages = async () => {
    const { data } = await api.get<PackageItem[]>('/admin/packages');
    setPackages(data);
  };

  useEffect(() => {
    loadPackages().catch((requestError) => setError(getErrorMessage(requestError)));
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    try {
      if (editingId) {
        await api.patch(`/admin/packages/${editingId}`, form);
      } else {
        await api.post('/admin/packages', form);
      }
      setForm(emptyForm);
      setEditingId(null);
      await loadPackages();
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  };

  const editPackage = (pkg: PackageItem) => {
    setEditingId(pkg.id);
    setForm({
      name: pkg.name,
      price: pkg.price,
      adLimit: pkg.adLimit,
      features: pkg.features ?? '',
      googleSkuId: pkg.googleSkuId ?? ''
    });
  };

  const removePackage = async (id: string) => {
    await api.delete(`/admin/packages/${id}`);
    await loadPackages();
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Paketler</h1>
          <p>Ilan hakki paketlerini olusturun ve guncelleyin.</p>
        </div>
      </div>
      {error && <div className="alert danger">{error}</div>}

      <form className="form-grid" onSubmit={submit}>
        <label>
          Paket adi
          <input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required />
        </label>
        <label>
          Fiyat
          <input type="number" min="0" value={form.price} onChange={(event) => setForm({ ...form, price: Number(event.target.value) })} required />
        </label>
        <label>
          Ilan hakki
          <input type="number" min="1" value={form.adLimit} onChange={(event) => setForm({ ...form, adLimit: Number(event.target.value) })} required />
        </label>
        <label>
          Ozellikler
          <input value={form.features ?? ''} onChange={(event) => setForm({ ...form, features: event.target.value })} />
        </label>
        <label>
          Google Play SKU ID
          <input
            value={form.googleSkuId ?? ''}
            placeholder="ornek: avorium_basic_5ads"
            onChange={(event) => setForm({ ...form, googleSkuId: event.target.value })}
          />
        </label>
        <button className="btn primary-button" type="submit">{editingId ? 'Guncelle' : 'Ekle'}</button>
      </form>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>Paket</th>
              <th>Fiyat</th>
              <th>Ilan Hakki</th>
              <th>Ozellikler</th>
              <th>Google SKU ID</th>
              <th>Siparis</th>
              <th>Islem</th>
            </tr>
          </thead>
          <tbody>
            {packages.map((pkg) => (
              <tr key={pkg.id}>
                <td>{pkg.name}</td>
                <td>{pkg.price} TL</td>
                <td>{pkg.adLimit}</td>
                <td>{pkg.features ?? '-'}</td>
                <td><code style={{fontSize:12,color:'var(--primary-light)'}}>{pkg.googleSkuId ?? '-'}</code></td>
                <td>{pkg._count?.orders ?? 0}</td>
                <td>
                  <div className="actions">
                    <button className="btn btn-success" type="button" onClick={() => editPackage(pkg)}>Duzenle</button>
                    <button className="btn btn-danger" type="button" onClick={() => void removePackage(pkg.id)}>Sil</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PackagesPage;