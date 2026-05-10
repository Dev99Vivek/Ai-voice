import React from "react";
import { motion } from "framer-motion";
import { Terminal, Shield, Zap } from "lucide-react";
import phoneMockup from "@assets/Screenshot_20260510_094919_Chrome_1778390662070.jpg";

export default function Hero() {
  return (
    <section className="relative w-full min-h-[90vh] flex flex-col items-center justify-center pt-32 pb-20 px-4">
      {/* Background elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute w-[800px] h-[800px] bg-primary/20 rounded-full blur-[120px] top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 opacity-50"></div>
        <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:40px_40px] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_50%,#000_10%,transparent_100%)]"></div>
      </div>

      <div className="container max-w-6xl relative z-10 flex flex-col items-center text-center">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="mb-6 inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-primary font-mono text-xs"
        >
          <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
          SYSTEM ONLINE. READY FOR INPUT.
        </motion.div>

        <motion.h1
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="text-5xl md:text-7xl lg:text-8xl font-black tracking-tighter uppercase leading-[0.9] mb-6"
        >
          <span className="block text-transparent bg-clip-text bg-gradient-to-br from-white to-white/50">SHADOW /</span>
          <span className="block text-transparent bg-clip-text bg-gradient-to-br from-primary to-secondary">Hands-free.</span>
          <span className="block text-transparent bg-clip-text bg-gradient-to-br from-white to-white/30">Mind-fast.</span>
        </motion.h1>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="max-w-2xl text-lg md:text-xl text-muted-foreground font-mono mb-10"
        >
          An advanced AI assistant that hears, sees, reads, clicks and navigates your Android phone — using voice control, accessibility automation, OCR and screen analysis.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="flex flex-col sm:flex-row gap-4 mb-16"
        >
          <a
            href="#install"
            className="px-8 py-4 font-mono font-bold text-background bg-primary hover:bg-primary/90 hover:shadow-[0_0_30px_rgba(0,245,212,0.4)] transition-all flex items-center justify-center gap-2"
          >
            <Terminal size={18} />
            INSTALL APK
          </a>
          <a
            href="#demos"
            className="px-8 py-4 font-mono font-bold text-white border border-white/20 bg-black/50 hover:bg-white/5 transition-all flex items-center justify-center gap-2"
          >
            ► SEE IT WORK
          </a>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full max-w-4xl mb-20 border-t border-white/10 pt-10"
        >
          <div className="flex flex-col items-center justify-center gap-2">
            <Zap className="text-primary mb-2" />
            <div className="font-mono text-xl font-bold text-white">{"< 200ms"}</div>
            <div className="text-xs text-muted-foreground font-mono tracking-widest">WAKE LATENCY</div>
          </div>
          <div className="flex flex-col items-center justify-center gap-2">
            <Terminal className="text-secondary mb-2" />
            <div className="font-mono text-xl font-bold text-white">10+ CORE</div>
            <div className="text-xs text-muted-foreground font-mono tracking-widest">CAPABILITIES</div>
          </div>
          <div className="flex flex-col items-center justify-center gap-2">
            <Shield className="text-primary mb-2" />
            <div className="font-mono text-xl font-bold text-white">100%</div>
            <div className="text-xs text-muted-foreground font-mono tracking-widest">HANDS-FREE</div>
          </div>
        </motion.div>

      </div>
    </section>
  );
}
