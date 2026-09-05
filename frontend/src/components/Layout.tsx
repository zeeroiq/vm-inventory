import { useState } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Server,
  FolderGit2,
  History,
  ShieldCheck,
  RefreshCw,
  CheckCircle2,
  Cloud,
  ChevronDown
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { triggerGcpSync, fetchGcpSyncStatus } from '../api';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export default function Layout() {
  const location = useLocation();
  const { user, role, switchRole } = useAuth();
  const queryClient = useQueryClient();
  const [roleDropdownOpen, setRoleDropdownOpen] = useState(false);

  // Poll sync status every 30 seconds
  const { data: syncStatus } = useQuery({
    queryKey: ['gcpSyncStatus'],
    queryFn: fetchGcpSyncStatus,
    refetchInterval: 30000,
  });

  const syncMutation = useMutation({
    mutationFn: triggerGcpSync,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['gcpSyncStatus'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      queryClient.invalidateQueries({ queryKey: ['instances'] });
      queryClient.invalidateQueries({ queryKey: ['deployments'] });
    }
  });

  const navItems = [
    { name: 'Executive Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Projects', path: '/projects', icon: FolderGit2 },
    { name: 'Compute Instances', path: '/instances', icon: Server },
    { name: 'Deployments', path: '/deployments', icon: History },
    { name: 'Administration & Audit', path: '/admin', icon: ShieldCheck },
  ];

  const roleLabels: Record<string, { label: string; badge: string }> = {
    ROLE_ADMIN: { label: 'Admin', badge: 'bg-purple-100 text-purple-800 border-purple-300' },
    ROLE_SRE: { label: 'SRE Lead', badge: 'bg-blue-100 text-blue-800 border-blue-300' },
    ROLE_DEVELOPER: { label: 'Developer', badge: 'bg-emerald-100 text-emerald-800 border-emerald-300' },
    ROLE_VIEWER: { label: 'Viewer (Read-Only)', badge: 'bg-gray-100 text-gray-800 border-gray-300' },
  };

  return (
    <div className="flex h-screen bg-slate-50 text-slate-900">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-900 text-slate-200 border-r border-slate-800 flex flex-col flex-shrink-0">
        <div className="p-5 border-b border-slate-800 flex items-center space-x-3">
          <div className="w-9 h-9 rounded-lg bg-indigo-600 flex items-center justify-center text-white shadow-md">
            <Cloud className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-bold tracking-tight text-white">VM Inventory</h1>
            <p className="text-[11px] text-slate-400">GCP Compute & Artifacts</p>
          </div>
        </div>

        {/* Navigation items */}
        <nav className="p-4 space-y-1.5 flex-1 overflow-y-auto">
          <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider px-3 mb-2">
            Main Navigation
          </div>
          {navItems.map((item) => {
            const isActive = location.pathname === item.path ||
              (item.path !== '/' && location.pathname.startsWith(item.path));
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center space-x-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`}
              >
                <item.icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                <span>{item.name}</span>
              </Link>
            );
          })}
        </nav>

        {/* Sync Status Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/40 text-xs">
          <div className="flex items-center justify-between mb-1.5">
            <span className="text-slate-400">GCP Sync</span>
            <span className="flex items-center gap-1.5 text-[11px] text-emerald-400 font-medium">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              {syncStatus?.lastSyncStatus || 'ACTIVE'}
            </span>
          </div>
          <p className="text-[11px] text-slate-400 truncate">
            {syncStatus?.lastSyncTime
              ? new Date(syncStatus.lastSyncTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
              : 'Synced'}
          </p>
          <button
            onClick={() => syncMutation.mutate()}
            disabled={syncMutation.isPending}
            className="mt-2.5 w-full flex items-center justify-center gap-1.5 px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded border border-slate-700 text-xs transition disabled:opacity-50"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${syncMutation.isPending ? 'animate-spin' : ''}`} />
            <span>{syncMutation.isPending ? 'Syncing...' : 'Sync Now'}</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Header */}
        <header className="h-16 bg-white border-b border-slate-200 px-6 flex items-center justify-between z-20">
          <div className="flex items-center gap-3">
            <span className="text-xs font-semibold uppercase px-2.5 py-1 rounded bg-slate-100 text-slate-600 border border-slate-200">
              Enterprise Portal
            </span>
            <span className="text-xs text-slate-400">|</span>
            <span className="text-xs text-slate-500">
              {location.pathname === '/' ? 'Overview & Metrics' : location.pathname.replace('/', '').toUpperCase()}
            </span>
          </div>

          <div className="flex items-center gap-4">
            {/* RBAC Role Switcher */}
            <div className="relative">
              <button
                onClick={() => setRoleDropdownOpen(!roleDropdownOpen)}
                className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 text-xs transition"
              >
                <span className="text-slate-500">Role:</span>
                <span className={`px-2 py-0.5 rounded border text-xs font-semibold ${roleLabels[role]?.badge}`}>
                  {roleLabels[role]?.label}
                </span>
                <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
              </button>

              {roleDropdownOpen && (
                <div className="absolute right-0 mt-2 w-56 bg-white border border-slate-200 rounded-lg shadow-lg py-1 z-50">
                  <div className="px-3 py-1.5 text-[11px] font-semibold text-slate-400 uppercase tracking-wider border-b border-slate-100">
                    Switch Active RBAC Role
                  </div>
                  {(['ROLE_ADMIN', 'ROLE_SRE', 'ROLE_DEVELOPER', 'ROLE_VIEWER'] as const).map((r) => (
                    <button
                      key={r}
                      onClick={() => {
                        switchRole(r);
                        setRoleDropdownOpen(false);
                      }}
                      className={`w-full text-left px-3 py-2 text-xs hover:bg-slate-50 flex items-center justify-between ${
                        role === r ? 'font-bold bg-indigo-50/50 text-indigo-700' : 'text-slate-700'
                      }`}
                    >
                      <span>{roleLabels[r].label}</span>
                      {role === r && <CheckCircle2 className="w-3.5 h-3.5 text-indigo-600" />}
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* User Profile Tag */}
            <div className="flex items-center gap-2.5 pl-3 border-l border-slate-200">
              <div className="w-8 h-8 rounded-full bg-indigo-100 border border-indigo-200 flex items-center justify-center font-bold text-indigo-700 text-xs">
                {user?.username?.substring(0, 2).toUpperCase() || 'AD'}
              </div>
              <div className="text-left hidden sm:block">
                <div className="text-xs font-semibold text-slate-800">{user?.username || 'admin'}</div>
                <div className="text-[10px] text-slate-400">{user?.department || 'Architecture'}</div>
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto p-6 md:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
