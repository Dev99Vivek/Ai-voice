import React, { useState, useEffect } from "react";
import { StyleSheet, Text, View, Pressable } from "react-native";
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withRepeat,
  withTiming,
  withSequence,
  withDelay,
  Easing,
  interpolate,
} from "react-native-reanimated";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Feather } from "@expo/vector-icons";

import { useColors } from "@/hooks/useColors";

const DEMO_STATES = [
  {
    state: "LISTENING",
    user: "Turn off all lights in the living room.",
    shadow: "...",
  },
  {
    state: "PROCESSING",
    user: "Turn off all lights in the living room.",
    shadow: "Processing intent: HomeAutomation/Lights",
  },
  {
    state: "DONE",
    user: "Turn off all lights in the living room.",
    shadow: "Living room lights have been turned off.",
  },
  {
    state: "LISTENING",
    user: "Read the screen and summarize the article.",
    shadow: "...",
  },
  {
    state: "PROCESSING",
    user: "Read the screen and summarize the article.",
    shadow: "Extracting text via OCR...",
  },
  {
    state: "DONE",
    user: "Read the screen and summarize the article.",
    shadow: "Article summarized: 3 key points found.",
  },
];

function WaveformBar({ index, active }: { index: number; active: boolean }) {
  const colors = useColors();
  const height = useSharedValue(10);

  useEffect(() => {
    if (active) {
      height.value = withRepeat(
        withSequence(
          withTiming(Math.random() * 30 + 10, { duration: 200, easing: Easing.linear }),
          withTiming(Math.random() * 40 + 20, { duration: 200, easing: Easing.linear })
        ),
        -1,
        true
      );
    } else {
      height.value = withTiming(10, { duration: 300 });
    }
  }, [active]);

  const style = useAnimatedStyle(() => ({
    height: height.value,
  }));

  return (
    <Animated.View
      style={[
        styles.waveformBar,
        { backgroundColor: colors.primary },
        style,
      ]}
    />
  );
}

export default function TabOneScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const [demoIndex, setDemoIndex] = useState(0);

  const currentDemo = DEMO_STATES[demoIndex];
  const isListening = currentDemo.state === "LISTENING";
  const isProcessing = currentDemo.state === "PROCESSING";

  const pulseRing1 = useSharedValue(0);
  const pulseRing2 = useSharedValue(0);
  const buttonScale = useSharedValue(1);

  useEffect(() => {
    pulseRing1.value = withRepeat(
      withTiming(1, { duration: 2000, easing: Easing.out(Easing.ease) }),
      -1,
      false
    );
    pulseRing2.value = withDelay(
      1000,
      withRepeat(
        withTiming(1, { duration: 2000, easing: Easing.out(Easing.ease) }),
        -1,
        false
      )
    );
  }, []);

  const ringStyle1 = useAnimatedStyle(() => {
    return {
      transform: [{ scale: interpolate(pulseRing1.value, [0, 1], [1, 2]) }],
      opacity: interpolate(pulseRing1.value, [0, 0.8, 1], [0.5, 0, 0]),
    };
  });

  const ringStyle2 = useAnimatedStyle(() => {
    return {
      transform: [{ scale: interpolate(pulseRing2.value, [0, 1], [1, 2]) }],
      opacity: interpolate(pulseRing2.value, [0, 0.8, 1], [0.5, 0, 0]),
    };
  });

  const btnAnimatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: buttonScale.value }],
  }));

  const handleNextDemo = () => {
    setDemoIndex((prev) => (prev + 1) % DEMO_STATES.length);
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={[styles.header, { paddingTop: insets.top + 20 }]}>
        <View style={styles.headerLeft}>
          <Text style={[styles.logoText, { color: colors.foreground }]}>SHADOW</Text>
          <View style={[styles.badge, { backgroundColor: colors.muted }]}>
            <Text style={[styles.badgeText, { color: colors.mutedForeground }]}>v1.0</Text>
          </View>
        </View>
        <View style={styles.activeIndicatorContainer}>
          <View style={[styles.activeDot, { backgroundColor: colors.primary }]} />
          <Text style={[styles.activeText, { color: colors.primary }]}>ACTIVE</Text>
        </View>
      </View>

      <View style={styles.mainCenter}>
        <View style={styles.circleContainer}>
          <Animated.View
            style={[
              styles.pulseRing,
              { borderColor: colors.primary },
              ringStyle1,
            ]}
          />
          <Animated.View
            style={[
              styles.pulseRing,
              { borderColor: colors.primary },
              ringStyle2,
            ]}
          />
          <View style={[styles.mainCircle, { borderColor: colors.primary, backgroundColor: colors.card }]}>
            <Text style={[styles.mainCircleLetter, { color: colors.primary }]}>S</Text>
          </View>
        </View>

        <Text style={[styles.stateLabel, { color: colors.primary }]}>
          {currentDemo.state}
        </Text>

        <View style={styles.waveformContainer}>
          {Array.from({ length: 15 }).map((_, i) => (
            <WaveformBar key={i} index={i} active={isListening || isProcessing} />
          ))}
        </View>
      </View>

      <View style={[styles.transcriptContainer, { paddingBottom: insets.bottom + 100 }]}>
        <View style={[styles.transcriptBox, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Text style={[styles.transcriptLabel, { color: colors.mutedForeground }]}>YOU:</Text>
          <Text style={[styles.transcriptText, { color: colors.foreground }]}>
            {currentDemo.user}
          </Text>
        </View>
        <View style={[styles.transcriptBox, { backgroundColor: colors.card, borderColor: colors.primary + "40" }]}>
          <Text style={[styles.transcriptLabel, { color: colors.primary }]}>SHADOW:</Text>
          <Text style={[styles.transcriptText, { color: colors.primary }]}>
            {currentDemo.shadow}
          </Text>
        </View>

        <Text style={[styles.bottomCaption, { color: colors.mutedForeground }]}>
          Say 'Shadow' to wake
        </Text>
      </View>

      <Pressable
        style={[styles.fabContainer, { bottom: insets.bottom + 90 }]}
        onPressIn={() => (buttonScale.value = withTiming(0.9))}
        onPressOut={() => (buttonScale.value = withTiming(1))}
        onPress={handleNextDemo}
      >
        <Animated.View style={[styles.fab, { backgroundColor: colors.card, borderColor: colors.primary, shadowColor: colors.primary }, btnAnimatedStyle]}>
          <Text style={[styles.fabText, { color: colors.primary }]}>S</Text>
        </Animated.View>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: 24,
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
  logoText: {
    fontFamily: "Inter_700Bold",
    fontSize: 20,
    letterSpacing: 2,
  },
  badge: {
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  badgeText: {
    fontFamily: "Inter_600SemiBold",
    fontSize: 10,
  },
  activeIndicatorContainer: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  activeDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  activeText: {
    fontFamily: "Inter_600SemiBold",
    fontSize: 12,
    letterSpacing: 1,
  },
  mainCenter: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  circleContainer: {
    width: 120,
    height: 120,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 32,
  },
  mainCircle: {
    width: 100,
    height: 100,
    borderRadius: 50,
    borderWidth: 2,
    alignItems: "center",
    justifyContent: "center",
    zIndex: 2,
  },
  mainCircleLetter: {
    fontFamily: "Inter_700Bold",
    fontSize: 48,
    textShadowColor: "#00F5D480",
    textShadowOffset: { width: 0, height: 0 },
    textShadowRadius: 10,
  },
  pulseRing: {
    position: "absolute",
    width: 100,
    height: 100,
    borderRadius: 50,
    borderWidth: 2,
  },
  stateLabel: {
    fontFamily: "Inter_600SemiBold",
    fontSize: 14,
    letterSpacing: 2,
    marginBottom: 24,
  },
  waveformContainer: {
    flexDirection: "row",
    alignItems: "center",
    height: 60,
    gap: 4,
  },
  waveformBar: {
    width: 4,
    borderRadius: 2,
  },
  transcriptContainer: {
    paddingHorizontal: 24,
    gap: 12,
  },
  transcriptBox: {
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
    gap: 4,
  },
  transcriptLabel: {
    fontFamily: "Inter_700Bold",
    fontSize: 12,
  },
  transcriptText: {
    fontFamily: "Inter_400Regular",
    fontSize: 14,
    lineHeight: 20,
  },
  bottomCaption: {
    fontFamily: "Inter_500Medium",
    fontSize: 12,
    textAlign: "center",
    marginTop: 8,
  },
  fabContainer: {
    position: "absolute",
    right: 24,
    zIndex: 10,
  },
  fab: {
    width: 60,
    height: 60,
    borderRadius: 30,
    borderWidth: 2,
    alignItems: "center",
    justifyContent: "center",
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 10,
    elevation: 5,
  },
  fabText: {
    fontFamily: "Inter_700Bold",
    fontSize: 24,
  },
});
