package com.cooked.app.feature.home;

import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase;
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
public final class TodayViewModel_Factory implements Factory<TodayViewModel> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider;

  private final Provider<CompleteReminderUseCase> completeReminderUseCaseProvider;

  private final Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider;

  private TodayViewModel_Factory(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider) {
    this.repositoryProvider = repositoryProvider;
    this.calculateCookedScoreUseCaseProvider = calculateCookedScoreUseCaseProvider;
    this.completeReminderUseCaseProvider = completeReminderUseCaseProvider;
    this.snoozeReminderUseCaseProvider = snoozeReminderUseCaseProvider;
  }

  @Override
  public TodayViewModel get() {
    return newInstance(repositoryProvider.get(), calculateCookedScoreUseCaseProvider.get(), completeReminderUseCaseProvider.get(), snoozeReminderUseCaseProvider.get());
  }

  public static TodayViewModel_Factory create(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider) {
    return new TodayViewModel_Factory(repositoryProvider, calculateCookedScoreUseCaseProvider, completeReminderUseCaseProvider, snoozeReminderUseCaseProvider);
  }

  public static TodayViewModel newInstance(ReminderRepository repository,
      CalculateCookedScoreUseCase calculateCookedScoreUseCase,
      CompleteReminderUseCase completeReminderUseCase,
      SnoozeReminderUseCase snoozeReminderUseCase) {
    return new TodayViewModel(repository, calculateCookedScoreUseCase, completeReminderUseCase, snoozeReminderUseCase);
  }
}
