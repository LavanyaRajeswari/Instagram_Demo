import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getPostById } from "../api/postsApi";
import PostCard from "../components/PostCard";

function PostPage() {
  const { postId } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!postId) return;
    setLoading(true);
    setError("");
    getPostById(postId)
      .then((data) => {
        if (!data) { setError("Post not found"); return; }
        setPost(data);
      })
      .catch((err) => {
        setError(err?.response?.data?.message || "Failed to load post");
      })
      .finally(() => setLoading(false));
  }, [postId]);

  if (loading) {
    return (
      <main className="mx-auto min-h-screen max-w-[935px] px-4 py-8">
        <p className="text-center text-sm text-gray-500">Loading post...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="mx-auto min-h-screen max-w-[935px] px-4 py-8">
        <p className="text-center text-sm text-[#ed4956]">{error}</p>
        <button
          onClick={() => navigate(-1)}
          className="mx-auto mt-4 block text-sm font-semibold text-[#0095f6]"
        >
          Go back
        </button>
      </main>
    );
  }

  return (
    <main className="mx-auto min-h-screen max-w-[935px] px-4 py-8 pb-[82px] md:pb-10">
      <PostCard
        post={post}
        onPostUpdated={(updatedPost) => setPost((prev) => prev ? { ...prev, ...updatedPost } : prev)}
        onPostDeleted={() => navigate(-1)}
      />
    </main>
  );
}

export default PostPage;
