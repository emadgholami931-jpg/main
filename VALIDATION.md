# Validation report

Validation performed before packaging `flashcard 2.1.0`:

- The 2.0.0 Room database implementation (`AppDatabase.kt`) is byte-for-byte unchanged, so the database remains schema version 2 with the existing v1 → v2 migration.
- The 2.0.0 full-backup implementation (`BackupManager.kt`) is byte-for-byte unchanged and still uses backup format version 2. Full-backup JSON files created by 2.0.0 therefore remain supported by 2.1.0.
- New Text-to-Speech and appearance-preference classes were type-checked with Kotlin compiler stubs for their Android/Compose APIs.
- Theme application through `MainActivity` / `VazheYarTheme` was type-checked with Kotlin compiler stubs.
- Modified DAO interfaces (single + bulk delete queries) were type-checked with Room annotation stubs.
- The large Compose UI source and all other changed Kotlin files were parser-checked with `kotlinc`; no Kotlin syntax/parser errors were found. A full Compose type-check is delegated to the included GitHub Actions Android build.
- Review-session counter smoke test passed for 75 due cards, confirming the displayed total is not capped at the 50-card in-memory queue window.
- Static feature checks confirmed the example-sentence TTS control, multi-select bulk delete UI, due-session total, and System/Light/Dark appearance controls are present.
- All Android XML resources and the manifest parse successfully.
- Source scan found no Gemini/Groq API-key-shaped secrets and no `.jks`/`.keystore` signing material.
- `VERSION_NAME=2.1.0` and `VERSION_CODE=20100` are set in `version.properties`.

A complete Android/Gradle build is intentionally delegated to `.github/workflows/android-apk.yml`, because this packaging environment has no Android SDK/Gradle dependency cache and no direct Maven/Gradle network access.
