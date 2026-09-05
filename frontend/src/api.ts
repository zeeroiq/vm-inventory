import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('vm_auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}

export interface Project {
  id: string;
  name: string;
  businessUnit: string;
  ownerTeam: string;
  environment: string;
  description?: string;
  creationDate: string;
  instanceCount?: number;
}

export interface Instance {
  id: string;
  name: string;
  zone: string;
  region: string;
  machineType: string;
  internalIp: string;
  externalIp: string;
  status: string;
  labels: string;
  ownerTeam: string;
  environment: string;
  lastUpdateTimestamp: string;
  cpuCores?: number;
  memoryMb?: number;
  projectId?: string;
  projectName?: string;
  currentArtifact?: string;
  currentVersion?: string;
}

export interface Deployment {
  id: number;
  applicationName: string;
  artifactName: string;
  artifactVersion: string;
  buildNumber: string;
  gitCommitId: string;
  branch: string;
  deploymentTimestamp: string;
  deploymentSource: string;
  deployedBy: string;
  deploymentStatus: string;
  releaseNotes?: string;
  instanceId?: string;
  instanceName?: string;
  projectId?: string;
}

export interface ChartDataPoint {
  label: string;
  value: number;
  secondaryLabel?: string;
}

export interface DashboardSummary {
  totalProjects: number;
  totalInstances: number;
  devInstances: number;
  sreInstances: number;
  deploymentFailures: number;
  runningInstances: number;
  stoppedInstances: number;
  latestDeployments: Deployment[];
  environmentDistribution: Record<string, number>;
  projectWiseInstanceCount: ChartDataPoint[];
  environmentBreakdown: ChartDataPoint[];
  artifactVersionDistribution: ChartDataPoint[];
  deploymentTrend: ChartDataPoint[];
}

export interface User {
  id: number;
  username: string;
  email: string;
  role: 'ROLE_ADMIN' | 'ROLE_SRE' | 'ROLE_DEVELOPER' | 'ROLE_VIEWER';
  department: string;
  enabled: boolean;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  timestamp: string;
  username: string;
  action: string;
  resourceType: string;
  resourceId: string;
  details: string;
  ipAddress: string;
}

export interface SyncReport {
  completed: boolean;
  message: string;
  lastSyncTime: string;
  lastSyncStatus: string;
  syncedProjects: number;
  syncedInstances: number;
}

// API Functions

export const fetchDashboard = async (): Promise<DashboardSummary> => {
  const res = await api.get<ApiResponse<DashboardSummary>>('/dashboard');
  return res.data.data;
};

export const fetchProjects = async (params?: {
  environment?: string;
  ownerTeam?: string;
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}): Promise<PageResponse<Project>> => {
  const res = await api.get<ApiResponse<PageResponse<Project>>>('/projects', { params });
  return res.data.data;
};

export const fetchAllProjects = async (): Promise<Project[]> => {
  const res = await api.get<ApiResponse<Project[]>>('/projects/all');
  return res.data.data;
};

export const fetchProjectById = async (id: string): Promise<Project> => {
  const res = await api.get<ApiResponse<Project>>(`/projects/${id}`);
  return res.data.data;
};

export const createProject = async (data: Partial<Project>): Promise<Project> => {
  const res = await api.post<ApiResponse<Project>>('/projects', data);
  return res.data.data;
};

export const updateProject = async (id: string, data: Partial<Project>): Promise<Project> => {
  const res = await api.put<ApiResponse<Project>>(`/projects/${id}`, data);
  return res.data.data;
};

export const deleteProject = async (id: string): Promise<void> => {
  await api.delete(`/projects/${id}`);
};

export const fetchProjectFilters = async (): Promise<{ environments: string[]; ownerTeams: string[] }> => {
  const res = await api.get<ApiResponse<{ environments: string[]; ownerTeams: string[] }>>('/projects/meta/filters');
  return res.data.data;
};

export const fetchProjectInstances = async (projectId: string): Promise<Instance[]> => {
  const res = await api.get<ApiResponse<Instance[]>>(`/projects/${projectId}/instances`);
  return res.data.data;
};

export const fetchInstances = async (params?: {
  projectId?: string;
  environment?: string;
  ownerTeam?: string;
  status?: string;
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}): Promise<PageResponse<Instance>> => {
  const res = await api.get<ApiResponse<PageResponse<Instance>>>('/instances', { params });
  return res.data.data;
};

export const fetchInstanceById = async (id: string): Promise<Instance> => {
  const res = await api.get<ApiResponse<Instance>>(`/instances/${id}`);
  return res.data.data;
};

export const createInstance = async (data: Partial<Instance> & { projectId: string }): Promise<Instance> => {
  const res = await api.post<ApiResponse<Instance>>('/instances', data);
  return res.data.data;
};

export const updateInstance = async (id: string, data: Partial<Instance>): Promise<Instance> => {
  const res = await api.put<ApiResponse<Instance>>(`/instances/${id}`, data);
  return res.data.data;
};

export const deleteInstance = async (id: string): Promise<void> => {
  await api.delete(`/instances/${id}`);
};

export const exportInstancesCsvUrl = (params?: {
  projectId?: string;
  environment?: string;
  ownerTeam?: string;
  status?: string;
}): string => {
  const query = new URLSearchParams(params as any).toString();
  return `${API_BASE_URL}/instances/export?${query}`;
};

export const fetchDeployments = async (params?: {
  instanceId?: string;
  status?: string;
  source?: string;
  search?: string;
  page?: number;
  size?: number;
}): Promise<PageResponse<Deployment>> => {
  const res = await api.get<ApiResponse<PageResponse<Deployment>>>('/deployments', { params });
  return res.data.data;
};

export const recordDeployment = async (data: {
  instanceId: string;
  applicationName: string;
  artifactName: string;
  artifactVersion: string;
  buildNumber?: string;
  gitCommitId?: string;
  branch?: string;
  deploymentSource?: string;
  deployedBy?: string;
  deploymentStatus?: string;
  releaseNotes?: string;
}): Promise<Deployment> => {
  const res = await api.post<ApiResponse<Deployment>>('/deployments', data);
  return res.data.data;
};

export const fetchDeploymentsByInstance = async (instanceId: string): Promise<Deployment[]> => {
  const res = await api.get<ApiResponse<Deployment[]>>(`/instances/${instanceId}/deployments`);
  return res.data.data;
};

// Admin & RBAC
export const fetchUsers = async (): Promise<User[]> => {
  const res = await api.get<ApiResponse<User[]>>('/admin/users');
  return res.data.data;
};

export const updateUserRole = async (userId: number, role: string): Promise<User> => {
  const res = await api.patch<ApiResponse<User>>(`/admin/users/${userId}/role`, { role });
  return res.data.data;
};

export const fetchAuditLogs = async (page = 0, size = 20): Promise<PageResponse<AuditLog>> => {
  const res = await api.get<ApiResponse<PageResponse<AuditLog>>>('/admin/audit-logs', {
    params: { page, size }
  });
  return res.data.data;
};

export const triggerGcpSync = async (): Promise<SyncReport> => {
  const res = await api.post<ApiResponse<SyncReport>>('/admin/sync/trigger');
  return res.data.data;
};

export const fetchGcpSyncStatus = async (): Promise<SyncReport> => {
  const res = await api.get<ApiResponse<SyncReport>>('/admin/sync/status');
  return res.data.data;
};

// Discovery
export const sendAgentReport = async (data: {
  instanceId: string;
  applicationName: string;
  artifactName: string;
  artifactVersion: string;
  buildNumber?: string;
  gitCommitId?: string;
  branch?: string;
  status?: string;
}): Promise<Deployment> => {
  const res = await api.post<ApiResponse<Deployment>>('/discovery/agent-report', data);
  return res.data.data;
};

export const fetchManifest = async (): Promise<Record<string, any>> => {
  const res = await api.get<ApiResponse<Record<string, any>>>('/discovery/manifest');
  return res.data.data;
};

// Auth
export const login = async (username: string, password: string): Promise<{ token: string; user: User }> => {
  const res = await api.post<ApiResponse<{ token: string; username: string; email: string; role: any; department: string }>>('/auth/login', {
    username,
    password
  });
  const data = res.data.data;
  localStorage.setItem('vm_auth_token', data.token);
  return {
    token: data.token,
    user: {
      id: 1,
      username: data.username,
      email: data.email,
      role: data.role,
      department: data.department,
      enabled: true,
      createdAt: new Date().toISOString()
    }
  };
};

export const fetchCurrentUser = async (): Promise<User> => {
  const res = await api.get<ApiResponse<User>>('/auth/me');
  return res.data.data;
};
