import { api } from "./client";

export const getCollections = async () => {
  const { data } = await api.get("/collections");
  return Array.isArray(data) ? data : [];
};

export const createCollection = async (name) => {
  const { data } = await api.post("/collections", { name });
  return data;
};

export const renameCollection = async (collectionId, name) => {
  const { data } = await api.put(`/collections/${collectionId}`, { name });
  return data;
};

export const deleteCollection = async (collectionId) => {
  const { data } = await api.delete(`/collections/${collectionId}`);
  return data;
};

export const addPostToCollection = async (collectionId, postId) => {
  const { data } = await api.post(`/collections/${collectionId}/posts/${postId}`);
  return data;
};

export const removePostFromCollection = async (collectionId, postId) => {
  const { data } = await api.delete(`/collections/${collectionId}/posts/${postId}`);
  return data;
};
