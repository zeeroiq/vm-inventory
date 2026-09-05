import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchUsers,
  updateUserRole,
  fetchAuditLogs,
  triggerGcpSync,
  fetchGcpSyncStatus,
  sendAgentReport,
  fetchManifest
} from '../api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  RefreshCw,
  Clock,
  Terminal,
  Send,
  CheckCircle2,
  UserCheck,
  Lock
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Administration() {
  const queryClient = useQueryClient();
  const { role } = useAuth();
  const isAdmin = role === 'ROLE_ADMIN';

  const [activeTab, setActiveTab] = useState<'rbac' | 'sync' | 'discovery' | 'audit'>('rbac');
  const [auditPage, setAuditPage] = useState(0);

  // Agent Discovery Test state
  const [agentPayload, setAgentPayload] = useState({
    instanceId: 'inst-pay-dev-01',
    applicationName: 'Payment Service Host Agent',
    artifactName: 'payment-service-daemon.jar',
    artifactVersion: '3.2.5',
    buildNumber: '#301',
    gitCommitId: 'd4e5f6a',
    branch: 'release/v3',
    status: 'SUCCESS',
  });
  const [agentResult, setAgentResult] = useState<any>(null);

  // Queries
  const { data: users = [] } = useQuery({
    queryKey: ['adminUsers'],
    queryFn: fetchUsers,
    enabled: isAdmin,
  });

  const { data: auditData } = useQuery({
    queryKey: ['adminAuditLogs', auditPage],
    queryFn: () => fetchAuditLogs(auditPage, 15),
    enabled: isAdmin,
  });

  const { data: syncStatus } = useQuery({
    queryKey: ['gcpSyncStatusAdmin'],
    queryFn: fetchGcpSyncStatus,
    refetchInterval: 10000,
  });

  const { data: manifest } = useQuery({
    queryKey: ['discoveryManifest'],
    queryFn: fetchManifest,
  });

  // Mutations
  const updateRoleMutation = useMutation({
    mutationFn: ({ id, newRole }: { id: number; newRole: string }) => updateUserRole(id, newRole),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminUsers'] });
    },
  });

  const syncMutation = useMutation({
    mutationFn: triggerGcpSync,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['gcpSyncStatusAdmin'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      queryClient.invalidateQueries({ queryKey: ['instances'] });
      queryClient.invalidateQueries({ queryKey: ['deployments'] });
      queryClient.invalidateQueries({ queryKey: ['adminAuditLogs'] });
    },
  });

  const agentMutation = useMutation({
    mutationFn: sendAgentReport,
    onSuccess: (data) => {
      setAgentResult(data);
      queryClient.invalidateQueries({ queryKey: ['deployments'] });
      queryClient.invalidateQueries({ queryKey: ['instances'] });
    },
  });

  if (!isAdmin) {
    return (
      <div className="p-8 max-w-lg mx-auto text-center space-y-3">
        <div className="w-12 h-12 rounded-full bg-amber-50 border border-amber-200 text-amber-600 flex items-center justify-center mx-auto">
          <Lock className="w-6 h-6" />
        </div>
        <h3 className="text-base font-bold text-slate-900">Administrator Access Required</h3>
        <p className="text-xs text-slate-500">
          Your current role is <span className="font-semibold text-slate-800">{role}</span>. Administration, user role management, and audit logs require <span className="font-semibold text-indigo-600">ROLE_ADMIN</span>.
        </p>
        <p className="text-[11px] text-slate-400">
          Tip: You can use the Role switcher in the top navigation bar to switch to Admin for testing.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900">Administration & Governance</h2>
        <p className="text-sm text-slate-500 mt-0.5">
          RBAC user assignments, GCP API synchronization, Artifact Discovery simulation bench, and Audit logs.
        </p>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 text-xs font-semibold space-x-6">
        <button
          onClick={() => setActiveTab('rbac')}
          className={`pb-3 border-b-2 transition flex items-center gap-2 ${
            activeTab === 'rbac'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <UserCheck className="w-4 h-4" />
          <span>RBAC Users & Roles</span>
        </button>

        <button
          onClick={() => setActiveTab('sync')}
          className={`pb-3 border-b-2 transition flex items-center gap-2 ${
            activeTab === 'sync'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <RefreshCw className="w-4 h-4" />
          <span>GCP Synchronization Engine</span>
        </button>

        <button
          onClick={() => setActiveTab('discovery')}
          className={`pb-3 border-b-2 transition flex items-center gap-2 ${
            activeTab === 'discovery'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Terminal className="w-4 h-4" />
          <span>Artifact Discovery Bench</span>
        </button>

        <button
          onClick={() => setActiveTab('audit')}
          className={`pb-3 border-b-2 transition flex items-center gap-2 ${
            activeTab === 'audit'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Clock className="w-4 h-4" />
          <span>Security Audit Trail</span>
        </button>
      </div>

      {/* Tab 1: RBAC Users */}
      {activeTab === 'rbac' && (
        <Card className="border border-slate-200 shadow-sm overflow-hidden">
          <CardHeader className="pb-3 border-b border-slate-100">
            <CardTitle className="text-sm font-bold text-slate-800">Enterprise Users & Role Assignments</CardTitle>
            <p className="text-xs text-slate-500 mt-0.5">Define role-based authorization for portal operations</p>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left text-slate-600">
                <thead className="text-[11px] text-slate-500 uppercase bg-slate-50/80 border-b border-slate-200">
                  <tr>
                    <th className="px-6 py-3 font-semibold">Username</th>
                    <th className="px-6 py-3 font-semibold">Email</th>
                    <th className="px-6 py-3 font-semibold">Department</th>
                    <th className="px-6 py-3 font-semibold">Assigned Role</th>
                    <th className="px-6 py-3 font-semibold">Status</th>
                    <th className="px-6 py-3 font-semibold text-right">Modify Role</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-slate-50/70">
                      <td className="px-6 py-4 font-bold text-slate-900">{u.username}</td>
                      <td className="px-6 py-4 font-mono text-[11px] text-slate-500">{u.email}</td>
                      <td className="px-6 py-4">{u.department || 'Platform Engineering'}</td>
                      <td className="px-6 py-4">
                        <span className={`px-2 py-0.5 rounded border text-[10px] font-bold ${
                          u.role === 'ROLE_ADMIN'
                            ? 'bg-purple-50 text-purple-700 border-purple-200'
                            : u.role === 'ROLE_SRE'
                            ? 'bg-blue-50 text-blue-700 border-blue-200'
                            : u.role === 'ROLE_DEVELOPER'
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            : 'bg-slate-50 text-slate-700 border-slate-200'
                        }`}>
                          {u.role}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span className="inline-flex items-center gap-1 text-emerald-600 font-semibold text-[11px]">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Active
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <select
                          value={u.role}
                          onChange={(e) => updateRoleMutation.mutate({ id: u.id, newRole: e.target.value })}
                          disabled={updateRoleMutation.isPending}
                          className="text-xs border border-slate-200 rounded px-2 py-1 bg-white text-slate-700"
                        >
                          <option value="ROLE_ADMIN">ROLE_ADMIN</option>
                          <option value="ROLE_SRE">ROLE_SRE</option>
                          <option value="ROLE_DEVELOPER">ROLE_DEVELOPER</option>
                          <option value="ROLE_VIEWER">ROLE_VIEWER</option>
                        </select>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Tab 2: GCP Synchronization Engine */}
      {activeTab === 'sync' && (
        <div className="space-y-6">
          <Card className="border border-slate-200 shadow-sm p-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h3 className="text-base font-bold text-slate-900">Google Cloud Platform Synchronization</h3>
                <p className="text-xs text-slate-500 mt-1 max-w-xl">
                  Synchronizes inventory from Resource Manager API, Compute Engine API, and Asset Inventory API. Scheduled to run automatically in the background every 5 minutes.
                </p>
              </div>
              <button
                onClick={() => syncMutation.mutate()}
                disabled={syncMutation.isPending}
                className="flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold shadow-sm transition disabled:opacity-50"
              >
                <RefreshCw className={`w-4 h-4 ${syncMutation.isPending ? 'animate-spin' : ''}`} />
                <span>{syncMutation.isPending ? 'Synchronizing GCP Inventory...' : 'Trigger Manual GCP Sync'}</span>
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-6">
              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[11px] text-slate-400 font-medium uppercase tracking-wider block">Sync Engine Status</span>
                <span className="text-lg font-bold text-slate-900 mt-1 block">
                  {syncStatus?.lastSyncStatus || 'ACTIVE'}
                </span>
                <span className="text-[11px] text-emerald-600 font-semibold mt-1 inline-flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3" /> Scheduled Cron Active
                </span>
              </div>

              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[11px] text-slate-400 font-medium uppercase tracking-wider block">Last Completed Sync</span>
                <span className="text-lg font-bold text-slate-900 mt-1 block">
                  {syncStatus?.lastSyncTime ? new Date(syncStatus.lastSyncTime).toLocaleTimeString() : 'Just now'}
                </span>
                <span className="text-[11px] text-slate-500 mt-1 block">
                  {syncStatus?.lastSyncTime ? new Date(syncStatus.lastSyncTime).toLocaleDateString() : ''}
                </span>
              </div>

              <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[11px] text-slate-400 font-medium uppercase tracking-wider block">Discovered Resources</span>
                <span className="text-lg font-bold text-slate-900 mt-1 block">
                  {syncStatus?.syncedProjects || 6} Projects / {syncStatus?.syncedInstances || 8} VMs
                </span>
                <span className="text-[11px] text-indigo-600 font-semibold mt-1 block">
                  100% Up to date
                </span>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Tab 3: Artifact Discovery Bench */}
      {activeTab === 'discovery' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Option 1: Agent Heartbeat */}
          <Card className="border border-slate-200 shadow-sm p-5 space-y-4">
            <div>
              <span className="px-2 py-0.5 rounded bg-blue-50 text-blue-700 text-[10px] font-bold border border-blue-200">
                OPTION 1: AGENT-BASED
              </span>
              <h4 className="text-sm font-bold text-slate-900 mt-1.5">Lightweight Host Agent Discovery</h4>
              <p className="text-xs text-slate-500 mt-0.5">
                Simulate an agent running on a VM instance posting its runtime artifact version directly to the portal.
              </p>
            </div>

            <form
              onSubmit={(e) => {
                e.preventDefault();
                agentMutation.mutate(agentPayload);
              }}
              className="space-y-3 text-xs"
            >
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Target VM Instance ID</label>
                <input
                  type="text"
                  required
                  value={agentPayload.instanceId}
                  onChange={(e) => setAgentPayload({ ...agentPayload, instanceId: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Artifact Name</label>
                  <input
                    type="text"
                    required
                    value={agentPayload.artifactName}
                    onChange={(e) => setAgentPayload({ ...agentPayload, artifactName: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Version</label>
                  <input
                    type="text"
                    required
                    value={agentPayload.artifactVersion}
                    onChange={(e) => setAgentPayload({ ...agentPayload, artifactVersion: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={agentMutation.isPending}
                className="w-full flex items-center justify-center gap-2 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-semibold disabled:opacity-50"
              >
                <Send className="w-3.5 h-3.5" />
                <span>{agentMutation.isPending ? 'Sending Heartbeat...' : 'Transmit Agent Discovery Heartbeat'}</span>
              </button>
            </form>

            {agentResult && (
              <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs">
                <span className="font-bold flex items-center gap-1">
                  <CheckCircle2 className="w-3.5 h-3.5" /> Ingested Successfully
                </span>
                <p className="mt-1 font-mono text-[11px]">
                  Artifact {agentResult.artifactName}:{agentResult.artifactVersion} bound to instance {agentResult.instanceId}.
                </p>
              </div>
            )}
          </Card>

          {/* Option 3: Startup Manifest */}
          <Card className="border border-slate-200 shadow-sm p-5 space-y-4">
            <div>
              <span className="px-2 py-0.5 rounded bg-purple-50 text-purple-700 text-[10px] font-bold border border-purple-200">
                OPTION 3: STARTUP MANIFEST & ACTUATOR
              </span>
              <h4 className="text-sm font-bold text-slate-900 mt-1.5">Application Build Manifest</h4>
              <p className="text-xs text-slate-500 mt-0.5">
                Exposes /actuator/info and /api/discovery/manifest build-info metadata for automated discovery.
              </p>
            </div>

            <div className="p-4 bg-slate-900 rounded-xl text-slate-200 font-mono text-xs overflow-x-auto">
              <pre>{JSON.stringify(manifest || {}, null, 2)}</pre>
            </div>

            <div className="text-xs text-slate-500 space-y-1">
              <p className="font-semibold text-slate-700">Supported Endpoints:</p>
              <p className="font-mono text-[11px] text-indigo-600">GET /api/discovery/manifest</p>
              <p className="font-mono text-[11px] text-indigo-600">GET /actuator/info</p>
              <p className="font-mono text-[11px] text-indigo-600">GET /actuator/health</p>
            </div>
          </Card>
        </div>
      )}

      {/* Tab 4: Audit Trail */}
      {activeTab === 'audit' && (
        <Card className="border border-slate-200 shadow-sm overflow-hidden">
          <CardHeader className="pb-3 border-b border-slate-100">
            <CardTitle className="text-sm font-bold text-slate-800">Platform Security Audit Log</CardTitle>
            <p className="text-xs text-slate-500 mt-0.5">Tamper-evident record of administrative and deployment actions</p>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left text-slate-600">
                <thead className="text-[11px] text-slate-500 uppercase bg-slate-50/80 border-b border-slate-200">
                  <tr>
                    <th className="px-5 py-3 font-semibold">Timestamp</th>
                    <th className="px-5 py-3 font-semibold">User</th>
                    <th className="px-5 py-3 font-semibold">Action</th>
                    <th className="px-5 py-3 font-semibold">Resource</th>
                    <th className="px-5 py-3 font-semibold">Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {auditData?.content.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-50/70">
                      <td className="px-5 py-3 font-mono text-[11px] text-slate-400">
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                      <td className="px-5 py-3 font-semibold text-slate-900">{log.username}</td>
                      <td className="px-5 py-3">
                        <span className="px-2 py-0.5 rounded bg-slate-100 border border-slate-200 font-mono text-[10px] font-bold text-slate-800">
                          {log.action}
                        </span>
                      </td>
                      <td className="px-5 py-3 font-mono text-slate-700">
                        {log.resourceType}: {log.resourceId}
                      </td>
                      <td className="px-5 py-3 text-slate-600">{log.details}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {auditData && auditData.totalPages > 1 && (
              <div className="p-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                <span>Page {auditData.pageNumber + 1} of {auditData.totalPages}</span>
                <div className="flex gap-2">
                  <button
                    onClick={() => setAuditPage(Math.max(0, auditPage - 1))}
                    disabled={auditPage === 0}
                    className="px-2.5 py-1 rounded border border-slate-200 disabled:opacity-40"
                  >
                    Previous
                  </button>
                  <button
                    onClick={() => setAuditPage(Math.min(auditData.totalPages - 1, auditPage + 1))}
                    disabled={auditData.isLast}
                    className="px-2.5 py-1 rounded border border-slate-200 disabled:opacity-40"
                  >
                    Next
                  </button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
