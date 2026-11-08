package com.cooked.app.di;

import com.cooked.app.domain.usecase.CalculatePressureUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class UseCaseModule_ProvideCalculatePressureUseCaseFactory implements Factory<CalculatePressureUseCase> {
  @Override
  public CalculatePressureUseCase get() {
    return provideCalculatePressureUseCase();
  }

  public static UseCaseModule_ProvideCalculatePressureUseCaseFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CalculatePressureUseCase provideCalculatePressureUseCase() {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideCalculatePressureUseCase());
  }

  private static final class InstanceHolder {
    static final UseCaseModule_ProvideCalculatePressureUseCaseFactory INSTANCE = new UseCaseModule_ProvideCalculatePressureUseCaseFactory();
  }
}
