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
public final class BootReceiver_MembersInjector implements MembersInjector<BootReceiver> {
  private final Provider<ReminderRepository> repositoryProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  private BootReceiver_MembersInjector(Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  @Override
  public void injectMembers(BootReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
    injectAlarmScheduler(instance, alarmSchedulerProvider.get());
  }

  public static MembersInjector<BootReceiver> create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new BootReceiver_MembersInjector(repositoryProvider, alarmSchedulerProvider);
  }

  @InjectedFieldSignature("com.cooked.app.alarm.BootReceiver.repository")
  public static void injectRepository(BootReceiver instance, ReminderRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.cooked.app.alarm.BootReceiver.alarmScheduler")
  public static void injectAlarmScheduler(BootReceiver instance, AlarmScheduler alarmScheduler) {
    instance.alarmScheduler = alarmScheduler;
  }
}
