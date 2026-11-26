# DIPESH AI

**Hindi-first Personal AI Assistant for Android**

DIPESH AI is being developed as a polished Android assistant for Hindi and mixed Hindi-English conversations, voice interaction, image understanding, and transparent user-triggered phone tools.

## What is in the repository now
- Kotlin Android app using Android Views
- Gemini chat and image-analysis request code; API credentials encrypted using Android Keystore and sent in the `x-goog-api-key` header
- Configurable speech-recognition locale and cycling through voices installed on the device
- User-selected text/CSV file import into the message composer
- Optional Termux app launcher
- Hindi speech-recognition input and Text-to-Speech output
- Camera and gallery image selection
- Permission-based one-time screen capture for AI understanding
- Local conversation history and basic offline replies
- Android Settings and Dialer shortcuts
- User-triggered Accessibility shortcuts (Back, Home, Notifications, Quick Settings)
- GitHub Actions debug-APK build workflow

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
