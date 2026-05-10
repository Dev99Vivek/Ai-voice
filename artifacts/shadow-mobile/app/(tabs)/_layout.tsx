import { BlurView } from "expo-blur";
import { isLiquidGlassAvailable } from "expo-glass-effect";
import { Tabs } from "expo-router";
import { Icon, NativeTabs } from "expo-router/unstable-native-tabs";
import { SymbolView } from "expo-symbols";
import { Feather } from "@expo/vector-icons";
import React from "react";
import { Platform, StyleSheet, View, useColorScheme } from "react-native";

import { useColors } from "@/hooks/useColors";

function NativeTabLayout() {
  return (
    <NativeTabs>
      <NativeTabs.Trigger name="index">
        <Icon sf={{ default: "dot.radiowaves.left.and.right", selected: "dot.radiowaves.left.and.right" }} />
      </NativeTabs.Trigger>
      <NativeTabs.Trigger name="capabilities">
        <Icon sf={{ default: "bolt", selected: "bolt.fill" }} />
      </NativeTabs.Trigger>
      <NativeTabs.Trigger name="commands">
        <Icon sf={{ default: "terminal", selected: "terminal.fill" }} />
      </NativeTabs.Trigger>
    </NativeTabs>
  );
}

function ClassicTabLayout() {
  const colors = useColors();
  const colorScheme = useColorScheme();
  const isDark = true;
  const isIOS = Platform.OS === "ios";
  const isWeb = Platform.OS === "web";

  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.mutedForeground,
        headerShown: false,
        tabBarShowLabel: false,
        tabBarStyle: {
          position: "absolute",
          backgroundColor: isIOS ? "transparent" : colors.background,
          borderTopWidth: isWeb ? 1 : 0,
          borderTopColor: colors.border,
          elevation: 0,
          ...(isWeb ? { height: 84 } : {}),
        },
        tabBarBackground: () =>
          isIOS ? (
            <BlurView
              intensity={100}
              tint={"dark"}
              style={StyleSheet.absoluteFill}
            />
          ) : isWeb ? (
            <View
              style={[
                StyleSheet.absoluteFill,
                { backgroundColor: colors.background },
              ]}
            />
          ) : null,
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: "SHADOW",
          tabBarIcon: ({ color }) =>
            isIOS ? (
              <SymbolView name="dot.radiowaves.left.and.right" tintColor={color} size={24} />
            ) : (
              <Feather name="radio" size={24} color={color} />
            ),
        }}
      />
      <Tabs.Screen
        name="capabilities"
        options={{
          title: "CAPABILITIES",
          tabBarIcon: ({ color }) =>
            isIOS ? (
              <SymbolView name="bolt" tintColor={color} size={24} />
            ) : (
              <Feather name="zap" size={24} color={color} />
            ),
        }}
      />
      <Tabs.Screen
        name="commands"
        options={{
          title: "COMMANDS",
          tabBarIcon: ({ color }) =>
            isIOS ? (
              <SymbolView name="terminal" tintColor={color} size={24} />
            ) : (
              <Feather name="terminal" size={24} color={color} />
            ),
        }}
      />
    </Tabs>
  );
}

export default function TabLayout() {
  if (isLiquidGlassAvailable()) {
    return <NativeTabLayout />;
  }
  return <ClassicTabLayout />;
}
