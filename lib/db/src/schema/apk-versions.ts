import { pgTable, text, serial, integer, timestamp, boolean } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod/v4";

export const apkVersionsTable = pgTable("apk_versions", {
  id: serial("id").primaryKey(),
  versionCode: integer("version_code").notNull().unique(),
  version: text("version").notNull(),
  releaseDate: timestamp("release_date", { withTimezone: true }).notNull().defaultNow(),
  sizeBytes: integer("size_bytes").notNull(),
  sizeMb: text("size_mb").notNull(),
  minAndroidVersion: text("min_android_version").notNull().default("9.0"),
  minAndroidApi: integer("min_android_api").notNull().default(28),
  downloadCount: integer("download_count").notNull().default(0),
  changelog: text("changelog").array().notNull().default([]),
  isLatest: boolean("is_latest").notNull().default(false),
  downloadUrl: text("download_url").notNull(),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const insertApkVersionSchema = createInsertSchema(apkVersionsTable).omit({
  id: true,
  createdAt: true,
});
export type InsertApkVersion = z.infer<typeof insertApkVersionSchema>;
export type ApkVersion = typeof apkVersionsTable.$inferSelect;
