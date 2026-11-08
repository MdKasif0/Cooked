package com.cooked.app.scheduling.alarm;

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
public final class AlarmSchedulerImpl_Factory implements Factory<AlarmSchedulerImpl> {
  private final Provider<Context> contextProvider;

  private AlarmSchedulerImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AlarmSchedulerImpl get() {
    return newInstance(contextProvider.get());
  }

  public static AlarmSchedulerImpl_Factory create(Provider<Context> contextProvider) {
    return new AlarmSchedulerImpl_Factory(contextProvider);
  }

  public static AlarmSchedulerImpl newInstance(Context context) {
    return new AlarmSchedulerImpl(context);
  }
}
