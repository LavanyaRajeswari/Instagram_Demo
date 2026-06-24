import { api, unwrapPage } from "./client";

export const getCallHistory = async () => {
  const { data } = await api.get("/calls/history");
  return unwrapPage(data);
};

export const answerCall = async (callId) => {
  const { data } = await api.post(`/calls/${callId}/answer`);
  return data;
};

export const rejectCall = async (callId) => {
  const { data } = await api.post(`/calls/${callId}/reject`);
  return data;
};

export const endCall = async (callId) => {
  const { data } = await api.post(`/calls/${callId}/end`);
  return data;
};

export const getCallHistoryWithUser = async (userId) => {
  const { data } = await api.get(`/calls/history/${userId}`);
  return unwrapPage(data);
};
