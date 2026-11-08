package com.cooked.app.feature.detail;

import androidx.lifecycle.SavedStateHandle;
import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.CalculatePressureUseCase;
import com.cooked.app.domain.usecase.CompleteReminderUseCase;
import com.cooked.app.domain.usecase.DeleteReminderUseCase;
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
public final class ReminderDetailViewModel_Factory implements Factory<ReminderDetailViewModel> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider;

  private final Provider<CompleteReminderUseCase> completeReminderUseCaseProvider;

  private final Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider;

  private final Provider<DeleteReminderUseCase> deleteReminderUseCaseProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private ReminderDetailViewModel_Factory(Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider,
      Provider<DeleteReminderUseCase> deleteReminderUseCaseProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.calculatePressureUseCaseProvider = calculatePressureUseCaseProvider;
    this.completeReminderUseCaseProvider = completeReminderUseCaseProvider;
    this.snoozeReminderUseCaseProvider = snoozeReminderUseCaseProvider;
    this.deleteReminderUseCaseProvider = deleteReminderUseCaseProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ReminderDetailViewModel get() {
    return newInstance(repositoryProvider.get(), calculatePressureUseCaseProvider.get(), completeReminderUseCaseProvider.get(), snoozeReminderUseCaseProvider.get(), deleteReminderUseCaseProvider.get(), savedStateHandleProvider.get());
  }

  public static ReminderDetailViewModel_Factory create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<CalculatePressureUseCase> calculatePressureUseCaseProvider,
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider,
      Provider<DeleteReminderUseCase> deleteReminderUseCaseProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ReminderDetailViewModel_Factory(repositoryProvider, calculatePressureUseCaseProvider, completeReminderUseCaseProvider, snoozeReminderUseCaseProvider, deleteReminderUseCaseProvider, savedStateHandleProvider);
  }

  public static ReminderDetailViewModel newInstance(ReminderRepository repository,
      CalculatePressureUseCase calculatePressureUseCase,
      CompleteReminderUseCase completeReminderUseCase, SnoozeReminderUseCase snoozeReminderUseCase,
      DeleteReminderUseCase deleteReminderUseCase, SavedStateHandle savedStateHandle) {
    return new ReminderDetailViewModel(repository, calculatePressureUseCase, completeReminderUseCase, snoozeReminderUseCase, deleteReminderUseCase, savedStateHandle);
  }
}
