package com.cooked.app.feature.deadlines;

import com.cooked.app.core.time.CookedClock;
import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase;
import com.cooked.app.domain.usecase.CalculatePressureUseCase;
import com.cooked.app.domain.usecase.CompleteReminderUseCase;
import com.cooked.app.domain.usecase.SnoozeReminderUseCase;
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
public final class DeadlinesViewModel_Factory implements Factory<DeadlinesViewModel> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider;

  private final Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider;

  private final Provider<CompleteReminderUseCase> completeReminderUseCaseProvider;

  private final Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider;

  private final Provider<CookedClock> clockProvider;

  private DeadlinesViewModel_Factory(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider,
      Provider<CookedClock> clockProvider) {
    this.repositoryProvider = repositoryProvider;
    this.calculatePressureUseCaseProvider = calculatePressureUseCaseProvider;
    this.calculateCookedScoreUseCaseProvider = calculateCookedScoreUseCaseProvider;
    this.completeReminderUseCaseProvider = completeReminderUseCaseProvider;
    this.snoozeReminderUseCaseProvider = snoozeReminderUseCaseProvider;
    this.clockProvider = clockProvider;
  }

  @Override
  public DeadlinesViewModel get() {
    return newInstance(repositoryProvider.get(), calculatePressureUseCaseProvider.get(), calculateCookedScoreUseCaseProvider.get(), completeReminderUseCaseProvider.get(), snoozeReminderUseCaseProvider.get(), clockProvider.get());
  }

  public static DeadlinesViewModel_Factory create(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider,
      Provider<CookedClock> clockProvider) {
    return new DeadlinesViewModel_Factory(repositoryProvider, calculatePressureUseCaseProvider, calculateCookedScoreUseCaseProvider, completeReminderUseCaseProvider, snoozeReminderUseCaseProvider, clockProvider);
  }

  public static DeadlinesViewModel newInstance(ReminderRepository repository,
      CalculatePressureUseCase calculatePressureUseCase,
      CalculateCookedScoreUseCase calculateCookedScoreUseCase,
      CompleteReminderUseCase completeReminderUseCase, SnoozeReminderUseCase snoozeReminderUseCase,
      CookedClock clock) {
    return new DeadlinesViewModel(repository, calculatePressureUseCase, calculateCookedScoreUseCase, completeReminderUseCase, snoozeReminderUseCase, clock);
  }
}
