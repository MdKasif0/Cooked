package com.cooked.app.feature.add;

import com.cooked.app.core.time.CookedClock;
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
public final class QuickAddViewModel_Factory implements Factory<QuickAddViewModel> {
  private final Provider<SaveReminderUseCase> saveReminderUseCaseProvider;

  private final Provider<CookedClock> clockProvider;

  private QuickAddViewModel_Factory(Provider<SaveReminderUseCase> saveReminderUseCaseProvider,
      Provider<CookedClock> clockProvider) {
    this.saveReminderUseCaseProvider = saveReminderUseCaseProvider;
    this.clockProvider = clockProvider;
  }

  @Override
  public QuickAddViewModel get() {
    return newInstance(saveReminderUseCaseProvider.get(), clockProvider.get());
  }

  public static QuickAddViewModel_Factory create(
      Provider<SaveReminderUseCase> saveReminderUseCaseProvider,
      Provider<CookedClock> clockProvider) {
    return new QuickAddViewModel_Factory(saveReminderUseCaseProvider, clockProvider);
  }

  public static QuickAddViewModel newInstance(SaveReminderUseCase saveReminderUseCase,
      CookedClock clock) {
    return new QuickAddViewModel(saveReminderUseCase, clock);
  }
}
