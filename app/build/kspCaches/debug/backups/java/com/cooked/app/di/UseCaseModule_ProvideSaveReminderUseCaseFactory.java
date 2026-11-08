package com.cooked.app.di;

import com.cooked.app.alarm.AlarmScheduler;
import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.domain.usecase.SaveReminderUseCase;
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
public final class UseCaseModule_ProvideSaveReminderUseCaseFactory implements Factory<SaveReminderUseCase> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private UseCaseModule_ProvideSaveReminderUseCaseFactory(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public SaveReminderUseCase get() {
    return provideSaveReminderUseCase(repositoryProvider.get(), alarmSchedulerProvider.get());
  }

  public static UseCaseModule_ProvideSaveReminderUseCaseFactory create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new UseCaseModule_ProvideSaveReminderUseCaseFactory(repositoryProvider, alarmSchedulerProvider);
  }

  public static SaveReminderUseCase provideSaveReminderUseCase(ReminderRepository repository,
      AlarmScheduler alarmScheduler) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideSaveReminderUseCase(repository, alarmScheduler));
  }
}
