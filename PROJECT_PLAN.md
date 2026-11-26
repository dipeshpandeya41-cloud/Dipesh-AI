# DIPESH AI — Product Plan

## Product vision
DIPESH AI is a Hindi-first personal AI assistant for Android. The goal is a polished app for text and voice chat, image and user-approved screen understanding, and explicitly requested phone tools.

The app should be usable from an Android phone without a PC. Cloud AI features require the user's own Gemini API key and internet access. Basic help and phone shortcuts should remain useful offline.

## Current repository baseline
The current source tree contains:
- Native Android application written in Kotlin using Android Views.
- Gemini generateContent request code with a configurable model (default: gemini-2.5-flash), plus a visible connection status for offline, saved-key, connecting, success, and failure states.
- Android Keystore-backed AES-GCM storage for the Gemini API key; requests use the `x-goog-api-key` header.
- Configurable speech-recognition locale, installed TTS voice cycling, a user-selected text/CSV file picker, and a Termux launcher shortcut.
- Hindi speech recognition intent and Android Text-to-Speech output.
- Camera capture and gallery image selection followed by Gemini image analysis.
- One-time screen capture using Android MediaProjection consent before analysis.
- Local conversation-history persistence using app preferences.
- Basic offline replies when no API key is configured.
- Phone Settings and Dialer shortcuts.
- Searchable installed-app picker lists launchable apps with package-name hints; the user explicitly selects which app to open.
- Accessibility Service with user-invoked Back, Home, Notifications, and Quick Settings actions.
- GitHub Actions workflow that builds a debug APK and uploads it as an artifact.

These are observations from source code, not a claim that every feature has passed physical-device testing.

## Product requirements

### 1. Polished home and chat experience
- Clear app header, connection/status indicator, role-labelled conversation bubbles, persistent conversation rendering, and simple message composer.
- Keep voice, camera, gallery, screen understanding, and settings easy to reach.
- Show loading, empty, offline, and error states clearly.
- Provide controls to clear local chat history and open AI settings.

### 2. Hindi-first AI conversation
- Support Hindi, English, and mixed Hindi-English messages.
- Keep the Gemini model configurable.
- Handle API errors and network loss with understandable messages and offline fallback.
- Never ask users to paste an API key into a chat conversation.

### 3. Voice
- Hindi speech-to-text input.
- Text-to-speech playback of assistant responses.
- Clear microphone permission handling and visible speech-recognition state.

### 4. Image understanding
- Take a photo or choose an image from the gallery.
- Resize and compress images before sending them to Gemini.
- Show what is being sent, analysis progress, and errors.

### 5. User-approved screen understanding
- Request Android MediaProjection permission for each screen-capture session as required by Android.
- Capture only after the user explicitly starts the action and approves the system prompt.
- Send the captured frame for analysis, then stop the capture service.
- Do not silently record, continuously monitor, or transmit the screen in the background.

### 6. Phone tools and automation
- Provide shortcuts to Android Settings and the Dialer.
- Let the user open an installed app through Android's system app chooser; do not silently launch or control an arbitrary app.
- Keep Accessibility actions transparent and user-triggered.
- Ask for relevant Android permissions before actions that need them.
- Do not send messages, place calls, change settings, or make purchases without a clear user action and appropriate system confirmation.

### 7. Privacy and reliability
- Keep API credentials on-device; never commit secrets to Git.
- Avoid placing API keys in request URLs. Prefer supported authentication headers.
- Explain what information is sent to the AI service before image or screen analysis.
- Keep local history under the user's control and provide a clear-history action.
- Handle missing permissions, unsupported devices, network errors, and API quota errors gracefully.

### 8. Build and release
- Build a debug APK with GitHub Actions.
- Verify workflow results before describing an APK as ready.
- Add release signing only through secure secrets; never store signing keys in the repository.
- Test on a real Android device before marking a feature complete.

## Delivery roadmap
1. Foundation and audit — document the app, confirm package/build settings, and identify gaps.
2. Home and chat redesign — make the main screen responsive and easier to use on phones.
3. AI connection hardening — improve API errors, credential handling, model settings, and offline states.
4. Voice and image experience — polish speech input/output, camera, gallery, and image analysis.
5. Screen understanding — verify permission-based one-shot capture across supported Android versions.
6. Automation — expand only safe, transparent, user-triggered Android actions.
7. Testing and APK delivery — run GitHub Actions, fix build failures, and verify the APK artifact.


### 9. AI agent and website development
- Provide an AI Agent Builder for a project brief, runtime, stack, required capabilities, architecture, tool contracts, starter code, setup, and tests.
- Provide a Website Builder that generates a responsive, accessible starter website with HTML, CSS, and JavaScript.
- Let users review generated output, copy it, or save it through Android's file picker.
- Treat generated code as untrusted until reviewed and tested. Never execute generated code automatically or embed API secrets in browser code.
- Keep the first website export self-contained as index.html; multi-file projects and direct hosting/deployment remain future work.


### 10. Reference-image navigation
- **Images:** provide shortcuts to camera, gallery, and user-approved one-time screenshot analysis.
- **Library:** store notes locally and import user-selected text files.
- **Customize:** persist a dark/light appearance preference.
- **Projects:** reopen up to 20 saved AI agent and website outputs.
- **Remote:** show capability status and secure-pairing requirements; do not expose the device or accept remote commands in this build.
- **Scheduled:** hand a labelled, user-selected alarm time to Android Clock; the user reviews/confirms it there.
- **Plugins:** list existing built-in integrations and route each item to its corresponding feature; no arbitrary third-party plugin code is executed.


### 11. Portrait Command Center UI
- Add a phone-first futuristic dashboard inspired by the supplied visual: dark navy surface, cyan neon borders, central AI core, system status, voice log, analytics status, and assistant option tiles.
- Lock the main activity to portrait orientation for a 9:16-style mobile experience. The interface adapts to the device's actual available height; it is not a fixed-resolution 9:16 canvas.
- Show only real app state. Predictive analytics is labelled as not connected until a real data source is implemented.


### 12. Command Center motion and voice feedback
- Animate the central AI core with a subtle scale pulse and cancel animators when the activity is destroyed.
- Add voice-log feedback when speech recognition is requested and when recognized text is returned. The waveform glyph is decorative; no live microphone amplitude is claimed or recorded.

## Build
The repository includes a GitHub Actions workflow at .github/workflows/android.yml. Open the Actions tab, select **Build Dipesh AI APK**, and run it manually, or push a commit to main. Download the Dipesh-AI-debug-apk artifact only after the workflow succeeds.

## Important status note
The roadmap describes intended work. A feature should be called complete only after its implementation and build/device testing have been verified.


## Advanced assistant feature targets

The broader feature request is tracked in [docs/ADVANCED_ASSISTANT_ARCHITECTURE.md](docs/ADVANCED_ASSISTANT_ARCHITECTURE.md). It covers provider switching, user-controlled memory, multilingual installed voices, selected-file access, CSV/XLSX workflows, Termux, GitHub coding workflows, safe skill loading, Android automation, and paired computer/TV/smart-device integrations. These are not all implemented yet. Each item must pass build and real-device verification before being marked complete.
