# Changelog

## 2.1.1

- Hardened production update signing: release builds now require the permanent signing configuration.
- GitHub Actions no longer publishes debug APKs as update artifacts.
- Added APK signature verification and signing-certificate output to every CI release artifact.
- Added a guarded version-bump helper and a safe update/release guide.
- Kept Room database schema at v2 and backup format at v2; no user-data migration is required from 2.1.0.

## 2.1.0

- Added Text-to-Speech playback for the English example sentence on the back of review cards.
- Fixed Review progress so the denominator reflects the actual due review session instead of the 50-card in-memory queue cap.
- Added multi-select mode in Words with select-all-visible, clear selection, and confirmed bulk deletion including review history.
- Added persistent appearance settings: System default, Light, and Dark.
- Kept Room database schema at v2 and backup format at v2 so full backups created by 2.0.0 remain restorable.

## 2.0.0

- Added Gemini + Groq provider architecture with Auto fallback.
- Added encrypted Groq API key storage.
- Replaced legacy two-button scheduler with FSRS-6 and Again/Hard/Good/Easy ratings.
- Added persistent review history.
- Added manual card editing.
- Added library search and status filters.
- Added full JSON backup/restore and CSV export.
- Added CSV import progress and stronger duplicate normalization.
- Added precise transient/permanent AI retry handling and diagnostics.
- Added Room database migration v1 → v2 and schema export.
- Added review reset, retry-all, and delete-all maintenance actions.
- Added version display and production versioning.
- Added optional GitHub Actions signed release APK build using repository secrets.
