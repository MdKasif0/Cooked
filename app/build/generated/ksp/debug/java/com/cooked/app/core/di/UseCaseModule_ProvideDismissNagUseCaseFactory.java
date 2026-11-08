package com.cooked.app.core.di;

import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.DismissNagUseCase;
import com.cooked.app.scheduling.alarm.AlarmScheduler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
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
public final class UseCaseModule_ProvideDismissNagUseCaseFactory implements Factory<DismissNagUseCase> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private UseCaseModule_ProvideDismissNagUseCaseFactory(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public DismissNagUseCase get() {
    return provideDismissNagUseCase(repositoryProvider.get(), alarmSchedulerProvider.get());
  }

  public static UseCaseModule_ProvideDismissNagUseCaseFactory create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new UseCaseModule_ProvideDismissNagUseCaseFactory(repositoryProvider, alarmSchedulerProvider);
  }

  public static DismissNagUseCase provideDismissNagUseCase(ReminderRepository repository,
      AlarmScheduler alarmScheduler) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideDismissNagUseCase(repository, alarmScheduler));
  }
}
