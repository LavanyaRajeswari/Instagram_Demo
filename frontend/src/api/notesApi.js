import { api, unwrapPage } from "./client";

const NOTES = "/notes";

export const getUserNotes = async (userId) => {
  const { data } = await api.get(`${NOTES}/user/${userId}`);
  return unwrapPage(data);
};

export const getMyNotes = async () => {
  const { data } = await api.get(`${NOTES}/my`);
  return unwrapPage(data);
};

export const createNote = async ({ text, color, audience = "PUBLIC", expiryHours }) => {
  const { data } = await api.post(NOTES, null, {
    params: { text, color, audience, expiryHours },
  });
  return data;
};

export const updateNote = async ({ id, text, color, audience, expiryHours }) => {
  const { data } = await api.put(`${NOTES}/${id}`, null, {
    params: { text, color, audience, expiryHours },
  });
  return data;
};

export const deleteNote = async (id) => {
  await api.delete(`${NOTES}/${id}`);
  return true;
};
