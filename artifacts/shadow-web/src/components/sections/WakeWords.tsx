import React from "react";
import { motion } from "framer-motion";

const words = [
  "Shadow", "Hey Shadow", "Shadow Wake Up", 
  "Shadow", "Hey Shadow", "Shadow Wake Up",
  "Shadow", "Hey Shadow", "Shadow Wake Up",
  "Shadow", "Hey Shadow", "Shadow Wake Up"
];

export default function WakeWords() {
  return (
    <section className="w-full py-10 border-y border-white/5 bg-black/50 overflow-hidden flex items-center">
      <motion.div
        className="flex whitespace-nowrap gap-12"
        animate={{ x: [0, -1000] }}
        transition={{ 
          repeat: Infinity, 
          ease: "linear", 
          duration: 20 
        }}
      >
        {words.map((word, i) => (
          <div key={i} className="flex items-center gap-12 text-white/20 font-mono text-3xl font-black uppercase italic tracking-widest">
            <span>{word}</span>
            <span className="w-2 h-2 rounded-full bg-primary/30"></span>
          </div>
        ))}
      </motion.div>
    </section>
  );
}
