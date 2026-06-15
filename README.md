<div align="center">

# SNOKONOKO

**A personal finance tracking app for Android built with modern architecture and clean design principles.**

<br/>

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/Min_API-24-pink?style=flat-square)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-pink?style=flat-square)
![Room](https://img.shields.io/badge/Database-Room-pink?style=flat-square)

</div>

---

## ◈ Overview

Snokonoko helps you track income and expenses with a focus on **simplicity** and **clarity**. The app is designed for everyday users who want to understand their spending habits without the complexity of traditional finance applications.

The core philosophy is **mindful tracking** — when you manually record each transaction, you become more aware of your spending patterns. The clean interface provides instant insights without overwhelming you with unnecessary features.

**Target Users**
- Individuals wanting to track personal finances
- Students managing limited budgets
- Anyone seeking a simple alternative to complex banking apps
- Users who prefer manual entry for better spending awareness

---

## ◈ Key Features

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;User Authentication</summary>
<br/>

Secure local account system with email and password login. Each user's data is isolated, ensuring privacy even on shared devices. Multi-user support allows different people to use the same app with completely separate financial data.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Home Dashboard</summary>
<br/>

Instant financial snapshot showing current balance, monthly income and expenses, recent transactions, and monthly goal progress. The dashboard provides immediate visibility into your financial health without navigation.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Transaction Management</summary>
<br/>

Quick transaction entry via bottom sheet with income/expense toggle, category selection, description, amount, date, and optional photo attachment. Edit and delete functionality ensures data accuracy. The type badge clearly indicates whether you're adding an expense or income.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Custom Categories</summary>
<br/>

Built-in categories (food, transport, shopping, medical, etc.) with the ability to create custom categories. Each category has a distinct color for visual identification in charts and lists.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Budget Tracking</summary>
<br/>

Set spending limits per category with real-time progress tracking. Visual indicators show when you're approaching budget limits, helping prevent overspending before it happens.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Monthly Goals</summary>
<br/>

Define minimum and maximum spending targets for each month. Progress bars on the home screen show how close you are to your goals, providing big-picture financial context.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Date Filtering</summary>
<br/>

Filter transactions by custom date ranges to analyze specific periods. Useful for tracking spending during vacations, events, or any time frame that doesn't align with calendar months.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Category Breakdown</summary>
<br/>

View spending totals by category within any date range. Identifies largest spending areas to inform better financial decisions.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Analytics</summary>
<br/>

Visual charts including pie charts for category distribution and bar charts for income vs expense comparison. Charts are integrated into the Reports tab for easy access.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Photo Attachments</summary>
<br/>

Attach receipt photos or item images to transactions. Useful for warranty claims, returns, tax documentation, or simply remembering what you purchased.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Profile Management</summary>
<br/>

Users can update their name and surname through the settings screen. Email remains read-only for account stability.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Admin Access</summary>
<br/>

View registered users protected by admin password (555). Allows administrators to see all accounts in the system.

</details>

---

## ◈ Getting Started

### Prerequisites

```
◆  Android Studio Hedgehog or later
◆  Android SDK API 24+ (Android 7.0)
◆  Kotlin 1.9+
```

### Opening in Android Studio

```
01  →  Clone or download the repository
02  →  Open Android Studio
03  →  Select "Open an Existing Project"
04  →  Navigate to the project directory and select it
05  →  Wait for Gradle sync to complete
06  →  Connect an Android device or start an emulator
07  →  Click the Run button or press Shift+F10
```

### Building from Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

---

## ◈ File Structure

```
MoneyThing/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/snokonoko/app/
│           │   ├── data/
│           │   │   ├── AppDatabase.kt           # Room database configuration
│           │   │   ├── User.kt                   # User entity
│           │   │   ├── Transaction.kt            # Transaction entity
│           │   │   ├── Budget.kt                 # Budget entity
│           │   │   ├── Category.kt               # Category entity
│           │   │   ├── MonthlyGoal.kt            # Monthly goal entity
│           │   │   ├── UserDao.kt                # User data access
│           │   │   ├── TransactionDao.kt         # Transaction data access
│           │   │   ├── BudgetDao.kt              # Budget data access
│           │   │   ├── CategoryDao.kt            # Category data access
│           │   │   └── MonthlyGoalDao.kt         # Monthly goal data access
│           │   │
│           │   ├── repository/
│           │   │   ├── FinanceRepository.kt      # Main data repository
│           │   │   └── UserRepository.kt         # Authentication repository
│           │   │
│           │   ├── viewmodel/
│           │   │   ├── MainViewModel.kt          # Core business logic
│           │   │   └── UserViewModel.kt          # Authentication logic
│           │   │
│           │   ├── ui/
│           │   │   ├── MainActivity.kt           # Main activity with navigation
│           │   │   ├── LoginActivity.kt          # Login/signup screen
│           │   │   ├── HomeFragment.kt           # Dashboard
│           │   │   ├── ReportsFragment.kt        # Analytics and charts
│           │   │   ├── BudgetFragment.kt         # Budget management
│           │   │   ├── AccountFragment.kt        # Settings and profile
│           │   │   ├── CategoriesFragment.kt     # Category management
│           │   │   ├── AddTransactionSheet.kt    # Transaction form
│           │   │   ├── AddBudgetSheet.kt         # Budget form
│           │   │   ├── DateFilterFragment.kt     # Date range filter
│           │   │   └── CategoryTotalsFragment.kt # Category breakdown
│           │   │
│           │   └── SnokonokoApplication.kt       # Application class
│           │
│           └── res/
│               ├── layout/
│               │   ├── activity_main.xml
│               │   ├── activity_login.xml
│               │   ├── fragment_home.xml
│               │   ├── fragment_reports.xml
│               │   ├── fragment_budget.xml
│               │   ├── fragment_account.xml
│               │   ├── fragment_categories.xml
│               │   ├── sheet_add_transaction.xml
│               │   ├── sheet_add_budget.xml
│               │   └── ...
│               │
│               ├── drawable/
│               │   ├── btn_primary.xml
│               │   ├── btn_ghost.xml
│               │   ├── btn_danger.xml
│               │   ├── input_background.xml
│               │   └── card_background.xml
│               │
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               │
│               └── menu/
│                   └── bottom_nav.xml
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew
```

---

## ◈ Architecture

The app follows **MVVM** (Model-View-ViewModel) architecture with a **Repository pattern** for data access.

### Data Layer
- **Room Database** — Local SQLite database with 5 entities
- **Entities** — User, Transaction, Budget, Category, MonthlyGoal
- **DAOs** — Data access objects for database operations
- **Repository** — FinanceRepository and UserRepository mediate data access

### ViewModel Layer
- **MainViewModel** — Handles transactions, budgets, categories, and goals
- **UserViewModel** — Manages authentication and user operations
- **LiveData** — Reactive data that automatically updates UI on changes

### View Layer
- **Fragments** — Reusable UI components for each screen
- **Bottom Sheets** — Modal forms for data entry
- **XML Layouts** — Declarative UI definitions

### Key Patterns

| Pattern | Usage |
|:---|:---|
| Repository Pattern | Centralizes data access logic |
| Observer Pattern | LiveData for reactive UI updates |
| Dependency Injection | Manual DI through ViewModel providers |
| Coroutines | Asynchronous database operations |

---

## ◈ Database Schema

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Users Table</summary>
<br/>

| Column | Type | Notes |
|:---|:---|:---|
| `id` | INTEGER | Primary Key, Auto-increment |
| `firstName` | TEXT | — |
| `surname` | TEXT | — |
| `email` | TEXT | Unique |
| `password` | TEXT | — |

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Transactions Table</summary>
<br/>

| Column | Type | Notes |
|:---|:---|:---|
| `id` | INTEGER | Primary Key, Auto-increment |
| `userId` | INTEGER | Foreign Key |
| `type` | TEXT | income / expense |
| `category` | TEXT | — |
| `description` | TEXT | — |
| `amount` | REAL | — |
| `date` | TEXT | — |
| `startTime` | TEXT | Optional |
| `endTime` | TEXT | Optional |
| `photoPath` | TEXT | Optional |

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Budgets Table</summary>
<br/>

| Column | Type | Notes |
|:---|:---|:---|
| `id` | INTEGER | Primary Key, Auto-increment |
| `userId` | INTEGER | Foreign Key |
| `category` | TEXT | — |
| `limitAmount` | REAL | — |

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Categories Table</summary>
<br/>

| Column | Type | Notes |
|:---|:---|:---|
| `id` | INTEGER | Primary Key, Auto-increment |
| `userId` | INTEGER | Foreign Key |
| `name` | TEXT | — |
| `colour` | TEXT | — |
| `isDefault` | BOOLEAN | — |

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Monthly Goals Table</summary>
<br/>

| Column | Type | Notes |
|:---|:---|:---|
| `userId` | INTEGER | Primary Key |
| `monthYear` | TEXT | Primary Key |
| `minGoal` | REAL | — |
| `maxGoal` | REAL | — |

</details>

---

## ◈ Technical Decisions

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Room Database</summary>
<br/>

Chosen over raw SQLite for compile-time query validation, automatic migrations, and coroutine support. Room is the recommended Android persistence solution.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;MVVM Architecture</summary>
<br/>

Google's recommended architecture pattern. Separates concerns, improves testability, and handles configuration changes gracefully. ViewModels survive screen rotations.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Manual Transaction Entry</summary>
<br/>

Deliberate choice to promote spending awareness. Automatic bank imports create invisible data that users rarely review. Manual entry forces reflection on each transaction.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Bottom Sheet for Forms</summary>
<br/>

Modal UI pattern that maintains context. Users can see the underlying screen while entering data. Modern UI pattern that feels natural on mobile.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Dark Theme Default</summary>
<br/>

Reduces eye strain, saves battery on OLED screens, and provides a premium aesthetic. Pink accent color creates visual distinction from typical finance apps.

</details>

<details>
<summary>&nbsp;&nbsp;▸&nbsp;&nbsp;Local-Only Storage</summary>
<br/>

No cloud sync for privacy and simplicity. Data stays on the device. Future cloud sync can be added through the Repository layer without changing ViewModels.

</details>

---

## ◈ Summary

Snokonoko is a thoughtfully designed personal finance application that prioritizes user experience and simplicity over feature complexity. The app demonstrates modern Android development practices including MVVM architecture, Room persistence, and reactive programming with LiveData.

The multi-user data isolation ensures privacy while the category system, budget tracking, and analytics provide meaningful insights into spending patterns. The clean, dark-themed interface with pink accents creates a distinctive and pleasant user experience.

Key technical achievements include proper separation of concerns through the Repository pattern, reactive UI updates via LiveData, and a well-structured database schema supporting the full feature set. The codebase serves as a reference for implementing modern Android applications with local data persistence.

---

<div align="center">
<sub>built with kotlin &nbsp;·&nbsp; mvvm &nbsp;·&nbsp; room</sub>
</div>
