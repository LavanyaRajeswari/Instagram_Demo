import { api } from "./client";

export const pinPost = async (postId) => {
  const { data } = await api.post(`/posts/${postId}/pin`);
  return data;
};

export const unpinPost = async (postId) => {
  const { data } = await api.delete(`/posts/${postId}/pin`);
  return data;
};

export const isPinned = async (postId) => {
  const { data } = await api.get(`/posts/${postId}/pin/status`);
  return data;
};

export const disableComments = async (postId) => {
  const { data } = await api.put(`/posts/${postId}/comments/disable`);
  return data;
};

export const enableComments = async (postId) => {
  const { data } = await api.put(`/posts/${postId}/comments/enable`);
  return data;
};

export const hideLikeCount = async (postId) => {
  const { data } = await api.put(`/posts/${postId}/hide-likes`);
  return data;
};

export const showLikeCount = async (postId) => {
  const { data } = await api.put(`/posts/${postId}/show-likes`);
  return data;
};
