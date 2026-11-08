package com.cooked.app.scheduling.notification;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ReminderNotificationBuilder_Factory implements Factory<ReminderNotificationBuilder> {
  private final Provider<Context> contextProvider;

  private ReminderNotificationBuilder_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ReminderNotificationBuilder get() {
    return newInstance(contextProvider.get());
  }

  public static ReminderNotificationBuilder_Factory create(Provider<Context> contextProvider) {
    return new ReminderNotificationBuilder_Factory(contextProvider);
  }

  public static ReminderNotificationBuilder newInstance(Context context) {
    return new ReminderNotificationBuilder(context);
  }
}
