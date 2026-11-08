package com.cooked.app.feature.insights;

import com.cooked.app.core.time.CookedClock;
import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class InsightsViewModel_Factory implements Factory<InsightsViewModel> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider;

  private final Provider<CookedClock> clockProvider;

  private InsightsViewModel_Factory(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CookedClock> clockProvider) {
    this.repositoryProvider = repositoryProvider;
    this.calculateCookedScoreUseCaseProvider = calculateCookedScoreUseCaseProvider;
    this.clockProvider = clockProvider;
  }

  @Override
  public InsightsViewModel get() {
    return newInstance(repositoryProvider.get(), calculateCookedScoreUseCaseProvider.get(), clockProvider.get());
  }

  public static InsightsViewModel_Factory create(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CookedClock> clockProvider) {
    return new InsightsViewModel_Factory(repositoryProvider, calculateCookedScoreUseCaseProvider, clockProvider);
  }

  public static InsightsViewModel newInstance(ReminderRepository repository,
      CalculateCookedScoreUseCase calculateCookedScoreUseCase, CookedClock clock) {
    return new InsightsViewModel(repository, calculateCookedScoreUseCase, clock);
  }
}
