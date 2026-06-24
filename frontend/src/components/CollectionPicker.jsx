import { useEffect, useState } from "react";
import { X, Plus, Check } from "lucide-react";
import { getCollections, createCollection, addPostToCollection, removePostFromCollection } from "../api/collectionsApi";

function CollectionPicker({ postId, onClose }) {
  const [collections, setCollections] = useState([]);
  const [collectionPostMap, setCollectionPostMap] = useState({});
  const [newName, setNewName] = useState("");
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    loadCollections();
  }, []);

  const loadCollections = async () => {
    try {
      const data = await getCollections();
      setCollections(data);
      const map = {};
      data.forEach((col) => {
        map[col.id] = Array.isArray(col.posts)
          ? col.posts.some((p) => (p.id || p) === postId)
          : false;
      });
      setCollectionPostMap(map);
    } catch {
      setCollections([]);
    }
  };

  const toggleCollection = async (collectionId) => {
    const isIn = collectionPostMap[collectionId];
    try {
      if (isIn) {
        await removePostFromCollection(collectionId, postId);
      } else {
        await addPostToCollection(collectionId, postId);
      }
      setCollectionPostMap((prev) => ({ ...prev, [collectionId]: !isIn }));
    } catch {}
  };

  const handleCreate = async () => {
    const name = newName.trim();
    if (!name) return;
    setCreating(true);
    try {
      const col = await createCollection(name);
      if (col?.id) {
        setCollections((prev) => [...prev, col]);
        setCollectionPostMap((prev) => ({ ...prev, [col.id]: false }));
      }
      setNewName("");
    } catch {}
    setCreating(false);
  };

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-[25000]">
      <div className="w-[380px] bg-white rounded-xl overflow-hidden">
        <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
          <h3 className="text-sm font-bold">Save to collection</h3>
          <button onClick={onClose}><X className="w-5 h-5" /></button>
        </div>

        <div className="max-h-[50vh] overflow-y-auto p-2">
          {collections.length === 0 && (
            <p className="text-center text-sm text-gray-500 py-6">No collections yet</p>
          )}
          {collections.map((col) => (
            <button
              key={col.id}
              onClick={() => toggleCollection(col.id)}
              className="flex w-full items-center justify-between px-3 py-3 rounded-lg hover:bg-gray-50"
            >
              <span className="text-sm font-semibold">{col.name || "Collection"}</span>
              {collectionPostMap[col.id] && (
                <Check className="w-5 h-5 text-[#0095f6]" />
              )}
            </button>
          ))}
        </div>

        <div className="border-t border-gray-100 p-3 flex gap-2">
          <input
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            placeholder="New collection name..."
            className="flex-grow border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none"
          />
          <button
            onClick={handleCreate}
            disabled={creating || !newName.trim()}
            className="shrink-0 flex items-center gap-1 rounded-lg bg-[#0095f6] px-3 py-2 text-sm font-semibold text-white disabled:opacity-40"
          >
            <Plus className="w-4 h-4" /> Create
          </button>
        </div>
      </div>
    </div>
  );
}

export default CollectionPicker;
