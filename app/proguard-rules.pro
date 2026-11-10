# -----------------------------------------------------------------------------
# Cooked R8 / ProGuard Optimization Rules
# Ensures R8 minification preserves Room, Serialization, Glance, Alarms & Billing
# -----------------------------------------------------------------------------

# 1. Android Manifest Entry Points (Alarms, Boot, Receivers, Widgets)
-keep class com.cooked.app.scheduling.receivers.** { *; }
-keep class com.cooked.app.scheduling.alarm.** { *; }
-keep class com.cooked.app.widget.** { *; }
-keep class com.cooked.app.ui.quickadd.** { *; }
-keep class com.cooked.app.CookedApplication { *; }
-keep class com.cooked.app.MainActivity { *; }

# 2. Glance AppWidgets
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class * implements androidx.glance.appwidget.action.ActionCallback { *; }

# 3. WorkManager
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.cooked.app.scheduling.work.** { *; }

# 4. Room Database & SQLite
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void clearAllTables();
}
-keep class * extends androidx.room.RoomDatabase
-keep class com.cooked.app.data.db.** { *; }
-keep class com.cooked.app.data.entity.** { *; }
-keep interface com.cooked.app.data.dao.** { *; }
-dontwarn androidx.room.paging.**

# 5. KotlinX Serialization
-keepattributes *Annotation*,InnerClasses,Signature
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    *** Companion;
}
-keep class com.cooked.app.domain.model.** { *; }
-keep class com.cooked.app.domain.backup.** { *; }

# 6. RevenueCat & Google Play Billing
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# 7. Google Play In-App Review & App Updates
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# 8. Hilt Dependency Injection
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponentManager { *; }

# 9. Google Mobile Ads SDK (AdMob) & Cooked Ads
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.gms.internal.ads.**
-keep class com.cooked.app.core.ads.** { *; }
