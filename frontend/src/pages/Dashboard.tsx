import { useQuery } from '@tanstack/react-query';
import { fetchDashboard } from '../api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  FolderGit2,
  Server,
  Code2,
  ShieldCheck,
  AlertOctagon,
  CheckCircle2,
  ExternalLink
} from 'lucide-react';
import { BarChart, DonutChart, VersionDistributionChart, DeploymentTrendChart } from '../components/Charts';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const { data: dashboard, isLoading, error } = useQuery({
    queryKey: ['dashboard'],
    queryFn: fetchDashboard,
    refetchInterval: 15000,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64 text-slate-400">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-3 border-indigo-600 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm font-medium">Loading executive dashboard metrics...</span>
        </div>
      </div>
    );
  }

  if (error || !dashboard) {
    return (
      <div className="p-6 bg-red-50 border border-red-200 rounded-xl text-red-800 text-sm">
        <p className="font-semibold">Unable to fetch dashboard metrics</p>
        <p className="text-xs mt-1 text-red-600">Ensure the backend API is running at http://localhost:8080</p>
      </div>
    );
  }

  const kpis = [
    {
      title: 'Total GCP Projects',
      value: dashboard.totalProjects,
      description: 'Monitored projects across environments',
      icon: FolderGit2,
      color: 'text-indigo-600',
      bg: 'bg-indigo-50 border-indigo-100',
    },
    {
      title: 'Total VM Instances',
      value: dashboard.totalInstances,
      description: `${dashboard.runningInstances} running, ${dashboard.stoppedInstances} stopped`,
      icon: Server,
      color: 'text-blue-600',
      bg: 'bg-blue-50 border-blue-100',
    },
    {
      title: 'Dev Owned Instances',
      value: dashboard.devInstances,
      description: 'Engineered & managed by Dev teams',
      icon: Code2,
      color: 'text-emerald-600',
      bg: 'bg-emerald-50 border-emerald-100',
    },
    {
      title: 'SRE Owned Instances',
      value: dashboard.sreInstances,
      description: 'Tier-0 production & platform clusters',
      icon: ShieldCheck,
      color: 'text-purple-600',
      bg: 'bg-purple-50 border-purple-100',
    },
    {
      title: 'Deployment Failures',
      value: dashboard.deploymentFailures,
      description: 'Failed pipeline runs requiring audit',
      icon: AlertOctagon,
      color: dashboard.deploymentFailures > 0 ? 'text-red-600' : 'text-slate-400',
      bg: dashboard.deploymentFailures > 0 ? 'bg-red-50 border-red-100' : 'bg-slate-50 border-slate-100',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900">Executive Deployment Portal</h2>
        <p className="text-sm text-slate-500 mt-1">
          Centralized inventory, ownership, and artifact visibility across all GCP projects and Compute Engine instances.
        </p>
      </div>

      {/* KPI Cards */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        {kpis.map((kpi) => (
          <Card key={kpi.title} className="border border-slate-200 shadow-sm hover:shadow transition">
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                {kpi.title}
              </CardTitle>
              <div className={`p-2 rounded-lg ${kpi.bg}`}>
                <kpi.icon className={`w-4 h-4 ${kpi.color}`} />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-extrabold text-slate-900">{kpi.value}</div>
              <p className="text-[11px] text-slate-500 mt-1">{kpi.description}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Charts Grid */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Chart 1: Project-wise Instance Count */}
        <Card className="border border-slate-200 shadow-sm">
          <CardHeader className="pb-3 border-b border-slate-100">
            <CardTitle className="text-sm font-bold text-slate-800 flex items-center justify-between">
              <span>Project-wise Compute Instance Count</span>
              <span className="text-[11px] text-slate-400 font-normal">VM Allocation</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-4">
            <BarChart data={dashboard.projectWiseInstanceCount} />
          </CardContent>
        </Card>

        {/* Chart 2: Environment Distribution */}
        <Card className="border border-slate-200 shadow-sm">
          <CardHeader className="pb-3 border-b border-slate-100">
            <CardTitle className="text-sm font-bold text-slate-800 flex items-center justify-between">
              <span>Environment Breakdown</span>
              <span className="text-[11px] text-slate-400 font-normal">Dev / QA / UAT / Staging / Perf / Prod</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-4">
            <DonutChart data={dashboard.environmentBreakdown} />
          </CardContent>
        </Card>

        {/* Chart 3: Artifact Version Distribution */}
        <Card className="border border-slate-200 shadow-sm">
          <CardHeader className="pb-3 border-b border-slate-100">
            <CardTitle className="text-sm font-bold text-slate-800 flex items-center justify-between">
              <span>Artifact Version Distribution</span>
              <span className="text-[11px] text-slate-400 font-normal">Active Releases</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-4">
            <VersionDistributionChart data={dashboard.artifactVersionDistribution} />
          </CardContent>
        </Card>

        {/* Chart 4: Deployment Trend Chart */}
        <Card className="border border-slate-200 shadow-sm">
          <CardHeader className="pb-3 border-b border-slate-100">
            <CardTitle className="text-sm font-bold text-slate-800 flex items-center justify-between">
              <span>Deployment Trend History</span>
              <span className="text-[11px] text-slate-400 font-normal">Pipeline Activity</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="pt-4">
            <DeploymentTrendChart data={dashboard.deploymentTrend} />
          </CardContent>
        </Card>
      </div>

      {/* Latest Deployments Table */}
      <Card className="border border-slate-200 shadow-sm">
        <CardHeader className="flex flex-row items-center justify-between pb-3 border-b border-slate-100">
          <div>
            <CardTitle className="text-sm font-bold text-slate-800">Latest Live Deployments</CardTitle>
            <p className="text-xs text-slate-500 mt-0.5">Most recent artifact versions released to VM hosts</p>
          </div>
          <Link
            to="/deployments"
            className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
          >
            <span>View All Deployments</span>
            <ExternalLink className="w-3 h-3" />
          </Link>
        </CardHeader>
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left text-slate-600">
              <thead className="text-[11px] text-slate-500 uppercase bg-slate-50/70 border-b border-slate-100">
                <tr>
                  <th className="px-5 py-3">Application</th>
                  <th className="px-5 py-3">Artifact & Version</th>
                  <th className="px-5 py-3">Target VM</th>
                  <th className="px-5 py-3">Source & Build</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {dashboard.latestDeployments?.map((dep) => (
                  <tr key={dep.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="px-5 py-3.5 font-semibold text-slate-900">
                      {dep.applicationName}
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-slate-700">{dep.artifactName}</span>
                        <span className="px-1.5 py-0.5 rounded bg-indigo-50 text-indigo-700 font-mono text-[10px] font-semibold border border-indigo-100">
                          {dep.artifactVersion}
                        </span>
                      </div>
                    </td>
                    <td className="px-5 py-3.5">
                      <Link
                        to={`/instances?search=${dep.instanceName || ''}`}
                        className="text-indigo-600 hover:underline font-mono text-[11px]"
                      >
                        {dep.instanceName || dep.instanceId}
                      </Link>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-slate-100 text-slate-700 text-[10px] font-medium">
                        {dep.deploymentSource} ({dep.buildNumber || 'manual'})
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      {dep.deploymentStatus === 'SUCCESS' ? (
                        <span className="inline-flex items-center gap-1 text-emerald-700 font-semibold text-[11px]">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Success
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-red-700 font-semibold text-[11px]">
                          <AlertOctagon className="w-3.5 h-3.5" /> {dep.deploymentStatus}
                        </span>
                      )}
                    </td>
                    <td className="px-5 py-3.5 text-slate-400 font-mono text-[11px]">
                      {new Date(dep.deploymentTimestamp).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
