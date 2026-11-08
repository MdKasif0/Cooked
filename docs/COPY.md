# Cooked Copy Bible

> **The Voice of Cooked**: Relentless, self-aware, deadpan, and deeply respectful of the user's intelligence. We are the annoying friend who actually cares that you don't fail your class or get fired. We roast the *task*, never the *person*.

---

## 📜 The Five Non-Negotiable Rules

### 1. Short & Punchy
- Notifications: lowercase is allowed and encouraged for informal conversational punch (e.g. `me again. {title} isn't doing itself. 👀`).
- UI Headings & Body: Sentence case only. No title-casing of random words.
- If a sentence can be cut in half without losing meaning, cut it.

### 2. Specific Beats Generic
- Always embed the `{title}`, `{time}`, `{count}`, or `{minutes}`.
- Never write: *"You have an upcoming task due soon."*
- Always write: *"Time for {title}."* or *"{title} is due at 5:00 PM."*

### 3. Strict Emoji Allowance
- Maximum **one emoji per line**.
- **Allowed emoji set only**:
  - 💀 (Skull — used when high heat or dramatic unhinged alert)
  - 🔥 (Fire / Ember — heat indicator and countdowns)
  - 🥚 (Egg — our signature mascot)
  - 👀 (Eyes — watching persistent pending items)
  - 🥺 (Pleading face — rare soft nudges)
  - 🫡 (Saluting face — completion acknowledgments and mutual respect)
  - 🍳 (Skillet / Cooking — cooking and preparation)
- Any other emoji is banned unless the user explicitly selected it as a task icon.

### 4. Zero Shame Policy
- Never mention body, weight, diet, sleep hygiene shaming, intelligence, money troubles, or clinical mental health.
- No streaks that guilt the user when life gets overwhelming.
- Roast the absurdity of procrastination and the stubbornness of the task, not the human being behind the screen.

### 5. Banned Corporate AI Phrases
The following phrases are permanently forbidden in any Cooked surface, comment, or notification:
- ❌ *"boost your productivity"*
- ❌ *"welcome back"*
- ❌ *"unlock your potential"*
- ❌ *"game-changer"*
- ❌ *"seamless"*
- ❌ *"let's get started"*
- ❌ 🚀 (Rocket emoji)

---

## 🇮🇳 Hinglish Vibes Mode

Cooked supports an in-app toggle: **"Hinglish vibes"** (`UserPreferencesRepository.hinglishVibes`).
- **Core Rule**: UI structural labels remain in English (`Today`, `Deadlines`, `Insights`, `Settings`, `Save`, `Cancel`).
- **Personality Copy Changes**: Notifications, empty states, and status subtitles adopt casual, affectionate Indian peer phrasing:
  - *First alert*: `bhai, {title} ka time ho gaya. nipta le! 🍳`
  - *Early nag*: `abhi bhi pending? {title} khud nahi hone wala. 👀`
  - *Persistent nag*: `kuch toh sharam kar, {title} kab karega? 💀`
  - *Critical alert*: `FULL COOKED! abhi ke abhi {title} khatam kar! 🔥`
  - *Empty today*: `kuch pending nahi hai. shaanti hi shaanti.`

---

## 🗂️ Copy Catalog (`res/values/copy.xml`)

Every key in `app/src/main/res/values/copy.xml` is drafted with **three deliberate options** marked with `<!-- TODO: Pick Option 1, Option 2, or Option 3 -->`.

| Surface | XML Key | Active Text |
| :--- | :--- | :--- |
| **Notification Mild First** | `notif_mild_first` | `{title} is due now. 🍳` |
| **Notification Spicy First** | `notif_spicy_first` | `Clock's ticking on {title}. 🔥` |
| **Notification Unhinged First** | `notif_unhinged_first` | `THE EGG DEMANDS: {title}. 🥚` |
| **Notification Spicy Early** | `notif_spicy_early` | `me again. {title} isn't doing itself. 👀` |
| **Notification Spicy Persistent** | `notif_spicy_persistent` | `bro, do {title} already. 🔥` |
| **Notification Spicy Final** | `notif_spicy_final` | `DEFCON 1: {title} is officially burning. 🔥` |
| **Empty Today Title** | `empty_today_title` | `Nothing due. Suspiciously calm.` |
| **Empty Today Subtitle** | `empty_today_subtitle` | `No active reminders. Add one before life sneaks up on you.` |
| **Empty Deadlines Title** | `empty_deadlines_title` | `All clear!` |
| **Empty Deadlines Subtitle** | `empty_deadlines_subtitle` | `No deadlines. Either you're on top of life or you haven't added them yet.` |
| **Toast Item Saved** | `toast_item_saved` | `Locked in. I'll yap at {time}.` |
| **Toast Item Done** | `toast_item_done` | `Cooked another one. Nice.` |
| **Toast Item Snoozed** | `toast_item_snoozed` | `Snoozed for {duration}. I will be back. 👀` |
| **Onboarding Hook Title** | `onboarding_hook_title` | `Reminders that won't shut up.` |
| **Onboarding Hook Description** | `onboarding_hook_desc` | `Cooked nag-alerts you until you actually tap Done. Swiping it away does nothing. So you're never cooked.` |
| **Paywall Headline** | `paywall_headline` | `Never be cooked again.` |
| **Paywall Subhead** | `paywall_subhead` | `Everything in Free forever, plus full-screen alarm escalation, deep insights, and live countdown widgets.` |
| **Insights Cave Rate** | `insights_nags_per_win` | `You usually cave after {count} nags.` |
| **Insights Lateness** | `insights_lateness_median` | `When late, you're ~{minutes}m late. Respectable.` |
| **Insights Best Day** | `insights_best_weekday` | `{weekday} is your power day ({rate}% on time).` |
