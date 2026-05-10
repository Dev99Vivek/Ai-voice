import React from "react";
import { motion } from "framer-motion";
import phoneMockup from "@assets/Screenshot_20260510_094919_Chrome_1778390662070.jpg";
import { Mic } from "lucide-react";

export default function OverlayPreview() {
  return (
    <section className="w-full py-32 bg-black relative overflow-hidden flex flex-col items-center justify-center">
      <div className="container max-w-4xl mx-auto px-4 md:px-6 relative z-10 text-center mb-16">
        <h2 className="text-3xl md:text-5xl font-black uppercase mb-4 text-white tracking-tight">
          The <span className="text-primary">Overlay</span>
        </h2>
        <p className="text-muted-foreground font-mono">A draggable AI bubble lives above all apps. Always ready.</p>
      </div>

      <div className="relative w-full max-w-[320px] aspect-[9/19] rounded-[2.5rem] border-[8px] border-white/10 bg-black overflow-hidden shadow-2xl mx-auto">
        <img 
          src={phoneMockup} 
          alt="Phone Mockup" 
          className="absolute inset-0 w-full h-full object-cover opacity-30 mix-blend-luminosity"
        />
        
        {/* SHADOW Bubble UI Demo */}
        <div className="absolute inset-0 flex flex-col justify-end p-6 z-20 pb-12">
          
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: false }}
            className="w-full bg-black/80 backdrop-blur-xl border border-white/20 rounded-2xl p-4 shadow-2xl mb-4 relative overflow-hidden"
          >
            <div className="absolute top-0 left-0 w-full h-[2px] bg-gradient-to-r from-transparent via-primary to-transparent opacity-50"></div>
            
            <div className="flex flex-col gap-3 font-mono text-sm">
              <div className="flex gap-2">
                <span className="text-muted-foreground">YOU:</span>
                <span className="text-white">Hey Shadow, turn on flashlight</span>
              </div>
              <div className="flex gap-2">
                <span className="text-primary font-bold">SHADOW:</span>
                <span className="text-white">Done.</span>
              </div>
            </div>
            
            <div className="mt-4 flex items-center justify-between text-xs text-muted-foreground border-t border-white/10 pt-3">
              <span className="animate-pulse flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-primary inline-block"></span> Listening
              </span>
              <span>Say 'Shadow' to wake</span>
            </div>
          </motion.div>

          {/* Floating Bubble */}
          <motion.div 
            animate={{ y: [0, -10, 0] }}
            transition={{ repeat: Infinity, duration: 4, ease: "easeInOut" }}
            className="self-end w-14 h-14 rounded-full bg-black border border-primary flex items-center justify-center shadow-[0_0_20px_rgba(0,245,212,0.4)] relative cursor-grab"
          >
            <div className="absolute inset-0 rounded-full border border-primary animate-ping opacity-20"></div>
            <span className="font-bold text-primary text-xl font-mono">S</span>
          </motion.div>
          
        </div>
      </div>
    </section>
  );
}
