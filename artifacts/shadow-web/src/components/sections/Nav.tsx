import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { useGetLatestApk, getGetLatestApkQueryKey } from "@workspace/api-client-react";

export default function Nav() {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const { data: latestApk } = useGetLatestApk({ query: { queryKey: getGetLatestApkQueryKey() } });

  return (
    <motion.header
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 border-b ${
        scrolled
          ? "bg-background/80 backdrop-blur-md border-white/10 py-3"
          : "bg-transparent border-transparent py-5"
      }`}
    >
      <div className="container mx-auto px-4 md:px-6 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded bg-gradient-to-br from-primary to-secondary flex items-center justify-center font-bold text-background tracking-tighter">
            S
          </div>
          <span className="font-mono font-bold text-xl tracking-widest">SHADOW</span>
          <span className="hidden sm:inline-flex items-center px-2 py-0.5 rounded text-xs font-mono bg-primary/10 text-primary border border-primary/20">
            {latestApk ? `v${latestApk.version}` : "v1.0"}
          </span>
        </div>

        <nav className="hidden md:flex items-center gap-8 font-mono text-sm text-muted-foreground">
          <a href="#core" className="hover:text-primary transition-colors">SYSTEM</a>
          <a href="#capabilities" className="hover:text-primary transition-colors">POWERS</a>
          <a href="#demos" className="hover:text-primary transition-colors">TERMINAL</a>
        </nav>

        <div>
          <a
            href="#install"
            className="group relative inline-flex items-center justify-center px-6 py-2 font-mono text-sm font-medium text-background bg-primary hover:bg-primary/90 transition-all overflow-hidden"
          >
            <span className="absolute w-0 h-0 transition-all duration-500 ease-out bg-white rounded-full group-hover:w-56 group-hover:h-56 opacity-10"></span>
            <span className="relative">INSTALL APK</span>
          </a>
        </div>
      </div>
    </motion.header>
  );
}
