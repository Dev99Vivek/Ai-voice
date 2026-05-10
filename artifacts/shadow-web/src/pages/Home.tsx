import React from "react";
import Nav from "@/components/sections/Nav";
import Hero from "@/components/sections/Hero";
import WakeWords from "@/components/sections/WakeWords";
import CoreSystem from "@/components/sections/CoreSystem";
import Capabilities from "@/components/sections/Capabilities";
import LiveCommandDemos from "@/components/sections/LiveCommandDemos";
import OverlayPreview from "@/components/sections/OverlayPreview";
import ApkInstallSection from "@/components/sections/ApkInstallSection";
import Footer from "@/components/sections/Footer";

export default function Home() {
  return (
    <div className="w-full flex flex-col relative overflow-hidden">
      <div className="fixed inset-0 pointer-events-none bg-[radial-gradient(circle_at_top_right,var(--primary-foreground)_0%,transparent_50%)] opacity-20"></div>
      <Nav />
      <main className="flex-1 flex flex-col items-center w-full z-10">
        <Hero />
        <WakeWords />
        <CoreSystem />
        <Capabilities />
        <LiveCommandDemos />
        <OverlayPreview />
        <ApkInstallSection />
      </main>
      <Footer />
    </div>
  );
}
