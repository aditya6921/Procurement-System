const API_BASE = import.meta.env.VITE_API_BASE_URL || '';
const TOKEN_KEY = 'procureflow.accessToken';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const saveSession = ({ accessToken, user }) => {
  localStorage.setItem(TOKEN_KEY, accessToken);
  localStorage.setItem('procureflow.user', JSON.stringify(user));
};
export const clearSession = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem('procureflow.user');
};
export const getSavedUser = () => {
  try { return JSON.parse(localStorage.getItem('procureflow.user') || 'null'); }
  catch { return null; }
};

export async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json');
  const token = getToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (response.status === 204) return null;
  const payload = await response.json().catch(() => null);
  if (!response.ok) {
    const message = payload?.message || `Request failed (${response.status})`;
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }
  return payload;
}

export const json = (method, body) => ({ method, body: JSON.stringify(body) });
