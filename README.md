# DIPESH AI

**Hindi-first Personal AI Assistant for Android**

DIPESH AI is being developed as a polished Android assistant for Hindi and mixed Hindi-English conversations, voice interaction, image understanding, and transparent user-triggered phone tools.

## What is in the repository now
- Kotlin Android app using Android Views, with role-labelled chat bubbles and persistent conversation rendering
- Gemini chat and image-analysis request code; visible AI connection status (offline, saved key, connecting, success, failure); API credentials encrypted using Android Keystore and sent in the `x-goog-api-key` header
- Configurable speech-recognition locale, an installed TTS-language picker, and cycling through voices installed on the device
- User-selected text/CSV file import into the message composer
- Optional Termux app launcher
- Hindi speech-recognition input and Text-to-Speech output
- Camera and gallery image selection
- Permission-based one-time screen capture for AI understanding
- Offline-first mode toggle with local arithmetic (including parentheses), percentage calculations, common length/mass/volume/temperature conversions, device-local date/time, age calculations from a birth date, date-difference calculations, built-in app guidance, greetings, and explicit disclosure that this is not a downloaded local LLM
- Android Settings and Dialer shortcuts
- Searchable installed-app picker with package-name hints; the user explicitly selects which app to open
- User-triggered Accessibility shortcuts (Back, Home, Notifications, Quick Settings)
- GitHub Actions debug-APK build workflow
- AI Agent Builder: collect an agent brief and generate a starter architecture, file tree, tool contracts, code, setup steps, and tests with Gemini; review, copy, or save the result.
- Website Builder: generate a responsive single-file HTML/CSS/JavaScript website with Gemini; review, copy, or save it as index.html.
- Images hub for camera, gallery, and user-approved screenshot analysis
- On-device Library for saved notes and imported text files with offline keyword search
- Offline task manager: add up to 100 local tasks, mark them complete, and clear completed items
- Local scheduled reminders: choose a date/time and receive an Android notification; completing a reminder task cancels its pending alarm
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


- Offline calculator additions: percentage-of calculations and deterministic conversions for common length (km/mi/m/ft/cm/in), mass (kg/lb/g/oz), volume (L/mL), and temperature (°C/°F) units. No network or API key is used; this is not a live currency converter.


- Offline date utilities use the Android device's local clock and support age calculations and day differences for `YYYY-MM-DD` or `DD/MM/YYYY` dates. They require no network or API key; age/date outputs depend on the device clock.


## Local reminders
The Tasks hub can schedule a device-local reminder using Android's inexact alarm API and notification channel. Android 13+ requires notification permission. Reminders are stored in app preferences and are scheduled locally without a server, API key, paid service, or exact-alarm special access. Android may defer inexact alarms, and pending alarms are restored after the device finishes booting, when notification permission is available. Opening the app also retries restoration. Completing a reminder task cancels its pending alarm. Verify notification permission and battery settings on the target device.


## Task management and recurring reminders
Tasks can now be edited or deleted from the Tasks hub. Reminder notifications include a **Snooze 10 min** action that updates the saved due time and schedules the reminder again. Reminders can be scheduled once or repeated daily; daily reminders schedule their next occurrence after delivery. All scheduling remains local and uses inexact Android alarms, so Android battery management may delay delivery. Completing a task cancels its pending reminder. Reboot restoration uses the system boot broadcast and retries on app startup.


## Reminder timing improvements
Daily reminders retain their original local clock time across deliveries instead of drifting forward by 24 hours from the notification delivery time. Snoozing changes only the next due time and preserves the daily schedule anchor. Editing a reminder title refreshes its pending alarm so the notification uses the updated title. Existing daily reminders without an anchor are upgraded when their next occurrence is scheduled.


## Complete reminders from notifications
Reminder notifications now provide a **Mark done** action alongside **Snooze 10 min**. Marking a reminder done updates its local task record, cancels its next scheduled occurrence (including daily repeats), and dismisses the notification. This action works locally without an AI API or network connection.


## Offline task priorities
Tasks and reminders now carry a local priority label: LOW, MEDIUM, or HIGH. New tasks and reminders default to MEDIUM. Open a task and choose **Change priority** to update it; the task list displays the priority beside its title. Priority data is stored with the existing on-device task records and requires no network or AI API.


## Offline task filters
The Tasks screen includes local filters for all tasks, active tasks, completed tasks, and unfinished HIGH-priority tasks. Filter selection is applied on-device and preserves each task's existing edit, completion, priority, delete, and reminder actions.


## Offline task search
Search task titles directly in the local Tasks screen. Search can be combined with the All, Active, Completed, and High priority filters; clearing the search restores the current filter's full results. Search is on-device and requires no network or API.


## Offline task sorting
The Tasks screen supports local sorting by newest first, due date (soonest first), and priority (high to low). Sorting works together with the existing title search and task filters; tasks without a due date appear after dated tasks. No network access or paid API is required.


## Offline task backup
From the Tasks screen, choose **Export all tasks to CSV** and save through Android's document picker. The export includes task title, completion status, priority, due time, reminder ID, daily-repeat setting, and creation time. CSV values are escaped for spreadsheet compatibility. The export is generated locally; no account, network, or paid API is required.


### Restore an offline task backup
In Tasks, choose **Import task CSV backup** and select a CSV previously exported by DIPESH AI. The importer validates the expected header, handles quoted commas and escaped quotes, skips duplicate tasks, respects the 100-task limit, and restores future reminders with fresh local reminder IDs. Imported completed or expired reminders are not rescheduled. The operation is local-only.
