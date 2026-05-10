import { Router } from "express";
import { db, apkVersionsTable } from "@workspace/db";
import { eq, desc } from "drizzle-orm";

const router = Router();

router.get("/latest", async (req, res) => {
  try {
    const [latest] = await db
      .select()
      .from(apkVersionsTable)
      .where(eq(apkVersionsTable.isLatest, true))
      .limit(1);

    if (!latest) {
      const [fallback] = await db
        .select()
        .from(apkVersionsTable)
        .orderBy(desc(apkVersionsTable.versionCode))
        .limit(1);

      if (!fallback) {
        res.status(404).json({ error: "No APK versions found" });
        return;
      }
      res.json({
        id: String(fallback.id),
        version: fallback.version,
        versionCode: fallback.versionCode,
        releaseDate: fallback.releaseDate.toISOString(),
        sizeBytes: fallback.sizeBytes,
        sizeMb: fallback.sizeMb,
        minAndroidVersion: fallback.minAndroidVersion,
        minAndroidApi: fallback.minAndroidApi,
        downloadCount: fallback.downloadCount,
        changelog: fallback.changelog,
        isLatest: fallback.isLatest,
        downloadUrl: fallback.downloadUrl,
      });
      return;
    }

    res.json({
      id: String(latest.id),
      version: latest.version,
      versionCode: latest.versionCode,
      releaseDate: latest.releaseDate.toISOString(),
      sizeBytes: latest.sizeBytes,
      sizeMb: latest.sizeMb,
      minAndroidVersion: latest.minAndroidVersion,
      minAndroidApi: latest.minAndroidApi,
      downloadCount: latest.downloadCount,
      changelog: latest.changelog,
      isLatest: latest.isLatest,
      downloadUrl: latest.downloadUrl,
    });
  } catch (err) {
    req.log.error({ err }, "Failed to get latest APK");
    res.status(500).json({ error: "Internal server error" });
  }
});

router.get("/versions", async (req, res) => {
  try {
    const versions = await db
      .select()
      .from(apkVersionsTable)
      .orderBy(desc(apkVersionsTable.versionCode));

    res.json(
      versions.map((v) => ({
        id: String(v.id),
        version: v.version,
        versionCode: v.versionCode,
        releaseDate: v.releaseDate.toISOString(),
        sizeBytes: v.sizeBytes,
        sizeMb: v.sizeMb,
        minAndroidVersion: v.minAndroidVersion,
        minAndroidApi: v.minAndroidApi,
        downloadCount: v.downloadCount,
        changelog: v.changelog,
        isLatest: v.isLatest,
        downloadUrl: v.downloadUrl,
      }))
    );
  } catch (err) {
    req.log.error({ err }, "Failed to list APK versions");
    res.status(500).json({ error: "Internal server error" });
  }
});

router.get("/download/:versionId", async (req, res) => {
  try {
    const id = parseInt(req.params.versionId, 10);
    if (isNaN(id)) {
      res.status(400).json({ error: "Invalid version ID" });
      return;
    }

    const [version] = await db
      .select()
      .from(apkVersionsTable)
      .where(eq(apkVersionsTable.id, id))
      .limit(1);

    if (!version) {
      res.status(404).json({ error: "Version not found" });
      return;
    }

    // Increment download count
    await db
      .update(apkVersionsTable)
      .set({ downloadCount: version.downloadCount + 1 })
      .where(eq(apkVersionsTable.id, id));

    res.json({
      url: version.downloadUrl,
      version: version.version,
      expiresAt: null,
    });
  } catch (err) {
    req.log.error({ err }, "Failed to get APK download URL");
    res.status(500).json({ error: "Internal server error" });
  }
});

export default router;
