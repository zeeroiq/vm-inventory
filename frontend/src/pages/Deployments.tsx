import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  fetchDeployments,
  recordDeployment,
  fetchInstances
} from '../api';
import { Card } from '@/components/ui/card';
import {
  Search,
  Plus,
  GitBranch,
  CheckCircle2,
  AlertOctagon,
  Copy,
  Check,
  X,
  User
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Deployments() {
  const queryClient = useQueryClient();
  const { user } = useAuth();

  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [source, setSource] = useState('');
  const [page, setPage] = useState(0);
  const [copiedCommit, setCopiedCommit] = useState<string | null>(null);

  const [showRecordModal, setShowRecordModal] = useState(false);
  const [newDep, setNewDep] = useState({
    instanceId: 'inst-pay-dev-01',
    applicationName: 'Payment Service',
    artifactName: 'payment-service.jar',
    artifactVersion: '3.2.2',
    buildNumber: '#246',
    gitCommitId: '9a8b7c6',
    branch: 'release/3.2',
    deploymentSource: 'GitHub Actions',
    deployedBy: user?.username || 'admin',
    deploymentStatus: 'SUCCESS',
    releaseNotes: 'Performance improvements and bug fixes',
  });

  const { data: instancesData } = useQuery({
    queryKey: ['allInstancesShort'],
    queryFn: () => fetchInstances({ size: 100 }),
  });

  const { data: pageData, isLoading } = useQuery({
    queryKey: ['deployments', { search, status, source, page }],
    queryFn: () => fetchDeployments({
      search,
      status,
      source,
      page,
      size: 10,
    }),
  });

  const recordMutation = useMutation({
    mutationFn: recordDeployment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deployments'] });
      queryClient.invalidateQueries({ queryKey: ['instances'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      setShowRecordModal(false);
    },
  });

  const handleCopyCommit = (commit: string) => {
    navigator.clipboard.writeText(commit);
    setCopiedCommit(commit);
    setTimeout(() => setCopiedCommit(null), 2000);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Deployment Visibility & Audit</h2>
          <p className="text-sm text-slate-500 mt-0.5">
            Audit trail of artifact releases across Jenkins, GitHub Actions, Azure DevOps, and Cloud Build pipelines.
          </p>
        </div>
        <button
          onClick={() => setShowRecordModal(true)}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold shadow-sm transition"
        >
          <Plus className="w-4 h-4" />
          <span>Ingest Deployment Metadata</span>
        </button>
      </div>

      {/* Filter and Search Bar */}
      <Card className="border border-slate-200 shadow-sm p-4">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search application, artifact, commit ID, or version..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex items-center gap-2 w-full md:w-auto">
            {/* Status Filter */}
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value);
                setPage(0);
              }}
              className="text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700 focus:outline-none"
            >
              <option value="">All Statuses</option>
              <option value="SUCCESS">SUCCESS</option>
              <option value="FAILED">FAILED</option>
            </select>

            {/* Source Filter */}
            <select
              value={source}
              onChange={(e) => {
                setSource(e.target.value);
                setPage(0);
              }}
              className="text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700 focus:outline-none"
            >
              <option value="">All CI/CD Sources</option>
              <option value="Jenkins">Jenkins</option>
              <option value="GitHub Actions">GitHub Actions</option>
              <option value="Azure DevOps">Azure DevOps</option>
              <option value="Cloud Build">Cloud Build</option>
              <option value="VM Lightweight Agent">VM Host Agent</option>
            </select>
          </div>
        </div>
      </Card>

      {/* Deployments Table */}
      <Card className="border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left text-slate-600">
            <thead className="text-[11px] text-slate-500 uppercase bg-slate-50/80 border-b border-slate-200">
              <tr>
                <th className="px-5 py-3 font-semibold">Application & Artifact</th>
                <th className="px-5 py-3 font-semibold">Version & Build</th>
                <th className="px-5 py-3 font-semibold">Target VM</th>
                <th className="px-5 py-3 font-semibold">Git Commit & Branch</th>
                <th className="px-5 py-3 font-semibold">CI/CD Source</th>
                <th className="px-5 py-3 font-semibold">Operator</th>
                <th className="px-5 py-3 font-semibold">Status</th>
                <th className="px-5 py-3 font-semibold">Timestamp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="text-center py-10 text-slate-400">
                    Loading deployment history...
                  </td>
                </tr>
              ) : pageData?.content.length === 0 ? (
                <tr>
                  <td colSpan={8} className="text-center py-10 text-slate-400">
                    No deployment records found matching filters.
                  </td>
                </tr>
              ) : (
                pageData?.content.map((dep) => (
                  <tr key={dep.id} className="hover:bg-slate-50/70 transition-colors">
                    {/* Application */}
                    <td className="px-5 py-3.5">
                      <div className="font-bold text-slate-900">{dep.applicationName}</div>
                      <div className="font-mono text-[11px] text-slate-400">{dep.artifactName}</div>
                    </td>

                    {/* Version & Build */}
                    <td className="px-5 py-3.5">
                      <span className="px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-mono text-[10px] font-bold">
                        {dep.artifactVersion}
                      </span>
                      <div className="text-[10px] text-slate-400 font-mono mt-0.5">{dep.buildNumber}</div>
                    </td>

                    {/* Target VM */}
                    <td className="px-5 py-3.5 font-mono text-[11px] text-slate-800">
                      <div className="font-semibold">{dep.instanceName || dep.instanceId}</div>
                      <div className="text-[10px] text-slate-400">{dep.projectId}</div>
                    </td>

                    {/* Commit & Branch */}
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-1.5">
                        <span className="font-mono text-[11px] bg-slate-100 px-1.5 py-0.5 rounded text-slate-700 border border-slate-200">
                          {dep.gitCommitId?.substring(0, 7) || 'n/a'}
                        </span>
                        {dep.gitCommitId && (
                          <button
                            onClick={() => handleCopyCommit(dep.gitCommitId)}
                            className="text-slate-400 hover:text-slate-600 p-0.5"
                            title="Copy full commit hash"
                          >
                            {copiedCommit === dep.gitCommitId ? (
                              <Check className="w-3 h-3 text-emerald-600" />
                            ) : (
                              <Copy className="w-3 h-3" />
                            )}
                          </button>
                        )}
                      </div>
                      <div className="flex items-center gap-1 text-[10px] text-slate-400 mt-0.5">
                        <GitBranch className="w-3 h-3" />
                        <span>{dep.branch || 'main'}</span>
                      </div>
                    </td>

                    {/* Source */}
                    <td className="px-5 py-3.5">
                      <span className="px-2 py-0.5 rounded-full bg-slate-100 text-slate-700 font-medium text-[10px]">
                        {dep.deploymentSource}
                      </span>
                    </td>

                    {/* Deployed By */}
                    <td className="px-5 py-3.5 text-slate-700">
                      <div className="flex items-center gap-1">
                        <User className="w-3 h-3 text-slate-400" />
                        <span>{dep.deployedBy}</span>
                      </div>
                    </td>

                    {/* Status */}
                    <td className="px-5 py-3.5">
                      {dep.deploymentStatus === 'SUCCESS' ? (
                        <span className="inline-flex items-center gap-1 text-emerald-700 font-semibold text-[11px]">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          Success
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-red-700 font-semibold text-[11px]">
                          <AlertOctagon className="w-3.5 h-3.5" />
                          {dep.deploymentStatus}
                        </span>
                      )}
                      {dep.releaseNotes && (
                        <div className="text-[10px] text-slate-400 truncate max-w-[140px]" title={dep.releaseNotes}>
                          {dep.releaseNotes}
                        </div>
                      )}
                    </td>

                    {/* Timestamp */}
                    <td className="px-5 py-3.5 text-slate-400 font-mono text-[11px]">
                      {new Date(dep.deploymentTimestamp).toLocaleString()}
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
              {pageData.totalElements} records
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

      {/* Ingest Deployment Modal */}
      {showRecordModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full border border-slate-200 overflow-hidden">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h3 className="font-bold text-sm text-slate-800">CI/CD Deployment Metadata Ingest</h3>
                <p className="text-[11px] text-slate-400">Simulate Option 2 webhook ingestion from CI/CD pipeline</p>
              </div>
              <button
                onClick={() => setShowRecordModal(false)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                recordMutation.mutate(newDep);
              }}
              className="p-5 space-y-3.5 text-xs"
            >
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Target VM Instance</label>
                <select
                  value={newDep.instanceId}
                  onChange={(e) => setNewDep({ ...newDep, instanceId: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg bg-white font-mono"
                >
                  {instancesData?.content.map((i) => (
                    <option key={i.id} value={i.id}>
                      {i.name} ({i.id}) - {i.environment}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Application Name</label>
                  <input
                    type="text"
                    required
                    value={newDep.applicationName}
                    onChange={(e) => setNewDep({ ...newDep, applicationName: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Artifact Name</label>
                  <input
                    type="text"
                    required
                    value={newDep.artifactName}
                    onChange={(e) => setNewDep({ ...newDep, artifactName: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Artifact Version</label>
                  <input
                    type="text"
                    required
                    value={newDep.artifactVersion}
                    onChange={(e) => setNewDep({ ...newDep, artifactVersion: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Build Number</label>
                  <input
                    type="text"
                    value={newDep.buildNumber}
                    onChange={(e) => setNewDep({ ...newDep, buildNumber: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Git Commit ID</label>
                  <input
                    type="text"
                    value={newDep.gitCommitId}
                    onChange={(e) => setNewDep({ ...newDep, gitCommitId: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Branch</label>
                  <input
                    type="text"
                    value={newDep.branch}
                    onChange={(e) => setNewDep({ ...newDep, branch: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Deployment Source</label>
                  <select
                    value={newDep.deploymentSource}
                    onChange={(e) => setNewDep({ ...newDep, deploymentSource: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="Jenkins">Jenkins</option>
                    <option value="GitHub Actions">GitHub Actions</option>
                    <option value="Azure DevOps">Azure DevOps</option>
                    <option value="Cloud Build">Cloud Build</option>
                  </select>
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Status</label>
                  <select
                    value={newDep.deploymentStatus}
                    onChange={(e) => setNewDep({ ...newDep, deploymentStatus: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="SUCCESS">SUCCESS</option>
                    <option value="FAILED">FAILED</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Release Notes</label>
                <textarea
                  rows={2}
                  value={newDep.releaseNotes}
                  onChange={(e) => setNewDep({ ...newDep, releaseNotes: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowRecordModal(false)}
                  className="px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 text-slate-600"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={recordMutation.isPending}
                  className="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-semibold disabled:opacity-50"
                >
                  {recordMutation.isPending ? 'Recording...' : 'Record Deployment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
