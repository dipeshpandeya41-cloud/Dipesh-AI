# Advanced assistant architecture and capability boundaries

This project is designed as a free-first Android assistant. "Free-first" means choosing open-source/local components and provider free tiers where available; it cannot guarantee that every cloud API, model, speech service, or connected device will remain free forever.

## Implemented in this foundation branch

- Gemini API key encrypted locally with an AES-GCM key held by Android Keystore.
- Legacy plaintext API-key preference is migrated to encrypted storage on app startup and then removed.
- Gemini API key is sent in the `x-goog-api-key` request header, not in the URL.
- Configurable speech-recognition locale (for example, `hi-IN` or `en-US`).
- Cycle through TTS voices already installed on the Android device. Availability and quality depend on installed speech engines and downloaded voice data.
- User-selected text/CSV file picker with a 50,000-character preview inserted into the composer; the user reviews and sends it.
- Termux launcher shortcut. This opens Termux only; it does not silently execute shell commands or install packages.

## Planned modules

### AI and memory
- Provider interface so local/offline and optional free-tier cloud providers can be swapped.
- User-visible local memory with view, edit, export, and clear controls.
- Conversation summarization with explicit user control; no API key or secret is stored in model memory.
- Bounded retries, timeouts, cancellation, quota-aware errors, and offline fallback.

### Voice
- Installed Android TTS voices, locale selection, and optional speech-engine settings.
- Speech recognition via Android recognizer providers.
- Optional offline engines where the user installs a compatible engine and language pack.
- No promise that every language or voice exists on every phone.

### Files and spreadsheets
- Android Storage Access Framework for user-selected files.
- CSV import/export first; XLSX support requires a compatible open-source library and device testing.
- Excel/Sheets typing should use an explicit user-triggered accessibility or input-method flow. The app must not type into an app without the user's visible action.
- Never scrape or upload all local files. Only process files the user selects.

### Termux and coding agent
- Optional Termux hand-off for user-authored scripts.
- Repository operations through GitHub APIs with branch/commit/PR review.
- Downloaded skills are treated as untrusted data: inspect manifests and source, show permissions, and require confirmation before execution. No arbitrary remote code auto-execution.
- GitHub tokens stay in Android Keystore-backed storage and are never committed.

### Phone, computer, camera, and smart devices
- Android app launching and system shortcuts only through documented intents and user-approved accessibility actions.
- Screen capture only after Android's MediaProjection consent prompt, for a one-time capture.
- External computer, camera-feed, TV, and smart-home control requires a compatible paired agent/device, network protocol, and explicit pairing/permissions. There is no universal Android API that controls every computer or TV.
- Sensitive actions such as calls, messages, purchases, account changes, and settings changes require a clear confirmation.

## Delivery gates

1. Compile and lint in GitHub Actions.
2. Review workflow logs and fix build failures.
3. Download the debug APK artifact and test on the target Android phone.
4. Test API-key migration, voice locale/voice switching, file selection, screen consent, and accessibility behavior on-device.
5. Mark each capability complete only after implementation and device verification.

## Privacy and cost

- No paid dependency is required by the current Android foundation.
- Cloud AI still requires an internet connection and a provider API key; provider free quotas, availability, and terms can change.
- Keep secrets out of source control, chat messages, logs, screenshots, and crash reports.
- Request only the Android permissions needed for the feature the user starts.
