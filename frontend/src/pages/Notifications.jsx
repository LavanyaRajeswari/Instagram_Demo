import { useEffect, useState } from "react";
import { Heart, MessageCircle, UserPlus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import {
  getNotifications,
  markAllNotificationsSeen,
  markNotificationSeen,
} from "../api/notificationsApi";
import { getAvatarUrl } from "../utils/avatar";
import { subscribeToNotifications, connect } from "../hooks/useWebSocket";

const iconByType = {
  LIKE: Heart,
  COMMENT: MessageCircle,
  FOLLOW: UserPlus,
  FOLLOW_REQUEST: UserPlus,
  MESSAGE: MessageCircle,
  MENTION: MessageCircle,
};

function notificationText(item) {
  const actor = item.actorUsername || "Someone";
  const type = String(item.type || "").toUpperCase();
  if (type.includes("LIKE")) return `${actor} liked your post.`;
  if (type.includes("COMMENT")) return `${actor} commented: ${item.commentText || ""}`;
  if (type.includes("FOLLOW")) return `${actor} started following you.`;
  if (type.includes("MENTION")) return `${actor} mentioned you.`;
  if (type.includes("TAG")) return `${actor} tagged you in a post.`;
  return `${actor} sent you a notification.`;
}

function Notifications() {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const shouldInclude = (item) => {
    const type = String(item.type || "").toUpperCase();
    return !type.includes("MESSAGE") && !type.includes("CALL");
  };

  useEffect(() => {
    getNotifications()
      .then((data) => setItems(Array.isArray(data) ? (data.filter(shouldInclude)) : []))
      .catch(() => setItems([]))
      .finally(() => setLoading(false));

    const unsub = subscribeToNotifications((notification) => {
      if (shouldInclude(notification)) {
        setItems((prev) => [notification, ...prev]);
      }
    });

    connect();

    return () => {
      if (unsub) unsub();
    };
  }, []);

  const handleMarkAll = async () => {
    await markAllNotificationsSeen();
    setItems((prev) => prev.map((item) => ({ ...item, seen: true })));
  };

  const handleSeen = async (item) => {
    if (item.seen) return;
    await markNotificationSeen(item.id);
    setItems((prev) =>
      prev.map((entry) => (entry.id === item.id ? { ...entry, seen: true } : entry))
    );
  };

  return (
    <main className="mx-auto min-h-screen max-w-[640px] bg-white px-4 py-8 pb-[82px] md:pb-10">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-[#262626]">Notifications</h1>
        <button
          type="button"
          onClick={handleMarkAll}
          className="text-sm font-semibold text-[#0095f6]"
        >
          Mark all seen
        </button>
      </div>

      {loading ? (
        <p className="py-16 text-center text-sm text-gray-500">Loading notifications...</p>
      ) : items.length === 0 ? (
        <p className="py-16 text-center text-sm text-gray-500">No notifications yet</p>
      ) : (
        <div className="divide-y divide-[#efefef] border-y border-[#efefef]">
          {items.map((item) => {
            const Icon = iconByType[String(item.type || "").toUpperCase()] || Heart;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => {
                  handleSeen(item);
                  const type = String(item.type || "").toUpperCase();
                  if ((type.includes("LIKE") || type.includes("COMMENT") || type.includes("MENTION")) && item.postId) {
                    navigate(`/post/${item.postId}`);
                  } else if (item.actorId) {
                    navigate(`/profile/${item.actorId}`);
                  }
                }}
                className={`flex w-full items-center gap-3 px-2 py-4 text-left hover:bg-[#fafafa] ${
                  item.seen ? "bg-white" : "bg-blue-50"
                }`}
              >
                <div className="shrink-0">
                  <img
                    src={getAvatarUrl({ profilePicture: item.actorProfilePicture })}
                    alt=""
                    className="h-11 w-11 rounded-full object-cover"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleSeen(item);
                      if (item.actorId) navigate(`/profile/${item.actorId}`);
                    }}
                  />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm text-[#262626]">
                    <span className="font-semibold">{item.actorUsername || "Someone"}</span>{" "}
                    {notificationText(item).replace(`${item.actorUsername || "Someone"} `, "")}
                  </p>
                  <p className="mt-0.5 text-xs uppercase tracking-wide text-gray-400">
                    {item.type || "NOTIFICATION"} {item.seen ? "seen" : "unseen"}
                  </p>
                  <p className="mt-1 text-xs text-gray-500">
                    {item.createdAt ? new Date(item.createdAt).toLocaleString() : ""}
                  </p>
                </div>
                <Icon className="h-5 w-5 text-[#ed4956]" />
              </button>
            );
          })}
        </div>
      )}
    </main>
  );
}

export default Notifications;
