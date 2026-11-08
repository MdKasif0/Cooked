package com.cooked.app.alarm;

import com.cooked.app.domain.repository.ReminderRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class ReminderAlarmReceiver_MembersInjector implements MembersInjector<ReminderAlarmReceiver> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private ReminderAlarmReceiver_MembersInjector(Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public void injectMembers(ReminderAlarmReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
    injectAlarmScheduler(instance, alarmSchedulerProvider.get());
  }

  public static MembersInjector<ReminderAlarmReceiver> create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new ReminderAlarmReceiver_MembersInjector(repositoryProvider, alarmSchedulerProvider);
  }

  @InjectedFieldSignature("com.cooked.app.alarm.ReminderAlarmReceiver.repository")
  public static void injectRepository(ReminderAlarmReceiver instance,
      ReminderRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.cooked.app.alarm.ReminderAlarmReceiver.alarmScheduler")
  public static void injectAlarmScheduler(ReminderAlarmReceiver instance,
      AlarmScheduler alarmScheduler) {
    instance.alarmScheduler = alarmScheduler;
  }
}
