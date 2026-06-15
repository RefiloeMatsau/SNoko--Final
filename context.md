# Snokonoko — Feature Implementation Context

This file tracks progress as we implement each feature from future-features.md.
Updated at the end of every milestone.

---

## Feature Roadmap (in order)

1. Streaks — daily login, no-spend day, months under budget
2. Badges
3. Challenges — weekly & monthly
4. Savings Goals
5. AI (DeepSeek) — insights, transaction dump, budget recommendations, challenge generator

---

## Phase Log

### Phase 1 — Streaks
Status: complete

**What was done:**
- Created `StreakManager.kt` — handles all 3 streak calculations using SharedPreferences + transaction data
- Daily login streak: stored in prefs, increments each new consecutive day, resets if a day is missed
- No-spend streak: computed live from transactions — counts consecutive days backwards from today with no expense
- Months under budget streak: checks each past month against the user's monthly spending goal, counts consecutive passing months
- Added a STREAKS card to `fragment_home.xml` showing all 3 values side by side (pink / green / orange)
- Wired streak display into `HomeFragment.kt` via `updateStreaks()`, refreshes whenever transactions update
- Called `updateLoginStreak()` in `MainActivity.onCreate` so the streak ticks up every time the app is opened

**Challenges:**
- No-spend streak needed a guard (≤365) to prevent an infinite loop if a user has never logged an expense
- Budget streak only checks months where a monthly goal was set — months with no goal are treated as a streak break to avoid false counts
- Login streak shares `snokonoko_prefs` (cleared on logout) so it correctly resets per user

### Phase 2 — Badges
Status: complete

**What was done:**
- Created `Badge.kt` — simple data class (id, name, description, color, earned)
- Created `BadgeManager.kt` — evaluates all 10 badges against live data and persists earned IDs in SharedPreferences
- 10 badges: First Step, Week Warrior, Month Legend, Saving Start, Spend Nothing, Budget Kept, Hat-trick, Half Century, Century, Big Saver
- Created `item_badge.xml` — row layout with colored circle, name, description, and EARNED/Locked status
- Created `fragment_badges.xml` — scrollable list with earned count summary card at the top
- Created `BadgesFragment.kt` — inflates badge rows, earned badges show in color, locked badges are greyed out
- Added "Badges" button to `fragment_account.xml` and wired navigation in `AccountFragment.kt`

**Challenges:**
- Badges are re-evaluated every time transactions update, which is fine at this data size but could be moved to a background coroutine if the list grows large
- Earned badges are persisted so they are never un-earned (badges are permanent milestones)

### Phase 3 — Challenges
Status: complete

**What was done:**
- Created `Challenge.kt` — data class with built-in `isComplete` and `progress` computed properties
- Created `ChallengeManager.kt` — pool of 8 weekly + 5 monthly challenge templates; picks 2 weekly and 1 monthly per period using the ISO week/month number as a deterministic selector; computes live progress from transaction data
- Weekly challenges reset every Monday; monthly challenges reset on the 1st of each month; selections are stored in SharedPreferences so they remain stable within the period
- Created `fragment_challenges.xml` — two sections (THIS WEEK / THIS MONTH) with their own containers
- Created `ChallengesFragment.kt` — builds challenge cards programmatically matching the BudgetFragment style; each card shows title, description, progress bar, current/target label, and a DONE/% chip
- Added "Challenges" button to `fragment_account.xml` and wired navigation in `AccountFragment.kt`

**Challenges:**
- Had to ensure the two weekly picks are always different (added +1 fallback if indices collide)
- Progress direction differs per metric: cap metrics count down (lower is better), count/save metrics count up — handled cleanly in `Challenge.progress` and `isComplete`
- Phase 5 AI will replace the deterministic pool selection with personalised AI-generated challenges

### Phase 4 — Savings Goals
Status: pending

### Phase 5 — AI (DeepSeek)
Status: pending
