package com.cooked.app.di;

import com.cooked.app.alarm.AlarmScheduler;
import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.CompleteReminderUseCase;
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
public final class UseCaseModule_ProvideCompleteReminderUseCaseFactory implements Factory<CompleteReminderUseCase> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private UseCaseModule_ProvideCompleteReminderUseCaseFactory(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public CompleteReminderUseCase get() {
    return provideCompleteReminderUseCase(repositoryProvider.get(), alarmSchedulerProvider.get());
  }

  public static UseCaseModule_ProvideCompleteReminderUseCaseFactory create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new UseCaseModule_ProvideCompleteReminderUseCaseFactory(repositoryProvider, alarmSchedulerProvider);
  }

  public static CompleteReminderUseCase provideCompleteReminderUseCase(
      ReminderRepository repository, AlarmScheduler alarmScheduler) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideCompleteReminderUseCase(repository, alarmScheduler));
  }
}
