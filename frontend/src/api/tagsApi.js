import { api } from "./client";

export const getTaggedPosts = async (userId) => {
  const { data } = await api.get(`/tags/user/${userId}`);
  const tags = Array.isArray(data) ? data : data?.content ?? [];
  return tags
    .filter((tag) => tag.post)
    .map((tag) => tag.post);
};
