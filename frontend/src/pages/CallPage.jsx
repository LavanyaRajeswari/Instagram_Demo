import { useCallback, useEffect, useRef, useState } from "react";
import { Camera, CameraOff, Mic, MicOff, PhoneOff, Video } from "lucide-react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { endCall } from "../api/callsApi";
import { API_BASE_URL, getAuthToken } from "../api/config";
import { getAvatarUrl } from "../utils/avatar";
import { useCurrentUser } from "../hooks/useCurrentUser";
import { connect, subscribeToCall } from "../hooks/useWebSocket";

function CallPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { currentUserId } = useCurrentUser();
  const hasVideo = searchParams.get("has_video") === "true";
  const chatId = searchParams.get("ig_thread_id") || searchParams.get("chatId");
  const urlCallId = searchParams.get("callId");
  const callState = location.state || {};
  const [callId, setCallId] = useState(callState.callId || urlCallId || "");
  const [isStarting, setIsStarting] = useState(false);
  const [isEnding, setIsEnding] = useState(false);
  const [error, setError] = useState("");
  const [callEndedRemotely, setCallEndedRemotely] = useState(false);
  const streamRef = useRef(null);
  const endedRef = useRef(false);

  const cleanupCall = () => {
    if (streamRef.current) {
      stopMediaTracks(streamRef.current);
      streamRef.current = null;
    }
    setLocalStream(null);
    if (durationTimerRef.current) {
      window.clearInterval(durationTimerRef.current);
      durationTimerRef.current = null;
    }
  };

  useEffect(() => {
    if (!currentUserId || !callId || callId === "started") return;
    connect();
    const unsub = subscribeToCall(currentUserId, (event) => {
      if (!event || (String(event.callId) !== String(callId))) return;
      if (endedRef.current) return;
      if (event.type === "CALL_ENDED" || event.type === "CALL_REJECTED" || event.type === "CALL_CANCELLED") {
        endedRef.current = true;
        setCallEndedRemotely(true);
        cleanupCall();
        setTimeout(() => navigate("/messages", { replace: true }), 1500);
      }
    });
    return () => { if (unsub) unsub(); };
  }, [currentUserId, callId, navigate]);
  const [mediaError, setMediaError] = useState("");
  const [localStream, setLocalStream] = useState(null);
  const [isMuted, setIsMuted] = useState(false);
  const [isCameraOff, setIsCameraOff] = useState(false);
  const [callDuration, setCallDuration] = useState(0);
  const localVideoRef = useRef(null);
  const remoteVideoRef = useRef(null);
  const durationTimerRef = useRef(null);
  const username = callState.username || "Instagram user";
  const callType = hasVideo ? "VIDEO" : "VOICE";
  const callStarted = Boolean(callId);

  useEffect(() => {
    let cancelled = false;

    const requestLocalMedia = async () => {
      if (!navigator.mediaDevices?.getUserMedia) {
        setMediaError("Camera or microphone permission was denied.");
        return;
      }

      try {
        const constraints = hasVideo ? { video: true, audio: true } : { audio: true };
        const stream = await navigator.mediaDevices.getUserMedia(constraints);

        if (cancelled) {
          stopMediaTracks(stream);
          return;
        }

        setMediaError("");
        streamRef.current = stream;
        setLocalStream(stream);
      } catch (_err) {
        setMediaError("Camera or microphone permission was denied.");
      }
    };

    requestLocalMedia();

    return () => {
      cancelled = true;
      if (streamRef.current) stopMediaTracks(streamRef.current);
    };
  }, [hasVideo]);

  useEffect(() => {
    if (localVideoRef.current && localStream) {
      localVideoRef.current.srcObject = localStream;
    }
  }, [localStream]);

  useEffect(() => {
    if (callStarted) {
      durationTimerRef.current = window.setInterval(() => {
        setCallDuration((prev) => prev + 1);
      }, 1000);
    }

    return () => {
      if (durationTimerRef.current) {
        window.clearInterval(durationTimerRef.current);
      }
    };
  }, [callStarted]);

  useEffect(() => {
    return () => {
      if (localStream) stopMediaTracks(localStream);
      if (durationTimerRef.current) window.clearInterval(durationTimerRef.current);
    };
  }, []);

  const formatDuration = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(mins).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
  };

  const authedFetch = async (url, options = {}) => {
    const token = getAuthToken();
    return fetch(url, {
      credentials: "include",
      ...options,
      headers: {
        ...(options.headers || {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });
  };

  const handleStartCall = async () => {
    setError("");
    if (callId) return;

    setIsStarting(true);

    try {
      const otherUserId = callState.otherUserId || searchParams.get("userId");
      if (!otherUserId) {
        setError("Unable to find the user to call.");
        return;
      }

      const response = await authedFetch(`${API_BASE_URL}/calls/start/${otherUserId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ callType }),
      });

      if (!response.ok) {
        setError("Unable to start call right now.");
        return;
      }

      const data = response.status === 204 ? null : await response.json();
      const newCallId = data?.callId || data?.id || data?.data?.callId || data?.data?.id || "";
      setCallId(newCallId || "started");
    } catch (_err) {
      setError("Unable to start call right now.");
    } finally {
      setIsStarting(false);
    }
  };

  const handleEndCall = async () => {
    setError("");
    if (endedRef.current) return;
    endedRef.current = true;
    cleanupCall();

    if (!callId || callId === "started") {
      navigate("/messages", { replace: true });
      return;
    }

    setIsEnding(true);

    try {
      await endCall(callId);
    } catch (_err) {
    }

    navigate("/messages", { replace: true });
  };

  const toggleMute = () => {
    if (!localStream) return;
    localStream.getAudioTracks().forEach((track) => {
      track.enabled = isMuted;
    });
    setIsMuted(!isMuted);
  };

  const toggleCamera = () => {
    if (!localStream) return;
    localStream.getVideoTracks().forEach((track) => {
      track.enabled = isCameraOff;
    });
    setIsCameraOff(!isCameraOff);
  };

  return (
    <main className="fixed inset-0 bg-black text-white">
      <div className="relative h-full w-full">
        <section className="relative flex h-full w-full items-center justify-center bg-[#1a1a1a]">
          <div ref={remoteVideoRef} className="flex h-full w-full items-center justify-center">
            <div className="flex flex-col items-center text-center">
              <img
                src={getAvatarUrl({ profilePicture: callState.profilePicture })}
                alt=""
                className="h-28 w-28 rounded-full object-cover"
              />
              <h1 className="mt-5 truncate text-2xl font-semibold">{username}</h1>
              <p className="mt-2 text-sm text-white/60">
                {callStarted ? formatDuration(callDuration) : "Ready to call"}
              </p>
              {!callStarted && !mediaError && (
                <p className="mt-2 text-xs text-white/40">
                  {hasVideo ? "Video call" : "Audio call"}
                </p>
              )}
              {mediaError && (
                <p className="mt-5 max-w-[320px] text-sm leading-5 text-white/70">
                  {mediaError}
                </p>
              )}
              {error && (
                <p className="mt-4 rounded-md bg-[#ed4956]/15 px-3 py-2 text-sm text-[#ff8a96]">
                  {error}
                </p>
              )}
            </div>
          </div>

          {hasVideo && localStream && (
            <div className="absolute bottom-28 right-5 z-30 h-[180px] w-[120px] overflow-hidden rounded-xl border-2 border-white/20 shadow-lg">
              <video
                ref={localVideoRef}
                autoPlay
                muted
                playsInline
                className={`h-full w-full object-cover ${isCameraOff ? "hidden" : ""}`}
              />
              {isCameraOff && (
                <div className="flex h-full w-full items-center justify-center bg-[#333]">
                  <CameraOff className="h-8 w-8 text-white/50" />
                </div>
              )}
            </div>
          )}

          {!hasVideo && localStream && (
            <video
              ref={localVideoRef}
              autoPlay
              muted
              playsInline
              className="hidden"
            />
          )}

          <div className="absolute bottom-8 left-1/2 z-40 flex -translate-x-1/2 items-center gap-4">
            {!callStarted ? (
              <button
                type="button"
                onClick={handleStartCall}
                disabled={isStarting}
                className="flex h-16 w-16 items-center justify-center rounded-full bg-[#16a34a] text-white hover:bg-[#15803d] disabled:opacity-60"
                aria-label="Start call"
              >
                {hasVideo ? <Video className="h-7 w-7" /> : <Mic className="h-7 w-7" />}
              </button>
            ) : (
              <>
                <button
                  type="button"
                  onClick={toggleMute}
                  className={`flex h-14 w-14 items-center justify-center rounded-full text-white ${
                    isMuted ? "bg-white/30" : "bg-white/10 hover:bg-white/20"
                  }`}
                  aria-label={isMuted ? "Unmute" : "Mute"}
                >
                  {isMuted ? <MicOff className="h-6 w-6" /> : <Mic className="h-6 w-6" />}
                </button>

                {hasVideo && (
                  <button
                    type="button"
                    onClick={toggleCamera}
                    className={`flex h-14 w-14 items-center justify-center rounded-full text-white ${
                      isCameraOff ? "bg-white/30" : "bg-white/10 hover:bg-white/20"
                    }`}
                    aria-label={isCameraOff ? "Turn camera on" : "Turn camera off"}
                  >
                    {isCameraOff ? <CameraOff className="h-6 w-6" /> : <Camera className="h-6 w-6" />}
                  </button>
                )}

                <button
                  type="button"
                  onClick={handleEndCall}
                  disabled={isEnding}
                  className="flex h-14 w-14 items-center justify-center rounded-full bg-[#ed4956] text-white hover:bg-[#d63b49]"
                  aria-label="End call"
                >
                  <PhoneOff className="h-6 w-6" />
                </button>
              </>
            )}

            {!callStarted && (
              <button
                type="button"
                onClick={handleEndCall}
                className="flex h-14 w-14 items-center justify-center rounded-full bg-[#ed4956] text-white hover:bg-[#d63b49]"
                aria-label="Cancel"
              >
                <PhoneOff className="h-6 w-6" />
              </button>
            )}
          </div>
        </section>
      </div>
    </main>
  );
}

function stopMediaTracks(stream) {
  stream?.getTracks().forEach((track) => track.stop());
}

export default CallPage;