import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchProjects, createProject, deleteProject, fetchProjectFilters } from '../api';
import { Card } from '@/components/ui/card';
import {
  Search,
  Plus,
  ArrowUpDown,
  ExternalLink,
  Trash2,
  Server,
  X
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Projects() {
  const queryClient = useQueryClient();
  const { role } = useAuth();
  const canModify = role === 'ROLE_ADMIN' || role === 'ROLE_SRE';

  const [search, setSearch] = useState('');
  const [selectedEnv, setSelectedEnv] = useState('');
  const [selectedTeam, setSelectedTeam] = useState('');
  const sortBy = 'name';
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  const [page, setPage] = useState(0);

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newProject, setNewProject] = useState({
    id: '',
    name: '',
    businessUnit: 'Retail Banking',
    ownerTeam: 'Dev Team A',
    environment: 'Dev',
    description: '',
  });

  const { data: filterMeta } = useQuery({
    queryKey: ['projectFilters'],
    queryFn: fetchProjectFilters,
  });

  const { data: pageData, isLoading } = useQuery({
    queryKey: ['projects', { search, selectedEnv, selectedTeam, sortBy, sortDir, page }],
    queryFn: () => fetchProjects({
      search,
      environment: selectedEnv,
      ownerTeam: selectedTeam,
      sortBy,
      sortDir,
      page,
      size: 10,
    }),
  });

  const createMutation = useMutation({
    mutationFn: createProject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      setShowCreateModal(false);
      setNewProject({
        id: '',
        name: '',
        businessUnit: 'Retail Banking',
        ownerTeam: 'Dev Team A',
        environment: 'Dev',
        description: '',
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteProject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });

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
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">GCP Projects Inventory</h2>
          <p className="text-sm text-slate-500 mt-0.5">
            Discover, search, filter and monitor all GCP Projects and their assigned compute resources.
          </p>
        </div>
        {canModify && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold shadow-sm transition"
          >
            <Plus className="w-4 h-4" />
            <span>Register GCP Project</span>
          </button>
        )}
      </div>

      {/* Filter and Search Bar */}
      <Card className="border border-slate-200 shadow-sm p-4">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Search by project ID, name, or business unit..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
            {/* Environment Filter */}
            <select
              value={selectedEnv}
              onChange={(e) => {
                setSelectedEnv(e.target.value);
                setPage(0);
              }}
              className="text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700 focus:outline-none"
            >
              <option value="">All Environments</option>
              {filterMeta?.environments?.map((env) => (
                <option key={env} value={env}>{env}</option>
              ))}
            </select>

            {/* Team Filter */}
            <select
              value={selectedTeam}
              onChange={(e) => {
                setSelectedTeam(e.target.value);
                setPage(0);
              }}
              className="text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white text-slate-700 focus:outline-none"
            >
              <option value="">All Owner Teams</option>
              {filterMeta?.ownerTeams?.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>

            {/* Sort Direction Toggle */}
            <button
              onClick={() => setSortDir(sortDir === 'asc' ? 'desc' : 'asc')}
              className="flex items-center gap-1 text-xs border border-slate-200 rounded-lg px-2.5 py-1.5 bg-white hover:bg-slate-50 text-slate-700"
              title="Toggle Sort Order"
            >
              <ArrowUpDown className="w-3.5 h-3.5 text-slate-400" />
              <span>{sortDir.toUpperCase()}</span>
            </button>
          </div>
        </div>
      </Card>

      {/* Projects Table */}
      <Card className="border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left text-slate-600">
            <thead className="text-[11px] text-slate-500 uppercase bg-slate-50/80 border-b border-slate-200">
              <tr>
                <th className="px-6 py-3 font-semibold">Project Name & ID</th>
                <th className="px-6 py-3 font-semibold">Environment</th>
                <th className="px-6 py-3 font-semibold">Business Unit</th>
                <th className="px-6 py-3 font-semibold">Owner Team</th>
                <th className="px-6 py-3 font-semibold">Compute VMs</th>
                <th className="px-6 py-3 font-semibold">Created Date</th>
                <th className="px-6 py-3 font-semibold text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="text-center py-10 text-slate-400">
                    Loading projects data...
                  </td>
                </tr>
              ) : pageData?.content.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-10 text-slate-400">
                    No matching GCP projects found.
                  </td>
                </tr>
              ) : (
                pageData?.content.map((project) => (
                  <tr key={project.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="px-6 py-4">
                      <div>
                        <Link
                          to={`/projects/${project.id}`}
                          className="font-bold text-slate-900 hover:text-indigo-600 hover:underline flex items-center gap-1.5"
                        >
                          <span>{project.name}</span>
                          <ExternalLink className="w-3 h-3 text-slate-400" />
                        </Link>
                        <span className="font-mono text-[11px] text-slate-400">{project.id}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded-full border text-[10px] font-semibold ${envBadges[project.environment] || 'bg-slate-100 text-slate-700'}`}>
                        {project.environment}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-800 font-medium">
                      {project.businessUnit || 'General'}
                    </td>
                    <td className="px-6 py-4 text-slate-700">
                      {project.ownerTeam}
                    </td>
                    <td className="px-6 py-4">
                      <Link
                        to={`/instances?projectId=${project.id}`}
                        className="inline-flex items-center gap-1 px-2 py-0.5 rounded bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-semibold border border-indigo-200 text-[11px]"
                      >
                        <Server className="w-3 h-3" />
                        <span>{project.instanceCount || 0} Instances</span>
                      </Link>
                    </td>
                    <td className="px-6 py-4 text-slate-400 text-[11px]">
                      {project.creationDate ? new Date(project.creationDate).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Link
                          to={`/projects/${project.id}`}
                          className="p-1.5 rounded hover:bg-slate-100 text-slate-600 hover:text-indigo-600"
                          title="View Details"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                        </Link>
                        {role === 'ROLE_ADMIN' && (
                          <button
                            onClick={() => {
                              if (confirm(`Are you sure you want to delete project ${project.name}?`)) {
                                deleteMutation.mutate(project.id);
                              }
                            }}
                            className="p-1.5 rounded hover:bg-red-50 text-slate-400 hover:text-red-600 transition"
                            title="Delete Project (Admin only)"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
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
              {pageData.totalElements} projects
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

      {/* Create Project Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full border border-slate-200 overflow-hidden">
            <div className="p-4 border-b border-slate-100 flex items-center justify-between">
              <h3 className="font-bold text-sm text-slate-800">Register New GCP Project</h3>
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
                createMutation.mutate(newProject);
              }}
              className="p-5 space-y-4 text-xs"
            >
              <div>
                <label className="font-semibold text-slate-700 block mb-1">GCP Project ID</label>
                <input
                  type="text"
                  required
                  placeholder="e.g., payments-prod-02"
                  value={newProject.id}
                  onChange={(e) => setNewProject({ ...newProject, id: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 font-mono"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Display Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g., Payments Processing Prod"
                  value={newProject.name}
                  onChange={(e) => setNewProject({ ...newProject, name: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Environment</label>
                  <select
                    value={newProject.environment}
                    onChange={(e) => setNewProject({ ...newProject, environment: e.target.value })}
                    className="w-full px-2.5 py-1.5 border border-slate-200 rounded-lg bg-white"
                  >
                    <option value="Dev">Dev</option>
                    <option value="QA">QA</option>
                    <option value="UAT">UAT</option>
                    <option value="Staging">Staging</option>
                    <option value="Performance">Performance</option>
                    <option value="Production">Production</option>
                  </select>
                </div>

                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Owner Team</label>
                  <input
                    type="text"
                    required
                    value={newProject.ownerTeam}
                    onChange={(e) => setNewProject({ ...newProject, ownerTeam: e.target.value })}
                    className="w-full px-3 py-1.5 border border-slate-200 rounded-lg"
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Business Unit</label>
                <input
                  type="text"
                  value={newProject.businessUnit}
                  onChange={(e) => setNewProject({ ...newProject, businessUnit: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Description</label>
                <textarea
                  rows={2}
                  value={newProject.description}
                  onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
                  className="w-full px-3 py-1.5 border border-slate-200 rounded-lg"
                />
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
                  {createMutation.isPending ? 'Saving...' : 'Create Project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
