import React from "react";
import { motion } from "framer-motion";

const capabilities = [
  { id: "01", title: "Voice Control", desc: "Continuous background listening with instant wake-word detection.", tags: ["Vosk", "Whisper", "Porcupine"] },
  { id: "02", title: "Accessibility Automation", desc: "Tap, swipe, scroll, type and navigate any app.", tags: ["Tap", "Swipe", "Type", "Navigate"] },
  { id: "03", title: "Screen Analysis", desc: "Detects buttons, menus, icons and text.", tags: ["Vision", "Layout AI"] },
  { id: "04", title: "OCR Reading", desc: "Extract text from screenshots, PDFs, images and live camera feeds. Read OTPs, messages, URLs.", tags: ["ML Kit", "Tesseract"] },
  { id: "05", title: "Smart Clicking", desc: "Finds the right UI element by name, intent or context — then taps it precisely. No coordinates required.", tags: ["Intent → Action"] },
  { id: "06", title: "App & System Control", desc: "Open apps, change settings, place calls, send messages, launch URLs and trigger shortcuts.", tags: ["Apps", "Settings", "Shortcuts"] },
  { id: "07", title: "Floating Overlay UI", desc: "A draggable AI bubble lives above all apps.", tags: ["SYSTEM_ALERT_WINDOW"] },
  { id: "08", title: "Automation Engine", desc: "Voice → Intent → Plan → Execute → Confirm.", tags: ["Pipeline"] },
  { id: "09", title: "Background Execution", desc: "Always-on wake detection, notification monitoring, scheduled routines and background tasks.", tags: ["Foreground Service"] },
  { id: "10", title: "Memory & Routines", desc: "Remembers your apps, daily flows and preferences. Trigger entire macros with a single phrase.", tags: ["Routines", "Memory"] }
];

export default function Capabilities() {
  return (
    <section id="capabilities" className="w-full py-32 bg-black relative">
      <div className="container max-w-6xl mx-auto px-4 md:px-6">
        <div className="mb-20">
          <h2 className="text-3xl md:text-5xl font-black uppercase mb-4 text-white tracking-tight">
            Ten powers. <br/><span className="text-primary">One assistant.</span>
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-12">
          {capabilities.map((cap, i) => (
            <motion.div 
              key={cap.id}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-50px" }}
              transition={{ delay: i * 0.05 }}
              className="flex flex-col border-t border-white/10 pt-6 group"
            >
              <div className="flex items-baseline gap-4 mb-3">
                <span className="text-primary font-mono text-sm font-bold">{cap.id}</span>
                <h3 className="text-xl font-bold text-white group-hover:text-primary transition-colors">{cap.title}</h3>
              </div>
              <p className="text-muted-foreground font-mono text-sm mb-4 flex-1">
                {cap.desc}
              </p>
              <div className="flex flex-wrap gap-2 mt-auto">
                {cap.tags.map(tag => (
                  <span key={tag} className="px-2 py-1 bg-white/5 border border-white/10 rounded text-xs font-mono text-white/70">
                    {tag}
                  </span>
                ))}
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}
