import { useEffect, useMemo, useState } from "react";
import { MoreHorizontal, Plus, Trash2, X } from "lucide-react";
import { createHighlight, deleteHighlight, getUserHighlights, updateHighlight } from "../../api/highlightsApi";
import { getStories } from "../../api/storiesApi";
import StoryViewer from "../StoryViewer";

function ProfileHighlights({ user, isOwnProfile }) {
    const [highlights, setHighlights] = useState([]);
    const [stories, setStories] = useState([]);
    const [createOpen, setCreateOpen] = useState(false);
    const [title, setTitle] = useState("");
    const [selectedStoryIds, setSelectedStoryIds] = useState([]);
    const [saving, setSaving] = useState(false);
    const [editingHighlight, setEditingHighlight] = useState(null);
    const [selectedHighlight, setSelectedHighlight] = useState(null);
    const [highlightToDelete, setHighlightToDelete] = useState(null);
    const [openMenuId, setOpenMenuId] = useState(null);

    const loadHighlights = async () => {
        if (!user?.id) return;
        try {
            const data = await getUserHighlights(user.id);
            setHighlights(data);
        } catch (error) {
            console.error("Failed to load highlights", error);
            setHighlights([]);
        }
    };

    useEffect(() => {
        loadHighlights();
    }, [user?.id]);

    useEffect(() => {
        if ((!createOpen && !editingHighlight && !selectedHighlight) || !user?.id) return;
        getStories()
            .then((data) => {
                const ownStories = (Array.isArray(data) ? data : []).filter(
                    (story) => String(story.user?.id) === String(user.id)
                );
                setStories(ownStories);
            })
            .catch(() => setStories([]));
    }, [createOpen, editingHighlight, selectedHighlight, user?.id]);

    const storyById = useMemo(() => {
        const map = new Map();
        stories.forEach((story) => map.set(String(story.id), story));
        return map;
    }, [stories]);

    const toggleStory = (storyId) => {
        setSelectedStoryIds((prev) =>
            prev.includes(storyId)
                ? prev.filter((id) => id !== storyId)
                : [...prev, storyId]
        );
    };

    const handleCreate = async (event) => {
        event.preventDefault();
        if (!title.trim() || selectedStoryIds.length === 0) return;
        setSaving(true);
        try {
            const coverStory = storyById.get(String(selectedStoryIds[0]));
            const payload = {
              title: title.trim(),
              storyIds: selectedStoryIds,
              coverUrl: coverStory?.mediaUrl,
            };

            if (editingHighlight?.id) {
              await updateHighlight({ id: editingHighlight.id, ...payload });
            } else {
              await createHighlight(payload);
            }
            setTitle("");
            setSelectedStoryIds([]);
            setCreateOpen(false);
            setEditingHighlight(null);
            await loadHighlights();
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (highlightId) => {
        await deleteHighlight(highlightId);
        setHighlightToDelete(null);
        setOpenMenuId(null);
        await loadHighlights();
    };

    const openCreate = () => {
        setEditingHighlight(null);
        setTitle("");
        setSelectedStoryIds([]);
        setCreateOpen(true);
    };

    const openEdit = (highlight) => {
        setOpenMenuId(null);
        setEditingHighlight(highlight);
        setTitle(highlight.title || "");
        setSelectedStoryIds((highlight.storyIds || []).map((id) => Number(id)));
        setCreateOpen(true);
    };

    const viewerStories = selectedHighlight
        ? stories.filter((story) =>
            (selectedHighlight.storyIds || []).map(String).includes(String(story.id))
          )
        : [];

    return (
        <>
        <div
            className="
                mx-auto
                mt-[43px]
                flex
                w-full
                max-w-[613px]
                justify-center
                sm:justify-start
                sm:pl-[16px]
            "
        >
            <div className="flex gap-5 overflow-x-auto">
            {isOwnProfile && (
            <button type="button" onClick={openCreate} className="flex flex-col items-center">

                <div
                    className="
                        h-[76px]
                        w-[76px]
                        rounded-full
                        border-[3px]
                        border-[#dbdbdb]
                        bg-[#efefef]
                        p-[3px]
                    "
                >
                    <div
                        className="
                            h-full
                            w-full
                            rounded-full
                            border-[2px]
                            border-[#ffffff]
                            flex
                            items-center
                            justify-center
                        "
                    >
                        <Plus
                            size={40}
                            strokeWidth={1.8}
                            className="text-[#737373]"
                        />
                    </div>
                </div>

                <span
                    className="
                        mt-2
                        text-[12px]
                        font-semibold
                        leading-[15px]
                    "
                >
                    New
                </span>

            </button>
            )}

            {highlights.map((highlight) => (
                <div key={highlight.id} className="flex flex-col items-center">
                    <button
                        type="button"
                        onClick={() => setSelectedHighlight(highlight)}
                        className="relative h-[76px] w-[76px] overflow-hidden rounded-full border-[3px] border-[#dbdbdb] bg-[#efefef] p-[3px]"
                    >
                        {highlight.coverUrl ? (
                            <img src={highlight.coverUrl} alt={highlight.title} className="h-full w-full rounded-full border-2 border-white object-cover" />
                        ) : (
                            <div className="flex h-full w-full items-center justify-center rounded-full border-2 border-white text-lg font-semibold text-[#737373]">
                                {highlight.title?.[0]?.toUpperCase() || "H"}
                            </div>
                        )}
                    </button>
                    <div className="mt-2 flex max-w-[90px] items-center gap-1">
                        <button
                            type="button"
                            onClick={() => setSelectedHighlight(highlight)}
                            className="max-w-[76px] truncate text-[12px] font-semibold leading-[15px]"
                        >
                            {highlight.title}
                        </button>
                        {isOwnProfile && (
                            <div className="relative">
                                <button
                                    type="button"
                                    onClick={() => setOpenMenuId((prev) => (prev === highlight.id ? null : highlight.id))}
                                    aria-label="Highlight options"
                                >
                                    <MoreHorizontal className="h-4 w-4" />
                                </button>

                                {openMenuId === highlight.id && (
                                    <div className="absolute left-1/2 top-6 z-20 w-32 -translate-x-1/2 overflow-hidden rounded-lg border border-[#dbdbdb] bg-white text-sm shadow-lg">
                                        <button
                                            type="button"
                                            onClick={() => openEdit(highlight)}
                                            className="block w-full px-3 py-2 text-left hover:bg-[#fafafa]"
                                        >
                                            Edit
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => {
                                                setHighlightToDelete(highlight);
                                                setOpenMenuId(null);
                                            }}
                                            className="block w-full border-t border-[#efefef] px-3 py-2 text-left font-semibold text-[#ed4956] hover:bg-[#fafafa]"
                                        >
                                            Delete
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            ))}
            </div>
        </div>

        {createOpen && (
            <div
                className="fixed inset-0 z-[90000] flex items-center justify-center bg-black/60 p-4"
                onClick={() => {
                    setCreateOpen(false);
                    setEditingHighlight(null);
                }}
            >
                <form onSubmit={handleCreate} className="w-full max-w-[420px] rounded-xl bg-white" onClick={(event) => event.stopPropagation()}>
                    <div className="relative flex h-12 items-center justify-center border-b border-[#dbdbdb]">
                        <button
                            type="button"
                            onClick={() => {
                                setCreateOpen(false);
                                setEditingHighlight(null);
                            }}
                            className="absolute left-4"
                        >
                            <X className="h-5 w-5" />
                        </button>
                        <h2 className="text-sm font-bold">{editingHighlight ? "Edit Highlight" : "New Highlight"}</h2>
                    </div>
                    <div className="p-4">
                        <input
                            value={title}
                            onChange={(event) => setTitle(event.target.value)}
                            placeholder="Highlight name"
                            className="h-10 w-full rounded-lg border border-[#dbdbdb] px-3 text-sm outline-none"
                            maxLength={100}
                        />
                        <div className="mt-4 grid max-h-[300px] grid-cols-3 gap-2 overflow-y-auto">
                            {stories.map((story) => (
                                <button
                                    key={story.id}
                                    type="button"
                                    onClick={() => toggleStory(story.id)}
                                    className={`aspect-square overflow-hidden rounded-lg border-2 ${selectedStoryIds.map(String).includes(String(story.id)) ? "border-[#0095f6]" : "border-transparent"}`}
                                >
                                    <img src={story.mediaUrl} alt={story.caption || "story"} className="h-full w-full object-cover" />
                                </button>
                            ))}
                        </div>
                        {stories.length === 0 && (
                            <p className="mt-4 text-center text-sm text-[#737373]">
                                No backend story media available to add.
                            </p>
                        )}
                        <button
                            type="submit"
                            disabled={saving || !title.trim() || selectedStoryIds.length === 0}
                            className="mt-4 h-10 w-full rounded-lg bg-[#0095f6] text-sm font-bold text-white disabled:opacity-40"
                        >
                            {saving ? "Saving..." : editingHighlight ? "Save" : "Create"}
                        </button>
                    </div>
                </form>
            </div>
        )}

        {selectedHighlight && (
            viewerStories.length > 0 ? (
                <StoryViewer
                    user={user}
                    stories={viewerStories}
                    onClose={() => setSelectedHighlight(null)}
                />
            ) : (
                <div className="fixed inset-0 z-[90000] flex items-center justify-center bg-black/60 p-4" onClick={() => setSelectedHighlight(null)}>
                    <div className="w-full max-w-[360px] rounded-xl bg-white p-6 text-center" onClick={(event) => event.stopPropagation()}>
                        <h2 className="text-sm font-bold">{selectedHighlight.title}</h2>
                        <p className="mt-3 text-sm text-[#737373]">
                            No active backend story media is available for this highlight.
                        </p>
                        <button
                            type="button"
                            onClick={() => setSelectedHighlight(null)}
                            className="mt-5 h-10 w-full rounded-lg bg-[#0095f6] text-sm font-bold text-white"
                        >
                            Close
                        </button>
                    </div>
                </div>
            )
        )}

        {highlightToDelete && (
            <div className="fixed inset-0 z-[90000] flex items-center justify-center bg-black/60 p-4" onClick={() => setHighlightToDelete(null)}>
                <div className="w-full max-w-[360px] overflow-hidden rounded-xl bg-white text-center" onClick={(event) => event.stopPropagation()}>
                    <div className="p-5">
                        <Trash2 className="mx-auto h-8 w-8 text-[#ed4956]" />
                        <h2 className="mt-3 text-base font-bold">Delete highlight?</h2>
                        <p className="mt-2 text-sm text-[#737373]">
                            This removes {highlightToDelete.title} from your profile.
                        </p>
                    </div>
                    <button
                        type="button"
                        onClick={() => handleDelete(highlightToDelete.id)}
                        className="block w-full border-t border-[#dbdbdb] py-3 text-sm font-bold text-[#ed4956]"
                    >
                        Delete
                    </button>
                    <button
                        type="button"
                        onClick={() => setHighlightToDelete(null)}
                        className="block w-full border-t border-[#dbdbdb] py-3 text-sm"
                    >
                        Cancel
                    </button>
                </div>
            </div>
        )}
        </>
    );
}

export default ProfileHighlights;
