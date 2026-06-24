import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Repeat } from "lucide-react";
import { api } from "../api/client";
import { clearCurrentUserCache, useCurrentUser } from "../hooks/useCurrentUser";
import { clearAuthToken } from "../api/config";
import { getAvatarUrl } from "../utils/avatar";

function SwitchAccountPage() {
  const navigate = useNavigate();
  const { currentUser } = useCurrentUser();
  const [linkedAccounts, setLinkedAccounts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get("/auth/linked-accounts")
      .then(({ data }) => setLinkedAccounts(Array.isArray(data) ? data : []))
      .catch(() => setLinkedAccounts([]))
      .finally(() => setLoading(false));
  }, []);

  const handleSwitch = async (account) => {
    clearAuthToken();
    clearCurrentUserCache();
    navigate(`/login?username=${encodeURIComponent(account.username || "")}`);
  };

  const handleLogout = () => {
    clearAuthToken();
    clearCurrentUserCache();
    navigate("/login");
  };

  return (
    <main className="mx-auto min-h-screen max-w-[500px] bg-white px-4 py-8 pb-[82px] md:pb-10">
      <h1 className="mb-6 text-2xl font-bold text-[#262626]">Switch Accounts</h1>

      {currentUser && (
        <div className="mb-6 rounded-lg border border-[#dbdbdb] p-4">
          <p className="mb-3 text-xs font-semibold text-[#737373]">CURRENT ACCOUNT</p>
          <div className="flex items-center gap-3">
            <img src={getAvatarUrl(currentUser)} alt="" className="h-11 w-11 rounded-full object-cover" />
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold">{currentUser.username}</p>
              <p className="text-xs text-[#737373]">{currentUser.fullName || ""}</p>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <p className="py-8 text-center text-sm text-[#737373]">Loading...</p>
      ) : linkedAccounts.length > 0 ? (
        <div className="mb-6">
          <p className="mb-3 text-xs font-semibold text-[#737373]">LINKED ACCOUNTS</p>
          <div className="divide-y divide-[#efefef] rounded-lg border border-[#dbdbdb]">
            {linkedAccounts.map((account) => (
              <button
                key={account.id}
                type="button"
                onClick={() => handleSwitch(account)}
                className="flex w-full items-center gap-3 px-4 py-3 text-left hover:bg-[#fafafa]"
              >
                <img
                  src={getAvatarUrl(account)}
                  alt=""
                  className="h-11 w-11 rounded-full object-cover"
                />
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-semibold">{account.username}</p>
                  <p className="text-xs text-[#737373]">{account.fullName || ""}</p>
                </div>
                <Repeat className="h-5 w-5 text-[#737373]" />
              </button>
            ))}
          </div>
        </div>
      ) : null}

      <div className="space-y-3">
        <button
          type="button"
          onClick={() => navigate("/register")}
          className="flex w-full items-center gap-3 rounded-lg border border-[#dbdbdb] px-4 py-3 text-left text-sm font-semibold hover:bg-[#fafafa]"
        >
          <Plus className="h-5 w-5" />
          Add new account
        </button>

        <button
          type="button"
          onClick={handleLogout}
          className="flex w-full items-center gap-3 rounded-lg border border-[#dbdbdb] px-4 py-3 text-left text-sm font-semibold text-[#ed4956] hover:bg-[#fafafa]"
        >
          Log out of {currentUser?.username || "current account"}
        </button>
      </div>
    </main>
  );
}

export default SwitchAccountPage;
