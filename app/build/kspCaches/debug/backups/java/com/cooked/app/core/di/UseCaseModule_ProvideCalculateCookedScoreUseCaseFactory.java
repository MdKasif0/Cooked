package com.cooked.app.core.di;

import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase;
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
public final class UseCaseModule_ProvideCalculateCookedScoreUseCaseFactory implements Factory<CalculateCookedScoreUseCase> {
  @Override
  public CalculateCookedScoreUseCase get() {
    return provideCalculateCookedScoreUseCase();
  }

  public static UseCaseModule_ProvideCalculateCookedScoreUseCaseFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CalculateCookedScoreUseCase provideCalculateCookedScoreUseCase() {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideCalculateCookedScoreUseCase());
  }

  private static final class InstanceHolder {
    static final UseCaseModule_ProvideCalculateCookedScoreUseCaseFactory INSTANCE = new UseCaseModule_ProvideCalculateCookedScoreUseCaseFactory();
  }
}
