# Upgrade from flashcard 2.0.0

Version 2.1.0 keeps Room database schema version 2 and backup format version 2, so a **Create full backup** JSON made by flashcard 2.0.0 can be restored directly.

If Android refuses to install the new debug APK over the old debug APK because their debug signatures differ:

1. Keep the 2.0.0 full-backup JSON somewhere safe.
2. Uninstall the old app.
3. Install the new APK.
4. Open **Settings → Backup & export → Restore backup** and choose the 2.0.0 JSON file.
5. Re-enter Gemini/Groq API keys if you use AI enrichment; API keys are intentionally never included in backups.

Restoring the full backup brings back the cards, FSRS scheduling state, AI card metadata, and review history.

For all releases from 2.1.1 onward, use the permanently signed release APK and
follow `UPDATE-RELEASE-GUIDE.md`. Once the device has the permanent release-signed
installation, later release APKs can be installed in place without deleting the
Room database.
