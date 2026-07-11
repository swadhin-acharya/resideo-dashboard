import api from './client';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface ProjectInfo {
  id: string;
  name: string;
  slug: string;
  role: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  username: string;
  displayName: string;
  globalRole: string;
  projects: ProjectInfo[];
}

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  displayName: string;
  globalRole: string;
  enabled: boolean;
  lastLoginAt: string | null;
  createdAt: string;
}

export interface ProjectResponse {
  id: string;
  organizationId: string;
  name: string;
  slug: string;
  description: string;
  archived: boolean;
  createdAt: string;
}

export const login = (data: LoginRequest) =>
  api.post<LoginResponse>('/auth/login', data).then(r => r.data);

export const register = (data: { username: string; email: string; password: string }) =>
  api.post<LoginResponse>('/auth/register', data).then(r => r.data);

export const getMe = () =>
  api.get<LoginResponse>('/auth/me').then(r => r.data);

export const getUsers = () =>
  api.get<UserResponse[]>('/users').then(r => r.data);

export const getUser = (id: string) =>
  api.get<UserResponse>(`/users/${id}`).then(r => r.data);

export const createUser = (data: { username: string; email: string; password?: string; globalRole?: string }) =>
  api.post<UserResponse>('/users', data).then(r => r.data);

export const updateUser = (id: string, data: Record<string, unknown>) =>
  api.patch<UserResponse>(`/users/${id}`, data).then(r => r.data);

export const deleteUser = (id: string) =>
  api.delete(`/users/${id}`);

export const getProjects = () =>
  api.get<ProjectResponse[]>('/projects').then(r => r.data);

export const getProject = (id: string) =>
  api.get<ProjectResponse>(`/projects/${id}`).then(r => r.data);

export const createProject = (data: { name: string; slug: string; description?: string }) =>
  api.post<ProjectResponse>('/projects', data).then(r => r.data);

export const updateProject = (id: string, data: Record<string, unknown>) =>
  api.patch<ProjectResponse>(`/projects/${id}`, data).then(r => r.data);

export const archiveProject = (id: string) =>
  api.delete(`/projects/${id}`);

export interface ProjectMember {
  userId: string;
  role: string;
  username?: string;
  email?: string;
  displayName?: string;
}

export const getProjectMembers = (projectId: string) =>
  api.get<ProjectMember[]>(`/projects/${projectId}/members`).then(r => r.data);

export const addProjectMember = (projectId: string, userId: string, role: string) =>
  api.post(`/projects/${projectId}/members`, { userId, role });

export const updateProjectMemberRole = (projectId: string, userId: string, role: string) =>
  api.patch(`/projects/${projectId}/members/${userId}`, { role });

export const removeProjectMember = (projectId: string, userId: string) =>
  api.delete(`/projects/${projectId}/members/${userId}`);

export const getApiTokens = () =>
  api.get('/auth/tokens').then(r => r.data);

export const createApiToken = (data: { name: string; projectId?: string }) =>
  api.post('/auth/tokens', data).then(r => r.data);

export const revokeApiToken = (id: string) =>
  api.delete(`/auth/tokens/${id}`);
