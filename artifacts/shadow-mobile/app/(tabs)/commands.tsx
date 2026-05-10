import React from "react";
import { StyleSheet, Text, View, ScrollView } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { useColors } from "@/hooks/useColors";

const COMMANDS = [
  {
    cmd: "Open Settings and turn on Bluetooth",
    json: `{\n  "action": "system_intent",\n  "target": "android.settings.BLUETOOTH_SETTINGS",\n  "extras": { "enable": true }\n}`,
    res: "[SUCCESS] Bluetooth enabled"
  },
  {
    cmd: "Send 50 dollars to Alex on Venmo",
    json: `{\n  "action": "ui_automation",\n  "app": "com.venmo",\n  "sequence": [\n    {"tap": "Pay or Request"},\n    {"input": "Alex"},\n    {"input": "50"}\n  ]\n}`,
    res: "[PENDING] Awaiting user confirmation"
  },
  {
    cmd: "Read the current screen",
    json: `{\n  "action": "screen_analysis",\n  "method": "accessibility_tree",\n  "extract": "text_content"\n}`,
    res: "[SUCCESS] Found 4 paragraphs"
  },
  {
    cmd: "Scroll down to the bottom",
    json: `{\n  "action": "scroll",\n  "direction": "down",\n  "distance": "max"\n}`,
    res: "[SUCCESS] Scrolled to end of view"
  },
  {
    cmd: "What is this image about?",
    json: `{\n  "action": "ocr_scan",\n  "target": "active_window",\n  "analyze": true\n}`,
    res: "[SUCCESS] Detected a chart showing growth"
  },
  {
    cmd: "Call an Uber to Home",
    json: `{\n  "action": "deep_link",\n  "uri": "uber://?action=setPickup&dropoff[nickname]=Home"\n}`,
    res: "[SUCCESS] Uber app launched"
  }
];

export default function CommandsScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { paddingTop: insets.top + 20 }]}>
        <Text style={[styles.headerTitle, { color: colors.foreground }]}>COMMANDS</Text>
        <Text style={[styles.headerSubtitle, { color: colors.primary }]}>
          Just speak. SHADOW handles the rest.
        </Text>
      </View>

      <ScrollView 
        contentContainerStyle={[styles.scrollContent, { paddingBottom: insets.bottom + 100 }]}
        showsVerticalScrollIndicator={false}
      >
        {COMMANDS.map((item, idx) => (
          <View key={idx} style={[styles.terminalCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <View style={styles.terminalHeader}>
              <View style={[styles.dot, { backgroundColor: colors.destructive }]} />
              <View style={[styles.dot, { backgroundColor: "#FBBF24" }]} />
              <View style={[styles.dot, { backgroundColor: colors.primary }]} />
              <Text style={[styles.terminalTitle, { color: colors.mutedForeground }]}>bash - shadow</Text>
            </View>
            
            <View style={styles.terminalBody}>
              <Text style={[styles.cmdText, { color: colors.foreground }]}>
                <Text style={{ color: colors.primary }}>$ </Text>
                {item.cmd}
              </Text>
              <Text style={[styles.jsonText, { color: colors.secondary }]}>{item.json}</Text>
              <Text style={[styles.resText, { color: colors.mutedForeground }]}>{item.res}</Text>
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
  terminalCard: {
    borderRadius: 8,
    borderWidth: 1,
    overflow: "hidden",
  },
  terminalHeader: {
    flexDirection: "row",
    alignItems: "center",
    padding: 12,
    borderBottomWidth: 1,
    borderBottomColor: "#1E3040",
    backgroundColor: "#0A1016",
    gap: 6,
  },
  dot: {
    width: 10,
    height: 10,
    borderRadius: 5,
  },
  terminalTitle: {
    fontFamily: "Inter_500Medium",
    fontSize: 12,
    marginLeft: "auto",
  },
  terminalBody: {
    padding: 16,
    gap: 12,
  },
  cmdText: {
    fontFamily: "Inter_600SemiBold",
    fontSize: 14,
  },
  jsonText: {
    fontFamily: "Inter_400Regular",
    fontSize: 12,
    lineHeight: 18,
  },
  resText: {
    fontFamily: "Inter_500Medium",
    fontSize: 12,
  },
});
