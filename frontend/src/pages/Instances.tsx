import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchInstances,
  createInstance,
  deleteInstance,
  exportInstancesCsvUrl,
  fetchAllProjects,
  fetchProjectFilters
} from '../api';
import { Card } from '@/components/ui/card';
import {
  Search,
  Download,
  Plus,
  ArrowUpDown,
  Trash2,
  X
} from 'lucide-react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Instances() {
  const queryClient = useQueryClient();
  const { role } = useAuth();
  const canModify = role === 'ROLE_ADMIN' || role === 'ROLE_SRE';

  const [searchParams] = useSearchParams();
  const initialProjectId = searchParams.get('projectId') || '';
  const initialSearch = searchParams.get('search') || '';

  const [search, setSearch] = useState(initialSearch);
  const [projectId, setProjectId] = useState(initialProjectId);
  const [environment, setEnvironment] = useState('');
  const [ownerTeam, setOwnerTeam] = useState('');
  const [status, setStatus] = useState('');
  const sortBy = 'name';
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  const [page, setPage] = useState(0);

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newInstance, setNewInstance] = useState({
    id: '',
    name: '',
    projectId: initialProjectId || 'payments-dev',
    zone: 'us-central1-a',
    region: 'us-central1',
    machineType: 'n2-standard-4',
    internalIp: '10.128.0.10',
    externalIp: '',
    status: 'RUNNING',
    labels: 'tier=backend',
    ownerTeam: 'Dev Team A',
    environment: 'Dev',
    cpuCores: 4,
    memoryMb: 16384,
  });

  const { data: allProjects = [] } = useQuery({
    queryKey: ['allProjects'],
    queryFn: fetchAllProjects,
  });

  const { data: filterMeta } = useQuery({
    queryKey: ['projectFilters'],
    queryFn: fetchProjectFilters,
  });

  const { data: pageData, isLoading } = useQuery({
    queryKey: ['instances', { projectId, environment, ownerTeam, status, search, sortBy, sortDir, page }],
    queryFn: () => fetchInstances({
      projectId,
      environment,
      ownerTeam,
      status,
      search,
      sortBy,
      sortDir,
      page,
      size: 10,
    }),
  });

  const createMutation = useMutation({
    mutationFn: createInstance,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instances'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      setShowCreateModal(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteInstance,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instances'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });

  const handleExportCsv = () => {
    const url = exportInstancesCsvUrl({ projectId, environment, ownerTeam, status });
    window.open(url, '_blank');
  };

  const envBadges: Record<string, string> = {
    Dev: 'bg-blue-50 text-blue-700 border-blue-200',
    QA: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    UAT: 'bg-amber-50 text-amber-700 border-amber-200',
    Staging: 'bg-purple-50 text-purple-700 border-purple-200',
    Performance: 'bg-pink-50 text-pink-700 border-pink-200',
    Production: 'bg-red-50 text-red-700 border-red-200',
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Compute Engine VM Inventory</h2>
          <p className="text-sm text-slate-500 mt-0.5">
            Full visibility into virtual machines, hardware specs, network IPs, and currently deployed application artifacts.
          </p>
        </div>
        <div className="flex items-center gap-2.5">
          <button
            onClick={handleExportCsv}
            className="flex items-center gap-2 px-3.5 py-2 bg-white hover:bg-slate-50 text-slate-700 border border-slate-200 rounded-lg text-xs font-semibold shadow-sm transition"
          >
            <Download className="w-4 h-4 text-slate-500" />
            <span>Export to CSV</span>
          </button>
          {canModify && (
            <button
              onClick={() => setShowCreateModal(true)}
              className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold shadow-sm transition"
            >
              <Plus className="w-4 h-4" />
              <span>Provision VM</span>
            </button>
          )}
        </div>
      </div>

      {/* Filter and Search Toolbar */}
      <Card className="border border-slate-200 shadow-sm p-4">
        <div className="grid grid-cols-1 md:grid-cols-6 gap-3">
          {/* Search Box */}
          <div className="md:col-span-2 relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search VM name, ID, or IP..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          {/* Project Filter */}
          <div>
            <select
              value={projectId}
              onChange={(e) => {
                setProjectId(e.target.value);
                setPage(0);
              }}
              className="w-full text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700"
            >
              <option value="">All Projects</option>
              {allProjects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>

          {/* Environment Filter */}
          <div>
            <select
              value={environment}
              onChange={(e) => {
                setEnvironment(e.target.value);
                setPage(0);
              }}
              className="w-full text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700"
            >
              <option value="">All Environments</option>
              {filterMeta?.environments?.map((env) => (
                <option key={env} value={env}>{env}</option>
              ))}
            </select>
          </div>

          {/* Team Filter */}
          <div>
            <select
              value={ownerTeam}
              onChange={(e) => {
                setOwnerTeam(e.target.value);
                setPage(0);
              }}
              className="w-full text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700"
            >
              <option value="">All Teams</option>
              {filterMeta?.ownerTeams?.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-2">
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value);
                setPage(0);
              }}
              className="w-full text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700"
            >
              <option value="">All Statuses</option>
              <option value="RUNNING">RUNNING</option>
              <option value="STOPPED">STOPPED</option>
            </select>

            <button
              onClick={() => setSortDir(sortDir === 'asc' ? 'desc' : 'asc')}
              className="px-2.5 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-xs text-slate-700"
              title="Toggle Sort Direction"
            >
              <ArrowUpDown className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </Card>

      {/* Instances Table */}
      <Card className="border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left text-slate-600">
            <thead className="text-[11px] text-slate-500 uppercase bg-slate-50/80 border-b border-slate-200">
              <tr>
                <th className="px-5 py-3 font-semibold">Instance Name & ID</th>
                <th className="px-5 py-3 font-semibold">Status</th>
                <th className="px-5 py-3 font-semibold">Project & Env</th>
                <th className="px-5 py-3 font-semibold">Zone / Region</th>
                <th className="px-5 py-3 font-semibold">Machine Specs</th>
                <th className="px-5 py-3 font-semibold">Internal / External IP</th>
                <th className="px-5 py-3 font-semibold">Deployed Artifact</th>
                <th className="px-5 py-3 font-semibold">Owner Team</th>
                <th className="px-5 py-3 font-semibold text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={9} className="text-center py-10 text-slate-400">
                    Loading Compute Engine instances...
                  </td>
                </tr>
              ) : pageData?.content.length === 0 ? (
                <tr>
                  <td colSpan={9} className="text-center py-10 text-slate-400">
                    No instances matching current search or filters.
                  </td>
                </tr>
              ) : (
                pageData?.content.map((inst) => (
                  <tr key={inst.id} className="hover:bg-slate-50/70 transition-colors">
                    {/* Name & ID */}
                    <td className="px-5 py-3.5">
                      <div className="font-bold text-slate-900">{inst.name}</div>
                      <div className="font-mono text-[10px] text-slate-400">{inst.id}</div>
                    </td>

                    {/* Status */}
                    <td className="px-5 py-3.5">
                      {inst.status === 'RUNNING' ? (
                        <span className="inline-flex items-center gap-1 text-emerald-700 font-semibold text-[11px]">
                          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                          Running
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-slate-500 font-medium text-[11px]">
                          <span className="w-2 h-2 rounded-full bg-slate-400" />
                          {inst.status}
                        </span>
                      )}
                    </td>

                    {/* Project & Env */}
                    <td className="px-5 py-3.5">
                      <div className="text-slate-800 font-medium">{inst.projectName || inst.projectId}</div>
                      <span className={`px-2 py-0.5 rounded-full border text-[10px] font-semibold ${envBadges[inst.environment] || 'bg-slate-100 text-slate-700'}`}>
                        {inst.environment}
                      </span>
                    </td>

                    {/* Zone / Region */}
                    <td className="px-5 py-3.5">
                      <div>{inst.zone}</div>
                      <div className="text-[10px] text-slate-400">{inst.region}</div>
                    </td>

                    {/* Machine Type */}
                    <td className="px-5 py-3.5">
                      <div className="font-mono text-slate-800">{inst.machineType}</div>
                      <div className="text-[10px] text-slate-400">
                        {inst.cpuCores || 4} vCPU / {Math.round((inst.memoryMb || 16384) / 1024)}GB RAM
                      </div>
                    </td>

                    {/* IPs */}
                    <td className="px-5 py-3.5 font-mono text-[11px]">
                      <div>{inst.internalIp}</div>
                      <div className="text-slate-400">{inst.externalIp || 'No external IP'}</div>
                    </td>

                    {/* Deployed Artifact */}
                    <td className="px-5 py-3.5">
                      {inst.currentArtifact ? (
                        <div>
                          <span className="font-medium text-slate-800">{inst.currentArtifact}</span>
                          <div className="mt-0.5">
                            <span className="px-1.5 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-mono text-[10px] font-bold">
                              {inst.currentVersion}
                            </span>
                          </div>
                        </div>
                      ) : (
                        <span className="text-slate-400 italic">No artifact recorded</span>
                      )}
                    </td>

                    {/* Owner Team */}
                    <td className="px-5 py-3.5 text-slate-700">
                      {inst.ownerTeam}
                    </td>

                    {/* Actions */}
                    <td className="px-5 py-3.5 text-right">
                      {role === 'ROLE_ADMIN' && (
                        <button
                          onClick={() => {
                            if (confirm(`Delete instance ${inst.name}?`)) {
                              deleteMutation.mutate(inst.id);
                            }
                          }}
                          className="p-1.5 rounded hover:bg-red-50 text-slate-400 hover:text-red-600 transition"
                          title="Delete Instance"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        {pageData && pageData.totalPages > 1 && (
          <div className="p-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
            <span>
              Showing {pageData.pageNumber * pageData.pageSize + 1} to{' '}
              {Math.min((pageData.pageNumber + 1) * pageData.pageSize, pageData.totalElements)} of{' '}
              {pageData.totalElements} instances
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="px-2.5 py-1 rounded border border-slate-200 disabled:opacity-40 hover:bg-slate-50"
              >
                Previous
              </button>
              <span className="font-semibold text-slate-700">
                Page {page + 1} of {pageData.totalPages}
              </span>
              <button
                onClick={() => setPage(Math.min(pageData.totalPages - 1, page + 1))}
                disabled={pageData.isLast}
                className="px-2.5 py-1 rounded border border-slate-200 disabled:opacity-40 hover:bg-slate-50"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </Card>

      {/* Provision VM Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full border border-slate-200 overflow-hidden">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between">
              <h3 className="font-bold text-sm text-slate-800">Provision Compute Engine VM</h3>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                createMutation.mutate(newInstance);
              }}
              className="p-5 space-y-3.5 text-xs"
            >
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Instance ID</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g., inst-pay-dev-05"
                    value={newInstance.id}
                    onChange={(e) => setNewInstance({ ...newInstance, id: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">VM Name</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g., payment-api-vm-05"
                    value={newInstance.name}
                    onChange={(e) => setNewInstance({ ...newInstance, name: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg"
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Target GCP Project</label>
                <select
                  value={newInstance.projectId}
                  onChange={(e) => setNewInstance({ ...newInstance, projectId: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg bg-white"
                >
                  {allProjects.map((p) => (
                    <option key={p.id} value={p.id}>{p.name} ({p.environment})</option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Zone</label>
                  <input
                    type="text"
                    value={newInstance.zone}
                    onChange={(e) => setNewInstance({ ...newInstance, zone: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Machine Type</label>
                  <input
                    type="text"
                    value={newInstance.machineType}
                    onChange={(e) => setNewInstance({ ...newInstance, machineType: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Internal IP</label>
                  <input
                    type="text"
                    value={newInstance.internalIp}
                    onChange={(e) => setNewInstance({ ...newInstance, internalIp: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">External IP (Optional)</label>
                  <input
                    type="text"
                    value={newInstance.externalIp}
                    onChange={(e) => setNewInstance({ ...newInstance, externalIp: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Owner Team</label>
                  <input
                    type="text"
                    value={newInstance.ownerTeam}
                    onChange={(e) => setNewInstance({ ...newInstance, ownerTeam: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Environment</label>
                  <select
                    value={newInstance.environment}
                    onChange={(e) => setNewInstance({ ...newInstance, environment: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="Dev">Dev</option>
                    <option value="QA">QA</option>
                    <option value="UAT">UAT</option>
                    <option value="Staging">Staging</option>
                    <option value="Performance">Performance</option>
                    <option value="Production">Production</option>
                  </select>
                </div>
              </div>

              <div className="pt-2 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 text-slate-600"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-semibold disabled:opacity-50"
                >
                  {createMutation.isPending ? 'Provisioning...' : 'Provision VM'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
