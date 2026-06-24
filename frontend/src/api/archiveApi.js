import { api } from "./client";

export const archivePost = async (postId) => {
  const { data } = await api.post(`/archive/${postId}`);
  return data;
};

export const unarchivePost = async (postId) => {
  const { data } = await api.delete(`/archive/${postId}`);
  return data;
};

export const isArchived = async (postId) => {
  const { data } = await api.get(`/archive/${postId}/status`);
  return data;
};

export const getArchivedPosts = async (page = 0, size = 20) => {
  const { data } = await api.get("/archive", { params: { page, size } });
  return data?.content || data?.data || data || [];
};
