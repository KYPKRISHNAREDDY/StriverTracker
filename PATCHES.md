# DSA TRACKER - CRITICAL PATCHES AND MISSING FEATURES

## PRIORITY 1: CRITICAL (Must Complete for MVP)

### ✅ COMPLETED IN INITIAL BUILD
- Room Database Schema
- Basic DAOs and Repository
- Core Use Cases (Status update, Revision scheduling)
- Hilt DI Setup
- Basic Gradle Configuration

### ❌ MISSING - MUST IMPLEMENT

#### 1.1 Data Seeding System
**Files to Create:**
- `app/src/main/java/com/dsatracker/data/preferences/PreferencesManager.kt`
- `app/src/main/java/com/dsatracker/domain/usecase/SeedDatabaseUseCase.kt`
- Update `app/src/main/java/com/dsatracker/DSAApplication.kt`

**Status:** Complete implementation provided in audit report above

#### 1.2 ViewModels (ALL MISSING)
**Files to Create:**
- `HomeViewModel.kt` - Manage today's dashboard state
- `SheetViewModel.kt` - Topics list with progress
- `TopicViewModel.kt` - Problems in topic with filters
- `ProblemDetailViewModel.kt` - Problem details, notes, status
- `RevisionViewModel.kt` - Due revision cards
- `StatsViewModel.kt` - Charts and statistics
- `SettingsViewModel.kt` - App settings

#### 1.3 UI Screens (ALL MISSING except MainActivity)
**Files to Create:**
- `HomeScreen.kt`
- `SheetOverviewScreen.kt`
- `TopicProblemsScreen.kt`
- `ProblemDetailScreen.kt`
- `RevisionScreen.kt`
- `NotesScreen.kt`
- `CalendarScreen.kt`
- `StatsScreen.kt`
- `SettingsScreen.kt`
- `AboutScreen.kt`

#### 1.4 Navigation
**Files to Create:**
- `app/src/main/java/com/dsatracker/ui/navigation/NavGraph.kt`
- `app/src/main/java/com/dsatracker/ui/navigation/Routes.kt`

#### 1.5 Chrome Custom Tabs Integration
**File to Create:**
- `app/src/main/java/com/dsatracker/utils/ChromeTabsHelper.kt`

#### 1.6 Notifications System
**Files to Create:**
- `app/src/main/java/com/dsatracker/notification/NotificationHelper.kt`
- `app/src/main/java/com/dsatracker/notification/DailyReminderWorker.kt`
- `app/src/main/java/com/dsatracker/notification/WeeklySummaryWorker.kt`

## PRIORITY 2: IMPORTANT (Needed for Full Functionality)

#### 2.1 Notes Auto-save with Debounce
**Implementation needed in ProblemDetailViewModel**

#### 2.2 Export Notes Feature
**Create:** `app/src/main/java/com/dsatracker/domain/usecase/ExportNotesUseCase.kt`

#### 2.3 Filter Logic for Problems
**Create:** `app/src/main/java/com/dsatracker/domain/model/ProblemFilter.kt`

#### 2.4 Calendar Heatmap Component
**Create:** `app/src/main/java/com/dsatracker/ui/components/CalendarHeatmap.kt`

#### 2.5 Stats Charts
**Create:** `app/src/main/java/com/dsatracker/ui/components/WeeklyBarChart.kt`
**Create:** `app/src/main/java/com/dsatracker/ui/components/TopicBreakdownChart.kt`

## PRIORITY 3: BUGS TO FIX

#### 3.1 Add Foreign Keys to Entities
See Bug #1 in audit report

#### 3.2 Add Core Library Desugaring
See Bug #2 in audit report

#### 3.3 Add JitPack Repository
See Bug #3 in audit report

#### 3.4 Add Transaction Support
See Bug #5 in audit report

## PRIORITY 4: ENHANCEMENTS

#### 4.1 Add Domain Models
Separate from Room entities

#### 4.2 Add Result Wrapper
For proper error handling

#### 4.3 Add Repository Interfaces
For better testability

---

## ESTIMATED COMPLETION TIME

- Priority 1: 30-40 hours
- Priority 2: 15-20 hours
- Priority 3: 3-5 hours
- Priority 4: 10-15 hours

**Total: 58-80 hours of development**

---

## COMPLETION PERCENTAGE

**Current: ~40% Complete**

- ✅ Database Layer: 95%
- ✅ Repository Layer: 85%
- ✅ Use Cases: 60%
- ❌ ViewModels: 0%
- ❌ UI Screens: 5%
- ❌ Navigation: 0%
- ❌ Notifications: 0%
- ✅ DI Setup: 100%
- ❌ Utils: 20%

