package com.cooked.app.scheduling.receivers;

import com.cooked.app.domain.repository.ReminderRepository;
import com.cooked.app.scheduling.notification.ReminderNotificationBuilder;
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

  private final Provider<ReminderNotificationBuilder> notificationBuilderProvider;

  private ReminderAlarmReceiver_MembersInjector(Provider<ReminderRepository> repositoryProvider,
      Provider<ReminderNotificationBuilder> notificationBuilderProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationBuilderProvider = notificationBuilderProvider;
  }

  @Override
  public void injectMembers(ReminderAlarmReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
    injectNotificationBuilder(instance, notificationBuilderProvider.get());
  }

  public static MembersInjector<ReminderAlarmReceiver> create(
      Provider<ReminderRepository> repositoryProvider,
      Provider<ReminderNotificationBuilder> notificationBuilderProvider) {
    return new ReminderAlarmReceiver_MembersInjector(repositoryProvider, notificationBuilderProvider);
  }

  @InjectedFieldSignature("com.cooked.app.scheduling.receivers.ReminderAlarmReceiver.repository")
  public static void injectRepository(ReminderAlarmReceiver instance,
      ReminderRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.cooked.app.scheduling.receivers.ReminderAlarmReceiver.notificationBuilder")
  public static void injectNotificationBuilder(ReminderAlarmReceiver instance,
      ReminderNotificationBuilder notificationBuilder) {
    instance.notificationBuilder = notificationBuilder;
  }
}
