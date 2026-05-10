import React from "react";
import { motion } from "framer-motion";
import { Mic, Speech, BrainCircuit, ListChecks, Hand, Smartphone } from "lucide-react";

const steps = [
  { id: "01", title: "Voice Input", icon: <Mic size={24} />, desc: "Continuous low-power listening" },
  { id: "02", title: "Speech Recognition", icon: <Speech size={24} />, desc: "On-device transcription" },
  { id: "03", title: "AI Intent Processing", icon: <BrainCircuit size={24} />, desc: "Natural language understanding" },
  { id: "04", title: "Action Planning", icon: <ListChecks size={24} />, desc: "Step-by-step resolution" },
  { id: "05", title: "Accessibility Execution", icon: <Hand size={24} />, desc: "UI traversal & interaction" },
  { id: "06", title: "Phone Control", icon: <Smartphone size={24} />, desc: "System-level operations" },
];

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: { staggerChildren: 0.1 }
  }
};

const item = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0, transition: { type: "spring", stiffness: 300, damping: 24 } }
};

export default function CoreSystem() {
  return (
    <section id="core" className="w-full py-32 relative bg-black border-b border-white/5">
      <div className="container max-w-6xl mx-auto px-4 md:px-6 relative z-10">
        <div className="text-center mb-20">
          <h2 className="text-3xl md:text-5xl font-black uppercase mb-4 text-white tracking-tight">
            From voice to <span className="text-primary">action</span>
          </h2>
          <p className="text-muted-foreground font-mono">Six steps. Zero hands. Under a second.</p>
        </div>

        <motion.div 
          variants={container}
          initial="hidden"
          whileInView="show"
          viewport={{ once: true, margin: "-100px" }}
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
        >
          {steps.map((step, i) => (
            <motion.div key={step.id} variants={item} className="relative group">
              <div className="absolute inset-0 bg-gradient-to-b from-primary/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity rounded-xl"></div>
              <div className="absolute inset-0 border border-primary/20 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity blur-[2px]"></div>
              
              <div className="h-full bg-white/[0.02] border border-white/10 rounded-xl p-8 relative overflow-hidden backdrop-blur-sm transition-all group-hover:border-primary/30">
                <div className="absolute top-0 right-0 p-6 text-6xl font-black text-white/5 pointer-events-none group-hover:text-primary/10 transition-colors">
                  {step.id}
                </div>
                
                <div className="w-12 h-12 rounded-lg bg-black border border-white/10 flex items-center justify-center text-primary mb-6 group-hover:scale-110 group-hover:shadow-[0_0_20px_rgba(0,245,212,0.3)] transition-all">
                  {step.icon}
                </div>
                
                <h3 className="text-xl font-bold text-white mb-2">{step.title}</h3>
                <p className="text-sm font-mono text-muted-foreground">{step.desc}</p>
                
                {/* Connection arrows for larger screens */}
                {i % 3 !== 2 && (
                  <div className="hidden lg:block absolute top-1/2 -right-3 w-6 h-[1px] bg-white/20 group-hover:bg-primary/50"></div>
                )}
              </div>
            </motion.div>
          ))}
        </motion.div>
      </div>
    </section>
  );
}
