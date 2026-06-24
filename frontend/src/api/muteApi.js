import { api } from "./client";

export const muteUser = async (userId, muteType = "ALL") => {
  const { data } = await api.post(`/users/${userId}/mute`, null, {
    params: { type: muteType },
  });
  return data;
};

export const unmuteUser = async (userId) => {
  const { data } = await api.delete(`/users/${userId}/mute`);
  return data;
};

export const isMuted = async (userId) => {
  const { data } = await api.get(`/users/${userId}/mute/status`);
  return data;
};

export const getMutedUsers = async () => {
  const { data } = await api.get("/users/muted");
  return Array.isArray(data) ? data : [];
};
