# MASTER PROJECT PROMPT — Android Hardware Button Automation & Voice Recorder

## 1. Project Role

Act as a **Senior Android Systems Engineer, Kotlin Architect, Accessibility API Specialist, Background-Service Engineer, and Root-Integration Expert**.

Build a production-quality Android application named:

**ButtonPilot — Hardware Button Automation**

The app is intended for the **device owner's own Android phone** and allows the user to assign actions to physical hardware-button patterns such as:

- Volume Down
- Volume Up
- Volume Up + Volume Down combinations
- Double press
- Triple press
- Long press
- Sequential press patterns

The highest-priority feature is:

> **Triple-press Volume Down to toggle voice recording.**
>
> - First triple press: start voice recording.
> - Second triple press: stop voice recording and save it.
> - The app should remain ready for button shortcuts while the phone is being used normally.
> - Recording must use Android's required microphone permission and foreground-service notification/recording indicator.
> - Never create a hidden/covert microphone recorder.

The project must be optimized for **low RAM usage, low battery drain, reliability, modularity, and modern Android compatibility**.

---

# 2. Main Technical Goal

Create a hardware-button automation engine capable of recognizing physical button patterns globally and triggering configurable actions.

The app should use the following hierarchy:

1. **Standard Android APIs first**
2. **Accessibility Service where appropriate**
3. **Foreground Service for active microphone recording**
4. **Optional Root Adapter for advanced user-owned-device functionality**
5. Root functionality must be isolated behind a dedicated interface and must never be required for normal application startup.

Do not use undocumented/private Android APIs unless there is no supported alternative.

If a requested behavior is blocked by an Android version, implement the closest reliable supported behavior and clearly document the limitation.

---

# 3. Technology Stack

Use:

- Kotlin
- Android Studio
- Gradle Kotlin DSL
- Jetpack Compose
- Material 3
- AndroidX
- Coroutines
- StateFlow / SharedFlow
- ViewModel
- Room Database
- DataStore Preferences
- MediaRecorder or an appropriate modern Android audio-recording API
- Notification Channels
- Foreground Services
- AccessibilityService
- WorkManager only for suitable deferred maintenance tasks
- Hilt dependency injection if it improves architecture
- Root shell integration as an OPTIONAL module

Recommended architecture:

**Clean Architecture + MVVM**

Suggested modules/packages:

```text
app/
core/
  common/
  permissions/
  storage/
  logging/
  root/
feature/
  dashboard/
  shortcuts/
  recorder/
  automation/
  settings/
  diagnostics/
service/
  accessibility/
  recording/
domain/
data/
```

---

# 4. Core Feature — Triple Volume Down Voice Recorder

Implement an extremely reliable state machine for this shortcut:

```text
VOLUME_DOWN
VOLUME_DOWN
VOLUME_DOWN
within configurable time window
```

Default timing:

- maximum interval between presses: 450 ms
- sequence timeout: 1100 ms
- ignore accidental key-repeat events
- process KEY_DOWN only when appropriate
- correctly pair/ignore KEY_UP events
- debounce duplicate events

Behavior:

### State A — Recorder stopped

Triple Volume Down:

```text
START RECORDING
```

### State B — Recorder running

Triple Volume Down:

```text
STOP RECORDING
SAVE FILE
UPDATE DATABASE
SHOW CONFIRMATION
```

The shortcut engine must prevent a single triple press from firing twice.

Create a testable state machine rather than putting timing logic directly inside the service.

Example conceptual component:

```kotlin
HardwareShortcutDetector
```

with input:

```kotlin
KeySignal(
    keyCode,
    action,
    eventTime,
    repeatCount
)
```

and output:

```kotlin
ShortcutEvent.TripleVolumeDown
```

---

# 5. Voice Recording System

Create a robust `RecordingManager`.

Required features:

- start recording
- stop recording
- pause/resume if supported
- elapsed duration
- recording state
- save metadata
- safe file finalization
- handle microphone errors
- handle storage errors
- handle incoming calls/audio interruptions gracefully
- prevent multiple simultaneous recording instances

Recording format:

Prefer one of:

- M4A / AAC
- another efficient Android-supported format

File naming:

```text
BP_REC_yyyy-MM-dd_HH-mm-ss.m4a
```

Suggested storage:

```text
Music/ButtonPilot/
```

or an app-controlled media directory using modern scoped-storage rules.

Metadata:

```text
id
filename
path/uri
createdAt
duration
size
sourceShortcut
```

Provide a recordings screen with:

- recordings list
- play
- pause
- rename
- share
- delete
- duration
- date/time
- file size

Use MediaStore where appropriate.

---

# 6. Foreground Recording Service

While microphone recording is active, run a proper foreground service.

Suggested class:

```text
AudioRecordingService
```

Declare the appropriate foreground-service type:

```text
microphone
```

The app must request the required permissions for the target Android version.

The recording notification should contain:

- "Recording in progress"
- elapsed time if practical
- Stop action
- Pause/Resume action if supported
- Open App action

Never disguise an active microphone recording.

---

# 7. Accessibility Service

Implement:

```text
ButtonAccessibilityService
```

Purpose:

- observe supported hardware key events
- send normalized events to `HardwareShortcutDetector`
- keep this service lightweight

Use `onKeyEvent()` when available and configure the service to request key filtering capability.

The Accessibility Service must:

- be explicitly enabled by the user
- display an onboarding explanation
- explain why hardware-key access is needed
- avoid reading screen text/content unless required for a feature
- request the minimum capabilities needed
- never collect passwords or private UI content

The shortcut system should continue operating when the app activity is not visible, subject to Android platform restrictions.

---

# 8. Optional Root Mode

Create an OPTIONAL advanced mode called:

```text
Root Enhanced Mode
```

This mode is only for the device owner's own rooted device.

Build:

```kotlin
interface RootController {
    suspend fun isRootAvailable(): Boolean
    suspend fun requestRoot(): RootResult
}
```

and:

```text
RootControllerImpl
```

Rules:

- normal app features must not depend on root
- request root only when the user explicitly enables Root Enhanced Mode
- show exactly what capability needs root
- log commands safely
- apply strict command allow-listing
- never accept arbitrary shell commands from remote sources
- never expose a network command shell
- never disable Android security features
- never attempt to hide recording/privacy indicators
- never silently grant itself microphone/accessibility privileges

Possible root-enhanced responsibilities:

- improve hardware-key monitoring on devices where standard APIs are insufficient
- inspect supported `/dev/input` devices only after explicit user opt-in
- run a minimal privileged helper if absolutely necessary
- recover monitoring after system-specific service termination

Any low-level input implementation must be device/version aware.

Do not hard-code assumptions about Linux input event numbers.

Create a compatibility abstraction:

```text
HardwareInputSource
    ├── AccessibilityInputSource
    └── RootInputSource
```

Only one source should be active at a time unless carefully deduplicated.

---

# 9. Hardware Button Shortcut Engine

Create a generic shortcut model.

Example:

```kotlin
data class ShortcutRule(
    val id: Long,
    val name: String,
    val trigger: TriggerPattern,
    val action: ShortcutAction,
    val enabled: Boolean
)
```

Supported trigger types:

```text
Single Press
Double Press
Triple Press
Long Press
Press-and-Hold
Volume Up -> Volume Down
Volume Down -> Volume Up
Volume Up x2
Volume Down x2
Volume Up x3
Volume Down x3
Both Volume Buttons
```

Allow configurable:

- number of presses
- time window
- long-press duration
- cooldown period
- whether original volume action should still occur

Where Android does not allow suppressing the original system behavior reliably, do not fake it; clearly document that limitation.

---

# 10. Built-In Actions

Add a safe collection of shortcut actions.

## Audio

- Start/Stop Voice Recording
- Toggle mute where Android permissions permit
- Open recorder library

## Utility

- Flashlight toggle
- Open selected application
- Open camera
- Create note
- Show current time
- Start timer
- Launch calculator
- Open notification shade where supported
- Open quick settings where supported

## Device

- Media play/pause
- Next track
- Previous track
- Adjust media volume
- Toggle Do Not Disturb only when proper policy access has been granted

## Communication

Optional user-configured actions may:

- open dialer with a predefined number
- open SMS composer with predefined contact/message

Do not silently send calls/messages without explicit user interaction unless a documented Android API and explicit user permission make that behavior appropriate.

---

# 11. Shortcut Conflict Protection

Implement:

```text
ShortcutConflictResolver
```

Example conflict:

```text
Volume Down x2
Volume Down x3
```

The engine must delay execution where necessary to determine whether another press is coming.

Use a finite-state-machine approach.

Requirements:

- no duplicate triggers
- no ghost triggers
- no accidental action from normal volume use
- configurable cooldown
- reset state after timeout
- reset after screen/device state changes when needed

---

# 12. Background Reliability

The app should be designed to remain dependable without continuously burning CPU.

Do NOT create:

```text
while(true)
```

polling loops.

Prefer event-driven architecture.

Use:

- Accessibility callbacks
- StateFlow
- BroadcastReceiver where legitimate
- foreground service only while a task truly requires it
- coroutines
- Room/DataStore
- WorkManager for deferred maintenance, not constant key monitoring

Provide a screen called:

```text
Background Reliability
```

It should explain optional OEM battery-optimization steps for brands that aggressively stop background apps.

Do not attempt to bypass OS protections invisibly.

---

# 13. Boot Behavior

Create:

```text
BootReceiver
```

Use it only for lightweight restoration such as:

- reloading preferences
- scheduling safe maintenance
- notifying the user that a required service needs reactivation if necessary

Respect current Android restrictions.

Do not assume a microphone foreground service can automatically start recording from `BOOT_COMPLETED`.

Never begin microphone recording automatically at boot.

---

# 14. Permission Center

Build a dedicated page:

```text
Permission Center
```

Cards:

### Microphone
Required for voice recorder.

### Notifications
Required/recommended for foreground-service notifications on supported Android versions.

### Accessibility
Required for supported global hardware-button detection.

### Battery Optimization
Optional explanation/settings shortcut.

### Do Not Disturb Access
Only for DND actions.

### Root
Optional.

Each card shows:

```text
Granted
Not Granted
Optional
Required
```

Provide a button to open the relevant Android Settings page.

---

# 15. Privacy Design

This is a device-owner automation utility, not a surveillance application.

Mandatory rules:

- no cloud upload by default
- recordings remain local unless user explicitly shares them
- no analytics containing microphone recordings
- no hidden recordings
- no remote activation of microphone
- no remote command/control system
- no stealth icon/notification hiding
- no recording another person without legally required consent
- provide a persistent visible indicator/notification while recording as required by Android
- add a Privacy page explaining permissions

---

# 16. Dashboard UI

Create a polished Material 3 dashboard.

Top section:

```text
ButtonPilot
Hardware Automation: ACTIVE
```

Cards:

```text
Accessibility Service
Root Enhanced Mode
Recording Service
Shortcuts Enabled
```

Primary quick-action card:

```text
TRIPLE VOLUME DOWN
Voice Recorder Toggle
Enabled
```

Add:

```text
Test Shortcut
```

button.

When pressed, open a test screen showing real-time recognized button events.

---

# 17. Shortcut Builder Screen

Create a visual flow:

```text
Choose Trigger
      ↓
Choose Pattern
      ↓
Choose Action
      ↓
Configure Options
      ↓
Test
      ↓
Save
```

Example:

```text
Trigger:
Volume Down

Pattern:
Triple Press

Action:
Toggle Voice Recording

Window:
1100 ms

Cooldown:
1500 ms
```

---

# 18. Recorder UI

Screen:

```text
Recorder
```

When idle:

```text
Ready
Triple Volume Down to start
```

When recording:

```text
● Recording
00:01:42

[Pause]
[Stop]
```

Also display:

```text
Triggered by: Volume Down ×3
```

---

# 19. Diagnostics Screen

Create a developer-friendly diagnostics page containing:

- Android version
- device manufacturer/model
- target SDK
- Accessibility status
- root availability
- notification permission
- microphone permission
- battery optimization state
- active input source
- current shortcut detector state
- foreground recorder state
- last 20 shortcut events
- last error

Allow:

```text
Export Diagnostic Log
```

Logs must never include recorded audio or sensitive screen content.

---

# 20. Database

Use Room.

Tables/entities:

```text
ShortcutRuleEntity
RecordingEntity
ActionHistoryEntity
```

Example history:

```text
timestamp
shortcutName
action
result
errorCode
```

Allow the history system to be turned off.

---

# 21. Settings

Settings page:

### Detection

- Double press interval
- Triple press interval
- Sequence timeout
- Long press duration
- Trigger cooldown

### Recorder

- Audio quality
- storage location
- filename template
- vibration feedback
- sound feedback

### Background

- battery guidance
- service diagnostics

### Advanced

- Root Enhanced Mode
- developer logs
- reset shortcut engine

---

# 22. Haptic Feedback

When a shortcut is successfully recognized:

- short vibration

Recording start:

- distinct vibration pattern

Recording stop:

- different vibration pattern

Respect system haptic settings where possible.

Provide toggle:

```text
Shortcut Vibration
ON/OFF
```

---

# 23. Screen-Off Behavior

Test shortcut handling in:

- screen on
- screen off
- lock screen
- another app open
- home screen
- media playing
- incoming call
- battery saver

Do not assume all Android/OEM versions permit the same behavior.

Build a compatibility result screen:

```text
Feature                     Status
Triple Volume Down          Supported
Screen-Off Detection        Supported / Limited
Root Input Adapter          Available / Unavailable
Background Recording Start  Platform Restricted
```

---

# 24. Android Version Compatibility

Target a modern Android SDK.

Create version-specific behavior for:

```text
Android 10
Android 11
Android 12
Android 13
Android 14
Android 15+
```

Pay special attention to:

- foreground-service launch restrictions
- microphone while-in-use rules
- notification permission
- scoped storage
- foreground-service types
- accessibility behavior
- background execution limits

Do not solve new Android restrictions by targeting an obsolete SDK.

---

# 25. Required Manifest Planning

Generate the manifest carefully.

Only request permissions actually used.

Likely permissions/capabilities may include, depending on target SDK/features:

```text
RECORD_AUDIO
FOREGROUND_SERVICE
FOREGROUND_SERVICE_MICROPHONE
POST_NOTIFICATIONS
VIBRATE
RECEIVE_BOOT_COMPLETED
```

Additional permissions should be added only for features that genuinely require them.

Declare the accessibility service correctly with XML metadata.

Never request dangerous permissions simply because they might be useful later.

---

# 26. Accessibility Service Configuration

Create an XML file similar conceptually to:

```text
res/xml/accessibility_service_config.xml
```

Request only necessary capabilities.

Hardware-key filtering should be configured using the appropriate accessibility flag/capability where supported.

The service should avoid retrieving window content unless another explicit feature requires it.

---

# 27. Recorder State Machine

Implement explicit states:

```text
Idle
Preparing
Recording
Paused
Stopping
Saved
Error
```

Transitions must be atomic.

Prevent:

```text
Idle -> Stop
Recording -> Start another recorder
Stopping -> Stop again
```

Use `Mutex`, atomic state, or a serialized event processor.

---

# 28. Shortcut Event Bus

Create a central event pipeline such as:

```text
Raw Key Event
      ↓
Input Normalizer
      ↓
Shortcut Detector
      ↓
Conflict Resolver
      ↓
Action Dispatcher
      ↓
Action Handler
```

Components:

```text
InputNormalizer
HardwareShortcutDetector
ShortcutConflictResolver
ShortcutActionDispatcher
RecordingActionHandler
UtilityActionHandler
```

Do not let AccessibilityService directly contain business logic.

---

# 29. Action Dispatcher

Use sealed classes.

Example:

```kotlin
sealed interface ShortcutAction {
    data object ToggleRecording : ShortcutAction
    data object ToggleFlashlight : ShortcutAction
    data class LaunchApp(val packageName: String) : ShortcutAction
    data object MediaPlayPause : ShortcutAction
}
```

Dispatcher:

```text
ShortcutActionDispatcher
```

must:

- validate permission
- validate current state
- execute action
- return success/failure
- write optional history
- provide user feedback

---

# 30. Reliability Requirements

The app must survive normal lifecycle changes.

Handle:

- activity recreation
- process restart
- permission revoked
- accessibility disabled
- recorder failure
- storage unavailable
- root revoked
- service killed
- configuration changes

Persist only the state that makes sense.

Never claim recording is active unless recorder/service state confirms it.

---

# 31. Performance Requirements

Goal:

```text
near-zero CPU while no hardware events occur
```

Avoid:

- continuous polling
- wake locks kept forever
- high-frequency timers
- unnecessary location/network services
- constantly writing logs to disk

Use lightweight event processing.

Cap in-memory log buffers.

---

# 32. Battery Strategy

The app should NOT attempt to keep the CPU awake 24/7.

Instead:

```text
System callback
        ↓
Process event quickly
        ↓
Return to idle
```

Only microphone recording should run a dedicated foreground service for the duration of the recording.

If an OEM kills required services, explain battery-optimization settings to the user.

---

# 33. Security

Mandatory:

- no open exported service unless required
- `android:exported` values explicitly set
- protect internal broadcast receivers
- prefer explicit intents
- sanitize all external intent data
- do not expose arbitrary shell execution
- never execute a root command built from untrusted text
- encrypt sensitive preferences when truly necessary
- no credentials in source code
- no secret keys in repository

---

# 34. Root Command Safety

If root shell is implemented:

Create:

```text
RootCommandExecutor
```

with a strict enum/allow-list.

Incorrect:

```kotlin
exec(userSuppliedString)
```

Correct conceptual design:

```kotlin
execute(RootAction.StartInputMonitor)
execute(RootAction.StopInputMonitor)
```

Translate trusted predefined actions into commands internally.

---

# 35. Root Helper Architecture

If low-level event monitoring is necessary for a rooted device, create a minimal helper architecture:

```text
RootInputMonitor
      ↓
Normalized local IPC
      ↓
Android app process
```

Requirements:

- local-only communication
- authenticated/permission-restricted channel
- no TCP listener
- no external network interface
- stop helper when Root Enhanced Mode is disabled
- clear logs/status in UI

If accessibility already solves the device's button detection reliably, prefer accessibility instead of root.

---

# 36. Testing

Write:

### Unit Tests

For:

- double-press recognition
- triple-press recognition
- timing boundaries
- timeout reset
- key repeat filtering
- overlapping shortcut patterns
- cooldown
- recorder state transitions

Example test:

```text
DOWN @ 0 ms
DOWN @ 270 ms
DOWN @ 520 ms

Expected:
TripleVolumeDown
```

Example timeout:

```text
DOWN @ 0 ms
DOWN @ 300 ms
DOWN @ 1600 ms

Expected:
NoTriplePress
```

### Instrumentation Tests

Test:

- permissions
- database
- recorder service
- notification actions
- settings persistence

---

# 37. Manual Test Matrix

Create a table for:

```text
Android Version
OEM
Accessibility
Root
Screen On
Screen Off
Locked
Music Playing
Battery Saver
Result
```

Include test cases for:

- Google Pixel-style Android
- Samsung
- Xiaomi/Redmi
- Realme/Oppo
- Vivo
- Motorola

Treat OEM-specific results as test outcomes, not assumptions.

---

# 38. User Onboarding

First launch:

```text
Welcome to ButtonPilot
```

Step 1:

```text
What the app does
```

Step 2:

```text
Microphone permission
```

Step 3:

```text
Enable Accessibility shortcut detection
```

Step 4:

```text
Notification permission
```

Step 5:

```text
Test triple Volume Down
```

Step 6:

```text
Optional Root Enhanced Mode
```

Do not present root as mandatory.

---

# 39. Default Shortcut Presets

Ship with these disabled/enabled examples:

```text
Volume Down ×3
→ Toggle Voice Recorder
→ ENABLED

Volume Up ×2
→ Flashlight Toggle
→ DISABLED

Volume Up ×3
→ Open Camera
→ DISABLED

Volume Down ×2
→ Media Play/Pause
→ DISABLED

Volume Up then Volume Down
→ Launch selected app
→ DISABLED
```

---

# 40. Recording Start Edge Case

Because modern Android may restrict starting a microphone foreground service while the app is fully backgrounded, architect the app to detect the platform/version state and:

1. use any officially permitted path that applies,
2. otherwise show an immediate high-priority app notification/action asking the user to start recording,
3. never crash,
4. never silently fail,
5. never attempt to bypass microphone privacy restrictions.

If Root Enhanced Mode is active, it still must not be used to remove Android microphone privacy indicators or create covert recording.

---

# 41. Error Messages

Use clear errors.

Examples:

```text
Microphone permission is required.
Accessibility service is disabled.
Recording could not start because Android blocked background microphone access.
Storage is unavailable.
Root access was denied.
Shortcut conflict detected.
```

Include a direct Fix button wherever possible.

---

# 42. UI Style

Design:

- dark-friendly
- Material 3
- modern cards
- large status indicators
- simple animations
- professional typography
- responsive layout
- no clutter

Suggested colors can use the system dynamic Material theme.

Status concepts:

```text
Green-style state: Active
Warning-style state: Limited
Error-style state: Permission Missing
```

Do not hardcode colors if Material theme semantics can be used.

---

# 43. Code Quality

Every major component must have:

- interface where useful
- clear responsibility
- KDoc for non-obvious behavior
- error handling
- logging abstraction
- test coverage for timing/state logic

Avoid giant classes.

Maximum responsibility:

```text
one major concern per class
```

---

# 44. Deliverables

Generate the project in logical phases.

## Phase 1 — Foundation

Provide:

- package architecture
- Gradle setup
- manifest
- theme
- navigation
- Room
- DataStore
- permission manager

## Phase 2 — Accessibility Input

Provide:

- AccessibilityService
- service configuration
- event normalization
- triple-button detector
- test screen

## Phase 3 — Recorder

Provide:

- RecordingManager
- foreground service
- notification
- storage
- recordings database
- recording list UI

## Phase 4 — Automation

Provide:

- shortcut rule system
- action dispatcher
- shortcut builder
- conflict resolver

## Phase 5 — Root Enhanced Mode

Provide:

- root detection
- user consent screen
- isolated root controller
- optional root input adapter
- compatibility checks

## Phase 6 — Optimization

Provide:

- performance audit
- battery audit
- lifecycle audit
- error handling
- tests

---

# 45. AI Coding Instructions

When generating code:

1. Never output pseudocode when production Kotlin code can be written.
2. Include complete imports.
3. Mention exact file paths before each file.
4. Never omit required Manifest/XML entries.
5. Never say "rest of code here".
6. Never leave placeholder functions for core logic.
7. Keep code compilable after every phase.
8. Explain where every file belongs.
9. If an Android API differs by SDK version, include the version check.
10. Use current supported Android APIs.
11. Do not rely on deprecated approaches unless there is a documented compatibility reason.
12. Include ProGuard/R8 rules if a dependency needs them.
13. Include all Gradle dependencies and versions using a version catalog if appropriate.
14. Include unit tests for the button sequence detector before proceeding to UI.
15. Treat microphone and Accessibility as privacy-sensitive capabilities.

---

# 46. Mandatory Acceptance Tests

The project is not considered complete until these pass.

### Test A

```text
Press Volume Down 3 times rapidly.
```

Expected:

```text
Recording begins or, when Android background microphone policy prevents direct start,
the official user-visible start flow is triggered.
```

### Test B

While recording:

```text
Press Volume Down 3 times rapidly.
```

Expected:

```text
Recording stops.
File is finalized.
File appears in Recordings.
```

### Test C

```text
Press Volume Down only once.
```

Expected:

```text
No recorder action.
```

### Test D

```text
Press Volume Down twice.
Wait beyond timeout.
Press once.
```

Expected:

```text
No recorder action.
```

### Test E

```text
Use the normal phone for 30 minutes.
```

Expected:

```text
No noticeable CPU or battery spike from shortcut detection.
```

### Test F

```text
Revoke microphone permission.
Trigger shortcut.
```

Expected:

```text
No crash.
Explain permission requirement.
```

### Test G

```text
Disable Accessibility.
```

Expected:

```text
Dashboard shows detection unavailable and provides a settings shortcut.
```

---

# 47. Final Engineering Principle

Build this as a serious **device-owner hardware automation utility**, not as a hidden monitoring application.

The implementation should achieve the strongest reliable hardware-button automation Android permits while respecting:

- Android privacy rules
- foreground-service requirements
- microphone indicators
- user-granted Accessibility access
- battery efficiency
- rooted-device safety
- OEM differences

The application's signature feature is:

```text
VOLUME DOWN ×3
        ↓
TOGGLE VOICE RECORDING
```

Everything else should be architected around a modular, extensible shortcut engine so additional hardware-button automations can be added later without rewriting the core system.

---

# 48. Start Command for the Coding AI

Begin by generating:

1. complete project architecture,
2. package/file tree,
3. Gradle configuration,
4. Manifest,
5. Accessibility service XML,
6. `HardwareShortcutDetector`,
7. unit tests for triple Volume Down,
8. permission manager,
9. foreground `AudioRecordingService`,
10. `RecordingManager`.

Then proceed phase-by-phase.

Before each phase, briefly explain what will be created. After each phase, provide a checklist showing which files compile and what should be tested on a real Android phone.
