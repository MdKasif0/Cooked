# Privacy Policy for Cooked

**Last updated:** November 2026

Cooked ("we", "our", or "the app") is designed with a fundamental promise: **you own your data, and your business is your own.**

Cooked is an **offline-first** reminder and deadline app. We do not require you to create an account, log in with an email, or sync your data to our servers.

---

### 1. Information We Collect and Store

All reminders, deadlines, notes, effort estimates, checklists, and completion history created in Cooked are stored **strictly on your device** in a local SQLite database (powered by Android Room).

- **No Remote Database:** We do not host, store, or transmit your reminders, deadlines, or personal notes to any cloud server in version 1.
- **No Third-Party Brokers:** We never sell, rent, or share your tasks or personal data with data brokers, advertisers, or analytics aggregators.
- **Local Analytics Only:** The "Cooked Score", on-time completion rates, and snooze habit statistics displayed in the Insights tab are calculated entirely on your device using local algorithms.

---

### 2. Permissions and Why We Need Them

Cooked requests only the Android permissions necessary to reliably notify and alert you:

1. **Post Notifications (`android.permission.POST_NOTIFICATIONS`)**:
   - Required on Android 13+ to deliver alert notifications and ongoing nag reminders when a task is due.
2. **Exact Alarms (`android.permission.SCHEDULE_EXACT_ALARM`)**:
   - Required on Android 12+ so alarms trigger at the exact minute you specified, preventing Android's Doze mode from batching and delaying critical alerts.
3. **Full-Screen Alerts (`android.permission.USE_FULL_SCREEN_INTENT`)**:
   - Used only for critical reminders to wake the screen and display the full-screen alarm screen over your lock screen.
4. **Battery Optimization Exemption**:
   - We guide users to set Cooked to "Unrestricted" in system battery settings to prevent aggressive OEM task killers (such as Xiaomi MIUI/HyperOS, Samsung One UI, or OnePlus OxygenOS) from terminating scheduled alarms.

---

### 3. Backups and Data Export

- **Android Auto Backup:** If you have enabled Android's system Google Drive backup, an encrypted copy of your local database and preferences may be saved to your personal Google Drive account in accordance with Google's backup policy. We have no access to this encrypted file.
- **Manual Export:** You can export your data anytime as an unencrypted JSON file via the Android Storage Access Framework. You maintain full ownership and control over where this file is saved or shared.

---

### 4. Diagnostic & Crash Reports

Cooked includes optional diagnostic crash reporting (via Firebase Crashlytics / Google Play Vitals).
- These reports contain **strictly non-identifiable technical data**: device model, Android OS version, stack traces, and crash timestamps.
- Crash reports never include task titles, personal notes, or user input.

---

### 5. Data Deletion

You have complete control over your data:
- You can wipe all items, deadlines, checklists, events, and weekly snapshots at any time by going to **Settings > Data & Backup > Delete All Data**.
- Uninstalling Cooked permanently erases all local application databases and preferences from your device.

---

### 6. Contact Us

If you have questions about this Privacy Policy or Cooked's offline architecture, please email us at:
**privacy@cooked.app**
