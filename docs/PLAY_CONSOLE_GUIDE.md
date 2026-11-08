# Google Play Console Launch Guide: Cooked

This guide provides step-by-step instructions to configure, submit, test, and publish **Cooked: Deadline Reminders** (`com.cooked.app`) on the Google Play Console.

---

## 1. App Details & Store Presence

- **App Name**: `Cooked: Deadline Reminders` (26 / 30 chars)
- **Default Language**: English (United States)
- **App Type**: App
- **Free / Paid**: Free (monetized via in-app subscriptions and one-time purchases)
- **Category**: Productivity / Tools
- **Tags**: Productivity, Task Management, To-do list, Reminders, Education

---

## 2. Store Listing Assets (from `docs/STORE_LISTING.md`)

- **Short Description**: `Reminders that won't shut up. Heat-driven deadline countdowns that beat ADHD.` (78 / 80 chars)
- **Full Description**: Copy complete text from [`docs/STORE_LISTING.md`](file:///Users/mdkasifuddin/Developer/Andriod/Cooked/docs/STORE_LISTING.md)
- **App Icon**: 512 x 512 px PNG, 32-bit color with Ember flame motif.
- **Feature Graphic**: 1024 x 500 px PNG/JPEG (see specs in `STORE_LISTING.md`).
- **Phone Screenshots**: 8 screenshots (mix of Light and Dark themes, 1080 x 2400 px or 1440 x 3120 px).

---

## 3. Policy & App Content Declarations

### A. Privacy Policy
- Provide public URL pointing to [`docs/PRIVACY_POLICY.md`](file:///Users/mdkasifuddin/Developer/Andriod/Cooked/docs/PRIVACY_POLICY.md).

### B. Data Safety Questionnaire
- Follow the exact field-by-field answers documented in [`docs/PLAY_DATA_SAFETY.md`](file:///Users/mdkasifuddin/Developer/Andriod/Cooked/docs/PLAY_DATA_SAFETY.md).
- Key declaration: **Zero personal task data is collected or shared**. Only optional crash stack traces (Firebase Crashlytics) for diagnostic purposes.

### C. Target Audience & Content Rating
- **Target Age**: 13+ (Ages 13 to 17, 18 and over).
- **Does the app appeal to children?**: No.
- **Content Rating Questionnaire**:
  - Violence, blood, profanity, drugs, gambling: **No to all**.
  - Rating received: **PEGI 3 / Everyone**.

### D. Sensitive Permissions Declarations
1. **Exact Alarms (`SCHEDULE_EXACT_ALARM`)**:
   - Permitted use category: **Alarms and reminders**
   - Justification statement:
     > *"Cooked is a time-critical reminder and deadline tracking application. The core feature of the app is scheduling and delivering exact alerts and persistent escalating reminders at user-configured timestamps without Doze mode delays. Without exact alarm scheduling, reminders and deadline countdowns would fail to alert users reliably."*
2. **Full-Screen Intent (`USE_FULL_SCREEN_INTENT`)**:
   - Category: **Alarm / Timer**
   - Justification statement:
     > *"Used exclusively when the user marks an item as Critical, allowing the phone screen to turn on and display full-screen alarm controls over the lock screen until the user acknowledges or snoozes the alert."*

---

## 4. Monetization & Products (RevenueCat / Play Billing)

Create the following products under **Monetize > Products**:

### Subscriptions
- **Subscription ID**: `cooked_pro_sub`
  1. **Base Plan**: `cooked_pro_yearly`
     - Billing period: 1 Year
     - Free Trial: 7 days
     - Pricing: $14.99 / year (US) | ₹599 / year (India)
  2. **Base Plan**: `cooked_pro_monthly`
     - Billing period: 1 Month
     - Free Trial: None
     - Pricing: $2.99 / month (US) | ₹99 / month (India)

### In-App Products (One-Time)
- **Product ID**: `cooked_pro_lifetime`
  - Name: Cooked Pro Lifetime
  - Type: Non-consumable
  - Pricing: $39.99 (US) | ₹1,499 (India)

---

## 5. 14-Day Closed Testing Track (Play Console Requirement)

For personal Google Play accounts, Google requires **12+ testers enrolled for at least 14 consecutive days** before applying for production access.

### Setup Checklist:
1. Go to **Release > Testing > Closed testing**.
2. Create track named `alpha-testers`.
3. Create an email list with at least 12 testers (friends, peers, study group).
4. Upload signed Android App Bundle (`.aab`).
5. Share the opt-in link (`https://play.google.com/apps/testing/com.cooked.app`).
6. Verify all 12 testers install the app and leave feedback.
7. After 14 days, click **Apply for Production**.
