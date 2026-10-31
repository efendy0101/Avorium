import { useState } from 'react';
import type { FormEvent } from 'react';
import { Anchor } from 'lucide-react';
import { api, getErrorMessage, tokenStorageKey } from '../api';
import type { AuthResponse, AuthUser } from '../api';

interface LoginProps {
  onLogin: (user: AuthUser) => void;
  initialError?: string;
}

const Login = ({ onLogin, initialError }: LoginProps) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(initialError ?? '');
  const [isLoading, setIsLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const { data } = await api.post<AuthResponse>('/auth/login', { email, password });
      if (data.user.role !== 'ADMIN') {
        setError('Bu panele sadece admin kullanıcılar erişebilir.');
        return;
      }

      localStorage.setItem(tokenStorageKey, data.token);
      localStorage.setItem('avorium_admin_user', JSON.stringify(data.user));
      onLogin(data.user);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-screen">
      <form className="login-panel" onSubmit={submit}>
        <div className="login-brand">
          <Anchor size={32} />
          <span>Avorium Admin</span>
        </div>
        <label>
          E-posta
          <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
        </label>
        <label>
          Şifre
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
        </label>
        {error && <div className="alert danger">{error}</div>}
        <button className="btn primary-button" type="submit" disabled={isLoading}>
          {isLoading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
        </button>
      </form>
    </div>
  );
};

export default Login;
