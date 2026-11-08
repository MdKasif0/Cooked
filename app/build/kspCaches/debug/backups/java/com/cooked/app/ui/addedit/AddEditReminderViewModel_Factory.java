package com.cooked.app.ui.addedit;

import androidx.lifecycle.SavedStateHandle;
import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.DeleteReminderUseCase;
import com.cooked.app.domain.usecase.SaveReminderUseCase;
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
public final class AddEditReminderViewModel_Factory implements Factory<AddEditReminderViewModel> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<SaveReminderUseCase> saveReminderUseCaseProvider;

  private final Provider<DeleteReminderUseCase> deleteReminderUseCaseProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private AddEditReminderViewModel_Factory(Provider<ReminderRepository> repositoryProvider,
      Provider<SaveReminderUseCase> saveReminderUseCaseProvider,
      Provider<DeleteReminderUseCase> deleteReminderUseCaseProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.repositoryProvider = repositoryProvider;
    this.saveReminderUseCaseProvider = saveReminderUseCaseProvider;
    this.deleteReminderUseCaseProvider = deleteReminderUseCaseProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public AddEditReminderViewModel get() {
    return newInstance(repositoryProvider.get(), saveReminderUseCaseProvider.get(), deleteReminderUseCaseProvider.get(), savedStateHandleProvider.get());
  }

  public static AddEditReminderViewModel_Factory create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<SaveReminderUseCase> saveReminderUseCaseProvider,
      Provider<DeleteReminderUseCase> deleteReminderUseCaseProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new AddEditReminderViewModel_Factory(repositoryProvider, saveReminderUseCaseProvider, deleteReminderUseCaseProvider, savedStateHandleProvider);
  }

  public static AddEditReminderViewModel newInstance(ReminderRepository repository,
      SaveReminderUseCase saveReminderUseCase, DeleteReminderUseCase deleteReminderUseCase,
      SavedStateHandle savedStateHandle) {
    return new AddEditReminderViewModel(repository, saveReminderUseCase, deleteReminderUseCase, savedStateHandle);
  }
}
