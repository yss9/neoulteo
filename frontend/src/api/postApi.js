import { apiFetch } from "./http";

export const writePost = (post) => {
  return apiFetch("/api/posts", {
    method: "POST",
    body: JSON.stringify(post)
  });
};

export const uploadPostImage = (file) => {
  const formData = new FormData();
  formData.append("image", file);
  return apiFetch("/api/posts/images", {
    method: "POST",
    body: formData
  });
};

export const getPost = (id) => {
  return apiFetch(`/api/posts/${id}`);
};

export const getPosts = (params) => {
  const query = new URLSearchParams(params).toString();
  return apiFetch(`/api/posts?${query}`);
};

export const updatePost = (id, post) => {
  return apiFetch(`/api/posts/${id}`, {
    method: "PUT",
    body: JSON.stringify(post)
  });
};

export const deletePost = (id) => {
  return apiFetch(`/api/posts/${id}`, {
    method: "DELETE"
  });
};

export const getComments = (postId) => {
  return apiFetch(`/api/posts/${postId}/comments`);
};

export const writeComment = (postId, content) => {
  return apiFetch(`/api/posts/${postId}/comments`, {
    method: "POST",
    body: JSON.stringify({ content })
  });
};

export const deleteComment = (commentId) => {
  return apiFetch(`/api/posts/comments/${commentId}`, {
    method: "DELETE"
  });
};

export const togglePostLike = (postId) => {
  return apiFetch(`/api/posts/${postId}/like`, {
    method: "POST"
  });
};
