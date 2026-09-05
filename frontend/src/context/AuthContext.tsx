import React, { createContext, useContext, useState, useEffect } from 'react';
import type { User } from '../api';
import { login as apiLogin, fetchCurrentUser } from '../api';

interface AuthContextType {
  user: User | null;
  role: 'ROLE_ADMIN' | 'ROLE_SRE' | 'ROLE_DEVELOPER' | 'ROLE_VIEWER';
  isAuthenticated: boolean;
  switchRole: (role: 'ROLE_ADMIN' | 'ROLE_SRE' | 'ROLE_DEVELOPER' | 'ROLE_VIEWER') => Promise<void>;
  login: (u: string, p: string) => Promise<void>;
  logout: () => void;
}

const defaultUser: User = {
  id: 1,
  username: 'admin',
  email: 'admin@cloudenterprise.org',
  role: 'ROLE_ADMIN',
  department: 'Platform Architecture',
  enabled: true,
  createdAt: new Date().toISOString()
};

const AuthContext = createContext<AuthContextType>({
  user: defaultUser,
  role: 'ROLE_ADMIN',
  isAuthenticated: true,
  switchRole: async () => {},
  login: async () => {},
  logout: () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(defaultUser);

  useEffect(() => {
    const initAuth = async () => {
      try {
        // Auto-login with default admin if no token present
        if (!localStorage.getItem('vm_auth_token')) {
          const res = await apiLogin('admin', 'Password123!');
          setUser(res.user);
        } else {
          const me = await fetchCurrentUser();
          setUser(me);
        }
      } catch (err) {
        // Fallback default state
        setUser(defaultUser);
      }
    };
    initAuth();
  }, []);

  const switchRole = async (newRole: 'ROLE_ADMIN' | 'ROLE_SRE' | 'ROLE_DEVELOPER' | 'ROLE_VIEWER') => {
    const roleUsernameMap = {
      ROLE_ADMIN: 'admin',
      ROLE_SRE: 'sre_lead',
      ROLE_DEVELOPER: 'dev_lead',
      ROLE_VIEWER: 'viewer',
    };
    const targetUsername = roleUsernameMap[newRole];
    try {
      const res = await apiLogin(targetUsername, 'Password123!');
      setUser(res.user);
    } catch {
      if (user) {
        setUser({ ...user, role: newRole, username: targetUsername });
      }
    }
  };

  const login = async (u: string, p: string) => {
    const res = await apiLogin(u, p);
    setUser(res.user);
  };

  const logout = () => {
    localStorage.removeItem('vm_auth_token');
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        role: user?.role || 'ROLE_VIEWER',
        isAuthenticated: !!user,
        switchRole,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
