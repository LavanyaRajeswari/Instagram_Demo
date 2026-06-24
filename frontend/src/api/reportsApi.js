import { api } from "./client";

export const reportUser = async (userId, payload) => {
  const { data } = await api.post(`/reports/user/${userId}`, payload);
  return data;
};

export const reportChat = async (chatId, payload) => {
  const { data } = await api.post("/reports", {
    targetType: "CHAT",
    targetId: chatId,
    reason: payload.reason,
    description: payload.description || "",
  });
  return data;
};

export const reportMessage = async (messageId, payload) => {
  const { data } = await api.post(`/reports/message/${messageId}`, payload);
  return data;
};

export const createReport = async ({ targetType, targetId, reason, description = "" }) => {
  const { data } = await api.post("/reports", {
    targetType,
    targetId,
    reason,
    description,
  });
  return data;
};
