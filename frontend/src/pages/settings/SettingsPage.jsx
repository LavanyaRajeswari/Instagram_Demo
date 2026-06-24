import { useEffect, useMemo, useRef, useState } from "react";
import { NavLink, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Download, LogOut } from "lucide-react";
import { ChevronDown } from "lucide-react";
import { logoutUser, searchUsers, updateProfile, uploadProfilePicture } from "../../api/userApi";
import { useTheme } from "../../context/ThemeContext";
import {
  addCloseFriend,
  addHiddenStoryUser,
  blockUser,
  getBlockedAccounts,
  getCloseFriends,
  getHiddenStoryUsers,
  getLoginHistory,
  getMessagePrivacySettings,
  getNotificationSettings,
  getRestrictedAccounts,
  removeCloseFriend,
  removeHiddenStoryUser,
  restrictUser,
  unRestrictUser,
  unblockUser,
  updateMessagePrivacySettings,
  updateNotificationSettings,
  updatePrivacySetting,
  updateReelDownloads,
  updateSensitiveContent,
  updateStoryMentions,
  updateStoryReplies,
  setTheme,
  getActivity,
} from "../../api/settingsApi";
import { createReport } from "../../api/reportsApi";
import { useCurrentUser, clearCurrentUserCache } from "../../hooks/useCurrentUser";
import { getAvatarUrl } from "../../utils/avatar";

const items = [
  { slug: "", label: "Settings and Privacy" },
  { slug: "edit-profile", label: "Edit Profile" },
  { slug: "notifications", label: "Notifications" },
  { slug: "privacy", label: "Privacy" },
  { slug: "close-friends", label: "Close Friends" },
  { slug: "blocked-accounts", label: "Blocked Accounts" },
  { slug: "story-location", label: "Story and Location" },
  { slug: "messages-replies", label: "Messages and Story Replies" },
  { slug: "restricted-accounts", label: "Restricted Accounts" },
  { slug: "tags-mentions", label: "Tags and Mentions" },
  { slug: "comments", label: "Comments" },
  { slug: "sharing-reuse", label: "Sharing and Reuse" },
  { slug: "appearance", label: "Appearance" },
  { slug: "activity", label: "Your Activity" },
  { slug: "report-problem", label: "Report a Problem" },
  { slug: "login-activity", label: "Login Activity" },
];

function MissingEndpoint({ endpoints }) {
  return (
    <div className="rounded-lg border border-[#dbdbdb] bg-[#fafafa] p-4 text-sm text-[#737373]">
      Backend endpoint missing: {endpoints.join(", ")}
    </div>
  );
}

const normalizeUser = (item, key) => item?.[key] || item?.user || item;

function UserListSettings({ title, type, currentUserId }) {
  const isCloseFriends = type === "close-friends";
  const [items, setItems] = useState([]);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);

  const loadItems = async () => {
    setLoading(true);
    try {
      const data = isCloseFriends ? await getCloseFriends() : await getBlockedAccounts();
      setItems(data);
    } catch (error) {
      console.error(`Failed to load ${title}`, error);
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadItems();
  }, [type]);

  useEffect(() => {
    const timer = setTimeout(async () => {
      const trimmed = query.trim();
      if (trimmed.length < 2) {
        setResults([]);
        return;
      }
      try {
        const data = await searchUsers(trimmed);
        setResults((Array.isArray(data) ? data : []).filter((user) => user.id !== currentUserId));
      } catch (error) {
        console.error("User search failed", error);
        setResults([]);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query, currentUserId]);

  const listedUsers = items.map((item) => normalizeUser(item, isCloseFriends ? "friend" : "blocked")).filter(Boolean);
  const listedUserIds = new Set(listedUsers.map((user) => String(user.id)));

  const addUser = async (userId) => {
    setSavingId(userId);
    try {
      if (isCloseFriends) await addCloseFriend(userId);
      else await blockUser(userId);
      setQuery("");
      setResults([]);
      await loadItems();
    } finally {
      setSavingId(null);
    }
  };

  const removeUser = async (userId) => {
    setSavingId(userId);
    try {
      if (isCloseFriends) await removeCloseFriend(userId);
      else await unblockUser(userId);
      await loadItems();
    } finally {
      setSavingId(null);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-bold">{title}</h1>
      <div className="mt-6">
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search users"
          className="h-11 w-full rounded-lg border border-[#dbdbdb] px-3 text-sm outline-none focus:border-[#a8a8a8]"
        />
      </div>

      {results.length > 0 && (
        <div className="mt-3 overflow-hidden rounded-lg border border-[#dbdbdb]">
          {results.map((user) => (
            <div key={user.id} className="flex items-center justify-between gap-3 px-4 py-3">
              <UserRow user={user} />
              <button
                type="button"
                disabled={savingId === user.id || listedUserIds.has(String(user.id))}
                onClick={() => addUser(user.id)}
                className="rounded-lg bg-[#0095f6] px-3 py-1.5 text-xs font-bold text-white disabled:opacity-40"
              >
                {listedUserIds.has(String(user.id)) ? "Added" : isCloseFriends ? "Add" : "Block"}
              </button>
            </div>
          ))}
        </div>
      )}

      <div className="mt-6 overflow-hidden rounded-lg border border-[#dbdbdb]">
        {loading ? (
          <p className="p-4 text-sm text-[#737373]">Loading...</p>
        ) : listedUsers.length === 0 ? (
          <p className="p-4 text-sm text-[#737373]">No users found.</p>
        ) : (
          listedUsers.map((user) => (
            <div key={user.id} className="flex items-center justify-between gap-3 border-b border-[#efefef] px-4 py-3 last:border-b-0">
              <UserRow user={user} />
              <button
                type="button"
                disabled={savingId === user.id}
                onClick={() => removeUser(user.id)}
                className="rounded-lg border border-[#dbdbdb] px-3 py-1.5 text-xs font-bold disabled:opacity-40"
              >
                {isCloseFriends ? "Remove" : "Unblock"}
              </button>
            </div>
          ))
        )}
      </div>
    </>
  );
}

function UserRow({ user }) {
  const navigate = useNavigate();
  return (
    <div className="flex min-w-0 items-center gap-3">
      <button type="button" onClick={() => navigate(`/profile/${user.id}`)} className="shrink-0">
        <img src={getAvatarUrl(user)} alt="" className="h-10 w-10 rounded-full object-cover" />
      </button>
      <div className="min-w-0">
        <p className="truncate text-sm font-semibold">{user.username}</p>
        <p className="truncate text-xs text-[#737373]">{user.fullName}</p>
      </div>
    </div>
  );
}

function NotificationSettingsComponent() {
  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getNotificationSettings()
      .then((data) => setSettings(data))
      .finally(() => setLoading(false));
  }, []);

  const toggle = async (field) => {
    const next = { ...settings, [field]: !settings[field] };
    setSaving(true);
    try {
      const saved = await updateNotificationSettings({ [field]: next[field] });
      setSettings((prev) => ({ ...prev, ...saved }));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p className="text-sm text-[#737373]">Loading...</p>;

  const toggles = [
    { key: "pushEnabled", label: "Push notifications" },
    { key: "likesEnabled", label: "Likes" },
    { key: "commentsEnabled", label: "Comments" },
    { key: "followsEnabled", label: "Follow requests" },
    { key: "mentionsEnabled", label: "Mentions" },
    { key: "messagesEnabled", label: "Messages" },
    { key: "storiesEnabled", label: "Stories" },
    { key: "liveEnabled", label: "Live" },
  ];

  return (
    <>
      <h1 className="text-2xl font-bold">Notifications</h1>
      <div className="mt-6 space-y-3">
        {toggles.map(({ key, label }) => (
          <div key={key} className="flex items-center justify-between rounded-lg border border-[#dbdbdb] p-4">
            <div>
              <p className="text-sm font-semibold">{label}</p>
              <p className="mt-1 text-xs text-[#737373]">
                {saving ? "Saving..." : settings?.[key] ? "Enabled" : "Disabled"}
              </p>
            </div>
            <button
              type="button"
              onClick={() => toggle(key)}
              disabled={saving}
              className={`relative h-6 w-11 shrink-0 rounded-full transition-colors ${settings?.[key] ? "bg-[#0095f6]" : "bg-[#dbdbdb]"} disabled:opacity-50`}
              aria-pressed={settings?.[key]}
            >
              <span className={`absolute top-1 h-4 w-4 rounded-full bg-white transition-transform ${settings?.[key] ? "translate-x-5" : "translate-x-1"}`} />
            </button>
          </div>
        ))}
      </div>
    </>
  );
}

function AccountPrivacyToggle({ currentUser }) {
  const [isPrivate, setIsPrivate] = useState(Boolean(currentUser?.isPrivate));
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setIsPrivate(Boolean(currentUser?.isPrivate));
  }, [currentUser?.isPrivate]);

  const handleToggle = async () => {
    const nextValue = !isPrivate;
    setSaving(true);
    try {
      await updateProfile({
        fullName: currentUser?.fullName,
        username: currentUser?.username,
        bio: currentUser?.bio,
        gender: currentUser?.gender,
        website: currentUser?.website,
        email: currentUser?.email,
        isPrivate: nextValue,
      });
      setIsPrivate(nextValue);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="flex items-center justify-between gap-4 rounded-lg border border-[#dbdbdb] p-4">
      <div>
        <p className="text-sm font-semibold">Private account</p>
        <p className="mt-1 text-xs text-[#737373]">Saved with PUT /api/users/profile.</p>
      </div>
      <button
        type="button"
        onClick={handleToggle}
        disabled={saving || !currentUser?.id}
        className={`relative h-6 w-11 rounded-full transition-colors ${isPrivate ? "bg-[#0095f6]" : "bg-[#dbdbdb]"} disabled:opacity-50`}
        aria-pressed={isPrivate}
      >
        <span className={`absolute top-1 h-4 w-4 rounded-full bg-white transition-transform ${isPrivate ? "translate-x-5" : "translate-x-1"}`} />
      </button>
    </div>
  );
}

function StoryLocationSettings({ currentUserId }) {
  const [hiddenUsers, setHiddenUsers] = useState([]);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);

  const loadHiddenUsers = async () => {
    setLoading(true);
    try {
      setHiddenUsers(await getHiddenStoryUsers());
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHiddenUsers();
  }, []);

  useEffect(() => {
    const timer = setTimeout(async () => {
      const trimmed = query.trim();
      if (trimmed.length < 2) {
        setResults([]);
        return;
      }
      const data = await searchUsers(trimmed);
      setResults((Array.isArray(data) ? data : []).filter((user) => user.id !== currentUserId));
    }, 300);
    return () => clearTimeout(timer);
  }, [query, currentUserId]);

  const hiddenIds = new Set(hiddenUsers.map((user) => String(user.id)));

  const addUser = async (userId) => {
    setSavingId(userId);
    try {
      await addHiddenStoryUser(userId);
      setQuery("");
      setResults([]);
      await loadHiddenUsers();
    } finally {
      setSavingId(null);
    }
  };

  const removeUser = async (userId) => {
    setSavingId(userId);
    try {
      await removeHiddenStoryUser(userId);
      await loadHiddenUsers();
    } finally {
      setSavingId(null);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-bold">Story and Location</h1>
      <section className="mt-6">
        <h2 className="text-sm font-bold">Hide story and live from</h2>
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search users" className="mt-4 h-11 w-full rounded-lg border border-[#dbdbdb] px-3 text-sm outline-none focus:border-[#a8a8a8]" />

        {results.length > 0 && (
          <div className="mt-3 overflow-hidden rounded-lg border border-[#dbdbdb]">
            {results.map((user) => (
              <div key={user.id} className="flex items-center justify-between gap-3 px-4 py-3">
                <UserRow user={user} />
                <button type="button" disabled={savingId === user.id || hiddenIds.has(String(user.id))} onClick={() => addUser(user.id)} className="rounded-lg bg-[#0095f6] px-3 py-1.5 text-xs font-bold text-white disabled:opacity-40">
                  {hiddenIds.has(String(user.id)) ? "Hidden" : "Hide"}
                </button>
              </div>
            ))}
          </div>
        )}

        <div className="mt-6 overflow-hidden rounded-lg border border-[#dbdbdb]">
          {loading ? (
            <p className="p-4 text-sm text-[#737373]">Loading...</p>
          ) : hiddenUsers.length === 0 ? (
            <p className="p-4 text-sm text-[#737373]">No hidden users.</p>
          ) : (
            hiddenUsers.map((user) => (
              <div key={user.id} className="flex items-center justify-between gap-3 border-b border-[#efefef] px-4 py-3 last:border-b-0">
                <UserRow user={user} />
                <button type="button" disabled={savingId === user.id} onClick={() => removeUser(user.id)} className="rounded-lg border border-[#dbdbdb] px-3 py-1.5 text-xs font-bold disabled:opacity-40">
                  Remove
                </button>
              </div>
            ))
          )}
        </div>
      </section>
    </>
  );
}

function MessageStoryReplySettings() {
  const [settings, setSettings] = useState({
    messageRequestPermission: "EVERYONE",
    storyReplyPermission: "EVERYONE",
    showActivityStatus: true,
    onlineVisibility: "EVERYONE",
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getMessagePrivacySettings()
      .then((data) => setSettings((prev) => ({ ...prev, ...data })))
      .finally(() => setLoading(false));
  }, []);

  const save = async (nextSettings) => {
    setSettings(nextSettings);
    setSaving(true);
    try {
      const saved = await updateMessagePrivacySettings(nextSettings);
      setSettings((prev) => ({ ...prev, ...saved }));
    } finally {
      setSaving(false);
    }
  };

  const updateField = (field, value) => save({ ...settings, [field]: value });

  if (loading) return <p className="text-sm text-[#737373]">Loading...</p>;

  return (
    <>
      <h1 className="text-2xl font-bold">Messages and Story Replies</h1>
      <div className="mt-6 space-y-6">
        <SettingRadioGroup title="How people can reach you" value={settings.messageRequestPermission} onChange={(value) => updateField("messageRequestPermission", value)} options={[["EVERYONE", "Everyone"], ["FOLLOWERS", "People you follow"], ["NO_ONE", "No one"]]} />
        <SettingRadioGroup title="Story replies" value={settings.storyReplyPermission} onChange={(value) => updateField("storyReplyPermission", value)} options={[["EVERYONE", "Everyone"], ["FOLLOWERS", "People you follow"], ["NO_ONE", "No one"]]} />
        <SettingRadioGroup title="Who can see you're online" value={settings.onlineVisibility} onChange={(value) => updateField("onlineVisibility", value)} options={[["EVERYONE", "Everyone"], ["FOLLOWERS", "People you follow"], ["NO_ONE", "No one"]]} />
        <div className="flex items-center justify-between rounded-lg border border-[#dbdbdb] p-4">
          <div>
            <p className="text-sm font-semibold">Show activity status</p>
            <p className="mt-1 text-xs text-[#737373]">{saving ? "Saving..." : "Saved to backend"}</p>
          </div>
          <button type="button" onClick={() => updateField("showActivityStatus", !settings.showActivityStatus)} className={`relative h-6 w-11 rounded-full ${settings.showActivityStatus ? "bg-[#0095f6]" : "bg-[#dbdbdb]"}`} aria-pressed={settings.showActivityStatus}>
            <span className={`absolute top-1 h-4 w-4 rounded-full bg-white transition-transform ${settings.showActivityStatus ? "translate-x-5" : "translate-x-1"}`} />
          </button>
        </div>
      </div>
    </>
  );
}

function SettingRadioGroup({ title, value, onChange, options }) {
  return (
    <section className="rounded-lg border border-[#dbdbdb] p-4">
      <h2 className="text-sm font-bold">{title}</h2>
      <div className="mt-3 space-y-2">
        {options.map(([optionValue, label]) => (
          <label key={optionValue} className="flex items-center justify-between rounded-lg px-2 py-2 text-sm hover:bg-[#fafafa]">
            <span>{label}</span>
            <input type="radio" name={title} checked={value === optionValue} onChange={() => onChange(optionValue)} />
          </label>
        ))}
      </div>
    </section>
  );
}

function ToggleButton({ checked, disabled, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`relative h-6 w-11 shrink-0 rounded-full transition-colors ${checked ? "bg-[#0095f6]" : "bg-[#dbdbdb]"} disabled:opacity-50`}
      aria-pressed={checked}
    >
      <span className={`absolute top-1 h-4 w-4 rounded-full bg-white transition-transform ${checked ? "translate-x-5" : "translate-x-1"}`} />
    </button>
  );
}

function PrivacySettings({ currentUser }) {
  const [settings, setSettings] = useState({
    showActivityStatus: true,
    readReceipts: true,
    hideOffensiveComments: true,
    storySharing: true,
  });
  const [saving, setSaving] = useState(null);

  useEffect(() => {
    setSettings({
      showActivityStatus: currentUser?.showActivityStatus !== false,
      readReceipts: currentUser?.readReceipts !== false,
      hideOffensiveComments: currentUser?.hideOffensiveComments !== false,
      storySharing: currentUser?.storySharing !== false,
    });
  }, [currentUser]);

  const toggle = async (setting) => {
    const nextValue = !settings[setting];
    setSaving(setting);
    try {
      await updatePrivacySetting(setting, nextValue);
      setSettings((prev) => ({ ...prev, [setting]: nextValue }));
    } finally {
      setSaving(null);
    }
  };

  const toggles = [
    { key: "showActivityStatus", label: "Show activity status", desc: "Let people see when you're active or recently active" },
    { key: "readReceipts", label: "Read receipts", desc: "Let people see when you've seen their messages" },
    { key: "hideOffensiveComments", label: "Hide offensive comments", desc: "Automatically hide comments that may be offensive" },
    { key: "storySharing", label: "Allow story sharing", desc: "Let people share your stories as messages" },
  ];

  return (
    <>
      <h1 className="text-2xl font-bold">Privacy</h1>
      <div className="mt-6 space-y-3 text-sm text-[#262626]">
        <AccountPrivacyToggle currentUser={currentUser} />
        {toggles.map(({ key, label, desc }) => (
          <div key={key} className="flex items-center justify-between gap-4 rounded-lg border border-[#dbdbdb] p-4">
            <div className="min-w-0">
              <p className="text-sm font-semibold">{label}</p>
              <p className="mt-1 text-xs text-[#737373]">{desc}</p>
            </div>
            <ToggleButton checked={settings[key]} disabled={saving === key} onClick={() => toggle(key)} />
          </div>
        ))}
      </div>
    </>
  );
}

function TagsMentionsSettings() {
  const [allowMentions, setAllowMentions] = useState(true);
  const [allowReplies, setAllowReplies] = useState(true);
  const [saving, setSaving] = useState(null);

  const toggleMentions = async () => {
    const next = !allowMentions;
    setSaving("mentions");
    try {
      await updateStoryMentions(next);
      setAllowMentions(next);
    } finally {
      setSaving(null);
    }
  };

  const toggleReplies = async () => {
    const next = !allowReplies;
    setSaving("replies");
    try {
      await updateStoryReplies(next);
      setAllowReplies(next);
    } finally {
      setSaving(null);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-bold">Tags and Mentions</h1>
      <div className="mt-6 space-y-3">
        <div className="flex items-center justify-between gap-4 rounded-lg border border-[#dbdbdb] p-4">
          <div>
            <p className="text-sm font-semibold">Allow mentions from stories</p>
            <p className="mt-1 text-xs text-[#737373]">Let people mention you in their stories</p>
          </div>
          <ToggleButton checked={allowMentions} disabled={saving === "mentions"} onClick={toggleMentions} />
        </div>
        <div className="flex items-center justify-between gap-4 rounded-lg border border-[#dbdbdb] p-4">
          <div>
            <p className="text-sm font-semibold">Allow story replies</p>
            <p className="mt-1 text-xs text-[#737373]">Let people reply to your stories</p>
          </div>
          <ToggleButton checked={allowReplies} disabled={saving === "replies"} onClick={toggleReplies} />
        </div>
      </div>
    </>
  );
}

function CommentsSettings() {
  const [hideOffensive, setHideOffensive] = useState(true);
  const [blockStrangers, setBlockStrangers] = useState(false);
  const [saving, setSaving] = useState(null);

  const toggle = async (setting, value, setter) => {
    const next = !value;
    setSaving(setting);
    try {
      await updatePrivacySetting(setting, next);
      setter(next);
    } finally {
      setSaving(null);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-bold">Comments</h1>
      <div className="mt-6 space-y-3">
        <div className="flex items-center justify-between gap-4 rounded-lg border border-[#dbdbdb] p-4">
          <div>
            <p className="text-sm font-semibold">Hide offensive comments</p>
            <p className="mt-1 text-xs text-[#737373]">Automatically hide comments that may be offensive</p>
          </div>
          <ToggleButton checked={hideOffensive} disabled={saving === "hideOffensiveComments"} onClick={() => toggle("hideOffensiveComments", hideOffensive, setHideOffensive)} />
        </div>
        <div className="flex items-center justify-between gap-4 rounded-lg border border-[#dbdbdb] p-4">
          <div>
            <p className="text-sm font-semibold">Block comment notifications from people you don't follow</p>
            <p className="mt-1 text-xs text-[#737373]">You won't get notifications for comments from people you don't follow</p>
          </div>
          <ToggleButton checked={blockStrangers} disabled={saving === "blockCommentNotifications"} onClick={() => toggle("blockCommentNotifications", blockStrangers, setBlockStrangers)} />
        </div>
      </div>
    </>
  );
}

function SharingReuseSettings() {
  const [allowReelDownloads, setAllowReelDownloads] = useState(true);
  const [sensitiveContent, setSensitiveContent] = useState("STANDARD");
  const [saving, setSaving] = useState(null);

  const toggleReelDownloads = async () => {
    const next = !allowReelDownloads;
    setSaving("reelDownloads");
    try {
      await updateReelDownloads(next);
      setAllowReelDownloads(next);
    } finally {
      setSaving(null);
    }
  };

  const updateSensitive = async (value) => {
    setSaving("sensitive");
    try {
      await updateSensitiveContent(value);
      setSensitiveContent(value);
    } finally {
      setSaving(null);
    }
  };

  const sensitiveOptions = [
    { value: "MORE", label: "More" },
    { value: "STANDARD", label: "Standard" },
    { value: "LESS", label: "Less" },
  ];

  return (
    <>
      <h1 className="text-2xl font-bold">Sharing and Reuse</h1>
      <div className="mt-6 space-y-3">
        <div className="flex items-center justify-between gap-4 rounded-lg border border-[#dbdbdb] p-4">
          <div>
            <p className="text-sm font-semibold">Allow downloads of your reels</p>
            <p className="mt-1 text-xs text-[#737373]">Let people download your reels to their device</p>
          </div>
          <ToggleButton checked={allowReelDownloads} disabled={saving === "reelDownloads"} onClick={toggleReelDownloads} />
        </div>
        <div className="rounded-lg border border-[#dbdbdb] p-4">
          <p className="text-sm font-semibold mb-3">Sensitive content control</p>
          <p className="mt-1 text-xs text-[#737373] mb-3">Choose how much sensitive content you see</p>
          <div className="space-y-2">
            {sensitiveOptions.map(({ value, label }) => (
              <label key={value} className="flex items-center justify-between rounded-lg px-2 py-2 text-sm hover:bg-[#fafafa]">
                <span>{label}</span>
                <input
                  type="radio"
                  name="sensitiveContent"
                  checked={sensitiveContent === value}
                  onChange={() => updateSensitive(value)}
                  disabled={saving === "sensitive"}
                />
              </label>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}

function LoginHistorySettings({ onLogout }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getLoginHistory()
      .then((data) => setHistory(data))
      .catch(() => setHistory([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <>
      <h1 className="text-2xl font-bold">Login Activity</h1>
      <div className="mt-6 space-y-4">
        <button type="button" onClick={onLogout} className="rounded-lg bg-[#ed4956] px-4 py-2 text-sm font-bold text-white">
          Log out current session
        </button>
        <div className="overflow-hidden rounded-lg border border-[#dbdbdb]">
          {loading ? (
            <p className="p-4 text-sm text-[#737373]">Loading...</p>
          ) : history.length === 0 ? (
            <p className="p-4 text-sm text-[#737373]">No login history found.</p>
          ) : (
            history.map((entry, index) => (
              <div key={entry.id || index} className="flex items-center justify-between border-b border-[#efefef] px-4 py-3 last:border-b-0">
                <div>
                  <p className="text-sm font-semibold">{entry.device || entry.deviceName || entry.deviceType || "Unknown device"}</p>
                  <p className="text-xs text-[#737373]">
                    {(entry.location || entry.city || "") + ((entry.location || entry.city) && entry.timestamp ? " · " : "") + (entry.timestamp ? new Date(entry.timestamp).toLocaleString() : "") + (entry.ipAddress ? ` · ${entry.ipAddress}` : "")}
                  </p>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </>
  );
}

function RestrictedListSettings({ currentUserId }) {
  const [items, setItems] = useState([]);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [savingId, setSavingId] = useState(null);

  const loadItems = async () => {
    setLoading(true);
    try {
      const data = await getRestrictedAccounts();
      setItems(data);
    } catch (error) {
      console.error("Failed to load restricted accounts", error);
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadItems();
  }, []);

  useEffect(() => {
    const timer = setTimeout(async () => {
      const trimmed = query.trim();
      if (trimmed.length < 2) {
        setResults([]);
        return;
      }
      try {
        const data = await searchUsers(trimmed);
        setResults((Array.isArray(data) ? data : []).filter((user) => user.id !== currentUserId));
      } catch (error) {
        console.error("User search failed", error);
        setResults([]);
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [query, currentUserId]);

  const listedUsers = items.map((item) => normalizeUser(item, "user")).filter(Boolean);
  const listedUserIds = new Set(listedUsers.map((user) => String(user.id)));

  const addUser = async (userId) => {
    setSavingId(userId);
    try {
      await restrictUser(userId);
      setQuery("");
      setResults([]);
      await loadItems();
    } finally {
      setSavingId(null);
    }
  };

  const removeUser = async (userId) => {
    setSavingId(userId);
    try {
      await unRestrictUser(userId);
      await loadItems();
    } finally {
      setSavingId(null);
    }
  };

  return (
    <>
      <h1 className="text-2xl font-bold">Restricted Accounts</h1>
      <div className="mt-6">
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search users"
          className="h-11 w-full rounded-lg border border-[#dbdbdb] px-3 text-sm outline-none focus:border-[#a8a8a8]"
        />
      </div>

      {results.length > 0 && (
        <div className="mt-3 overflow-hidden rounded-lg border border-[#dbdbdb]">
          {results.map((user) => (
            <div key={user.id} className="flex items-center justify-between gap-3 px-4 py-3">
              <UserRow user={user} />
              <button
                type="button"
                disabled={savingId === user.id || listedUserIds.has(String(user.id))}
                onClick={() => addUser(user.id)}
                className="rounded-lg bg-[#0095f6] px-3 py-1.5 text-xs font-bold text-white disabled:opacity-40"
              >
                {listedUserIds.has(String(user.id)) ? "Restricted" : "Restrict"}
              </button>
            </div>
          ))}
        </div>
      )}

      <div className="mt-6 overflow-hidden rounded-lg border border-[#dbdbdb]">
        {loading ? (
          <p className="p-4 text-sm text-[#737373]">Loading...</p>
        ) : listedUsers.length === 0 ? (
          <p className="p-4 text-sm text-[#737373]">No restricted accounts.</p>
        ) : (
          listedUsers.map((user) => (
            <div key={user.id} className="flex items-center justify-between gap-3 border-b border-[#efefef] px-4 py-3 last:border-b-0">
              <UserRow user={user} />
              <button
                type="button"
                disabled={savingId === user.id}
                onClick={() => removeUser(user.id)}
                className="rounded-lg border border-[#dbdbdb] px-3 py-1.5 text-xs font-bold disabled:opacity-40"
              >
                Unrestrict
              </button>
            </div>
          ))
        )}
      </div>
    </>
  );
}

function InlineEditProfile({ currentUser }) {
  const fileInputRef = useRef(null);
  const [user, setUser] = useState(null);
  const [bio, setBio] = useState("");
  const [gender, setGender] = useState("");
  const [preview, setPreview] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const profileImage = preview || user?.profilePicture || "";

  useEffect(() => {
    if (!currentUser?.id) return;
    setUser(currentUser);
    setBio(currentUser?.bio || "");
    setGender(currentUser?.gender || "");
    setLoading(false);
  }, [currentUser]);

  const handlePhotoChange = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setError("");
    setMessage("");
    const previewUrl = URL.createObjectURL(file);
    setPreview(previewUrl);
    setUploading(true);
    try {
      const updatedUser = await uploadProfilePicture(file);
      setUser(updatedUser);
      setPreview(updatedUser.profilePicture);
      setMessage("Profile photo updated.");
    } catch (err) {
      URL.revokeObjectURL(previewUrl);
      setError(err.message || "Failed to upload photo");
    } finally {
      setUploading(false);
      event.target.value = "";
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");
    try {
      const updatedUser = await updateProfile({ bio, gender });
      setUser(updatedUser);
      setBio(updatedUser?.bio || "");
      setMessage("Profile saved.");
    } catch (err) {
      setError(err.message || "Could not save profile.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="flex justify-center py-16"><div className="h-8 w-8 animate-spin rounded-full border-[3px] border-[#dbdbdb] border-t-black" /></div>;
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <h1 className="text-2xl font-bold">Edit Profile</h1>
      <section className="flex flex-col gap-4 rounded-lg border border-[#dbdbdb] p-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex min-w-0 items-center gap-3">
          <img src={getAvatarUrl({ profilePicture: profileImage })} alt="" className="h-12 w-12 rounded-full object-cover" />
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold">{user?.username}</p>
            <p className="truncate text-xs text-[#737373]">{user?.fullName}</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {uploading && <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-[#0095f6]" />}
          <button type="button" onClick={() => fileInputRef.current?.click()} disabled={uploading} className="h-8 shrink-0 rounded-lg bg-[#0095f6] px-4 text-xs font-bold text-white disabled:opacity-60">
            {uploading ? "Uploading..." : "Change photo"}
          </button>
        </div>
        <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={handlePhotoChange} />
      </section>

      <div>
        <label className="mb-2 block text-sm font-bold">Bio</label>
        <div className="relative">
          <textarea value={bio} onChange={(e) => setBio(e.target.value.slice(0, 150))} placeholder="Bio" rows={2} className="min-h-[48px] w-full resize-none rounded-lg border border-[#dbdbdb] bg-white px-3 py-2 pr-16 text-sm leading-5 outline-none focus:border-[#a8a8a8]" />
          <span className="absolute bottom-2 right-3 text-xs text-[#737373]">{bio.length} / 150</span>
        </div>
      </div>

      <div>
        <label className="mb-2 block text-sm font-bold">Gender</label>
        <div className="relative">
          <select value={gender} onChange={(e) => setGender(e.target.value)} className="h-11 w-full appearance-none rounded-lg border border-[#dbdbdb] bg-white px-3 pr-10 text-sm outline-none focus:border-[#a8a8a8]">
            {["", "Prefer not to say", "Female", "Male", "Custom"].map((opt) => (<option key={opt} value={opt}>{opt || "Select gender"}</option>))}
          </select>
          <ChevronDown size={16} className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-[#737373]" />
        </div>
      </div>

      {(message || error) && (
        <div className={`rounded-lg px-3 py-2 text-sm font-semibold ${error ? "bg-red-50 text-red-600" : "bg-green-50 text-green-700"}`}>
          {error || message}
        </div>
      )}

      <button type="submit" disabled={saving || uploading} className="h-11 w-full rounded-lg bg-[#0095f6] text-sm font-bold text-white disabled:opacity-60">
        {saving ? "Saving..." : "Submit"}
      </button>
    </form>
  );
}

function AppearanceSettings() {
  const { theme, setTheme } = useTheme();
  const [saving, setSaving] = useState(false);

  const handleChange = async (value) => {
    setSaving(true);
    try {
      setTheme(value);
    } catch (err) {
      console.error("Failed to update theme", err);
    } finally {
      setSaving(false);
    }
  };

  const options = [
    { value: "LIGHT", label: "Light" },
    { value: "DARK", label: "Dark" },
  ];

  return (
    <>
      <h1 className="text-2xl font-bold">Appearance</h1>
      <div className="mt-6 space-y-3">
        <div className="rounded-lg border border-[var(--border-primary)] p-4">
          <p className="text-sm font-semibold mb-3">Theme</p>
          <div className="space-y-2">
            {options.map(({ value, label }) => (
              <label key={value} className="flex items-center justify-between rounded-lg px-2 py-2 text-sm hover:bg-[var(--hover-bg)]">
                <span>{label}</span>
                <input type="radio" name="theme" checked={theme === value.toLowerCase()} onChange={() => handleChange(value)} disabled={saving} />
              </label>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}

function ReportProblemSettings() {
  const [reason, setReason] = useState("");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!reason) return;
    setSaving(true);
    setError("");
    try {
      await createReport({ targetType: "APP", reason, description });
      setSuccess(true);
      setReason("");
      setDescription("");
    } catch (err) {
      setError(err.message || "Failed to submit report");
    } finally {
      setSaving(false);
    }
  };

  if (success) {
    return (
      <>
        <h1 className="text-2xl font-bold">Report a Problem</h1>
        <div className="mt-6 rounded-lg border border-[#dbdbdb] p-8 text-center">
          <p className="text-sm font-semibold text-green-700">Report submitted successfully.</p>
          <p className="mt-2 text-xs text-[#737373]">Thank you for your feedback.</p>
          <button type="button" onClick={() => setSuccess(false)} className="mt-4 text-sm font-semibold text-[#0095f6]">
            Submit another report
          </button>
        </div>
      </>
    );
  }

  return (
    <>
      <h1 className="text-2xl font-bold">Report a Problem</h1>
      <form onSubmit={handleSubmit} className="mt-6 space-y-4">
        <div>
          <label className="mb-2 block text-sm font-bold">Reason</label>
          <select value={reason} onChange={(e) => setReason(e.target.value)} required className="h-11 w-full rounded-lg border border-[#dbdbdb] bg-white px-3 text-sm outline-none focus:border-[#a8a8a8]">
            <option value="">Select a reason</option>
            <option value="BUG">Bug or glitch</option>
            <option value="FEATURE_REQUEST">Feature request</option>
            <option value="ACCOUNT_ISSUE">Account issue</option>
            <option value="CONTENT_ISSUE">Content issue</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
        <div>
          <label className="mb-2 block text-sm font-bold">Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Describe the problem..." rows={4} className="w-full resize-none rounded-lg border border-[#dbdbdb] bg-white px-3 py-2 text-sm outline-none focus:border-[#a8a8a8]" />
        </div>
        {error && <div className="rounded-lg bg-red-50 px-3 py-2 text-sm font-semibold text-red-600">{error}</div>}
        <button type="submit" disabled={saving || !reason} className="h-11 w-full rounded-lg bg-[#0095f6] text-sm font-bold text-white disabled:opacity-60">
          {saving ? "Submitting..." : "Submit"}
        </button>
      </form>
    </>
  );
}

function ActivitySettings() {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    getActivity()
      .then((data) => setActivities(Array.isArray(data) ? data : []))
      .catch(() => setActivities([]))
      .finally(() => setLoading(false));
  }, []);

  const getActivityText = (item) => {
    const actor = item.actorUsername || "Someone";
    const type = String(item.type || "").toUpperCase();
    if (type.includes("LIKE")) return { text: `${actor} liked your post.`, icon: "❤️" };
    if (type.includes("COMMENT")) return { text: `${actor} commented: ${item.commentText || ""}`, icon: "💬" };
    if (type.includes("FOLLOW")) return { text: `${actor} started following you.`, icon: "👤" };
    if (type.includes("MESSAGE")) return { text: `${actor} sent you a message.`, icon: "✉️" };
    return { text: `${actor} interacted with you.`, icon: "🔔" };
  };

  return (
    <>
      <h1 className="text-2xl font-bold">Your Activity</h1>
      {loading ? (
        <p className="mt-6 text-sm text-[#737373]">Loading activity...</p>
      ) : activities.length === 0 ? (
        <div className="mt-6 rounded-lg border border-[#dbdbdb] p-8 text-center">
          <p className="text-sm text-[#737373]">No recent activity.</p>
        </div>
      ) : (
        <div className="mt-6 divide-y divide-[#efefef] rounded-lg border border-[#dbdbdb]">
          {activities.map((item) => {
            const { text, icon } = getActivityText(item);
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => {
                  const type = String(item.type || "").toUpperCase();
                  if ((type.includes("LIKE") || type.includes("COMMENT")) && item.postId) {
                    navigate(`/post/${item.postId}`);
                  } else if (item.actorId) {
                    navigate(`/profile/${item.actorId}`);
                  }
                }}
                className="flex w-full items-center gap-3 px-4 py-3 text-left hover:bg-[#fafafa]"
              >
                <div className="h-10 w-10 shrink-0 rounded-full bg-[#efefef] flex items-center justify-center text-sm">
                  {icon}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm text-[#262626]">{text}</p>
                  <p className="mt-0.5 text-xs text-[#737373]">
                    {item.createdAt ? new Date(item.createdAt).toLocaleString() : ""}
                  </p>
                </div>
              </button>
            );
          })}
        </div>
      )}
    </>
  );
}

function SettingsPage() {
  const { section = "" } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useCurrentUser();

  const profileUrl = useMemo(() => {
    if (!currentUser?.id) return "";
    return `${window.location.origin}/profile/${currentUser.id}`;
  }, [currentUser?.id]);

  const handleLogout = async () => {
    await logoutUser();
    clearCurrentUserCache();
    navigate("/login", { replace: true });
  };

  const downloadQr = () => {
    const canvas = document.createElement("canvas");
    canvas.width = 640;
    canvas.height = 640;
    const ctx = canvas.getContext("2d");
    ctx.fillStyle = "#fff";
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = "#111";
    ctx.font = "bold 28px Arial";
    ctx.textAlign = "center";
    ctx.fillText(currentUser?.username || "Instagram profile", 320, 250);
    ctx.font = "18px Arial";
    ctx.fillText(profileUrl, 320, 310);
    ctx.strokeStyle = "#111";
    ctx.lineWidth = 12;
    ctx.strokeRect(90, 90, 460, 460);
    const link = document.createElement("a");
    link.download = `${currentUser?.username || "profile"}-profile.png`;
    link.href = canvas.toDataURL("image/png");
    link.click();
  };

  const content = {
    "": (
      <>
        <h1 className="text-2xl font-bold">Settings and Privacy</h1>
        <div className="mt-6 space-y-3 text-sm">
          <NavLink to="/settings/privacy" className="block rounded-lg border border-[#dbdbdb] p-4 font-semibold hover:bg-[#fafafa]">
            Privacy, password, blocked users, and close friends
          </NavLink>
          <button type="button" onClick={handleLogout} className="flex w-full items-center gap-3 rounded-lg border border-[#dbdbdb] p-4 font-semibold text-[#ed4956] hover:bg-[#fafafa]">
            <LogOut className="h-5 w-5" />
            Log Out
          </button>
        </div>
      </>
    ),
    "edit-profile": <InlineEditProfile currentUser={currentUser} />,
    "apps-and-websites": (
      <>
        <h1 className="text-2xl font-bold">Apps and Websites</h1>
        <div className="mt-6">
          <div className="rounded-lg border border-[#dbdbdb] p-6 text-center text-sm text-[#737373]">
            This feature is not available yet
          </div>
        </div>
      </>
    ),
    notifications: <NotificationSettingsComponent />,
    privacy: <PrivacySettings currentUser={currentUser} />,
    "close-friends": <UserListSettings title="Close Friends" type="close-friends" currentUserId={currentUser?.id} />,
    "blocked-accounts": <UserListSettings title="Blocked Accounts" type="blocked-accounts" currentUserId={currentUser?.id} />,
    "story-location": <StoryLocationSettings currentUserId={currentUser?.id} />,
    "messages-replies": <MessageStoryReplySettings />,
    "restricted-accounts": <RestrictedListSettings currentUserId={currentUser?.id} />,
    "tags-mentions": <TagsMentionsSettings />,
    comments: <CommentsSettings />,
    "sharing-reuse": <SharingReuseSettings />,
    appearance: <AppearanceSettings />,
    activity: <ActivitySettings />,
    "report-problem": <ReportProblemSettings />,
    "meta-verified": (
      <>
        <h1 className="text-2xl font-bold">Meta Verified</h1>
        <div className="mt-6">
          <div className="rounded-lg border border-[#dbdbdb] p-6 text-center text-sm text-[#737373]">
            This feature is not available yet
          </div>
        </div>
      </>
    ),
    supervision: (
      <>
        <h1 className="text-2xl font-bold">Supervision</h1>
        <div className="mt-6">
          <div className="rounded-lg border border-[#dbdbdb] p-6 text-center text-sm text-[#737373]">
            This feature is not available yet
          </div>
        </div>
      </>
    ),
    "login-activity": <LoginHistorySettings onLogout={handleLogout} />,
    "qr-code": (
      <>
        <h1 className="text-2xl font-bold">QR Code</h1>
        <div className="mt-6 max-w-sm rounded-xl border border-[#dbdbdb] p-6 text-center">
          <div className="mx-auto flex aspect-square w-56 items-center justify-center rounded-lg border-8 border-black bg-white p-4 text-xs font-bold break-all">
            {profileUrl}
          </div>
          <p className="mt-4 text-sm font-semibold">@{currentUser?.username}</p>
          <button type="button" onClick={downloadQr} className="mt-4 inline-flex items-center gap-2 rounded-lg bg-[#0095f6] px-4 py-2 text-sm font-bold text-white">
            <Download className="h-4 w-4" />
            Download
          </button>
        </div>
      </>
    ),
  };

  return (
    <main className="min-h-screen" style={{ backgroundColor: "var(--bg-primary)" }}>
      <div className="mx-auto grid max-w-[1200px] grid-cols-1 md:grid-cols-[280px_1fr] md:pt-2">
        <aside className="border-r border-[var(--border-primary)] p-6" style={{ backgroundColor: "var(--bg-primary)" }}>
          <h2 className="mb-4 text-xl font-bold">Settings</h2>
          <nav className="space-y-1">
            {items.map((item) => (
              <NavLink
                key={item.label}
                to={item.slug ? `/settings/${item.slug}` : "/settings"}
                end={!item.slug}
                className={({ isActive }) =>
                  `block rounded-lg px-3 py-3 text-sm font-semibold ${isActive ? "bg-[var(--bg-tertiary)]" : "hover:bg-[var(--hover-bg)]"}`
                }
                style={{ color: "var(--text-primary)" }}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </aside>
        <section className="p-6 md:p-10" style={{ backgroundColor: "var(--bg-primary)", color: "var(--text-primary)" }}>
          <div className="mb-4 md:hidden">
            <button type="button" onClick={() => navigate(section ? "/settings" : "/")} className="flex items-center gap-2 text-sm font-semibold" style={{ color: "var(--text-primary)" }}>
              <ArrowLeft className="h-4 w-4" />
              {section ? "Back" : "Home"}
            </button>
          </div>
          {content[section] || <MissingEndpoint endpoints={[`/settings/${section}`]} />}
        </section>
      </div>
    </main>
  );
}

export default SettingsPage;