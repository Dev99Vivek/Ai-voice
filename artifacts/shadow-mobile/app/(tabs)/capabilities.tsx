import React from "react";
import { StyleSheet, Text, View, ScrollView } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { useColors } from "@/hooks/useColors";

const CAPABILITIES = [
  { id: "01", title: "Voice Control", desc: "Execute complex workflows using natural language commands without touching the screen.", tags: ["NLP", "Speech-to-Text"] },
  { id: "02", title: "Accessibility Automation", desc: "Leverage Android Accessibility APIs to navigate apps, fill forms, and tap elements.", tags: ["A11y", "Navigation"] },
  { id: "03", title: "Screen Analysis", desc: "Read and understand UI hierarchies, recognizing buttons, text, and interactive elements.", tags: ["UI Tree", "Vision"] },
  { id: "04", title: "OCR Reading", desc: "Extract text from images, videos, and non-selectable UI components on the fly.", tags: ["OCR", "Tesseract"] },
  { id: "05", title: "Smart Clicking", desc: "Intelligently determine the exact coordinates of UI elements to simulate human taps.", tags: ["Coordinates", "Simulation"] },
  { id: "06", title: "App & System Control", desc: "Launch apps, toggle system settings, and manage device states directly.", tags: ["Intents", "System"] },
  { id: "07", title: "Floating Overlay UI", desc: "Always-on companion that hovers above other apps ready to assist at any moment.", tags: ["Overlay", "Service"] },
  { id: "08", title: "Automation Engine", desc: "Chain multiple actions together to perform multi-step routines automatically.", tags: ["Routines", "Execution"] },
  { id: "09", title: "Background Execution", desc: "Run scheduled tasks and monitors without keeping the main application open.", tags: ["Workers", "Background"] },
  { id: "10", title: "Memory & Routines", desc: "Remember user preferences, past commands, and frequently used workflows.", tags: ["Context", "Storage"] },
];

export default function CapabilitiesScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { paddingTop: insets.top + 20 }]}>
        <Text style={[styles.headerTitle, { color: colors.foreground }]}>CAPABILITIES</Text>
        <Text style={[styles.headerSubtitle, { color: colors.mutedForeground }]}>
          System Modules Loaded
        </Text>
      </View>

      <ScrollView 
        contentContainerStyle={[styles.scrollContent, { paddingBottom: insets.bottom + 100 }]}
        showsVerticalScrollIndicator={false}
      >
        {CAPABILITIES.map((cap) => (
          <View key={cap.id} style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <View style={styles.cardHeader}>
              <Text style={[styles.badge, { color: colors.primary }]}>{cap.id}</Text>
              <Text style={[styles.title, { color: colors.foreground }]}>{cap.title}</Text>
            </View>
            <Text style={[styles.desc, { color: colors.mutedForeground }]}>{cap.desc}</Text>
            <View style={styles.tags}>
              {cap.tags.map((tag, idx) => (
                <View key={idx} style={[styles.tag, { backgroundColor: colors.muted }]}>
                  <Text style={[styles.tagText, { color: colors.secondary }]}>{tag}</Text>
                </View>
              ))}
            </View>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: 24,
    paddingBottom: 24,
  },
  headerTitle: {
    fontFamily: "Inter_700Bold",
    fontSize: 24,
    letterSpacing: 1,
  },
  headerSubtitle: {
    fontFamily: "Inter_500Medium",
    fontSize: 14,
    marginTop: 4,
  },
  scrollContent: {
    paddingHorizontal: 24,
    gap: 16,
  },
  card: {
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
  },
  cardHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: 12,
    marginBottom: 8,
  },
  badge: {
    fontFamily: "Inter_700Bold",
    fontSize: 14,
  },
  title: {
    fontFamily: "Inter_600SemiBold",
    fontSize: 16,
  },
  desc: {
    fontFamily: "Inter_400Regular",
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 12,
  },
  tags: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 8,
  },
  tag: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
  },
  tagText: {
    fontFamily: "Inter_600SemiBold",
    fontSize: 10,
  },
});
