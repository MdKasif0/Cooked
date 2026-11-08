# Cooked — Release Playbook & Google Play Launch Checklist

This document is the complete, self-contained launch checklist for releasing **Cooked: Deadline Reminders** (`com.cooked.app`) to the Google Play Store. You can follow this guide start to finish to prepare, sign, test, and publish the application.

---

## Phase 1: Pre-Release Quality & Build Verification

Before touching the Google Play Console, execute the following commands locally to verify code quality, test suite stability, and R8 minification:

```bash
# Set Android Studio JDK environment (macOS)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# 1. Run complete unit test suite (170+ unit tests across domain, data, scheduling, UI)
./gradlew testDebugUnitTest

# 2. Run static analysis
./gradlew detekt

# 3. Build optimized Release APK with R8 shrinking & obfuscation
./gradlew assembleRelease

# 4. Generate Google Play App Bundle (.aab)
./gradlew bundleRelease
```

The output bundle will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

---

## Phase 2: Keystore & Signing Configuration

Cooked uses **Google Play App Signing**. You sign the initial bundle with an upload key, and Google re-signs the app with their secure distribution key.

### 1. Generate an Upload Keystore (One-Time Setup)
Run this command from your terminal to generate a production upload keystore in the project root:

```bash
keytool -genkey -v \
  -keystore release.keystore \
  -alias cooked_key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Cooked Team, OU=Mobile, O=Cooked, L=Bangalore, ST=Karnataka, C=IN"
```

> [!CAUTION]
> Back up `release.keystore` in a secure location (e.g., 1Password or encrypted drive). Never commit raw keystore files or passwords to public GitHub repositories!

### 2. Configure Environment Variables for Signing
Export your keystore credentials in your shell before building:

```bash
export COOKED_KEYSTORE_PASSWORD="your_keystore_password"
export COOKED_KEY_ALIAS="cooked_key"
export COOKED_KEY_PASSWORD="your_key_password"
```

The `app/build.gradle.kts` file automatically detects `release.keystore` and signs the release build with these credentials.

---

## Phase 3: RevenueCat & In-App Purchases Setup

Cooked Pro features are unlocked via RevenueCat integration (`com.cooked.app.data.billing.RevenueCatBillingManager`).

### 1. Google Play Console Configuration
1. Go to **Monetize > In-app products**:
   - Create In-app product:
     - **Product ID**: `cooked_pro_lifetime`
     - **Name**: `Cooked Pro Lifetime`
     - **Price**: $39.99 (US) / ₹1,499 (India)
2. Go to **Monetize > Subscriptions**:
   - Create Subscription:
     - **Product ID**: `cooked_pro_sub`
     - **Base Plan 1**: `cooked_pro_yearly`
       - Billing period: 1 year
       - Free trial: 7 days
       - Price: $14.99 / year (US) / ₹599 / year (India)
     - **Base Plan 2**: `cooked_pro_monthly`
       - Billing period: 1 month
       - Free trial: None
       - Price: $2.99 / month (US) / ₹99 / month (India)

### 2. RevenueCat Dashboard Configuration
1. Log in to [RevenueCat Dashboard](https://app.revenuecat.com/).
2. Create project `Cooked`.
3. Add Google Play App (`com.cooked.app`) and link your Google Cloud Service Account JSON credentials.
4. Under **Entitlements**, create entitlement with Identifier: `pro`.
5. Under **Offerings**, create default offering:
   - Attach package `$rc_annual` -> `cooked_pro_yearly`
   - Attach package `$rc_monthly` -> `cooked_pro_monthly`
   - Attach package `$rc_lifetime` -> `cooked_pro_lifetime`
6. Copy your RevenueCat **Public Google API Key** into `RevenueCatBillingManager.kt`.

### 3. Verify License Testing (Zero-Cost Verification)
1. In Google Play Console, go to **Setup > License testing**.
2. Add your Google account email to the license tester list.
3. Install the release build on your test device.
4. Open the Paywall, subscribe to `cooked_pro_yearly` (Play Billing displays "Test card, always approves").
5. Verify:
   - Pro features unlock immediately (30d/90d Insights, Unhinged personality, Countdown widgets).
   - Test "Restore purchases": uninstall, reinstall, tap Restore, verify Pro status restores instantly.

---

## Phase 4: Google Play Console Submission Checklist

### 1. Store Presence
- [ ] **App Name**: `Cooked: Deadline Reminders`
- [ ] **Short Description**: `Reminders that won't shut up. Heat-driven deadline countdowns that beat ADHD.`
- [ ] **Full Description**: Copy formatted copy from [`docs/STORE_LISTING.md`](file:///Users/mdkasifuddin/Developer/Andriod/Cooked/docs/STORE_LISTING.md)
- [ ] **App Icon**: 512 x 512 px PNG (Ember flame on warm dark background)
- [ ] **Feature Graphic**: 1024 x 500 px PNG
- [ ] **Phone Screenshots**: 8 screenshots from `docs/STORE_LISTING.md`

### 2. Policy & Content
- [ ] **Privacy Policy URL**: Link to public [`docs/PRIVACY_POLICY.md`](file:///Users/mdkasifuddin/Developer/Andriod/Cooked/docs/PRIVACY_POLICY.md)
- [ ] **Data Safety**: Fill questionnaire exactly as answered in [`docs/PLAY_DATA_SAFETY.md`](file:///Users/mdkasifuddin/Developer/Andriod/Cooked/docs/PLAY_DATA_SAFETY.md)
- [ ] **Target Audience**: 13+ (no Families policy restrictions)
- [ ] **Content Rating**: Complete rating questionnaire (all "No") -> PEGI 3 / Everyone

### 3. Permission Declarations
- [ ] **SCHEDULE_EXACT_ALARM**:
  - Category: *Alarms and Reminders*
  - Paste justification statement from `docs/PLAY_CONSOLE_GUIDE.md`
- [ ] **USE_FULL_SCREEN_INTENT**:
  - Category: *Alarm / Timer*
  - Paste justification statement from `docs/PLAY_CONSOLE_GUIDE.md`

---

## Phase 5: Closed Testing (14 Days, 12+ Testers)

> [!IMPORTANT]
> Google Play policy requires personal developer accounts to maintain **at least 12 opt-in testers continuously for 14 days** before requesting production publishing.

1. Navigate to **Release > Testing > Closed testing**.
2. Create track `alpha-release`.
3. Upload `app-release.aab`.
4. Create tester email list containing at least 15 verified Google accounts (to account for inactive testers).
5. Copy the opt-in URL:
   `https://play.google.com/apps/testing/com.cooked.app`
6. Send the onboarding message to testers:
   > *"Hey! I'm testing Cooked, an offline-first deadline tracker with persistent nagging reminders. Please join using this link, install the build from Google Play, and keep it installed on your device for the next 14 days. Feel free to report any bug you notice!"*
7. Monitor daily active testers in the Play Console dashboard.
8. On Day 15, click **Apply for Production**.

---

## Phase 6: Production Rollout

1. Once production access is approved by Google:
2. Go to **Release > Production**.
3. Create new release with `app-release.aab`.
4. Add release notes:
   ```markdown
   Initial launch of Cooked: Deadline Reminders!
   • Heat-driven urgency engine: Chill, Warm, Toasty, Cooked
   • Smart Start-By guidance and tabular countdowns
   • Natural language Quick Add
   • Glance home-screen widgets & Quick Settings tile
   • 100% offline-first architecture with zero account required
   ```
5. Choose **Staged Rollout**:
   - Day 1: 20% of users
   - Day 2: 50% of users
   - Day 3+: 100% full rollout
6. Monitor Play Vitals for ANR rate (< 0.47%) and crash rate (< 1.09%).

---

## Summary Checklist

| Step | Milestone | Status |
| :--- | :--- | :--- |
| **01** | All 170+ unit tests passing cleanly | ✅ Completed |
| **02** | R8 release minification tested and verified | ✅ Completed |
| **03** | Auto Backup XML rules configured | ✅ Completed |
| **04** | RevenueCat products and fallback plans ready | ✅ Completed |
| **05** | Telemetry opt-out verified in Settings | ✅ Completed |
| **06** | Data Safety and Privacy Policy docs ready | ✅ Completed |
| **07** | Store listing copy and screenshot specs ready | ✅ Completed |
| **08** | Ready for Play Console submission | 🚀 Ready |

