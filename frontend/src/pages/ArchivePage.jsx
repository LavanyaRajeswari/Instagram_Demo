import { useEffect, useState } from "react";
import { Archive, Heart, MessageCircle, Play, RotateCcw, Film } from "lucide-react";
import { getArchivedPosts, unarchivePost } from "../api/archiveApi";
import { getArchivedStories } from "../api/storiesApi";

const isVideoUrl = (url = "") => /\.(mp4|webm|ogg|mov)(\?.*)?$/i.test(url);

function ArchivePage() {
  const [tab, setTab] = useState("posts");
  const [posts, setPosts] = useState([]);
  const [stories, setStories] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadArchivedPosts = async () => {
    try {
      setLoading(true);
      const data = await getArchivedPosts();
      setPosts(Array.isArray(data) ? data : []);
    } catch (_err) {
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  const loadArchivedStories = async () => {
    try {
      setLoading(true);
      const data = await getArchivedStories();
      setStories(Array.isArray(data) ? data : []);
    } catch (_err) {
      setStories([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (tab === "posts") {
      loadArchivedPosts();
    } else {
      loadArchivedStories();
    }
  }, [tab]);

  const handleRestorePost = async (postId) => {
    try {
      await unarchivePost(postId);
      setPosts((prev) => prev.filter((p) => (p.post?.id || p.id) !== postId));
    } catch (_err) {
      console.error("Failed to restore post", _err);
    }
  };

  const getMediaUrl = (item) => {
    const post = item.post || item;
    return post.media?.[0]?.mediaUrl || post.mediaUrl || null;
  };

  const items = tab === "posts" ? posts : stories;

  return (
    <main className="mx-auto min-h-screen max-w-[935px] bg-white px-4 py-8 pb-[82px] md:pb-10">
      <h1 className="mb-6 text-2xl font-bold text-[#262626]">Archive</h1>
      <div className="mb-6 flex gap-6 border-b border-gray-200">
        <button
          type="button"
          onClick={() => setTab("posts")}
          className={`flex items-center gap-2 pb-2 text-sm font-semibold ${tab === "posts" ? "border-b-2 border-[#262626] text-[#262626]" : "text-gray-500"}`}
        >
          <Archive className="h-4 w-4" />
          Posts
        </button>
        <button
          type="button"
          onClick={() => setTab("stories")}
          className={`flex items-center gap-2 pb-2 text-sm font-semibold ${tab === "stories" ? "border-b-2 border-[#262626] text-[#262626]" : "text-gray-500"}`}
        >
          <Film className="h-4 w-4" />
          Stories
        </button>
      </div>
      {loading ? (
        <p className="py-16 text-center text-sm text-gray-500">Loading archived {tab}...</p>
      ) : items.length === 0 ? (
        <div className="py-16 text-center">
          <Archive className="mx-auto h-12 w-12 text-[#737373]" />
          <p className="mt-4 text-lg font-bold text-[#262626]">No archived {tab}</p>
          <p className="mt-2 text-sm text-[#737373]">Archived {tab} will appear here.</p>
        </div>
      ) : (
        <div className="grid grid-cols-3 gap-1 md:gap-2">
          {items.map((item) => {
            const mediaUrl = item.mediaUrl || getMediaUrl(item);
            const itemId = item.id;
            if (!mediaUrl) return null;
            return (
              <div key={itemId} className="relative aspect-square overflow-hidden bg-gray-100 group">
                {isVideoUrl(mediaUrl) ? (
                  <video src={mediaUrl} muted playsInline className="h-full w-full object-cover" />
                ) : (
                  <img src={mediaUrl} alt="" className="h-full w-full object-cover" />
                )}
                {tab === "posts" && (
                  <div className="absolute inset-0 flex items-center justify-center bg-black/35 opacity-0 group-hover:opacity-100 transition">
                    <button
                      type="button"
                      onClick={() => handleRestorePost(itemId)}
                      className="flex items-center gap-2 rounded-lg bg-white px-4 py-2 text-xs font-bold text-[#262626]"
                    >
                      <RotateCcw className="h-4 w-4" />
                      Restore
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </main>
  );
}

export default ArchivePage;
