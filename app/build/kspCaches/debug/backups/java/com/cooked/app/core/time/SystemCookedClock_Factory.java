package com.cooked.app.core.time;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class SystemCookedClock_Factory implements Factory<SystemCookedClock> {
  @Override
  public SystemCookedClock get() {
    return newInstance();
  }

  public static SystemCookedClock_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SystemCookedClock newInstance() {
    return new SystemCookedClock();
  }

  private static final class InstanceHolder {
    static final SystemCookedClock_Factory INSTANCE = new SystemCookedClock_Factory();
  }
}
