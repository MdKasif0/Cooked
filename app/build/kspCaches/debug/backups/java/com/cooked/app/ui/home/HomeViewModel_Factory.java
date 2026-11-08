package com.cooked.app.ui.home;

import com.cooked.app.data.datastore.UserPreferencesRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider;

  private final Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider;

  private final Provider<CompleteReminderUseCase> completeReminderUseCaseProvider;

  private final Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private HomeViewModel_Factory(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.calculatePressureUseCaseProvider = calculatePressureUseCaseProvider;
    this.calculateCookedScoreUseCaseProvider = calculateCookedScoreUseCaseProvider;
    this.completeReminderUseCaseProvider = completeReminderUseCaseProvider;
    this.snoozeReminderUseCaseProvider = snoozeReminderUseCaseProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(repositoryProvider.get(), calculatePressureUseCaseProvider.get(), calculateCookedScoreUseCaseProvider.get(), completeReminderUseCaseProvider.get(), snoozeReminderUseCaseProvider.get(), userPreferencesRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CalculateCookedScoreUseCase> calculateCookedScoreUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new HomeViewModel_Factory(repositoryProvider, calculatePressureUseCaseProvider, calculateCookedScoreUseCaseProvider, completeReminderUseCaseProvider, snoozeReminderUseCaseProvider, userPreferencesRepositoryProvider);
  }

  public static HomeViewModel newInstance(ReminderRepository repository,
      CalculatePressureUseCase calculatePressureUseCase,
      CalculateCookedScoreUseCase calculateCookedScoreUseCase,
      CompleteReminderUseCase completeReminderUseCase, SnoozeReminderUseCase snoozeReminderUseCase,
      UserPreferencesRepository userPreferencesRepository) {
    return new HomeViewModel(repository, calculatePressureUseCase, calculateCookedScoreUseCase, completeReminderUseCase, snoozeReminderUseCase, userPreferencesRepository);
  }
}
