# DSA Sheet Tracker - Offline-First Android App

An offline-first Android application for tracking progress through Striver's A2Z DSA Sheet with spaced repetition, goal planning, and comprehensive statistics.

## ⚠️ Disclaimer

**This is an unofficial planner/tracker app. Not affiliated with Striver or takeUforward.**

This app does NOT store or display full question statements or editorial content. It only stores metadata (title, platform, URL, difficulty, topic) and opens questions in the browser.

## 🚀 Features

### Core Features
- **Offline-First Architecture**: Full functionality without internet connection
- **Progress Tracking**: Track your solving status for 456+ DSA problems
- **Spaced Repetition**: Smart revision system with configurable intervals
- **Daily Goals**: Set problems/day or minutes/day targets
- **Streak Tracking**: Monitor your consistency and build habits
- **Notes System**: Take and sync notes for each problem
- **Statistics**: Comprehensive charts and insights
- **Calendar View**: Visual representation of your daily activity

### Technical Features
- **MVVM Architecture**: Clean separation of concerns
- **Room Database**: Robust local data storage
- **Jetpack Compose**: Modern declarative UI
- **Hilt DI**: Dependency injection
- **Kotlin Coroutines & Flow**: Reactive data streams
- **Material3 Design**: Modern, beautiful UI
- **WorkManager**: Background task scheduling
- **Chrome Custom Tabs**: Seamless browser integration

## 📋 Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Kotlin**: 1.9.20
- **Gradle**: 8.2.0

## 🏗️ Project Structure

```
app/src/main/java/com/dsatracker/
├── data/
│   ├── local/
│   │   ├── entity/          # Room entities
│   │   ├── dao/             # Room DAOs
│   │   ├── database/        # Database setup
│   │   └── converters/      # Type converters
│   └── repository/          # Repository layer
├── domain/
│   ├── model/               # Domain models
│   └── usecase/             # Business logic use cases
├── ui/
│   ├── screen/              # Compose screens
│   ├── components/          # Reusable UI components
│   ├── navigation/          # Navigation setup
│   └── theme/               # Material3 theme
├── di/                      # Hilt modules
├── notification/            # Notification handling
└── utils/                   # Utility classes
```

## 📦 Dependencies

### Core Dependencies
- AndroidX Core KTX
- AndroidX Lifecycle & ViewModel
- Jetpack Compose (BOM 2024.01.00)
- Material3
- Navigation Compose

### Database
- Room (2.6.1)
  - room-runtime
  - room-ktx
  - room-compiler (KSP)

### Dependency Injection
- Hilt Android (2.48)
- Hilt Navigation Compose

### Async Programming
- Kotlin Coroutines
- Kotlin Flow

### Additional
- WorkManager
- Gson (JSON parsing)
- Chrome Custom Tabs
- DataStore Preferences
- MPAndroidChart (for statistics)

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/KYPKRISHNAREDDY/StriverTracker.git
cd StriverTracker
```

### 2. Open in Android Studio

- Open Android Studio (Hedgehog or later recommended)
- File -> Open -> Select the project directory
- Wait for Gradle sync to complete

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the App

- Connect an Android device or start an emulator
- Click Run in Android Studio or use:

```bash
./gradlew installDebug
```

## 📊 Database Schema

### Core Entities

**UserEntity**: User profile and settings
- `id`, `name`, `email`, `currentGoalId`

**SheetEntity**: DSA sheet information
- `id` (e.g., "STRIVER_A2Z"), `name`, `description`

**TopicEntity**: Topics within a sheet (e.g., "Arrays", "Graphs")
- `id`, `sheetId`, `name`, `orderIndex`

**ProblemEntity**: Individual problems
- `id`, `sheetId`, `topicId`, `title`, `platform`, `url`, `difficulty`, `tags`

**UserProblemProgressEntity**: User's progress per problem
- `userId`, `problemId`, `status`, `timesSolved`, `lastSolvedAt`, `needsRevision`, `starred`

**RevisionCardEntity**: Spaced repetition cards
- `userId`, `problemId`, `nextRevisionDateUtc`, `difficultyRating`, `reviewCount`

**DailyStatsEntity**: Daily activity statistics
- `userId`, `dateLocalEpochDay`, `problemsSolvedCount`, `revisionsCount`, `minutesStudied`

**GoalEntity**: User goals
- `userId`, `mode` (PROBLEMS_PER_DAY/MINUTES_PER_DAY), `targetProblemsPerDay`, `daysPerWeek`

## 🎯 Core Business Logic

### Problem Status Transitions

The `UpdateProblemStatusUseCase` handles:
- Status changes: NOT_STARTED → IN_PROGRESS → SOLVED (or SKIPPED)
- Incrementing `timesSolved` only when transitioning TO SOLVED
- Recording daily stats automatically
- Awarding XP points

### Revision System

The `ScheduleRevisionCardUseCase` implements spaced repetition:
- **HARD**: Review in 1 day
- **OKAY**: Review in 3 days
- **EASY**: Review in 7 days

Cards are automatically deactivated if problem status changes from SOLVED.

### Streak Calculation

The `CalculateStreakUseCase` considers a day "active" if:
- Problems solved ≥ 1, OR
- Revisions completed ≥ 1, OR
- Minutes studied ≥ 20

## 🔔 Notifications

### Daily Reminder
- Shows count of new problems and due revisions
- Configurable time
- Uses AlarmManager for exact timing

### Weekly Summary
- Summary of week's progress
- Problems solved and revisions completed

## 🎨 UI Screens

1. **SplashScreen**: Initial loading
2. **OnboardingScreen**: First-time user experience
3. **AuthScreen**: Login/Guest mode
4. **SetupGoalScreen**: Initial goal configuration
5. **HomeScreen**: Today's dashboard with streak, goals, and due items
6. **SheetOverviewScreen**: All topics with progress
7. **TopicProblemsScreen**: Problems list with filters
8. **ProblemDetailScreen**: Problem details, notes, revision controls
9. **RevisionScreen**: Due revision cards
10. **NotesScreen**: All notes and starred problems
11. **CalendarScreen**: Monthly activity heatmap
12. **StatsScreen**: Charts and analytics
13. **SettingsScreen**: App configuration
14. **AboutScreen**: App info and disclaimer

## 📝 Data Preloading

The app includes a `striver_a2z.json` file in `res/raw/` with:
- Sheet metadata
- Topics and categories
- 456+ problems with:
  - Title
  - Platform (LeetCode/GFG/TUF)
  - URL
  - Difficulty
  - Tags

**Note**: The current JSON is a condensed version for demonstration. In production, include the complete dataset from the original Striver A2Z sheet.

## 🔐 Data Privacy

- All data stored locally using Room
- No data sent to external servers by default
- Optional Firebase integration for cloud sync (future feature)
- Users can export their notes as plain text

## 🚧 Future Enhancements

- [ ] Full implementation of all 15+ UI screens
- [ ] Complete navigation flow
- [ ] Firebase Auth integration (optional)
- [ ] Cloud sync with Firestore
- [ ] Advanced statistics and charts
- [ ] Dark mode customization
- [ ] Custom themes
- [ ] Problem filters and search
- [ ] Import/Export functionality
- [ ] Widget support
- [ ] Tablet/large screen optimization

## 🤝 Contributing

This is a personal tracking app. Feel free to fork and customize for your own use.

## 📄 License

MIT License - See LICENSE file for details

## 🙏 Acknowledgments

- **Striver (Raj Vikramaditya)** for the comprehensive A2Z DSA Sheet
- **takeUforward** for the excellent DSA content
- The Android development community

## 📧 Contact

For questions or suggestions:
- GitHub: [@KYPKRISHNAREDDY](https://github.com/KYPKRISHNAREDDY)
- Create an issue in this repository

---

**Remember**: This app is a personal productivity tool. The actual learning happens when you solve the problems!

Happy Coding! 🚀
