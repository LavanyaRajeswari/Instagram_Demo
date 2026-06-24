import { useEffect, useMemo, useRef, useState } from "react";
import { Bell, BellOff, Info, Pencil, Phone, Trash2, UserX, Video, X } from "lucide-react";
import { useNavigate } from "react-router-dom";
import {
  deleteChat,
  getChats,
  getMessages,
  markMessagesSeen,
  muteChatUntil,
  removeChatNickname,
  searchUsersForChat,
  sendMessage,
  startAudioCall,
  startChat,
  startVideoCall,
  unmuteChat,
  updateChatNickname,
} from "../api/messagesApi";
import { getCallHistoryWithUser } from "../api/callsApi";
import { createReport } from "../api/reportsApi";
import { blockUser } from "../api/settingsApi";
import { getCurrentUser, getUser } from "../api/userApi";
import { getAvatarUrl } from "../utils/avatar";
import { connect, subscribeToChat, subscribeToTyping, sendTyping } from "../hooks/useWebSocket";

function Messages() {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(null);
  const [chats, setChats] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageText, setMessageText] = useState("");
  const [searchText, setSearchText] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [loadingChats, setLoadingChats] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [error, setError] = useState("");
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [chatDetails, setChatDetails] = useState(null);
  const [reportOpen, setReportOpen] = useState(false);
  const [nicknamesOpen, setNicknamesOpen] = useState(false);
  const [muteOpen, setMuteOpen] = useState(false);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [blockTarget, setBlockTarget] = useState(null);
  const [actionMessage, setActionMessage] = useState("");
  const bottomRef = useRef(null);
  const [typingUsers, setTypingUsers] = useState({});
  const selectedChatSubs = useRef([]);
  const chatListSubs = useRef({});
  const selectedChatRef = useRef(null);
  const typingTimeoutRef = useRef(null);
  const lastTypingSentRef = useRef(0);

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    connect();
    return () => {
      Object.values(chatListSubs.current).forEach((unsub) => unsub?.());
      chatListSubs.current = {};
      selectedChatSubs.current.forEach((unsub) => unsub());
      selectedChatSubs.current = [];
    };
  }, []);

  useEffect(() => {
    const timeoutId = setTimeout(async () => {
      const query = searchText.trim();
      if (query.length < 2) {
        setSearchResults([]);
        return;
      }

      try {
        const users = await searchUsersForChat(query);
        setSearchResults(users.filter((user) => user.id !== currentUser?.id));
      } catch (err) {
        console.error("Failed to search users", err);
      }
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [searchText, currentUser?.id]);

  useEffect(() => {
    selectedChatRef.current = selectedChat;
  }, [selectedChat]);

  const sortedChats = useMemo(() => {
    return [...chats].sort((a, b) => {
      const aTime = a.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0;
      const bTime = b.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0;
      return bTime - aTime;
    });
  }, [chats]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, selectedChat?.id]);

  useEffect(() => {
    selectedChatSubs.current.forEach((unsub) => unsub());
    selectedChatSubs.current = [];

    if (!selectedChat?.id) return;

    const chatId = selectedChat.id;

    const unsubMsg = subscribeToChat(chatId, (newMessage) => {
      setMessages((prev) => {
        if (prev.some((m) => m.id === newMessage.id)) return prev;
        return [...prev, newMessage].sort(
          (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
        );
      });
    });
    selectedChatSubs.current.push(unsubMsg);

    const unsubTyping = subscribeToTyping(chatId, (typingDto) => {
      if (typingDto.userId === currentUser?.id) return;
      if (typingDto.typing) {
        setTypingUsers((prev) => ({ ...prev, [chatId]: typingDto.userId }));
      } else {
        setTypingUsers((prev) => {
          const next = { ...prev };
          delete next[chatId];
          return next;
        });
      }
    });
    selectedChatSubs.current.push(unsubTyping);

    return () => {
      selectedChatSubs.current.forEach((unsub) => unsub());
      selectedChatSubs.current = [];
    };
  }, [selectedChat?.id, currentUser?.id]);

  useEffect(() => {
    const subs = chatListSubs.current;
    const currentIds = new Set(chats.map((c) => c.id));

    Object.keys(subs).forEach((id) => {
      if (!currentIds.has(Number(id))) {
        subs[id]?.();
        delete subs[id];
      }
    });

    chats.forEach((chat) => {
      if (subs[chat.id]) return;
      subs[chat.id] = subscribeToChat(chat.id, (newMessage) => {
        setChats((prev) =>
          prev.map((c) =>
            c.id === chat.id
              ? {
                  ...c,
                  lastMessage: newMessage.content,
                  lastMessageAt: newMessage.createdAt,
                  unreadCount:
                    selectedChatRef.current?.id === chat.id
                      ? 0
                      : (c.unreadCount || 0) + 1,
                }
              : c
          )
        );
      });
    });
  }, [chats]);

  const loadInitialData = async () => {
    setError("");
    setLoadingChats(true);

    try {
      const [me, chatList] = await Promise.all([getCurrentUser(), getChats()]);
      setCurrentUser(me);
      setChats(chatList);
    } catch (err) {
      console.error("Failed to load messages page", err);
      setError("Please login again to view messages.");
    } finally {
      setLoadingChats(false);
    }
  };

  const openChat = async (chat) => {
    setSelectedChat(chat);
    setDetailsOpen(false);
    setChatDetails(null);
    setLoadingMessages(true);
    setError("");

    try {
      const otherUserId = getOtherUserId(chat);
      const [data, callHistory] = await Promise.all([
        getMessages(chat.id),
        otherUserId ? getCallHistoryWithUser(otherUserId).catch(() => []) : Promise.resolve([]),
      ]);
      const callEntries = (Array.isArray(callHistory) ? callHistory : []).map((call) => ({
        id: `call_${call.id}`,
        type: "CALL",
        content: formatCallHistoryEntry(call),
        createdAt: call.createdAt || call.startedAt,
        senderId: call.caller?.id,
      }));
      const all = [...data, ...callEntries].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
      setMessages(all);
      await markMessagesSeen(chat.id);
      setChats((prev) =>
        prev.map((item) =>
          item.id === chat.id ? { ...item, unreadCount: 0 } : item
        )
      );
    } catch (err) {
      console.error("Failed to load chat messages", err);
      setError("Could not load messages for this chat.");
    } finally {
      setLoadingMessages(false);
    }
  };

  const getOtherUserId = (chat = selectedChat) => chat?.otherUserId || chat?.user?.id;

  const buildChatDetails = async (chat = selectedChat) => {
    if (!chat || !currentUser) return null;
    let otherUser = {
      id: getOtherUserId(chat),
      username: chat.username,
      fullName: chat.fullName || "",
      profilePicture: chat.profilePicture,
      nickname: chat.nickname || "",
      blocked: chat.blocked || false,
    };

    if (otherUser.id) {
      try {
        const user = await getUser(otherUser.id);
        otherUser = { ...otherUser, ...user, nickname: chat.nickname || user.nickname || "" };
      } catch (error) {
        console.error("Failed to load chat member profile", error);
      }
    }

    return {
      id: chat.id,
      muted: Boolean(chat.muted),
      muteUntil: chat.muteUntil,
      members: [
        { ...currentUser, nickname: currentUser.nickname || "" },
        otherUser,
      ].filter((member) => member.id),
      nicknames: {
        [currentUser.id]: currentUser.nickname || "",
        [otherUser.id]: chat.nickname || otherUser.nickname || "",
      },
    };
  };

  const refreshDetails = async (chat = selectedChat) => {
    if (!chat) return null;
    const details = await buildChatDetails(chat);
    setChatDetails(details);
    return details;
  };

  const handleStartChat = async (user) => {
    try {
      const chat = await startChat(user.id);
      setSearchText("");
      setSearchResults([]);

      setChats((prev) => {
        const exists = prev.some((item) => item.id === chat.id);
        return exists
          ? prev.map((item) => (item.id === chat.id ? chat : item))
          : [chat, ...prev];
      });

      await openChat(chat);
    } catch (err) {
      console.error("Failed to start chat", err);
      setError("Could not start chat with this user.");
    }
  };

  const handleSend = async (event) => {
    event.preventDefault();

    const content = messageText.trim();
    if (!content || !selectedChat) return;

    try {
      const newMessage = await sendMessage({
        chatId: selectedChat.id,
        content,
      });

      setMessages((prev) => [...prev, newMessage]);
      setMessageText("");
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = null;
      }
      sendTyping(selectedChat.id, currentUser.id, false);
      setChats((prev) =>
        prev.map((chat) =>
          chat.id === selectedChat.id
            ? {
                ...chat,
                lastMessage: newMessage.content,
                lastMessageAt: newMessage.createdAt,
              }
            : chat
        )
      );
    } catch (err) {
      console.error("Failed to send message", err);
      alert("Message send failed. Please try again.");
    }
  };

  const handleTyping = (e) => {
    setMessageText(e.target.value);

    if (!selectedChat || !currentUser) return;

    const now = Date.now();
    if (now - lastTypingSentRef.current > 2000) {
      sendTyping(selectedChat.id, currentUser.id, true);
      lastTypingSentRef.current = now;
    }

    if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    typingTimeoutRef.current = setTimeout(() => {
      sendTyping(selectedChat.id, currentUser.id, false);
    }, 3000);
  };

  const getChatDisplayName = (chat = selectedChat) => chat?.nickname || chat?.username || "Instagram user";

  const applyOtherUserNickname = (chatId, userId, nickname) => {
    const displayNickname = nickname || "";
    setSelectedChat((chat) =>
      chat?.id === chatId ? { ...chat, nickname: displayNickname } : chat
    );
    setChats((prev) =>
      prev.map((chat) =>
        chat.id === chatId ? { ...chat, nickname: displayNickname } : chat
      )
    );
    setChatDetails((prev) =>
      prev?.id === chatId
        ? {
            ...prev,
            nicknames: { ...(prev.nicknames || {}), [userId]: displayNickname },
            members: (prev.members || []).map((member) =>
              String(member.id) === String(userId)
                ? { ...member, nickname: displayNickname }
                : member
            ),
          }
        : prev
    );
    setMessages((prev) =>
      prev.map((message) =>
        String(message.senderId) === String(userId) ||
        String(message.sender?.id) === String(userId)
          ? {
              ...message,
              senderNickname: displayNickname,
              sender: message.sender
                ? { ...message.sender, nickname: displayNickname }
                : message.sender,
            }
          : message
      )
    );
  };

  const handleCall = async (type) => {
    if (!selectedChat) return;
    const otherUserId = getOtherUserId();
    if (!otherUserId) {
      alert("Could not find the user to call.");
      return;
    }

    try {
      const call = await (type === "audio" ? startAudioCall(otherUserId) : startVideoCall(otherUserId));
      const callId = call?.callId || call?.id || call?.data?.callId || call?.data?.id || "";
      const callIdParam = callId ? `&callId=${callId}` : "";

      navigate(`/call?has_video=${type === "video"}&ig_thread_id=${selectedChat.id}${callIdParam}`, {
        state: {
          callId,
          chatId: selectedChat.id,
          otherUserId,
          username: getChatDisplayName(),
          profilePicture: selectedChat.profilePicture,
        },
      });
    } catch (error) {
      console.error("Failed to start call", error);
      if (error?.response?.status === 401) {
        alert("Please login again to start a call.");
      } else {
        alert("Could not start call.");
      }
    }
  };

  const handleToggleMute = async () => {
    if (!selectedChat) return;
    if (!chatDetails?.muted) {
      setActionMessage("");
      setMuteOpen(true);
      return;
    }

    setActionMessage("");
    const previousChat = selectedChat;
    const previousDetails = chatDetails;
    updateSelectedChat({ muted: false, muteUntil: null });
    setChatDetails((prev) => (prev ? { ...prev, muted: false, muteUntil: null } : prev));

    try {
      await unmuteChat(selectedChat.id);
    } catch (error) {
      setSelectedChat(previousChat);
      setChatDetails(previousDetails);
      setChats((prev) => prev.map((chat) => (chat.id === previousChat.id ? previousChat : chat)));
      setActionMessage("Mute settings were not updated. Please try again.");
    }
  };

  const updateSelectedChat = (updates) => {
    setSelectedChat((chat) => (chat ? { ...chat, ...updates } : chat));
    setChats((prev) =>
      prev.map((chat) => (chat.id === selectedChat?.id ? { ...chat, ...updates } : chat))
    );
  };

  const handleMuteChoice = async (hours) => {
    if (!selectedChat) return;
    const muteUntil = hours ? new Date(Date.now() + hours * 60 * 60 * 1000).toISOString().slice(0, 19) : null;
    const previousChat = selectedChat;
    const previousDetails = chatDetails;
    setActionMessage("");
    updateSelectedChat({ muted: true, muteUntil });
    setChatDetails((prev) => (prev ? { ...prev, muted: true, muteUntil } : prev));
    setMuteOpen(false);

    try {
      await muteChatUntil(selectedChat.id, muteUntil);
    } catch (error) {
      setSelectedChat(previousChat);
      setChatDetails(previousDetails);
      setChats((prev) => prev.map((chat) => (chat.id === previousChat.id ? previousChat : chat)));
      setActionMessage("Mute settings were not updated. Please try again.");
    }
  };

  const handleDeleteChat = async () => {
    if (!selectedChat) return;
    try {
      await deleteChat(selectedChat.id);
      setChats((prev) => prev.filter((chat) => chat.id !== selectedChat.id));
      setSelectedChat(null);
      setMessages([]);
      setDetailsOpen(false);
      setDeleteConfirmOpen(false);
    } catch (error) {
      console.error("Failed to delete chat", error);
      alert("Could not delete chat.");
    }
  };

  const handleBlockMember = async () => {
    if (!blockTarget?.id) return;
    try {
      await blockUser(blockTarget.id);
      setChatDetails((prev) => ({
        ...prev,
        members: (prev?.members || []).map((member) =>
          member.id === blockTarget.id ? { ...member, blocked: true } : member
        ),
      }));
      setBlockTarget(null);
      setActionMessage("User blocked.");
    } catch (error) {
      console.error("Failed to block user", error);
      if (error?.response?.status === 404) {
        alert("Block API is not available in backend yet.");
      } else {
        alert("Could not block this user.");
      }
    }
  };

  const handleReportSubmit = async ({ reason, description }) => {
    if (!selectedChat) return;
    try {
      await createReport({
        targetType: "CHAT",
        targetId: selectedChat.id,
        reason,
        description,
      });
      setReportOpen(false);
      setActionMessage("Report submitted.");
    } catch (error) {
      console.error("Failed to report chat", error);
      alert("Could not submit report.");
    }
  };

  const formatTime = (dateString) => {
    if (!dateString) return "";
    return new Date(dateString).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatCallHistoryEntry = (call) => {
    const type = call.callType === "VIDEO" ? "Video" : "Voice";
    const status = String(call.status || "").toUpperCase();
    const isOutgoing = String(call.caller?.id) === String(currentUser?.id);
    const direction = isOutgoing ? "Outgoing" : "Incoming";
    let result = "";
    if (status === "ANSWERED" || status === "ENDED") {
      const dur = call.durationSeconds ? ` (${Math.floor(call.durationSeconds / 60)}m ${call.durationSeconds % 60}s)` : "";
      result = `Answered${dur}`;
    } else if (status === "MISSED") result = "Missed";
    else if (status === "REJECTED") result = "Rejected";
    else if (status === "CANCELLED") result = "Cancelled";
    else result = status.charAt(0) + status.slice(1).toLowerCase();
    return `${direction} ${type} Call — ${result}`;
  };

  return (
    <div className="flex h-screen bg-white text-[#262626]">
      <aside className="w-full max-w-[420px] border-r border-[#dbdbdb] bg-white">
        <div className="border-b border-[#dbdbdb] p-5">
          <h1 className="text-xl font-bold">Messages</h1>
          <input
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            placeholder="Search users to message"
            className="mt-4 w-full rounded-lg border border-[#dbdbdb] bg-[#fafafa] px-4 py-2 text-sm outline-none focus:border-[#a8a8a8]"
          />
        </div>

        {searchResults.length > 0 && (
          <div className="border-b border-[#dbdbdb] bg-white">
            <p className="px-5 pt-4 text-xs font-semibold uppercase text-gray-400">
              Start new chat
            </p>
            {searchResults.map((user) => (
              <button
                key={user.id}
                onClick={() => handleStartChat(user)}
                className="flex w-full items-center gap-3 px-5 py-3 text-left hover:bg-[#fafafa]"
              >
                <img
                  src={getAvatarUrl(user)}
                  alt=""
                  className="h-11 w-11 rounded-full object-cover"
                />
                <div>
                  <p className="font-semibold">{user.username}</p>
                  <p className="text-sm text-gray-500">{user.fullName}</p>
                </div>
              </button>
            ))}
          </div>
        )}

        {loadingChats ? (
          <p className="p-5 text-sm text-gray-500">Loading chats...</p>
        ) : sortedChats.length === 0 ? (
          <p className="p-5 text-sm text-gray-500">
            No chats yet. Search a user to start messaging.
          </p>
        ) : (
          <div className="overflow-y-auto">
            {sortedChats.map((chat) => (
              <button
                key={chat.id}
                onClick={() => openChat(chat)}
                className={`flex w-full items-center gap-3 px-5 py-3 text-left hover:bg-[#fafafa] ${
                  selectedChat?.id === chat.id ? "bg-[#efefef]" : ""
                }`}
              >
                <div className="relative">
                  <img
                    src={getAvatarUrl({ profilePicture: chat.profilePicture })}
                    alt=""
                    className="h-12 w-12 rounded-full object-cover"
                  />
                  {chat.online && (
                    <span className="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-white bg-green-500" />
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <p className="truncate font-semibold">{chat.nickname || chat.username}</p>
                    {chat.unreadCount > 0 && (
                      <span className="rounded-full bg-blue-500 px-2 py-0.5 text-xs font-bold text-white">
                        {chat.unreadCount}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-1 text-sm text-gray-500">
                    {chat.muted && <BellOff className="h-3.5 w-3.5 shrink-0" />}
                    <p className="truncate">{chat.lastMessage || "Start chatting"}</p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </aside>

      <main className="flex flex-1 flex-col">
        {!selectedChat ? (
          <div className="flex flex-1 items-center justify-center text-center">
            <div>
              <div className="mx-auto mb-4 flex h-24 w-24 items-center justify-center rounded-full border-2 border-[#262626] text-4xl">
                ✉
              </div>
              <h2 className="text-2xl font-light">Your messages</h2>
              <p className="mt-2 text-sm text-gray-500">
                Search and select a user to start chatting.
              </p>
              {error && <p className="mt-4 text-sm text-red-500">{error}</p>}
            </div>
          </div>
        ) : (
          <>
            <header className="flex items-center gap-3 border-b border-[#dbdbdb] p-4">
              <button type="button" onClick={() => navigate(`/profile/${getOtherUserId()}`)} className="shrink-0">
                <img
                  src={getAvatarUrl({ profilePicture: selectedChat.profilePicture })}
                  alt=""
                  className="h-10 w-10 rounded-full object-cover"
                />
              </button>
              <div className="min-w-0 flex-1">
                <p className="truncate font-semibold">{selectedChat.nickname || selectedChat.username}</p>
                <p className="text-xs text-gray-500">
                  {typingUsers[selectedChat.id] ? "Typing..." : selectedChat.online ? "Active now" : "Instagram user"}
                </p>
              </div>
              <button type="button" onClick={() => handleCall("audio")} className="rounded-full p-2 hover:bg-[#f2f2f2]" aria-label="Start audio call">
                <Phone className="h-5 w-5" />
              </button>
              <button type="button" onClick={() => handleCall("video")} className="rounded-full p-2 hover:bg-[#f2f2f2]" aria-label="Start video call">
                <Video className="h-5 w-5" />
              </button>
              <button
                type="button"
                onClick={async () => {
                  setDetailsOpen((open) => !open);
                  if (!chatDetails) await refreshDetails(selectedChat);
                }}
                className="rounded-full p-2 hover:bg-[#f2f2f2]"
                aria-label="Open chat details"
              >
                <Info className="h-5 w-5" />
              </button>
            </header>

            <div className="flex min-h-0 flex-1">
              <section className="flex-1 space-y-3 overflow-y-auto bg-white p-5">
                {loadingMessages ? (
                  <p className="text-center text-sm text-gray-500">Loading messages...</p>
                ) : messages.length === 0 ? (
                  <p className="text-center text-sm text-gray-500">
                    No messages yet. Say hello 👋
                  </p>
                ) : (
                  <>
                  {messages.map((msg) => {
                    const mine = msg.senderId === currentUser?.id;
                    const isCall = msg.type === "CALL";
                    if (isCall) {
                      return (
                        <div key={msg.id} className="flex justify-center">
                          <div className="rounded-full bg-[#f2f2f2] px-4 py-2 text-xs font-semibold text-[#737373]">
                            {msg.content} · {formatTime(msg.createdAt)}
                          </div>
                        </div>
                      );
                    }
                    return (
                      <div
                        key={msg.id}
                        className={`flex ${mine ? "justify-end" : "justify-start"}`}
                      >
                        <div
                          className={`max-w-[75%] rounded-3xl px-4 py-2 text-sm ${
                            mine
                              ? "bg-[#3797f0] text-white"
                              : "bg-[#efefef] text-[#262626]"
                          }`}
                        >
                          <p className="whitespace-pre-wrap break-words">
                            {msg.content}
                          </p>
                          <p
                            className={`mt-1 text-[10px] ${
                              mine ? "text-blue-100" : "text-gray-500"
                            }`}
                          >
                            {formatTime(msg.createdAt)}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                  <div ref={bottomRef} />
                  </>
                )}
              </section>

              {detailsOpen && (
                <ChatDetailsPanel
                  details={chatDetails}
                  currentUserId={currentUser?.id}
                  onClose={() => setDetailsOpen(false)}
                  onToggleMute={handleToggleMute}
                  onNicknames={() => setNicknamesOpen(true)}
                  onBlock={setBlockTarget}
                  onReport={() => setReportOpen(true)}
                  onDelete={() => setDeleteConfirmOpen(true)}
                  actionMessage={actionMessage}
                  onProfileClick={(userId) => navigate(`/profile/${userId}`)}
                />
              )}
            </div>

            <form onSubmit={handleSend} className="border-t border-[#dbdbdb] p-4">
              <div className="flex items-center gap-3 rounded-full border border-[#dbdbdb] px-4 py-2">
                <input
                  value={messageText}
                  onChange={handleTyping}
                  placeholder="Message..."
                  className="flex-1 border-0 bg-transparent text-sm outline-none"
                />
                <button
                  type="submit"
                  disabled={!messageText.trim()}
                  className="text-sm font-semibold text-[#0095f6] disabled:opacity-40"
                >
                  Send
                </button>
              </div>
            </form>
          </>
        )}
      </main>

      {reportOpen && (
        <ReportModal onClose={() => setReportOpen(false)} onSubmit={handleReportSubmit} />
      )}
      {muteOpen && (
        <MuteMessagesModal
          onClose={() => setMuteOpen(false)}
          onSelect={handleMuteChoice}
        />
      )}
      {blockTarget && (
        <BlockUserModal
          user={blockTarget}
          onClose={() => setBlockTarget(null)}
          onConfirm={handleBlockMember}
        />
      )}
      {deleteConfirmOpen && (
        <DeleteChatModal
          onClose={() => setDeleteConfirmOpen(false)}
          onConfirm={handleDeleteChat}
        />
      )}
      {nicknamesOpen && (
        <NicknameModal
          details={chatDetails}
          currentUserId={currentUser?.id}
          onClose={() => setNicknamesOpen(false)}
          onSave={async (userId, nickname) => {
            const chatId = selectedChat.id;
            try {
              if (nickname) await updateChatNickname(chatId, userId, nickname);
              else await removeChatNickname(chatId, userId);
              applyOtherUserNickname(chatId, userId, nickname);
            } catch (error) {
              alert("Nickname was not saved. Please try again.");
            }
          }}
        />
      )}
    </div>
  );
}

function ChatDetailsPanel({ details, currentUserId, onClose, onToggleMute, onNicknames, onBlock, onReport, onDelete, actionMessage, onProfileClick }) {
  const otherMembers = (details?.members || []).filter((member) => member.id !== currentUserId);

  return (
    <aside className="w-[320px] shrink-0 overflow-y-auto border-l border-[#dbdbdb] bg-white">
      <div className="flex items-center justify-between border-b border-[#dbdbdb] p-4">
        <h2 className="font-bold">Details</h2>
        <button type="button" onClick={onClose} className="rounded-full p-2 hover:bg-[#f2f2f2]" aria-label="Close details">
          <X className="h-5 w-5" />
        </button>
      </div>
      <div className="space-y-1 border-b border-[#dbdbdb] p-3">
        <button type="button" onClick={onToggleMute} className="flex w-full items-center justify-between rounded-lg px-3 py-3 text-sm font-semibold hover:bg-[#fafafa]">
          <span className="flex items-center gap-3">{details?.muted ? <BellOff className="h-5 w-5" /> : <Bell className="h-5 w-5" />} Mute messages</span>
          <span className={`h-5 w-9 rounded-full ${details?.muted ? "bg-[#0095f6]" : "bg-[#dbdbdb]"}`}>
            <span className={`block h-5 w-5 rounded-full bg-white shadow ${details?.muted ? "translate-x-4" : ""}`} />
          </span>
        </button>
        <button type="button" onClick={onNicknames} className="flex w-full items-center rounded-lg px-3 py-3 text-sm font-semibold hover:bg-[#fafafa]">
          Nicknames
        </button>
      </div>
      <div className="border-b border-[#dbdbdb] p-4">
        <h3 className="mb-3 text-sm font-bold">Members</h3>
        {(details?.members || []).map((member) => (
          <button
            key={member.id}
            type="button"
            onClick={() => onProfileClick(member.id)}
            className="flex w-full items-center gap-3 rounded-lg py-2 text-left hover:bg-[#fafafa]"
          >
            <img src={getAvatarUrl(member)} alt="" className="h-10 w-10 rounded-full object-cover" />
            <div className="min-w-0">
              <p className="truncate text-sm font-semibold">{member.nickname || member.username}</p>
              <p className="truncate text-xs text-[#737373]">{member.fullName}</p>
            </div>
          </button>
        ))}
      </div>
      <div className="space-y-1 p-3">
        {otherMembers.map((member) => (
          <button key={member.id} type="button" onClick={() => onBlock(member)} disabled={member.blocked} className="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold text-[#ed4956] hover:bg-[#fff5f5] disabled:opacity-50">
            <UserX className="h-5 w-5" />
            {member.blocked ? "Blocked" : `Block ${member.username}`}
          </button>
        ))}
        <button type="button" onClick={onReport} className="flex w-full items-center rounded-lg px-3 py-3 text-left text-sm font-semibold text-[#ed4956] hover:bg-[#fff5f5]">
          Report
        </button>
        <button type="button" onClick={onDelete} className="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold text-[#ed4956] hover:bg-[#fff5f5]">
          <Trash2 className="h-5 w-5" />
          Delete chat
        </button>
        {actionMessage && <p className="px-3 pt-2 text-xs font-semibold text-green-600">{actionMessage}</p>}
      </div>
    </aside>
  );
}

function ReportModal({ onClose, onSubmit }) {
  const [reason, setReason] = useState("Spam");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const reasons = ["Spam", "Harassment", "Hate speech", "Scam or fraud", "Violence", "Nudity", "Other"];

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      await onSubmit({ reason, description });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[900] flex items-center justify-center bg-black/50 p-4">
      <form onSubmit={submit} className="w-full max-w-[420px] overflow-hidden rounded-2xl bg-white">
        <div className="relative flex h-12 items-center justify-center border-b border-[#dbdbdb]">
          <h2 className="text-sm font-bold">Report</h2>
          <button type="button" onClick={onClose} className="absolute right-4">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="border-b border-[#efefef] px-5 py-4">
          <h3 className="text-sm font-bold">Why are you reporting this chat?</h3>
        </div>
        <div className="py-1">
          {reasons.map((item) => (
            <label key={item} className="flex cursor-pointer items-center gap-3 border-b border-[#efefef] px-5 py-3 text-sm">
              <input type="radio" checked={reason === item} onChange={() => setReason(item)} />
              {item}
            </label>
          ))}
        </div>
        <div className="p-4">
          <textarea value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Add details" className="min-h-20 w-full rounded-lg border border-[#dbdbdb] p-3 text-sm outline-none" />
          <button type="submit" disabled={saving} className="mt-3 h-10 w-full rounded-lg bg-[#ed4956] text-sm font-bold text-white disabled:opacity-50">
            {saving ? "Submitting..." : "Submit report"}
          </button>
        </div>
      </form>
    </div>
  );
}

function NicknameModal({ details, currentUserId, onClose, onSave }) {
  const editableMembers = currentUserId == null
    ? []
    : (details?.members || []).filter((member) => String(member.id) !== String(currentUserId));
  const [values, setValues] = useState(() => {
    const entries = {};
    editableMembers.forEach((member) => {
      entries[member.id] = member.nickname || "";
    });
    return entries;
  });
  const [editingId, setEditingId] = useState(null);
  const [savingId, setSavingId] = useState(null);

  const saveNickname = async (memberId) => {
    setSavingId(memberId);
    await onSave(memberId, values[memberId]?.trim());
    setSavingId(null);
    setEditingId(null);
  };

  return (
    <div className="fixed inset-0 z-[900] flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-md overflow-hidden rounded-2xl bg-white">
        <div className="relative flex h-12 items-center justify-center border-b border-[#dbdbdb]">
          <h2 className="text-sm font-bold">Nicknames</h2>
          <button type="button" onClick={onClose} className="absolute right-4">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="p-4">
          {editableMembers.map((member) => (
            <div key={member.id} className="flex items-center gap-3 border-b border-[#efefef] py-3 last:border-b-0">
              <img src={getAvatarUrl(member)} alt="" className="h-10 w-10 rounded-full object-cover" />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold">{member.username}</p>
                <p className="truncate text-xs text-[#737373]">{member.fullName}</p>
                {editingId === member.id ? (
                  <input
                    value={values[member.id] || ""}
                    onChange={(event) => setValues((prev) => ({ ...prev, [member.id]: event.target.value }))}
                    placeholder={member.username}
                    className="mt-2 h-9 w-full rounded-lg border border-[#dbdbdb] px-3 text-sm outline-none"
                    autoFocus
                  />
                ) : (
                  <p className="mt-1 text-sm text-[#737373]">{values[member.id] || "No nickname"}</p>
                )}
              </div>
              {editingId === member.id ? (
                <button type="button" disabled={savingId === member.id} onClick={() => saveNickname(member.id)} className="rounded-lg bg-[#0095f6] px-3 py-2 text-xs font-bold text-white disabled:opacity-50">
                  Save
                </button>
              ) : (
                <button type="button" onClick={() => setEditingId(member.id)} className="rounded-full p-2 hover:bg-[#f2f2f2]" aria-label={`Edit ${member.username} nickname`}>
                  <Pencil className="h-4 w-4" />
                </button>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function MuteMessagesModal({ onClose, onSelect }) {
  const options = [
    ["For 1 hour", 1],
    ["For 8 hours", 8],
    ["For 24 hours", 24],
    ["Until I change it", null],
  ];

  return (
    <div className="fixed inset-0 z-[900] flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-[360px] overflow-hidden rounded-2xl bg-white text-center">
        <h2 className="border-b border-[#dbdbdb] py-4 text-sm font-bold">Mute messages</h2>
        {options.map(([label, hours]) => (
          <button key={label} type="button" onClick={() => onSelect(hours)} className="block w-full border-b border-[#efefef] py-4 text-sm hover:bg-[#fafafa]">
            {label}
          </button>
        ))}
        <button type="button" onClick={onClose} className="block w-full py-4 text-sm">
          Cancel
        </button>
      </div>
    </div>
  );
}

function BlockUserModal({ user, onClose, onConfirm }) {
  return (
    <div className="fixed inset-0 z-[900] flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-[400px] overflow-hidden rounded-2xl bg-white text-center">
        <div className="p-6">
          <h2 className="text-lg font-bold">Block {user?.username}?</h2>
          <p className="mt-3 text-sm leading-5 text-[#737373]">
            They won't be able to find your profile, posts or story on Instagram. Instagram won't let them know you blocked them.
          </p>
        </div>
        <button type="button" onClick={onConfirm} className="block w-full border-t border-[#dbdbdb] py-4 text-sm font-bold text-[#ed4956]">
          Block
        </button>
        <button type="button" onClick={onClose} className="block w-full border-t border-[#dbdbdb] py-4 text-sm">
          Cancel
        </button>
      </div>
    </div>
  );
}

function DeleteChatModal({ onClose, onConfirm }) {
  return (
    <div className="fixed inset-0 z-[900] flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-[420px] overflow-hidden rounded-2xl bg-white text-center">
        <div className="p-6">
          <h2 className="text-lg font-bold">Delete chat from inbox?</h2>
          <p className="mt-3 text-sm leading-5 text-[#737373]">
            This will remove the chat from your inbox and erase the chat history. To stop receiving new messages from this account, first block the account then delete the chat.
          </p>
        </div>
        <button type="button" onClick={onConfirm} className="block w-full border-t border-[#dbdbdb] py-4 text-sm font-bold text-[#ed4956]">
          Delete
        </button>
        <button type="button" onClick={onClose} className="block w-full border-t border-[#dbdbdb] py-4 text-sm">
          Cancel
        </button>
      </div>
    </div>
  );
}

export default Messages;
