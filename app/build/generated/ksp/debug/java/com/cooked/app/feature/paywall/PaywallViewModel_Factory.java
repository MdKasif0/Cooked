package com.cooked.app.feature.paywall;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class PaywallViewModel_Factory implements Factory<PaywallViewModel> {
  @Override
  public PaywallViewModel get() {
    return newInstance();
  }

  public static PaywallViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PaywallViewModel newInstance() {
    return new PaywallViewModel();
  }

  private static final class InstanceHolder {
    static final PaywallViewModel_Factory INSTANCE = new PaywallViewModel_Factory();
  }
}
