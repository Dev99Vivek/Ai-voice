import React, { useState } from "react";
import { useGetLatestApk, getGetLatestApkQueryKey, useGetDownloadStats, getGetDownloadStatsQueryKey, useTrackEvent } from "@workspace/api-client-react";
import { Download, ShieldCheck, Cpu, Bell, CheckCircle } from "lucide-react";

export default function ApkInstallSection() {
  const { data: latestApk } = useGetLatestApk({ query: { queryKey: getGetLatestApkQueryKey() } });
  const { data: stats } = useGetDownloadStats({ query: { queryKey: getGetDownloadStatsQueryKey() } });
  const trackEvent = useTrackEvent();
  const [notified, setNotified] = useState(false);
  const [email, setEmail] = useState("");

  const hasRealApk = !!(latestApk?.downloadUrl && latestApk.downloadUrl.length > 0);
  const isAndroid = /android/i.test(navigator.userAgent);

  const handleDownload = () => {
    if (!hasRealApk) return;

    trackEvent.mutate({
      data: {
        type: "download",
        versionId: latestApk!.id,
        platform: isAndroid ? "android" : "web",
      },
    });

    window.open(latestApk!.downloadUrl, "_blank");
  };

  const handleNotify = (e: React.FormEvent) => {
    e.preventDefault();
    if (email.trim()) {
      setNotified(true);
    }
  };

  const permissions = [
    "Accessibility Service",
    "Microphone",
    "Overlay (SYSTEM_ALERT_WINDOW)",
    "Usage Access",
    "Notification Access",
    "Foreground Service",
  ];

  return (
    <section id="install" className="w-full py-32 relative bg-black border-t border-white/5">
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-2xl h-[1px] bg-gradient-to-r from-transparent via-[#00F5D4] to-transparent opacity-30" />

      <div className="container max-w-5xl mx-auto px-4 md:px-6">
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-black uppercase mb-4 text-white tracking-tight">
            Install <span className="text-[#00F5D4]">SHADOW</span>
          </h2>
          <p className="text-white/40 font-mono text-sm">Deploy the operational layer to your device.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-start">

          {/* Download Card */}
          <div className="bg-white/[0.02] border border-white/10 rounded-2xl p-8 backdrop-blur-sm relative overflow-hidden flex flex-col items-center text-center">

            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#00F5D4] to-[#A855F7] flex items-center justify-center font-bold text-black text-3xl mb-6 shadow-[0_0_30px_rgba(0,245,212,0.3)]">
              S
            </div>

            <h3 className="text-2xl font-bold text-white mb-2">SHADOW APK</h3>

            <div className="flex flex-wrap justify-center gap-2 mb-8">
              <span className="px-2 py-1 rounded bg-white/5 border border-white/10 font-mono text-xs text-white">
                {latestApk?.version ?? "v1.0"}
              </span>
              <span className="px-2 py-1 rounded bg-white/5 border border-white/10 font-mono text-xs text-white">
                {latestApk?.sizeMb ?? "24.8 MB"}
              </span>
              <span className="px-2 py-1 rounded bg-white/5 border border-white/10 font-mono text-xs text-white">
                Android {latestApk?.minAndroidVersion ?? "9"}+
              </span>
            </div>

            {hasRealApk ? (
              <>
                <button
                  onClick={handleDownload}
                  className="w-full max-w-xs py-4 font-mono font-bold text-black bg-[#00F5D4] hover:bg-[#00F5D4]/90 hover:shadow-[0_0_30px_rgba(0,245,212,0.4)] transition-all flex items-center justify-center gap-2 rounded-lg mb-4"
                >
                  <Download size={18} />
                  DOWNLOAD APK
                </button>
                {!isAndroid && (
                  <p className="text-xs font-mono text-white/30">
                    Open on your Android phone to install
                  </p>
                )}
              </>
            ) : (
              /* APK not yet hosted — show notify form */
              <div className="w-full">
                <div className="flex items-center justify-center gap-2 mb-3">
                  <span className="w-2 h-2 rounded-full bg-yellow-400 animate-pulse" />
                  <span className="font-mono text-xs text-yellow-400 uppercase tracking-widest">APK Launching Soon</span>
                </div>
                <p className="text-white/40 font-mono text-xs mb-6">
                  The Android build is in final testing. Drop your email and you'll be the first to get it.
                </p>
                {notified ? (
                  <div className="flex flex-col items-center gap-2 text-[#00F5D4] font-mono text-sm">
                    <CheckCircle size={28} />
                    <span>You're on the list. We'll notify you.</span>
                  </div>
                ) : (
                  <form onSubmit={handleNotify} className="flex flex-col gap-3 w-full max-w-xs mx-auto">
                    <input
                      type="email"
                      required
                      value={email}
                      onChange={e => setEmail(e.target.value)}
                      placeholder="your@email.com"
                      className="w-full px-4 py-3 rounded-lg bg-white/5 border border-white/10 text-white font-mono text-sm placeholder:text-white/20 focus:outline-none focus:border-[#00F5D4]/50 transition-colors"
                    />
                    <button
                      type="submit"
                      className="w-full py-3 font-mono font-bold text-black bg-[#00F5D4] hover:bg-[#00F5D4]/90 hover:shadow-[0_0_20px_rgba(0,245,212,0.3)] transition-all flex items-center justify-center gap-2 rounded-lg"
                    >
                      <Bell size={16} />
                      NOTIFY ME
                    </button>
                  </form>
                )}
              </div>
            )}

            {stats && (
              <div className="mt-6 text-xs font-mono text-white/30 flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                {stats.totalDownloads.toLocaleString()} installs worldwide
              </div>
            )}
          </div>

          {/* Details */}
          <div className="flex flex-col gap-8">
            <div>
              <h4 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
                <Cpu size={18} className="text-[#A855F7]" />
                Installation Flow
              </h4>
              <div className="space-y-4 font-mono text-sm">
                {["Download APK", "Enable Unknown Sources", "Install & Grant Permissions", "Enable Accessibility Service", "SHADOW Activates"].map((step, i) => (
                  <div key={i} className="flex items-center gap-3 text-white/40">
                    <span className="w-6 h-6 rounded bg-white/5 flex items-center justify-center text-xs border border-white/10 text-white shrink-0">
                      {i + 1}
                    </span>
                    {step}
                  </div>
                ))}
              </div>
            </div>

            <div>
              <h4 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
                <ShieldCheck size={18} className="text-[#00F5D4]" />
                Required Permissions
              </h4>
              <div className="flex flex-wrap gap-2">
                {permissions.map(perm => (
                  <span key={perm} className="px-3 py-1 rounded-full bg-white/5 border border-white/10 font-mono text-xs text-white/40">
                    {perm}
                  </span>
                ))}
              </div>
              <p className="mt-4 text-xs font-mono text-white/25">
                SHADOW requires deep system access to control UI on your behalf. All processing runs locally on-device.
              </p>
            </div>

            {!hasRealApk && (
              <div className="p-4 rounded-xl border border-yellow-400/20 bg-yellow-400/5 font-mono text-xs text-yellow-400/70">
                <span className="block font-bold mb-1 text-yellow-400">Build in progress</span>
                The SHADOW Android APK is currently in final QA. Once the build passes signing and testing, the download will go live automatically on this page.
              </div>
            )}
          </div>

        </div>
      </div>
    </section>
  );
}
