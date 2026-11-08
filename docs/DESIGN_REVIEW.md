# Cooked Design Review & Brand Quality Checklist

This document details the deliberate design choices, layout rhythm, color contrast requirements, and motion standards enforced across **Cooked** to ensure a crafted, premium product feel with zero generic or AI-generated aesthetics.

---

## 1. Visual Rhythm & 4dp/8dp Spatial Grid

- [x] **Base Spatial Grid**: All padding, margins, spacers, and component heights adhere strictly to multiples of `4dp` and `8dp`.
- [x] **Screen Edge Boundary (20dp)**: Standard horizontal gutter padding on all primary content screens (Today, Deadlines, Insights, Settings) is fixed at `20dp`.
- [x] **Vertical Item Spacing**:
  - Compact lists: `10dp` to `12dp` spaced arrangement.
  - Standard cards and sections: `14dp` to `16dp` vertical spacing.
  - Section headers: `24dp` top margin, `8dp` bottom margin.
- [x] **Corner Radii Hierarchy** (`CookedRadius`):
  - Chips / Tags: `10dp` (`RoundedCornerShape(10.dp)`)
  - Buttons / Switches: `14dp` (`RoundedCornerShape(14.dp)`)
  - Cards / Containers: `20dp` (`RoundedCornerShape(20.dp)`)
  - Sheets / Dialogs / Surfaces: `28dp` (`RoundedCornerShape(28.dp)`)
  - Action FABs: Distinctive `18dp` squircle, deliberately avoiding generic circular FABs.

---

## 2. Palette, Dark Mode & Contrast Compliance

- [x] **Deep Warm Dark Theme (`#0E0D0C`)**:
  - Zero harsh `#000000` AMOLED blacks.
  - Surface elevations layered on top of `#0E0D0C`:
    - `DarkSurface`: `#151311`
    - `DarkSurfaceContainerLow`: `#191715`
    - `DarkSurfaceContainer`: `#1F1C19`
    - `DarkSurfaceContainerHigh`: `#282420`
- [x] **Brand Primary (Ember)**:
  - Light mode: `#C2410C` (Ember)
  - Dark mode: `#FF7A45` (Ember glow)
- [x] **Heat Scale Colors**:
  - **Chill**: `#2F9E94` (Calm teal) / Container `#E0F5F2`
  - **Warm**: `#C98A04` (Warm amber) / Container `#FEF3C7` / High-contrast text `#854D0E`
  - **Toasty**: `#E2620F` (Bright orange) / Container `#FFEDD5`
  - **Cooked**: `#D61F45` (Intense crimson) / Container `#FFE4E8`
  - **Done**: `#3C8D5A` (Forest emerald) / Container `#E8F5E9`
- [x] **Contrast Ratios (WCAG 2.1 AA)**:
  - All body copy, dates, and subtitle text maintain at least `4.5:1` contrast ratio against their respective surface.
  - Large headings and brand indicators maintain at least `3.0:1` contrast ratio.
  - Text on colored badges uses calibrated high-contrast tokens (e.g. `HeatWarmLightText`).

---

## 3. Elevation & Interaction Polish

- [x] **Ember-Tinted Ripples**:
  - Global ripple behavior configured via `@OptIn(ExperimentalMaterial3Api::class) LocalRippleConfiguration provides RippleConfiguration(color = cookedColors.primary)`.
  - Replaces default grey/purple ripple wash with a warm ember pulse.
- [x] **Zero TopAppBar Elevation**:
  - TopAppBars use flat, un-elevated containers (`containerColor = background`).
  - Scrolled states gently transition to `surfaceContainerLow` without artificial drop shadows or harsh dividers.
- [x] **Custom FAB Shapes**:
  - Floating Action Buttons use `RoundedCornerShape(18.dp)` squircle rather than standard Android circular shapes.

---

## 4. Typography & Tabular Figures

- [x] **Tabular Figures (`tnum`)**:
  - All countdown timers, Cooked Score readouts, and metric statistics enforce `fontFeatureSettings = "tnum"`.
  - Prevents horizontal jitter as digits count down or roll over.
- [x] **Number Rolling Motion**:
  - Score changes utilize animated rolling transitions (`RollingNumberText`).
- [x] **Hierarchy**:
  - Prominent bold display titles, clear medium subheads, and muted metadata (`onSurfaceVariant`).

---

## 5. Mascot Egg & Personality

- [x] **5 Mascot States**:
  - **Chill**: Raw egg, whole shell, calm and composed.
  - **Warm**: Sunny-side up, soft yolk, single tiny nervous sweat drop.
  - **Toasty**: Crispy brown edges, wavy border, worried wide eyes.
  - **Cooked**: Burnt charred shell, dramatic X eyes, curls of rising smoke.
  - **Done**: Golden egg with satisfied wink and chef's kiss pose.
- [x] **Animation Details**:
  - Idle wobble every ~6 seconds.
  - Steam particle physics (4 drifting particles) on Toasty and Cooked states.
  - Squash-and-stretch hop on heat state transitions.
  - **Accessibility Respect**: Checks `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` to freeze into static frames for users with reduce-motion enabled.
- [x] **Placement Policy**:
  - Displayed prominently in Deadlines hero, Today header status line, empty states, widgets (static), Cooked Report, onboarding, and paywall.
  - Strictly excluded from item rows to keep task lists legible and calm.

---

## 6. Audio & Haptics

- [x] **Custom Sound Assets**:
  - Normal nag: `res/raw/sizzle_soft.ogg`
  - Escalated nag: `res/raw/sizzle_loud.ogg`
  - Critical / Final call: `res/raw/kettle.ogg`
  - In-app completion: `res/raw/done_pop.ogg`
- [x] **Silent / Vibrate Guard**:
  - Audio playback strictly honors device ringer mode (`AudioManager.RINGER_MODE_NORMAL`). Audio is completely suppressed in silent or vibrate modes.
- [x] **Swipe-to-Done Haptic Choreography**:
  - Gentle tick when drag passes the completion threshold.
  - Firm confirmation haptic when released and completed.

---

## 7. Loading & Transition Polish

- [x] **Shimmer Skeletons**:
  - Zero indeterminate circular spinners on content screens (Today, Deadlines, Insights, Detail, Config).
  - Shimmer placeholders mirror exact content geometry while loading.
- [x] **Predictive Back Navigation**:
  - Enabled via `android:enableOnBackInvokedCallback="true"` in `AndroidManifest.xml`.
