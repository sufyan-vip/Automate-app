# MANZIL — Master Build Prompt

> **HOW TO USE THIS FILE (Roman Urdu):**
> 1. Ek **naya GitHub repo** banayein (khaali, sirf README ke saath) — naam: `manzil` (ya jo pasand ho).
> 2. Naya Arena/Claude/Cursor session kholein aur us repo ko connect karein.
> 3. **Is poori file ka content copy karke** naye session mein paste karein (ya file ko attach karein).
> 4. Bas. Naya agent Milestone M0 se khud shuru kar dega — aapko dobara kuch explain nahi karna.
>
> **Zaroori:** Yeh file *self-contained* hai. Ismein user ka goal, poora feature spec, database schema, OpenRouter integration, notification copy, aur GitHub Actions YAML (copy-paste ready) sab kuch hai.

---

## 1. YOUR ROLE AND MISSION

You are the **founding engineer** of a new Android application called **Manzil** (meaning "destination").

You are not writing a plan. You are **building and shipping the app**. Your first output must be a compiling Gradle project on disk, not a description of one.

**Mission in one sentence:** Build an offline-first Android app that turns one person's multi-year life goal (opening a software house in Sahiwal, Pakistan, by 2030) into a **live, daily-tracked system** — with a real-time calendar, instant full-text search, an OpenRouter-powered AI coach, goal-change highlighting, and daily notifications that tell the user exactly *what to do today, what got done, and what is still left*.

---

## 2. OPERATING RULES (NON-NEGOTIABLE)

1. **Do not ask clarifying questions.** When something is ambiguous, pick the sensible default, implement it, and record the decision in `DECISIONS.md` with a one-line rationale. Asking questions wastes the user's turns.
2. **Ship incrementally.** Work milestone by milestone (M0 → M7). Commit after every milestone with a descriptive message.
3. **Never claim something works without running it.** Before saying "done", run `./gradlew assembleDebug` and `./gradlew testDebugUnitTest`, and name the class/function your test actually executed. A green exit code with wrong output is not a pass.
4. **Offline-first, always.** The app must be 100% usable with zero network. OpenRouter is an *enhancement*, never a dependency. Every AI feature must degrade gracefully with a clear "AI unavailable — add your API key in Settings" state.
5. **Privacy is a feature.** All user data stays in local Room storage. The only network call the app ever makes is to `https://openrouter.ai/api/v1/*` with the user's own key. No analytics, no crash reporting SDK, no telemetry, no ads.
6. **Secrets:** the OpenRouter API key is stored in `EncryptedSharedPreferences` (AndroidX Security Crypto, Keystore-backed) — never in plain DataStore, never logged, never committed, never included in backups/exports.
7. **Reactive everywhere.** All lists and dashboards are backed by Room `Flow`/`StateFlow`. No manual `refresh()` buttons for local data.
8. **English + Urdu (Roman + Nastaliq).** Every user-facing string goes in `strings.xml` with an `values-ur/strings.xml` counterpart. No hardcoded strings in Composables.
9. **Keep the build green.** If a milestone cannot compile, fix it before moving on. Do not leave `TODO()` stubs that crash at runtime — use graceful empty states.
10. **Write tests for logic, not for UI pixels.** Mandatory unit tests: recurrence expansion, goal-diff engine, search tokeniser, notification-copy builder, briefing aggregator, OpenRouter request/response mapping.

---

## 3. USER CONTEXT (WHY THIS APP EXISTS)

The owner of this app is a **first-year university student in Sahiwal, Punjab, Pakistan** (session starting 2026). Their documented, dated plan is:

| Phase | When | Goal |
|---|---|---|
| Phase 0 | Uni Year 1–2 (2026–28) | Learn a money stack, land first clients, NTN + PSEB freelancer registration |
| Phase 1 | Uni Year 3 (2028–29) | Retainer clients, first junior hires, white-label partnership |
| Phase 2 | Uni Year 4 (2029–30) | Incorporate Pvt Ltd, ESFCA account, build launch runway |
| Phase 3 | 2030 | Launch a 5-seat office (own building) + co-working zone |
| Phase 4 | 2031–33 | Scale to 10–14 seats, Rs 40–50 lakh/month revenue |

**Hard KPI targets the app must track as first-class objects:**

| KPI | Target | Unit |
|---|---|---|
| Savings at graduation | 55,00,000 | PKR |
| Launch business capital needed | 59,78,000 | PKR |
| Freelance income by end of Year 1 | 50,000 / month | PKR |
| Freelance income by end of Year 2 | 1,20,000 / month | PKR |
| MRR before launch | 3,50,000 / month | PKR |
| International retainer clients | 3 | count |
| Local AMC clients | 10 | count |
| Team size at launch | 5 | count |
| GitHub projects shipped | 20 | count |
| Upwork reviews | 5 | count |
| CGPA floor | 3.0 | GPA |
| Deep-work hours per week | 25 | hours |

**The psychological job this app does:** the user is 4 years away from launch. Motivation decays. The app must make the long goal feel *alive every single day* — showing movement, calling out slippage, and ending each day with an honest scorecard.

---

## 4. PRODUCT SPEC — SCREEN MAP

```
Bottom nav (4 tabs) + FAB
├── TODAY        → daily briefing, focus task, live timer, streak, quick capture
├── CALENDAR     → Day / 3-Day / Week / Month / Agenda, drag-to-reschedule
├── GOALS        → goal tree, progress rings, KPI trackers, GOAL PULSE (diff)
└── SEARCH       → global instant search (also reachable via top-bar icon on every screen)

Secondary (from top bar / settings)
├── AI COACH     → chat + "plan my day" + "weekly review" + "explain what changed"
├── JOURNAL      → daily note, mood, wins, blockers
├── TIME         → time-tracking ledger, per-goal rollup
└── SETTINGS     → OpenRouter key, notifications, theme, language, backup/restore
```

---

## 5. TECH STACK (EXACT — DO NOT SUBSTITUTE)

| Concern | Choice |
|---|---|
| Language | Kotlin 1.9.22 (JVM target 17) |
| UI | Jetpack Compose + Material 3, Compose BOM 2024.06.00 |
| Build | Gradle 8.6, AGP 8.3.2, version catalog (`gradle/libs.versions.toml`) |
| DI | Hilt 2.51 + KSP 1.9.22-1.0.17 |
| Persistence | Room 2.6.1 (KSP), **FTS4 virtual table for search** |
| Prefs | DataStore Preferences 1.0.0 (non-secret) + AndroidX Security Crypto 1.1.0-alpha06 (secrets) |
| Background | WorkManager 2.9.0 + `AlarmManager.setExactAndAllowWhileIdle` for time-critical notifications |
| Nav | Navigation Compose 2.7.7 |
| Async | kotlinx.coroutines 1.8.0 + Flow |
| Network | OkHttp 4.12.0 + kotlinx.serialization-json 1.6.3 (**no Retrofit needed** — one endpoint family) |
| Date/time | java.time (API 26+; `minSdk = 26` so no desugaring required) |
| Recurrence | Implement RFC-5545 subset yourself (see F6) — no heavy dependency |
| Charts | Vico 1.13.1 (Compose charts) |
| Testing | JUnit 4.13.2, Turbine 1.1.0, kotlinx-coroutines-test, Room testing, MockWebServer 4.12.0 |
| Lint/format | ktlint Gradle plugin 12.1.0 + Android Lint |
| SDKs | `minSdk = 26`, `compileSdk = 34`, `targetSdk = 34` |
| Package | `com.manzil.app` |

---

## 6. PROJECT STRUCTURE (CREATE EXACTLY THIS)

```
manzil/
├── .github/workflows/
│   ├── build-apk.yml          # see §14
│   ├── ci.yml                 # see §14
│   └── release.yml            # see §14
├── .gitignore
├── README.md
├── DECISIONS.md               # running log of every default you chose
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── gradle/wrapper/gradle-wrapper.properties
├── gradlew / gradlew.bat / gradle/wrapper/gradle-wrapper.jar
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/values/{strings.xml,colors.xml,themes.xml}
        │   ├── res/values-ur/strings.xml
        │   ├── res/xml/{backup_rules.xml,data_extraction_rules.xml}
        │   └── java/com/manzil/app/
        │       ├── ManzilApp.kt                  # @HiltAndroidApp, WorkManager config
        │       ├── MainActivity.kt
        │       ├── ManzilNavGraph.kt
        │       ├── core/
        │       │   ├── common/{Result.kt,TimeProvider.kt,Dispatchers.kt,Constants.kt}
        │       │   ├── crypto/SecretVault.kt     # EncryptedSharedPreferences wrapper
        │       │   ├── logging/AppLog.kt
        │       │   ├── recurrence/{RRule.kt,RRuleParser.kt,RRuleExpander.kt}
        │       │   ├── diff/{JsonDiff.kt,DiffEntry.kt}
        │       │   └── markdown/RoadmapImporter.kt
        │       ├── data/
        │       │   ├── local/
        │       │   │   ├── ManzilDatabase.kt
        │       │   │   ├── entity/{Goal,GoalRevision,Milestone,Task,TaskInstance,TimeEntry,
        │       │   │   │            CalendarEvent,JournalEntry,DailyReview,Habit,HabitLog,
        │       │   │   │            KpiSnapshot,SearchDoc,NotificationLog,Setting}.kt
        │       │   │   ├── dao/{GoalDao,TaskDao,CalendarDao,SearchDao,ReviewDao,
        │       │   │   │        TimeDao,HabitDao,KpiDao,NotificationDao}.kt
        │       │   │   ├── fts/SearchDocFts.kt   # @Fts4(contentEntity = SearchDoc::class)
        │       │   │   ├── Converters.kt
        │       │   │   └── SeedData.kt
        │       │   ├── prefs/SettingsRepository.kt
        │       │   └── remote/openrouter/
        │       │       ├── OpenRouterApi.kt
        │       │       ├── OpenRouterModels.kt
        │       │       ├── dto/{ChatRequest,ChatResponse,ModelError}.kt
        │       │       └── PromptLibrary.kt
        │       ├── domain/
        │       │   ├── model/{...domain models...}
        │       │   ├── usecase/{GetTodayBriefing,ComputeGoalProgress,BuildGoalDiff,
        │       │   │              ScheduleRecurringTasks,SearchEverything,
        │       │   │              AggregateWeeklyReview,TrackKpi}.kt
        │       │   └── repository/{GoalRepository,TaskRepository,CalendarRepository,
        │       │                     SearchRepository,ReviewRepository,AiRepository}.kt
        │       ├── notification/
        │       │   ├── NotificationChannels.kt
        │       │   ├── BriefingBuilder.kt        # builds EN/UR notification text
        │       │   ├── DailyBriefingWorker.kt
        │       │   ├── EveningReviewWorker.kt
        │       │   ├── OverdueTaskWorker.kt
        │       │   ├── StreakRiskWorker.kt
        │       │   ├── ExactAlarmScheduler.kt
        │       │   └── BootReceiver.kt           # re-schedule alarms after reboot
        │       ├── widget/
        │       │   ├── TodayWidgetReceiver.kt
        │       │   └── TodayWidget.kt            # Glance
        │       ├── di/{DatabaseModule,NetworkModule,RepositoryModule,WorkerModule}.kt
        │       └── feature/
        │           ├── onboarding/{OnboardingScreen,OnboardingViewModel}
        │           ├── today/{TodayScreen,TodayViewModel,components/...}
        │           ├── calendar/{CalendarScreen,CalendarViewModel,DayColumn,MonthGrid,AgendaList}
        │           ├── goals/{GoalsScreen,GoalDetailScreen,GoalPulseScreen,KpiScreen,GoalsViewModel}
        │           ├── search/{SearchScreen,SearchViewModel}
        │           ├── tasks/{TaskSheet,TaskListScreen,TasksViewModel}
        │           ├── time/{TimeScreen,TimerController,TimeViewModel}
        │           ├── ai/{AiCoachScreen,AiCoachViewModel}
        │           ├── journal/{JournalScreen,WeeklyReviewScreen,JournalViewModel}
        │           └── settings/{SettingsScreen,OpenRouterSettingsSection,
        │                          NotificationSettingsSection,BackupSection,SettingsViewModel}
        └── test/java/com/manzil/app/...   (unit tests, mirror of main tree)
```

---

## 7. DATA MODEL (ROOM SCHEMA — IMPLEMENT EXACTLY)

All IDs are `String` (UUID v4). All timestamps are epoch milliseconds (`Long`). All dates are `LocalDate` stored as ISO-8601 strings via a `TypeConverter`.

```kotlin
// ── GOALS ────────────────────────────────────────────────────────────────
@Entity(tableName = "goals", indices = [Index("parentGoalId"), Index("status")])
data class Goal(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val category: GoalCategory,          // SKILL, CLIENT, MONEY, HEALTH, EDUCATION, BUSINESS, PERSONAL
    val parentGoalId: String? = null,    // enables the goal tree
    val status: GoalStatus,              // ACTIVE, PAUSED, DONE, DROPPED
    val priority: Int = 2,               // 1 = critical … 4 = low
    val startDate: LocalDate,
    val targetDate: LocalDate?,
    val progressPercent: Int = 0,        // 0..100, recomputed by ComputeGoalProgress
    val metricLabel: String? = null,     // e.g. "Monthly income"
    val metricTarget: Double? = null,    // e.g. 50000
    val metricCurrent: Double = 0.0,
    val metricUnit: String? = null,      // "PKR", "clients", "hours"
    val sortOrder: Int = 0,
    val createdAt: Long, val updatedAt: Long,
    val archivedAt: Long? = null
)

// Every mutation writes a revision. This powers GOAL PULSE (F4).
@Entity(tableName = "goal_revisions", indices = [Index("goalId"), Index("createdAt")])
data class GoalRevision(
    @PrimaryKey val id: String,
    val goalId: String,
    val snapshotJson: String,            // full goal serialised
    val changedFieldsJson: String,       // [{"field":"targetDate","from":"2030-06-01","to":"2030-09-01"}]
    val changeType: ChangeType,          // CREATED, UPDATED, PROGRESS, METRIC, STATUS, DELETED
    val note: String? = null,            // optional user note ("client asked for delay")
    val createdAt: Long
)

@Entity(tableName = "milestones", indices = [Index("goalId")])
data class Milestone(
    @PrimaryKey val id: String, val goalId: String,
    val title: String, val dueDate: LocalDate?,
    val done: Boolean = false, val doneAt: Long? = null,
    val sortOrder: Int = 0, val createdAt: Long
)

// ── TASKS ────────────────────────────────────────────────────────────────
@Entity(tableName = "tasks", indices = [Index("goalId"), Index("dueDate"), Index("status")])
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String = "",
    val goalId: String? = null,
    val milestoneId: String? = null,
    val parentTaskId: String? = null,
    val dueDate: LocalDate?, val dueTime: LocalTime? = null,
    val startDate: LocalDate? = null,    // for scheduled/all-day blocks
    val estimatedMinutes: Int? = null,
    val actualMinutes: Int = 0,
    val status: TaskStatus,              // TODO, IN_PROGRESS, DONE, CANCELLED, BLOCKED
    val priority: Int = 2,
    val recurrenceRule: String? = null,  // RFC-5545 RRULE subset, e.g. "FREQ=WEEKLY;BYDAY=MO,WE,FR"
    val recurrenceEndDate: LocalDate? = null,
    val reminderMinutesBefore: Int? = null,
    val completedAt: Long? = null,
    val createdAt: Long, val updatedAt: Long
)

// One row per realised occurrence of a recurring task (materialised 60 days ahead).
@Entity(tableName = "task_instances",
        indices = [Index("taskId"), Index("occurrenceDate")],
        primaryKeys = ["taskId", "occurrenceDate"])
data class TaskInstance(
    val taskId: String,
    val occurrenceDate: LocalDate,
    val status: TaskStatus = TaskStatus.TODO,
    val completedAt: Long? = null,
    val movedFrom: LocalDate? = null     // set when the user drags it to another day
)

@Entity(tableName = "time_entries", indices = [Index("taskId"), Index("startedAt")])
data class TimeEntry(
    @PrimaryKey val id: String, val taskId: String?, val goalId: String?,
    val label: String, val startedAt: Long, val endedAt: Long?,
    val source: TimeSource               // MANUAL, TIMER, IMPORTED
)

// ── CALENDAR ─────────────────────────────────────────────────────────────
@Entity(tableName = "calendar_events", indices = [Index("startAt"), Index("source")])
data class CalendarEvent(
    @PrimaryKey val id: String,
    val title: String, val description: String = "",
    val startAt: Long, val endAt: Long, val allDay: Boolean = false,
    val location: String? = null,
    val source: EventSource,             // LOCAL, TASK, EXAM, CLIENT, IMPORTED
    val linkedTaskId: String? = null,
    val colorArgb: Int? = null,
    val createdAt: Long, val updatedAt: Long
)

// ── JOURNAL / REVIEW ─────────────────────────────────────────────────────
@Entity(tableName = "journal_entries", indices = [Index("date")])
data class JournalEntry(
    @PrimaryKey val id: String, val date: LocalDate,
    val mood: Int = 3,                   // 1..5
    val wins: String = "", val blockers: String = "", val note: String = "",
    val createdAt: Long, val updatedAt: Long
)

@Entity(tableName = "daily_reviews", indices = [Index("date")])
data class DailyReview(
    @PrimaryKey val id: String, val date: LocalDate,
    val plannedCount: Int, val doneCount: Int, val pendingCount: Int, val overdueCount: Int,
    val focusMinutes: Int, val scorePercent: Int,      // doneCount / plannedCount * 100
    val pendingTitlesJson: String,                     // ["Ship FYP proposal", ...]
    val aiSummary: String? = null,
    val createdAt: Long
)

// ── HABITS / STREAKS ─────────────────────────────────────────────────────
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String, val name: String, val icon: String, val colorArgb: Int,
    val targetPerWeek: Int = 7, val active: Boolean = true, val createdAt: Long
)
@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "date"])
data class HabitLog(val habitId: String, val date: LocalDate, val done: Boolean, val at: Long)

// ── KPI SNAPSHOTS (drives the business dashboard) ────────────────────────
@Entity(tableName = "kpi_snapshots", indices = [Index("key"), Index("date")])
data class KpiSnapshot(
    @PrimaryKey val id: String, val key: String,   // "mrr_pkr", "savings_pkr", "clients_intl", ...
    val value: Double, val date: LocalDate, val note: String? = null, val createdAt: Long
)

// ── SEARCH (FTS4, content-synced) ────────────────────────────────────────
@Entity(tableName = "search_docs")
data class SearchDoc(
    @PrimaryKey val id: String,
    val entityType: String,       // GOAL | TASK | MILESTONE | EVENT | JOURNAL | KPI
    val entityId: String,
    val title: String, val body: String,
    val goalTitle: String? = null,
    val dateIso: String? = null,
    val updatedAt: Long
)
@Fts4(contentEntity = SearchDoc::class)
@Entity(tableName = "search_docs_fts")
data class SearchDocFts(val title: String, val body: String, val goalTitle: String)
```

**Triggers:** create Room `@RawQuery`-installed SQLite triggers (in a `RoomDatabase.Callback.onCreate`) so that every INSERT/UPDATE/DELETE on `goals`, `tasks`, `milestones`, `calendar_events`, `journal_entries` keeps `search_docs` in sync automatically. Then keep `search_docs_fts` in sync from `search_docs` with a second trigger set. This is what makes search "real-time" without any manual re-index button.

---

## 8. FEATURE SPECS

### F1 — Onboarding + Roadmap Import  *(M1)*

- 3 screens: (1) "What are you building?" — free text; (2) "When?" — target date picker; (3) "How will I remind you?" — notification time + permission request (`POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`).
- **Roadmap Import:** a screen where the user pastes Markdown (or picks a `.md` file via SAF). `RoadmapImporter` parses `##`/`###` headings into a goal tree, checkbox lines `- [ ]` into tasks, and table rows containing a currency/number + label into KPI targets. Show a preview tree with checkboxes before committing.
- Must successfully import a document shaped like the user's Sahiwal roadmap (headings + tables + checklists) into ≥ 20 goals and ≥ 40 tasks. Write a unit test with a 40-line fixture proving this.
- On first launch, seed the KPI targets from §3 automatically.

**Acceptance:** fresh install → 60 seconds → user sees a populated GOALS tab and a TODAY briefing.

---

### F2 — TODAY Dashboard (the heart of the app)  *(M2)*

Live, auto-updating screen. Sections top → bottom:

1. **Live header** — date, day-of-week, live clock (ticking every second while the screen is resumed), "Day N of your plan" counter computed from the root goal's `startDate` and `targetDate`.
2. **Briefing card** — the same content as the morning notification (see F9), with a "Regenerate with AI" button if an OpenRouter key is present.
3. **Focus block** — one task marked *focus of the day*. Big **Start / Pause / Stop** timer button. Elapsed time updates every second. On Stop → writes a `TimeEntry`, adds to `Task.actualMinutes`, rolls up to the goal.
4. **Today's plan** — list of today's `TaskInstance`s + events, each with a checkbox, priority dot, goal chip, and drag handle to reorder. Swipe right = done, swipe left = postpone (opens a day picker).
5. **Progress strip** — three rings: *Today* (done/planned), *This week*, *Root goal %*.
6. **Streak card** — consecutive days with ≥1 completed task; a 12-week GitHub-style heatmap; "streak at risk" warning after 20:00 if nothing is done.
7. **Quick capture** — always-visible text field. Natural-language parsing: `"proposal 5pm friday !1 #client"` → title `proposal`, due Fri 17:00, priority 1, goal matched by `#client` fuzzy match. Write unit tests for the parser.
8. **Yesterday's leftovers** — anything not done yesterday, with "Move to today" / "Drop" actions.

**Acceptance:** completing a task instantly (no reload) updates the ring, the streak, and the briefing text.

---

### F3 — Real-time Calendar  *(M2)*

- Modes: **Day / 3-Day / Week / Month / Agenda**, switched by a segmented control; pinch to zoom between Day ↔ Week ↔ Month.
- **A red "now" line** that moves in real time (updates every 30 s); on open, auto-scroll to now ± 1 h.
- Week/Day views render both `CalendarEvent`s and scheduled `TaskInstance`s (tasks render as hatched blocks).
- **Drag to reschedule** (change time) and **drag to another day column** (change date); resizing a block changes duration. Every change persists immediately and writes a `TaskInstance.movedFrom`.
- **Conflict detection:** overlapping events show a warning stripe and a snackbar "2 events overlap".
- Tap empty slot → create event sheet. Tap block → detail sheet.
- Month view: dots per day coloured by load (green ≤3 items, amber 4–6, red 7+); long-press a day → "Plan this day".
- Agenda: infinite list grouped by day, with "Today / Tomorrow / This week / Later" sticky headers.
- Optional: read-only mirror of the device calendar (`READ_CALENDAR`, opt-in in Settings) rendered greyed out. Never write to the device calendar.

**Acceptance:** dragging a task from Monday to Wednesday persists across app restart and appears in Wednesday's TODAY list when Wednesday arrives.

---

### F4 — Goals + **GOAL PULSE** (change highlighting)  *(M3)*

This is the feature the user explicitly asked for: *"mere goal mein kya change aaya hai, woh highlight ho"*.

**Goals tab**
- Tree view (root goal → sub-goals → milestones), collapsible, with a circular progress ring per node.
- Progress is computed by `ComputeGoalProgress`: leaf goal progress = done milestones / total milestones (weighted by task completion if no milestones); parent progress = average of children weighted by priority. Recomputed reactively whenever a task or milestone changes.
- Metric goals (with `metricTarget`) show a live bar: `metricCurrent / metricTarget`, plus a sparkline from `KpiSnapshot` history.
- Overdue goals (past `targetDate`, not DONE) get a red left border and a "Slipping — N days late" chip.

**GOAL PULSE screen** — the differentiator
- A reverse-chronological feed of `GoalRevision`s, grouped by day, filtered to *this week / this month / all*.
- Each entry is a **colour-coded diff card**:
  - 🟢 `CREATED` — "New goal added: *Land first Upwork client*"
  - 🔵 `UPDATED` — field-level diff chips: `targetDate` `2030-06-01` → `2030-09-01`, with the old value struck through in red and the new value in green
  - 📈 `PROGRESS` — "Progress moved 42% → 55% (+13)" with a mini bar animation
  - 💰 `METRIC` — "Monthly income 25,000 → 40,000 PKR (+60%)"
  - 🟡 `STATUS` — "Paused" / "Resumed" / "Dropped"
  - 🔴 `DELETED` — greyed, with Undo (10 s window)
- **Weekly Goal Digest** card at the top: "This week: 2 goals added · 1 deadline moved · 3 milestones closed · progress +7% · 1 goal slipping".
- If an OpenRouter key exists, a button **"Explain what changed"** sends the diff JSON to the model and renders a 3–4 line plain-Urdu/English interpretation ("Aap ne deadline 3 mahine aage kar di — iska matlab Year 3 ka retainer target bhi slide karega. Suggestion: …").
- Filtering chips: by goal, by change type, "only slipping goals".

**Acceptance:** editing a goal's target date makes a `🔵 UPDATED` card appear at the top of GOAL PULSE within 300 ms, showing old → new values.

---

### F5 — Real-time Search  *(M3)*

- One search field, reachable from **every** screen (top-bar icon) and as its own tab.
- **Instant:** 150 ms debounce, results stream in as you type, target < 50 ms query on 5,000 documents. Show query time in a debug chip (long-press the field).
- Backed by Room **FTS4** `MATCH` with prefix matching (`term*`), ranked by `bm25()`, boosted: title match ×3, goal title ×2, recency ×1.2.
- Searches across: goals, tasks, milestones, calendar events, journal entries, KPI notes. Results grouped by type with icons and counts.
- **Filters row:** type chips (All / Goals / Tasks / Events / Journal), date range, goal, status (open/done).
- **Recent searches** (last 10, persisted) and **saved searches** (named filter presets, e.g. "Overdue client work").
- **Semantic mode toggle** (only visible when an OpenRouter key exists): sends the query + top 30 local titles to the model for re-ranking / "did you mean". Must never block local results — local results render first, AI re-ordering arrives async with a subtle shimmer.
- Empty state with suggestions; no-results state with "Create task titled …" shortcut.
- Keyboard: `Enter` jumps to the first result; hardware `Ctrl+K` / `⌘K` opens search.

**Acceptance:** typing "upw" shows "Upwork profile", "First Upwork client", and a journal entry mentioning Upwork — in that order — before the second keystroke settles.

---

### F6 — Tasks & Recurrence  *(M2)*

- Full CRUD, subtasks (1 level), notes, priority (P1–P4 colour-coded), estimated minutes, goal/milestone linking, reminders.
- **Implement an RFC-5545 subset yourself** in `core/recurrence/`:
  - `FREQ` = `DAILY | WEEKLY | MONTHLY | YEARLY`
  - `INTERVAL`, `BYDAY` (MO,TU,…), `BYMONTHDAY`, `COUNT`, `UNTIL`
  - Expand into `TaskInstance` rows **60 days ahead**, refreshed nightly by a Worker and on app start.
  - Editing one occurrence edits only that instance; "edit series" edits the `Task` and re-expands.
- Views: List (grouped by Today / Tomorrow / This week / Later / No date), Kanban (TODO → IN_PROGRESS → DONE), and a Priority Matrix (Eisenhower 2×2).
- Bulk actions: multi-select → complete / reschedule / re-prioritise / delete.
- Postpone semantics: postponing writes `movedFrom` so the app can later report "you postponed this 4 times" (a nudge, not a nag).

**Acceptance:** unit test — `FREQ=WEEKLY;BYDAY=MO,WE,FR` from 2026-10-01 produces exactly the right 26 occurrences in 60 days.

---

### F7 — Time Tracking  *(M4)*

- Single active timer app-wide (`TimerController` singleton, survives process death via DataStore + a foreground-service-free approach using timestamps).
- Start from TODAY, from a task, or ad-hoc ("Deep work — React course").
- Auto-stop protection: if the timer runs > 4 h with no interaction, the app posts a notification "Still working?" and auto-pauses at 6 h.
- Time screen: daily bar chart (last 14 days), per-goal rollup donut, "focus minutes today vs 7-day average", weekly total vs the user's 25 h/week target with a progress bar.
- Manual entry + edit + delete; imported entries flagged `IMPORTED`.

---

### F8 — AI Coach via OpenRouter  *(M4)*

- Chat screen, streaming responses (SSE from OpenRouter), message history persisted locally, per-conversation goal context injection.
- **Four built-in actions** (buttons, not just chat):
  1. **Plan my day** — sends today's pending tasks, calendar, energy/mood, and the last 7 days' completion rate; receives a ranked plan with time blocks. One tap applies it (creates `TaskInstance` times).
  2. **Weekly review** — sends the week's `DailyReview`s + goal diffs; receives wins / slippage / next week's 3 priorities.
  3. **Explain what changed** — used by GOAL PULSE (F4).
  4. **Unblock me** — user names a blocker; model returns 3 concrete next actions.
- All prompts live in `PromptLibrary.kt` as named constants with `{placeholders}`. Prompts must instruct the model to answer in the user's chosen language (English or Roman Urdu) and to be **brutally concise** (max 120 words for actions).
- Never send the API key anywhere except the `Authorization` header. Never log request bodies.
- Cost guard: Settings shows an estimated token/credit usage counter per day with a soft cap (default 50 requests/day) and a hard stop with a clear message.

**Acceptance:** with an invalid key, every AI surface shows a friendly error state and the rest of the app is unaffected. With no key, the buttons are hidden or show "Add key in Settings".

---

### F9 — Notifications & Daily Briefing  *(M3)*

**Channels** (`NotificationChannels.kt`), all created at first launch:

| Channel ID | Name | Importance |
|---|---|---|
| `briefing` | Daily briefing | HIGH |
| `review` | Evening review | DEFAULT |
| `reminders` | Task reminders | HIGH |
| `streak` | Streak alerts | DEFAULT |
| `ai` | AI insights | LOW |

**Scheduling:** `AlarmManager.setExactAndAllowWhileIdle` for time-critical (briefing, reminders) with a WorkManager periodic fallback; `BootReceiver` re-registers everything after reboot; `RECEIVE_BOOT_COMPLETED` in the manifest. Request `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` and degrade to inexact with a warning banner in Settings if denied.

**Morning briefing** (default 07:00, user-configurable, skipped on days the user marks as "rest day"):

> **EN:**
> ```
> Good morning — Day 412 of your plan
>
> DO TODAY (3)
>  • 09:00  React: useEffect + data fetching  (2h)
>  • 14:00  Send 10 Upwork proposals
>  • 20:00  FYP: write chapter 2 outline
>
> DONE YESTERDAY (4/5) — 80%
> Still pending: "CS50 week 3 problem set"
>
> GOAL MOVE: Freelance income 25,000 → 40,000 PKR this month (+60%)
> Streak: 11 days 🔥   ·   Root goal: 18%
> ```
>
> **UR (Roman):**
> ```
> Subah bakhair — plan ka din 412
>
> AAJ KARNA HAI (3)
>  • 09:00  React: useEffect + data fetching  (2 ghante)
>  • 14:00  10 Upwork proposals bhejein
>  • 20:00  FYP: chapter 2 ka outline likhein
>
> KAL HO GAYA (4/5) — 80%
> Abhi baqi: "CS50 week 3 problem set"
>
> GOAL UPDATE: Is mahine income 25,000 → 40,000 PKR (+60%)
> Streak: 11 din 🔥   ·   Root goal: 18%
> ```

The exact three blocks the user asked for — **"yeh karna hai" / "yeh ho gaya" / "yeh reh gaya"** — are mandatory and must appear in that order.

`BriefingBuilder` composes this text from local data **without any network call**. If an OpenRouter key exists and "AI briefing" is enabled, the local text is sent to the model to be rewritten in a warmer tone; on any failure the local text is used. This ordering is a hard requirement: **the notification must never be empty because the network failed.**

**Evening review** (default 21:30):
> ```
> Aaj ka hisaab — 4/6 done (67%)
> Reh gaya: "10 Upwork proposals", "CS50 week 3"
> Focus time: 3h 20m (7-day avg 2h 45m) ✅
> Kal ka pehla kaam: React useEffect
> ```
> Actions: **[ Log day ]** **[ Move leftovers to tomorrow ]** **[ Done for the day ]**

**Other notifications:** task reminder (N min before, exact), overdue (19:00 daily digest of overdue items), streak-at-risk (20:00 if 0 tasks done), weekly goal digest (Sunday 18:00, links to GOAL PULSE), goal-slipping alert (when a goal passes its target date).

**Anti-nag rules:** max 6 notifications/day; never between 23:00–07:00; if the user dismisses 3 consecutive briefings without opening, auto-suggest reducing frequency; every notification has a "Snooze 1h" and a "Quiet this week" action. Log everything to `NotificationLog` and show a "Notification history" debug screen.

**Acceptance:** unit test `BriefingBuilderTest` asserts the three blocks appear in order, that overdue items land in "reh gaya", and that with zero data the notification still renders a meaningful line instead of being blank.

---

### F10 — Settings  *(M1 basic, M4 full)*

1. **OpenRouter** (top of the screen, the user specifically asked for this)
   - Label: *"OpenRouter API key"* with a helper link to `https://openrouter.ai/keys`
   - Paste field, masked by default (`sk-or-v1-••••••••4f2a`), eye toggle, **Save**, **Test connection**, **Remove**
   - Saved via `SecretVault` (EncryptedSharedPreferences). Show a green lock icon + "Stored encrypted on this device only."
   - **Test connection** calls `GET /api/v1/models` (cheap, no tokens) and shows latency + account name; on 401 shows "Key invalid or revoked".
   - **Model picker** — dropdown populated from `/api/v1/models`, filtered to chat-capable models, sorted by context length; shows a "Free" badge where applicable; remembers the last choice. Default: `openrouter/auto`.
   - Usage: requests today / soft cap slider / hard-stop toggle.
2. **Notifications** — morning time, evening time, rest days (weekday multi-select), quiet hours, per-channel toggles, AI-briefing toggle, "Send test briefing" button, exact-alarm status with a deep link to system settings.
3. **Appearance** — theme (System / Light / Dark), accent colour, font scale, compact mode.
4. **Language** — English / اردو (switches `values-ur` at runtime via `AppCompatDelegate.setApplicationLocales`).
5. **Data** — export JSON (full, minus secrets) via SAF, export CSV (tasks/goals/time), import JSON, **backup reminder**, "Erase all data" behind a type-to-confirm.
6. **Plan** — root goal start/target dates, daily deep-work hour target, currency, timezone, week start day.
7. **About** — version, build, open-source licences, `DECISIONS.md` link, "No analytics. No telemetry. Your data never leaves this phone except to OpenRouter, only when you ask."

---

### F11 — Widgets (Glance)  *(M5)*

- **Today widget (4×2):** date, top 3 tasks with checkboxes, streak flame, focus timer status. Tapping a checkbox completes the task and refreshes the widget.
- **Streak widget (2×2):** big streak number + 7-day dots.
- Refresh on data change (WorkManager `addContentTrigger`-style: enqueue an update after any write) + every 30 min.

### F12 — Journal & Weekly Review  *(M5)*

- Daily entry: mood (1–5 emoji), wins (bulleted), blockers (bulleted), free note. Pre-filled with today's done/pending lists.
- Weekly review screen: 7-day score trend, focus-hours total, goal-diff summary, habit heatmap, and (with AI) a generated narrative + "3 priorities for next week" that can be one-tapped into tasks.

### F13 — KPI / Business Dashboard  *(M5)*

- Grid of KPI cards seeded from §3 (savings, MRR, clients, income, GitHub projects, CGPA, deep-work hours).
- Each card: current value, target, progress bar, sparkline from `KpiSnapshot`, last-updated, tap → add-a-reading sheet.
- A single "Weekly check-in" flow that walks through all KPIs in 60 seconds and writes all snapshots at once — this is the mechanism that keeps a 4-year plan honest.

### F14 — Backup / Export / Import  *(M6)*

- JSON schema versioned (`"schemaVersion": 1`), forward-compatible importer.
- Excludes `SecretVault` contents entirely.
- Optional: SAF "auto-backup" weekly to a user-chosen folder, with a notification on failure.

---

## 9. OPENROUTER INTEGRATION SPEC

```kotlin
// data/remote/openrouter/OpenRouterApi.kt
class OpenRouterApi(
    private val client: OkHttpClient,
    private val json: Json,
    private val secrets: SecretVault,
) {
    private val base = "https://openrouter.ai/api/v1".toHttpUrl()

    suspend fun listModels(): Result<List<OrModel>>           // GET  /models
    suspend fun chat(req: ChatRequest): Result<ChatResponse>   // POST /chat/completions
    fun chatStream(req: ChatRequest): Flow<String>             // POST /chat/completions, stream = true (SSE)
}
```

**Headers on every request:**
```
Authorization: Bearer <key from SecretVault>
Content-Type: application/json
HTTP-Referer: https://github.com/<you>/manzil
X-Title: Manzil
```

**Request body (chat):**
```json
{
  "model": "openrouter/auto",
  "messages": [
    { "role": "system", "content": "You are Manzil, a brutally practical planning coach for a student in Sahiwal, Pakistan who is building toward launching a software house by 2030. Answer in {LANGUAGE}. Max {MAX_WORDS} words. Be concrete, never generic. Never invent data that was not given to you." },
    { "role": "user", "content": "..." }
  ],
  "temperature": 0.4,
  "max_tokens": 600,
  "stream": false
}
```

**Error mapping (must be exhaustive and user-friendly):**

| HTTP | User-facing message |
|---|---|
| 401 / 403 | "API key invalid or revoked. Check it in Settings." |
| 402 | "Out of OpenRouter credits. Top up at openrouter.ai/credits." |
| 408 / 5xx / IOException | "Can't reach OpenRouter. Your data is safe — try again." |
| 429 | "Rate limited. Wait a moment." |
| parse failure | "Unexpected response from OpenRouter." |

**Non-negotiables:** 20 s connect / 60 s read timeout; retry once on 5xx with 1.5 s backoff; never retry on 4xx; never store the key in Room or DataStore; never include the key in `AppLog`; strip the key from any exported backup; work perfectly when the key is absent.

---

## 10. UI/UX SYSTEM

- **Design language:** calm, dense-but-breathable, Material 3 expressive. Light + dark. One accent colour (default teal `#0F766E`); semantic colours: success `#16A34A`, warning `#D97706`, danger `#DC2626`, info `#2563EB`.
- **Type:** `MaterialTheme.typography`; tabular numerals for all counters and timers (`FontFeatureSettings("tnum")`) so digits don't jitter.
- **Motion:** 200 ms standard, spring for progress rings, shared-element transition from a task row to its detail sheet. Respect `Settings → Accessibility → reduce motion`.
- **Empty states:** every list has a purposeful empty state with one primary action. Never a blank screen.
- **Accessibility:** all touch targets ≥ 48 dp; every icon button has `contentDescription`; minimum contrast 4.5:1; full TalkBack pass on TODAY, CALENDAR, SEARCH.
- **Landscape + tablet:** two-pane layout above 600 dp width (list + detail).
- **Haptics** on task completion and timer start/stop.

---

## 11. MILESTONES (DELIVER IN THIS ORDER, COMMIT AFTER EACH)

| # | Milestone | Must contain | Verify by |
|---|---|---|---|
| **M0** | **Bootstrap** | Gradle project compiles; version catalog; Hilt wired; empty Compose screen with bottom nav (4 tabs, placeholders); `.gitignore`; `README.md`; `DECISIONS.md`; all 3 workflow files from §14 | `./gradlew assembleDebug` green; `./gradlew test` green |
| **M1** | **Data layer + Settings shell** | Full Room schema + DAOs + migrations; FTS4 + triggers; `SecretVault`; Settings screen with OpenRouter key save/test/remove + model picker; onboarding (3 screens) | `./gradlew test` incl. `SearchDaoTest`, `SecretVaultTest` |
| **M2** | **TODAY + Calendar + Tasks** | F2, F3, F6 complete with recurrence expander and timer | `RRuleExpanderTest`, `QuickCaptureParserTest` |
| **M3** | **Goals + GOAL PULSE + Search + Notifications** | F4, F5, F9 complete — **this is the milestone that delivers the user's core ask** | `JsonDiffTest`, `BriefingBuilderTest`, `SearchRankingTest`; manually verify a notification fires |
| **M4** | **AI + Time** | F7, F8 complete with streaming, cost guard, graceful degradation | `OpenRouterApiTest` (MockWebServer), `PromptLibraryTest` |
| **M5** | **KPI + Journal + Widgets + Roadmap Import** | F1, F11, F12, F13 | `RoadmapImporterTest` with a real fixture |
| **M6** | **Backup, polish, a11y, i18n** | F14, full `values-ur`, TalkBack pass, dark theme audit | `BackupRoundTripTest` |
| **M7** | **Release** | `versionName 1.0.0`, R8 rules, signed release build instructions in README, tag `v1.0.0` | `release.yml` produces an APK artifact |

---

## 12. DEFINITION OF DONE (per milestone)

- [ ] `./gradlew assembleDebug` succeeds with **zero errors**
- [ ] `./gradlew testDebugUnitTest` succeeds; new logic has tests
- [ ] `./gradlew lintDebug` produces no new errors
- [ ] `./gradlew ktlintCheck` clean
- [ ] No hardcoded user-facing strings; `values-ur` updated
- [ ] No `TODO()` / `println` / commented-out code left behind
- [ ] New screens have empty, loading, and error states
- [ ] Works fully offline
- [ ] `DECISIONS.md` updated with any default you chose
- [ ] Committed with a descriptive message

**Before you tell the user a milestone is done, paste the actual command output** (last ~15 lines) into your reply and name the test class that exercised the new logic. If you could not run something, say so explicitly instead of implying it works.

---

## 13. GITHUB ACTIONS — COPY-PASTE FILES

Create these three files **exactly as written**. They are known-good for Gradle 8.6 / JDK 17 / AGP 8.3.2 and include the SDK-path and missing-wrapper-jar workarounds that break most Android CI runs.

### 13.1 `.github/workflows/build-apk.yml`

```yaml
name: Build Manzil APK

on:
  push:
    branches: [ main, "arena/**" ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    name: Assemble & Unit Test
    runs-on: ubuntu-latest
    timeout-minutes: 45

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: gradle

      - name: Resolve Android SDK path
        run: |
          set -euo pipefail
          if [ -z "${ANDROID_SDK_ROOT:-}" ]; then
            if [ -d "/usr/local/lib/android/sdk" ]; then
              echo "ANDROID_SDK_ROOT=/usr/local/lib/android/sdk" >> "$GITHUB_ENV"
              export ANDROID_SDK_ROOT="/usr/local/lib/android/sdk"
            elif [ -d "$HOME/Android/Sdk" ]; then
              echo "ANDROID_SDK_ROOT=$HOME/Android/Sdk" >> "$GITHUB_ENV"
              export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
            else
              echo "::error::No Android SDK found on runner"
              exit 1
            fi
          fi
          echo "Using ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
          for c in "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin" "$ANDROID_SDK_ROOT/cmdline-tools/bin" "$ANDROID_SDK_ROOT/tools/bin"; do
            if [ -f "$c/sdkmanager" ]; then echo "$c" >> "$GITHUB_PATH"; break; fi
          done

      - name: Accept licenses and install SDK packages
        run: |
          set -euo pipefail
          yes | sdkmanager --licenses > /dev/null 2>&1 || true
          sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0"

      - name: Ensure gradle wrapper jar exists
        run: |
          set -euo pipefail
          chmod +x gradlew
          if [ ! -s gradle/wrapper/gradle-wrapper.jar ]; then
            mkdir -p gradle/wrapper
            curl -fsSL --retry 3 -o gradle/wrapper/gradle-wrapper.jar \
              "https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar"
          fi
          ./gradlew --version

      - name: Write local.properties
        run: echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties

      - name: ktlint
        run: ./gradlew ktlintCheck --console=plain || echo "::warning::ktlint reported issues"

      - name: Unit tests
        run: ./gradlew testDebugUnitTest --console=plain --stacktrace 2>&1 | tee test.log

      - name: Assemble debug APK
        run: ./gradlew assembleDebug --console=plain --stacktrace 2>&1 | tee build.log

      - name: Print compile errors on failure
        if: failure()
        run: |
          echo "=== Kotlin/Java diagnostics ==="
          grep -hE "^(e|w): " build.log test.log 2>/dev/null | head -100 || true
          echo "=== build.log tail ==="
          tail -200 build.log 2>/dev/null || true

      - name: List artifacts
        if: always()
        run: find . -name "*.apk" -type f -exec ls -lh {} \; || echo "No APK produced"

      - name: Upload debug APK
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: manzil-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: warn
          retention-days: 30

      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-report
          path: app/build/reports/tests/
          if-no-files-found: ignore
          retention-days: 14
```

### 13.2 `.github/workflows/ci.yml`

```yaml
name: CI

on:
  pull_request:
  push:
    branches: [ main ]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  quality:
    name: Lint + Tests
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: gradle

      - name: Ensure gradle wrapper jar exists
        run: |
          set -euo pipefail
          chmod +x gradlew
          if [ ! -s gradle/wrapper/gradle-wrapper.jar ]; then
            mkdir -p gradle/wrapper
            curl -fsSL --retry 3 -o gradle/wrapper/gradle-wrapper.jar \
              "https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar"
          fi

      - name: Write local.properties
        run: echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties

      - name: ktlint
        run: ./gradlew ktlintCheck --console=plain

      - name: Android Lint
        run: ./gradlew lintDebug --console=plain

      - name: Unit tests
        run: ./gradlew testDebugUnitTest --console=plain

      - name: Publish test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-and-test-reports
          path: |
            app/build/reports/lint-results-debug.html
            app/build/reports/tests/
          if-no-files-found: ignore
          retention-days: 14
```

### 13.3 `.github/workflows/release.yml`

```yaml
name: Release APK

on:
  push:
    tags: [ "v*" ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  release:
    name: Build signed release
    runs-on: ubuntu-latest
    timeout-minutes: 60
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: gradle

      - name: Ensure gradle wrapper jar exists
        run: |
          set -euo pipefail
          chmod +x gradlew
          if [ ! -s gradle/wrapper/gradle-wrapper.jar ]; then
            mkdir -p gradle/wrapper
            curl -fsSL --retry 3 -o gradle/wrapper/gradle-wrapper.jar \
              "https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar"
          fi

      - name: Write local.properties
        run: echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties

      - name: Decode keystore
        if: ${{ env.KEYSTORE_B64 != '' }}
        env:
          KEYSTORE_B64: ${{ secrets.KEYSTORE_BASE64 }}
        run: |
          set -euo pipefail
          mkdir -p ~/.signing
          echo "$KEYSTORE_B64" | base64 -d > ~/.signing/release.keystore
          {
            echo "MANZIL_STORE_FILE=$HOME/.signing/release.keystore"
            echo "MANZIL_STORE_PASSWORD=${{ secrets.KEYSTORE_PASSWORD }}"
            echo "MANZIL_KEY_ALIAS=${{ secrets.KEY_ALIAS }}"
            echo "MANZIL_KEY_PASSWORD=${{ secrets.KEY_PASSWORD }}"
          } >> gradle.properties

      - name: Build release APK
        run: ./gradlew assembleRelease --console=plain --stacktrace

      - name: Rename artifact
        run: |
          set -euo pipefail
          mkdir -p dist
          cp app/build/outputs/apk/release/*.apk "dist/manzil-${GITHUB_REF_NAME:-dev}.apk" || true
          ls -lh dist

      - name: Create GitHub release
        uses: softprops/action-gh-release@v2
        with:
          files: dist/*.apk
          generate_release_notes: true
          fail_on_unmatched_files: false
```

> **Signing note:** if the repo has no secrets configured, `release.yml` still builds an **unsigned** release APK. That is intentional — the build must never fail just because signing is absent. Add `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` to repo secrets when you are ready to sign.

### 13.4 `.github/dependabot.yml`

```yaml
version: 2
updates:
  - package-ecosystem: gradle
    directory: "/"
    schedule: { interval: weekly }
    open-pull-requests-limit: 5
  - package-ecosystem: github-actions
    directory: "/"
    schedule: { interval: weekly }
```

### 13.5 `.gitignore`

```gitignore
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
*.apk
*.aab
*.keystore
!debug.keystore
*.log
/app/release/
```

### 13.6 `gradle/libs.versions.toml` (baseline — extend as needed)

```toml
[versions]
agp = "8.3.2"
kotlin = "1.9.22"
composeCompiler = "1.5.8"
coreKtx = "1.12.0"
lifecycle = "2.7.0"
activityCompose = "1.8.2"
composeBom = "2024.06.00"
navigationCompose = "2.7.7"
room = "2.6.1"
datastore = "1.0.0"
hilt = "2.51"
hiltNavigationCompose = "1.2.0"
ksp = "1.9.22-1.0.17"
coroutines = "1.8.0"
workManager = "2.9.0"
okhttp = "4.12.0"
serialization = "1.6.3"
securityCrypto = "1.1.0-alpha06"
vico = "1.13.1"
glance = "1.0.0"
junit = "4.13.2"
turbine = "1.1.0"
androidxJunit = "1.1.5"
espresso = "3.5.1"
ktlintPlugin = "12.1.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version = "1.6.1" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "securityCrypto" }
androidx-glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
androidx-glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-sse = { group = "com.squareup.okhttp3", name = "okhttp-sse", version.ref = "okhttp" }
okhttp-mockwebserver = { group = "com.squareup.okhttp3", name = "mockwebserver", version.ref = "okhttp" }
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxJunit" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlintPlugin" }
```

---

## 14. START NOW — YOUR FIRST FIVE ACTIONS

1. Create the repo skeleton: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, the Gradle 8.6 wrapper, `app/build.gradle.kts`, `AndroidManifest.xml`, `.gitignore`.
2. Write the three workflow files from §13 verbatim, plus `dependabot.yml`.
3. Create `ManzilApp.kt`, `MainActivity.kt`, `ManzilNavGraph.kt` with the 4-tab bottom navigation and placeholder screens.
4. Run `./gradlew assembleDebug` and `./gradlew test`. **Paste the output.**
5. Commit as `M0: project bootstrap — compiling Compose shell with bottom nav and CI`, then continue straight into M1 without waiting for approval.

**Remember:** the user's core ask is *goal-change highlighting* (F4 GOAL PULSE) and *daily "karna hai / ho gaya / reh gaya" notifications* (F9). If you must cut scope anywhere, cut it from F11/F12 — never from F4 or F9.

Build it. Ship it. Show the command output.
