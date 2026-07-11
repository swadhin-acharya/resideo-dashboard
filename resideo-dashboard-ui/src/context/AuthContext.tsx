import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { login as apiLogin, getMe, LoginResponse } from '../api/auth';
import api from '../api/client';

interface AuthCtx {
  user: LoginResponse | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  setProjectId: (id: string) => void;
  activeProjectId: string | null;
}

const AuthContext = createContext<AuthCtx>({
  user: null,
  loading: true,
  login: async () => {},
  logout: () => {},
  setProjectId: () => {},
  activeProjectId: null,
});

function getStoredToken(): string | null {
  return localStorage.getItem('dashboard_token');
}

function setStoredToken(token: string | null) {
  if (token) {
    localStorage.setItem('dashboard_token', token);
  } else {
    localStorage.removeItem('dashboard_token');
  }
}

function getStoredProject(): string | null {
  return localStorage.getItem('dashboard_project');
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeProjectId, setActiveProjectId] = useState<string | null>(getStoredProject());

  const setProjectId = useCallback((id: string | null) => {
    setActiveProjectId(id);
    if (id) {
      localStorage.setItem('dashboard_project', id);
    } else {
      localStorage.removeItem('dashboard_project');
    }
  }, []);

  const logout = useCallback(() => {
    setStoredToken(null);
    setUser(null);
    setProjectId(null);
  }, [setProjectId]);

  const login = useCallback(async (username: string, password: string) => {
    const resp = await apiLogin({ username, password });
    setStoredToken(resp.token);
    setUser(resp);
    if (resp.projects?.length > 0) {
      setProjectId(resp.projects[0].id);
    }
  }, [setProjectId]);

  useEffect(() => {
    const token = getStoredToken();
    if (!token) {
      setLoading(false);
      return;
    }
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    getMe()
      .then((u) => {
        setUser(u);
        const validProjectIds = new Set((u.projects || []).map(p => p.id));
        if (activeProjectId && !validProjectIds.has(activeProjectId) && u.projects?.length > 0) {
          setProjectId(u.projects[0].id);
        } else if (!activeProjectId && u.projects?.length > 0) {
          setProjectId(u.projects[0].id);
        }
      })
      .catch(() => {
        setStoredToken(null);
      })
      .finally(() => setLoading(false));
  }, [activeProjectId, setProjectId]);

  useEffect(() => {
    const token = getStoredToken();
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete api.defaults.headers.common['Authorization'];
    }
    if (activeProjectId) {
      api.defaults.headers.common['X-Project-Id'] = activeProjectId;
    } else {
      delete api.defaults.headers.common['X-Project-Id'];
    }
  }, [user, activeProjectId]);

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, setProjectId, activeProjectId }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
