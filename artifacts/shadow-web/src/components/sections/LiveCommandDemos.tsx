import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Terminal as TerminalIcon } from "lucide-react";

const demos = [
  { cmd: "open Instagram", action: "launch_app", target: '"Instagram"', color: "text-primary" },
  { cmd: "turn on flashlight", action: "toggle_flashlight", target: "true", color: "text-secondary" },
  { cmd: "read this screen", action: "ocr_read_screen() → summarize()", target: "", color: "text-blue-400" },
  { cmd: "find and click login button", action: 'screen.detect("login") → smart_click()', target: "", color: "text-green-400" },
  { cmd: "reply okay to Rahul", action: 'open(WhatsApp) → find(Rahul) → type → send', target: "", color: "text-yellow-400" },
  { cmd: "activate gaming mode", action: 'routine', target: '"Gaming Mode"', color: "text-red-400" },
];

export default function LiveCommandDemos() {
  const [activeIdx, setActiveIdx] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setActiveIdx((prev) => (prev + 1) % demos.length);
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  return (
    <section id="demos" className="w-full py-32 bg-black border-y border-white/5 relative overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,rgba(168,85,247,0.1)_0%,transparent_70%)]"></div>
      
      <div className="container max-w-5xl mx-auto px-4 md:px-6 relative z-10">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-5xl font-black uppercase mb-4 text-white tracking-tight">
            Just speak. <br/><span className="text-secondary">SHADOW handles the rest.</span>
          </h2>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-center">
          <div className="flex flex-col gap-4">
            {demos.map((demo, i) => (
              <div 
                key={i} 
                className={`p-4 rounded-xl border font-mono text-sm transition-all duration-300 ${
                  i === activeIdx 
                  ? "bg-white/10 border-white/20 scale-[1.02] shadow-xl" 
                  : "bg-white/[0.02] border-transparent opacity-50 hover:opacity-80 cursor-pointer"
                }`}
                onClick={() => setActiveIdx(i)}
              >
                <div className="flex items-center gap-3">
                  <span className={`${i === activeIdx ? demo.color : "text-muted-foreground"}`}>
                    <TerminalIcon size={16} />
                  </span>
                  <span className="text-white">"{demo.cmd}"</span>
                </div>
              </div>
            ))}
          </div>

          <div className="bg-black/80 backdrop-blur-md border border-white/10 rounded-2xl p-6 shadow-2xl relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-secondary to-transparent opacity-50"></div>
            
            <div className="flex items-center gap-2 mb-6 border-b border-white/10 pb-4">
              <div className="w-3 h-3 rounded-full bg-red-500/50"></div>
              <div className="w-3 h-3 rounded-full bg-yellow-500/50"></div>
              <div className="w-3 h-3 rounded-full bg-green-500/50"></div>
              <span className="ml-2 text-xs font-mono text-muted-foreground">shadow_execution_console</span>
            </div>

            <div className="font-mono text-sm min-h-[200px]">
              <AnimatePresence mode="wait">
                <motion.div
                  key={activeIdx}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.2 }}
                  className="space-y-4"
                >
                  <div className="text-muted-foreground">
                    <span className="text-primary mr-2">❯</span> 
                    Listening... captured intent: <span className="text-white">"{demos[activeIdx].cmd}"</span>
                  </div>
                  
                  <div className="text-muted-foreground pl-4">
                    Compiling action payload...
                  </div>
                  
                  <div className="bg-white/5 p-4 rounded text-white overflow-x-auto">
                    <span className={demos[activeIdx].color}>{demos[activeIdx].action}</span>
                    {demos[activeIdx].target && (
                      <span className="text-white">({demos[activeIdx].target})</span>
                    )}
                  </div>
                  
                  <div className="text-secondary pl-4 flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-secondary animate-pulse"></span>
                    Executing...
                  </div>
                </motion.div>
              </AnimatePresence>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
