import { api } from "./client";

export const restrictUser = async (userId) => {
  const { data } = await api.post(`/users/${userId}/restrict`);
  return data;
};

export const unrestrictUser = async (userId) => {
  const { data } = await api.delete(`/users/${userId}/restrict`);
  return data;
};

export const isRestricted = async (userId) => {
  const { data } = await api.get(`/users/${userId}/restrict/status`);
  return data;
};

export const getRestrictedUsers = async () => {
  const { data } = await api.get("/users/restricted");
  return Array.isArray(data) ? data : [];
};
