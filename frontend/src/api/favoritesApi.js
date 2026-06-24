import { api } from "./client";

export const getFavorites = async (page = 0, size = 20) => {
  const { data } = await api.get("/favorites", { params: { page, size } });
  return data?.content || data?.data || data || [];
};

export const addFavorite = async (postId) => {
  const { data } = await api.post(`/favorites/${postId}`);
  return data;
};

export const removeFavorite = async (postId) => {
  const { data } = await api.delete(`/favorites/${postId}`);
  return data;
};

export const checkFavorite = async (postId) => {
  const { data } = await api.get(`/favorites/check/${postId}`);
  return data;
};
