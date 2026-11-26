# DIPESH AI

**Hindi-first Personal AI Assistant for Android**

DIPESH AI is being developed as a polished Android assistant for Hindi and mixed Hindi-English conversations, voice interaction, image understanding, and transparent user-triggered phone tools.

## What is in the repository now
- Kotlin Android app using Android Views, with role-labelled chat bubbles and persistent conversation rendering
- Gemini chat and image-analysis request code; visible AI connection status (offline, saved key, connecting, success, failure); API credentials encrypted using Android Keystore and sent in the `x-goog-api-key` header
- Configurable speech-recognition locale and cycling through voices installed on the device
- User-selected text/CSV file import into the message composer
- Optional Termux app launcher
- Hindi speech-recognition input and Text-to-Speech output
- Camera and gallery image selection
- Permission-based one-time screen capture for AI understanding
- Local conversation history and basic offline replies
- Android Settings and Dialer shortcuts
- Searchable installed-app picker with package-name hints; the user explicitly selects which app to open
- User-triggered Accessibility shortcuts (Back, Home, Notifications, Quick Settings)
- GitHub Actions debug-APK build workflow
- AI Agent Builder: collect an agent brief and generate a starter architecture, file tree, tool contracts, code, setup steps, and tests with Gemini; review, copy, or save the result.
- Website Builder: generate a responsive single-file HTML/CSS/JavaScript website with Gemini; review, copy, or save it as index.html.
- Images hub for camera, gallery, and user-approved screenshot analysis
- On-device Library for saved notes and imported text files
- Customize menu with persistent dark/light appearance preference
- Projects hub for reopening up to 20 saved AI agent and website outputs
- Scheduled alarm handoff to Android Clock with user review and confirmation
- Built-in Plugins directory for existing app integrations and shortcuts
- Remote section documenting secure pairing requirements; remote control is deliberately not enabled

> These items are present in source code; they are not a claim that every feature has passed device testing.

## Project direction
The goal is to turn the current foundation into a complete, polished Android application. The planned work is:
1. Redesign the home screen and chat interface.
2. Harden Gemini connection, settings, privacy, and offline/error handling.
3. Polish Hindi voice input and spoken replies.
4. Improve camera, gallery, and image understanding.
5. Verify permission-based screen understanding.
6. Improve safe, transparent, user-triggered phone automation.
7. Run builds and test the APK before release.

See [PROJECT_PLAN.md](PROJECT_PLAN.md) for the full product requirements, current baseline, privacy rules, and delivery roadmap. See [docs/ADVANCED_ASSISTANT_ARCHITECTURE.md](docs/ADVANCED_ASSISTANT_ARCHITECTURE.md) for the free-first architecture, planned modules, and capability boundaries.

## Build the debug APK
The repository has a GitHub Actions workflow:
1. Open the **Actions** tab.
2. Select **Build Dipesh AI APK**.
3. Choose **Run workflow**, or push a commit to main.
4. Wait for the workflow to succeed.
5. Download the **Dipesh-AI-debug-apk** artifact.

## API key and privacy
Gemini online features require your own API key. Keep it private: do not paste it into chat, commit it to GitHub, or include it in screenshots. The app stores it encrypted with Android Keystore; it is not stored in model memory. Screen understanding must remain user-initiated and use Android's system permission prompt.

## Project details
- Repository: dipeshpandeya41-cloud/Dipesh-AI
- Android application ID: com.dipesh.ai
- Language: Kotlin
- Current UI toolkit: Android Views
- Default Gemini model in the current source: gemini-2.5-flash

- Futuristic portrait Command Center inspired by the supplied reference: neon cyan panels, central AI core, system status, voice log, analytics status, and quick assistant actions.
- Main activity is locked to portrait orientation for a phone-first 9:16-style experience.

- Added a gently pulsing central AI core animation and a voice-log panel that reflects voice recognition request/results. The waveform graphic is decorative, not microphone amplitude telemetry.

- Free API Hub with working no-key integrations: Open-Meteo weather/geocoding, Frankfurter reference currency rates, and REST Countries country metadata.
- Public API requests use HTTPS, bounded connection/read timeouts, and GET-only requests; no extra provider key is stored for these three connectors.

- Added Nager.Date public holiday lookup by two-letter country code for the current year; no API key or paid subscription required. Uses the provider's public REST API and fair-use expectations.

- Free API Hub expanded with Wikipedia article search through the Wikimedia REST API, Open Library book/author metadata search, and Free Dictionary API English definitions/phonetics/examples. These are public no-key endpoints; usage policies and rate limits apply. Successful dictionary lookups are cached locally (up to 30 words) and can be reopened offline.

- Website ZIP export: generated website projects can be exported through Android's document picker as a ZIP containing `index.html`, a README, and an assets folder placeholder. No hosting service or paid dependency is required.

- Multi-file website generation: website output uses separate `index.html`, `styles.css`, and `script.js` files when appropriate. ZIP export preserves safe relative paths and includes a README.

- Free translation: optional MyMemory public translation endpoint with no API key, language-code inputs, a 1,500-character per-request guard, and a privacy warning. Public service limits and availability apply.

- Starter templates: AI Agent Development includes research, task planning, document Q&A, customer support, and website coding templates. Website Development includes portfolio, small business, storefront demo, blog, and landing-page templates. Templates prefill editable briefs; generation still requires the user's Gemini key and quota.
