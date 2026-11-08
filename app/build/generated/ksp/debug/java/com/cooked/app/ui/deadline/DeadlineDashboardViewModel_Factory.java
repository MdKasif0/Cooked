package com.cooked.app.ui.deadline;

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
public final class DeadlineDashboardViewModel_Factory implements Factory<DeadlineDashboardViewModel> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider;

  private final Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider;

  private final Provider<CompleteReminderUseCase> completeReminderUseCaseProvider;

  private final Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider;

  private DeadlineDashboardViewModel_Factory(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider) {
    this.repositoryProvider = repositoryProvider;
    this.calculatePressureUseCaseProvider = calculatePressureUseCaseProvider;
    this.calculateCookedScoreUseCaseProvider = calculateCookedScoreUseCaseProvider;
    this.completeReminderUseCaseProvider = completeReminderUseCaseProvider;
    this.snoozeReminderUseCaseProvider = snoozeReminderUseCaseProvider;
  }

  @Override
  public DeadlineDashboardViewModel get() {
    return newInstance(repositoryProvider.get(), calculatePressureUseCaseProvider.get(), calculateCookedScoreUseCaseProvider.get(), completeReminderUseCaseProvider.get(), snoozeReminderUseCaseProvider.get());
  }

  public static DeadlineDashboardViewModel_Factory create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider) {
    return new DeadlineDashboardViewModel_Factory(repositoryProvider, calculatePressureUseCaseProvider, calculateCookedScoreUseCaseProvider, completeReminderUseCaseProvider, snoozeReminderUseCaseProvider);
  }

  public static DeadlineDashboardViewModel newInstance(ReminderRepository repository,
      CalculatePressureUseCase calculatePressureUseCase,
      CalculateCookedScoreUseCase calculateCookedScoreUseCase,
      CompleteReminderUseCase completeReminderUseCase,
      SnoozeReminderUseCase snoozeReminderUseCase) {
    return new DeadlineDashboardViewModel(repository, calculatePressureUseCase, calculateCookedScoreUseCase, completeReminderUseCase, snoozeReminderUseCase);
  }
}
