import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import PostCard from "../components/PostCard";
import CreatePostModal from "../components/CreatePostModal";
import ImmersivePostModal from "../components/ImmersivePostModal";
import { getFeedPosts, getPosts } from "../api/postsApi";
import StoriesBar from "../components/StoriesBar";
import { getSuggestedUsers } from "../api/userApi";
import { followUser, unfollowUser, isFollowingUser } from "../api/followApi";
import { getAvatarUrl } from "../utils/avatar";
import { useCurrentUser } from "../hooks/useCurrentUser";

function Home() {
  const navigate = useNavigate();
  const { currentUserId, currentUser } = useCurrentUser();
  const [posts, setPosts] = useState([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [selectedImmersivePost, setSelectedImmersivePost] = useState(null);
  const [suggestedUsers, setSuggestedUsers] = useState([]);
  const [followingMap, setFollowingMap] = useState({});
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);

  const loadPosts = async () => {
    try {
      setLoading(true);
      let data;
      try {
        data = await getFeedPosts();
      } catch (_feedError) {
        data = await getPosts();
      }
      setPosts(Array.isArray(data) ? data : []);
    } catch (_error) {
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPosts();
  }, []);

  useEffect(() => {
    if (!currentUserId) return;
    const loadSuggestions = async () => {
      setSuggestionsLoading(true);
      try {
        const users = await getSuggestedUsers();
        const list = Array.isArray(users) ? users.filter((u) => String(u.id) !== String(currentUserId)) : [];
        setSuggestedUsers(list.slice(0, 5));
        const map = {};
        for (const u of list.slice(0, 5)) {
          try { map[u.id] = await isFollowingUser(u.id); } catch { map[u.id] = false; }
        }
        setFollowingMap(map);
      } catch { setSuggestedUsers([]); }
      finally { setSuggestionsLoading(false); }
    };
    loadSuggestions();
  }, [currentUserId]);

  const handleFollowSuggestion = async (userId) => {
    try {
      if (followingMap[userId]) {
        await unfollowUser(userId);
        setFollowingMap((prev) => ({ ...prev, [userId]: false }));
      } else {
        await followUser(userId);
        setFollowingMap((prev) => ({ ...prev, [userId]: true }));
      }
    } catch {}
  };

  const handlePostUpdated = (updatedPost) => {
    setPosts((prev) =>
      prev.map((item) => (item.id === updatedPost.id ? { ...item, ...updatedPost } : item))
    );
    setSelectedImmersivePost((prev) =>
      prev?.id === updatedPost.id ? { ...prev, ...updatedPost } : prev
    );
  };

  const handlePostDeleted = (postId) => {
    setPosts((prev) => prev.filter((item) => item.id !== postId));
    setSelectedImmersivePost((prev) => (prev?.id === postId ? null : prev));
  };

  return (
    <div className="min-h-screen bg-[#fafafa]">
      <main className="flex justify-center gap-[64px] px-4 md:px-8 py-8 pb-[82px] md:pb-10 transition-all duration-300" id="feed-layout">
        <section className="w-full max-w-[630px]" id="feed-column">
          <StoriesBar />

          {loading && (
            <div className="flex flex-col items-center justify-center py-20" id="loading-spinner">
              <div className="border-[3px] border-[#efefef] border-t-[#0095f6] rounded-full w-8 h-8 animate-spin"></div>
              <p className="text-gray-500 text-xs font-semibold mt-4">Loading posts...</p>
            </div>
          )}

          {!loading && posts.length === 0 && (
            <div className="text-center py-20 border border-dashed border-[#dbdbdb] rounded-xl bg-white p-8" id="empty-feed">
              <p className="text-gray-500 font-semibold text-sm">No posts found</p>
              <button
                onClick={() => setCreateOpen(true)}
                className="mt-3 text-xs font-semibold text-[#0095f6] hover:text-[#005f9e]"
              >
                Create your first post
              </button>
            </div>
          )}

          {!loading &&
            posts.map((post) => (
              <PostCard
                key={post.id}
                post={post}
                onPostUpdated={handlePostUpdated}
                onPostDeleted={handlePostDeleted}
                onMediaClick={(p) => setSelectedImmersivePost(p)}
              />
            ))}
        </section>

        <aside className="hidden xl:block w-[320px] shrink-0" id="suggestions-column">
          <div className="fixed w-[320px]">
            {currentUser && (
              <div className="flex items-center gap-3 mb-4">
                <button type="button" onClick={() => navigate("/profile")} className="shrink-0">
                  <img src={getAvatarUrl(currentUser)} alt="" className="h-11 w-11 rounded-full object-cover" />
                </button>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-semibold text-[#262626]">{currentUser.username}</p>
                  <p className="truncate text-xs text-gray-500">{currentUser.fullName || ""}</p>
                </div>
              </div>
            )}

            <div className="flex items-center justify-between mb-3">
              <p className="text-sm font-semibold text-gray-500">Suggested for you</p>
              <button type="button" onClick={() => {
                if (!currentUserId) return;
                setSuggestionsLoading(true);
                getSuggestedUsers().then((users) => {
                  const list = Array.isArray(users) ? users.filter((u) => String(u.id) !== String(currentUserId)) : [];
                  setSuggestedUsers(list.slice(0, 5));
                }).catch(() => {}).finally(() => setSuggestionsLoading(false));
              }} className="text-xs font-semibold text-[#262626] hover:text-gray-500">
                Refresh
              </button>
            </div>

            {suggestionsLoading ? (
              <div className="h-8 w-8 animate-spin rounded-full border-[3px] border-[#efefef] border-t-[#0095f6] mx-auto" />
            ) : (
              <div className="space-y-2">
                {suggestedUsers.map((user) => {
                  const isFollowing = followingMap[user.id];
                  return (
                    <div key={user.id} className="flex items-center gap-3">
                      <button type="button" onClick={() => navigate(`/profile/${user.id}`)} className="shrink-0">
                        <img src={getAvatarUrl(user)} alt="" className="h-8 w-8 rounded-full object-cover" />
                      </button>
                      <div className="min-w-0 flex-1">
                        <button type="button" onClick={() => navigate(`/profile/${user.id}`)} className="truncate text-sm font-semibold text-[#262626]">
                          {user.username}
                        </button>
                        {user.followedBy && (
                          <p className="truncate text-xs text-gray-500">Followed by {user.followedBy}</p>
                        )}
                      </div>
                      <button
                        type="button"
                        onClick={() => handleFollowSuggestion(user.id)}
                        className={`shrink-0 text-xs font-semibold ${isFollowing ? "text-[#262626]" : "text-[#0095f6]"}`}
                      >
                        {isFollowing ? "Following" : "Follow"}
                      </button>
                    </div>
                  );
                })}
                {!suggestionsLoading && suggestedUsers.length === 0 && (
                  <p className="text-xs text-gray-400">No suggestions available</p>
                )}
              </div>
            )}
          </div>
        </aside>
      </main>

      {createOpen && (
        <CreatePostModal
          onClose={() => setCreateOpen(false)}
          onPostCreated={() => {
            setCreateOpen(false);
            loadPosts();
          }}
        />
      )}

      {selectedImmersivePost && (
        <ImmersivePostModal
          post={selectedImmersivePost}
          postsList={posts}
          onClose={() => setSelectedImmersivePost(null)}
          onPostUpdated={handlePostUpdated}
          onSelectPost={(p) => setSelectedImmersivePost(p)}
        />
      )}
    </div>
  );
}

export default Home;