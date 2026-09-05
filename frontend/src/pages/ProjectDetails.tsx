import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchProjectById, fetchProjectInstances } from '../api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  ArrowLeft,
  Server,
  FolderGit2,
  Calendar,
  ExternalLink
} from 'lucide-react';

export default function ProjectDetails() {
  const { id } = useParams<{ id: string }>();

  const { data: project, isLoading: loadingProject } = useQuery({
    queryKey: ['project', id],
    queryFn: () => fetchProjectById(id!),
    enabled: !!id,
  });

  const { data: instances = [], isLoading: loadingInstances } = useQuery({
    queryKey: ['projectInstances', id],
    queryFn: () => fetchProjectInstances(id!),
    enabled: !!id,
  });

  if (loadingProject || loadingInstances) {
    return (
      <div className="flex items-center justify-center h-64 text-slate-400">
        <div className="w-8 h-8 border-3 border-indigo-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!project) {
    return (
      <div className="space-y-4">
        <Link to="/projects" className="inline-flex items-center gap-1.5 text-xs text-indigo-600 hover:underline">
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Projects
        </Link>
        <Card className="p-8 text-center text-slate-500">
          <p className="font-semibold">Project not found: {id}</p>
        </Card>
      </div>
    );
  }

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
      <Link
        to="/projects"
        className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-indigo-600 transition"
      >
        <ArrowLeft className="w-3.5 h-3.5" /> Back to Projects
      </Link>

      {/* Project Overview Card */}
      <Card className="border border-slate-200 shadow-sm">
        <CardHeader className="border-b border-slate-100 pb-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-lg bg-indigo-50 text-indigo-600 border border-indigo-100">
                <FolderGit2 className="w-6 h-6" />
              </div>
              <div>
                <h2 className="text-xl font-bold text-slate-900">{project.name}</h2>
                <div className="flex items-center gap-2 mt-0.5">
                  <span className="font-mono text-xs text-slate-500">{project.id}</span>
                  <span className="text-slate-300">|</span>
                  <span className={`px-2 py-0.5 rounded-full border text-[10px] font-semibold ${envBadges[project.environment] || 'bg-slate-100 text-slate-700'}`}>
                    {project.environment}
                  </span>
                </div>
              </div>
            </div>
            <div className="flex items-center gap-2 text-xs">
              <div className="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-700">
                <span className="text-slate-400 block text-[10px]">Owner Team</span>
                <span className="font-semibold">{project.ownerTeam}</span>
              </div>
              <div className="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-slate-700">
                <span className="text-slate-400 block text-[10px]">Business Unit</span>
                <span className="font-semibold">{project.businessUnit || 'General'}</span>
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent className="pt-4 text-xs text-slate-600">
          <p className="text-slate-600">{project.description || 'No description provided for this project.'}</p>
          <div className="mt-4 flex items-center gap-6 text-[11px] text-slate-400">
            <span className="flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" />
              Created on {project.creationDate ? new Date(project.creationDate).toLocaleDateString() : 'N/A'}
            </span>
            <span className="flex items-center gap-1">
              <Server className="w-3.5 h-3.5" />
              {instances.length} VM Instances Managed
            </span>
          </div>
        </CardContent>
      </Card>

      {/* Compute Engine Instances Table */}
      <Card className="border border-slate-200 shadow-sm overflow-hidden">
        <CardHeader className="pb-3 border-b border-slate-100 flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-sm font-bold text-slate-800">Compute Engine VM Instances</CardTitle>
            <p className="text-xs text-slate-500 mt-0.5">VM hosts and deployed application artifacts</p>
          </div>
          <Link
            to={`/instances?projectId=${project.id}`}
            className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
          >
            <span>Open in Full Inventory</span>
            <ExternalLink className="w-3 h-3" />
          </Link>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left text-slate-600">
              <thead className="text-[11px] text-slate-500 uppercase bg-slate-50/80 border-b border-slate-200">
                <tr>
                  <th className="px-5 py-3">Instance Name</th>
                  <th className="px-5 py-3">Zone / Region</th>
                  <th className="px-5 py-3">Machine Type</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3">Internal / External IP</th>
                  <th className="px-5 py-3">Deployed Artifact</th>
                  <th className="px-5 py-3">Version</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {instances.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center py-8 text-slate-400">
                      No Compute Engine instances provisioned in this project yet.
                    </td>
                  </tr>
                ) : (
                  instances.map((inst) => (
                    <tr key={inst.id} className="hover:bg-slate-50/70">
                      <td className="px-5 py-3.5">
                        <div className="font-semibold text-slate-900">{inst.name}</div>
                        <div className="font-mono text-[10px] text-slate-400">{inst.id}</div>
                      </td>
                      <td className="px-5 py-3.5">
                        <div>{inst.zone}</div>
                        <div className="text-[10px] text-slate-400">{inst.region}</div>
                      </td>
                      <td className="px-5 py-3.5 font-mono text-slate-700">
                        {inst.machineType}
                      </td>
                      <td className="px-5 py-3.5">
                        {inst.status === 'RUNNING' ? (
                          <span className="inline-flex items-center gap-1 text-emerald-700 font-semibold">
                            <span className="w-2 h-2 rounded-full bg-emerald-500" /> Running
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-slate-500">
                            <span className="w-2 h-2 rounded-full bg-slate-400" /> {inst.status}
                          </span>
                        )}
                      </td>
                      <td className="px-5 py-3.5 font-mono text-[11px]">
                        <div>{inst.internalIp}</div>
                        <div className="text-slate-400">{inst.externalIp || 'No external IP'}</div>
                      </td>
                      <td className="px-5 py-3.5 font-medium text-slate-800">
                        {inst.currentArtifact || 'N/A'}
                      </td>
                      <td className="px-5 py-3.5">
                        {inst.currentVersion ? (
                          <span className="px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-mono text-[10px] font-bold">
                            {inst.currentVersion}
                          </span>
                        ) : (
                          <span className="text-slate-400">N/A</span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
