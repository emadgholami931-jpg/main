# Safe Android updates without losing user data

The production package identity is intentionally fixed:

- `applicationId`: `com.vazheyar.app`
- Every production APK must be signed by the **same permanent release key**.
- `VERSION_CODE` must increase for every new production APK.
- Room migrations must be supplied whenever the database schema version changes.

When these rules are followed, Android installs a newer APK as an update of the
existing app. App-private data, including the Room database, remains in place.

## One-time release-signing setup

Do this once on a trusted computer:

```bash
./scripts/generate-signing-key.sh
```

Back up the generated `.jks` file in at least two secure locations. Never commit
it to Git. Losing this key prevents future APKs from updating installations that
were signed with it.

Create these GitHub Actions repository secrets:

- `ANDROID_KEYSTORE_BASE64` — the Base64 output printed by the script
- `ANDROID_KEYSTORE_PASSWORD` — keystore password
- `ANDROID_KEY_ALIAS` — normally `flashcard`
- `ANDROID_KEY_PASSWORD` — key password

The CI workflow now **fails** when any signing secret is missing. It no longer
publishes a debug APK as a production artifact.

## Building an update

1. Make and test the app changes.
2. Increase the app version, for example:

   ```bash
   ./scripts/bump-version.sh 2.1.2 20102
   ```

3. Commit and push to `main`.
4. Open GitHub Actions and download the artifact whose name ends in `-release`.
5. Install that APK over the previous **release-signed** installation.

The workflow verifies the APK signature and includes `signing-certificate.txt`
in the artifact so the signing certificate can be audited between releases.

## Database changes

Do not change the Room schema without a migration. For example, if database
version 2 becomes version 3, add `MIGRATION_2_3` and register it with
`addMigrations(...)`. Never add destructive migration fallback for production
user data.

## Important: existing debug installations

A debug APK that was signed by a different debug key cannot be upgraded directly
to the permanent release-signed APK. Android correctly rejects that signature
change.

For that one-time transition only:

1. In the currently installed app, create a **full JSON backup**.
2. Keep the backup outside the app's private storage.
3. Uninstall the old debug build.
4. Install the first permanently signed release APK.
5. Restore the full JSON backup.
6. Re-enter AI API keys (they are intentionally excluded from backups).

After this one-time transition, never install a debug APK over the production
installation. Future permanently signed release APKs can update in place and
retain the database automatically.
