import React from "react";
import { useGetLatestApk, getGetLatestApkQueryKey, useGetDownloadStats, getGetDownloadStatsQueryKey, useTrackEvent } from "@workspace/api-client-react";
import { Download, ShieldCheck, Cpu, ArrowRight } from "lucide-react";
import { motion } from "framer-motion";

export default function ApkInstallSection() {
  const { data: latestApk } = useGetLatestApk({ query: { queryKey: getGetLatestApkQueryKey() } });
  const { data: stats } = useGetDownloadStats({ query: { queryKey: getGetDownloadStatsQueryKey() } });
  const trackEvent = useTrackEvent();

  const handleDownload = () => {
    if (latestApk?.downloadUrl) {
      const isAndroid = /android/i.test(navigator.userAgent);
      
      trackEvent.mutate({
        data: {
          type: "download",
          versionId: latestApk.id,
          platform: isAndroid ? "android" : "web"
        }
      });

      if (isAndroid) {
        window.location.href = latestApk.downloadUrl;
      } else {
        alert("Please open this page on your Android device to install the APK.");
      }
    }
  };

  const permissions = [
    "Accessibility Service",
    "Microphone",
    "Overlay (SYSTEM_ALERT_WINDOW)",
    "Usage Access",
    "Notification Access",
    "Foreground Service"
  ];

  return (
    <section id="install" className="w-full py-32 relative bg-black border-t border-white/5">
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-2xl h-[1px] bg-gradient-to-r from-transparent via-primary to-transparent opacity-30"></div>
      
      <div className="container max-w-5xl mx-auto px-4 md:px-6">
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-black uppercase mb-4 text-white tracking-tight">
            Install <span className="text-primary">SHADOW</span>
          </h2>
          <p className="text-muted-foreground font-mono">Deploy the operational layer to your device.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-start">
          
          {/* Download Card */}
          <div className="bg-white/[0.02] border border-white/10 rounded-2xl p-8 backdrop-blur-sm relative overflow-hidden flex flex-col items-center text-center">
            <div className="absolute top-0 right-0 p-4 opacity-10">
              <Download size={100} />
            </div>
            
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center font-bold text-background tracking-tighter text-3xl mb-6 shadow-[0_0_30px_rgba(0,245,212,0.3)]">
              S
            </div>
            
            <h3 className="text-2xl font-bold text-white mb-2">SHADOW APK</h3>
            
            <div className="flex gap-3 mb-8">
              <span className="px-2 py-1 rounded bg-white/5 border border-white/10 font-mono text-xs text-white">
                v{latestApk?.version || "1.0"}
              </span>
              <span className="px-2 py-1 rounded bg-white/5 border border-white/10 font-mono text-xs text-white">
                {latestApk?.sizeMb || "24.8"} MB
              </span>
              <span className="px-2 py-1 rounded bg-white/5 border border-white/10 font-mono text-xs text-white">
                Android {latestApk?.minAndroidVersion || "9"}+
              </span>
            </div>

            <button 
              onClick={handleDownload}
              className="w-full max-w-xs py-4 font-mono font-bold text-background bg-primary hover:bg-primary/90 hover:shadow-[0_0_30px_rgba(0,245,212,0.4)] transition-all flex items-center justify-center gap-2 rounded-lg mb-6 group relative overflow-hidden"
            >
              <span className="relative z-10 flex items-center gap-2">
                <Download size={18} />
                DOWNLOAD APK
              </span>
            </button>

            {stats && (
              <div className="text-xs font-mono text-muted-foreground flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></span>
                {stats.totalDownloads.toLocaleString()} deployments worldwide
              </div>
            )}
          </div>

          {/* Details & Flow */}
          <div className="flex flex-col gap-8">
            <div>
              <h4 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
                <Cpu size={18} className="text-secondary" />
                Installation Flow
              </h4>
              <div className="space-y-4 font-mono text-sm">
                {["Download APK", "Enable Unknown Sources", "Install & Grant Permissions", "Enable Accessibility", "SHADOW Activates"].map((step, i) => (
                  <div key={i} className="flex items-center gap-3 text-muted-foreground">
                    <span className="w-6 h-6 rounded bg-white/5 flex items-center justify-center text-xs border border-white/10 text-white">
                      {i + 1}
                    </span>
                    {step}
                  </div>
                ))}
              </div>
            </div>

            <div>
              <h4 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
                <ShieldCheck size={18} className="text-primary" />
                Required Permissions
              </h4>
              <div className="flex flex-wrap gap-2">
                {permissions.map(perm => (
                  <span key={perm} className="px-3 py-1 rounded-full bg-white/5 border border-white/10 font-mono text-xs text-muted-foreground">
                    {perm}
                  </span>
                ))}
              </div>
              <p className="mt-4 text-xs font-mono text-muted-foreground opacity-70">
                SHADOW requires deep system access to control UI elements on your behalf. Processing runs locally.
              </p>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}
