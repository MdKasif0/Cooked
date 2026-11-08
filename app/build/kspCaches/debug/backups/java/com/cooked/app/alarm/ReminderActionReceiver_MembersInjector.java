package com.cooked.app.alarm;

import com.cooked.app.domain.usecase.CompleteReminderUseCase;
import com.cooked.app.domain.usecase.SnoozeReminderUseCase;
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
public final class ReminderActionReceiver_MembersInjector implements MembersInjector<ReminderActionReceiver> {
  private final Provider<CompleteReminderUseCase> completeReminderUseCaseProvider;

  private final Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider;

  private ReminderActionReceiver_MembersInjector(
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider) {
    this.completeReminderUseCaseProvider = completeReminderUseCaseProvider;
    this.snoozeReminderUseCaseProvider = snoozeReminderUseCaseProvider;
  }

  @Override
  public void injectMembers(ReminderActionReceiver instance) {
    injectCompleteReminderUseCase(instance, completeReminderUseCaseProvider.get());
    injectSnoozeReminderUseCase(instance, snoozeReminderUseCaseProvider.get());
  }

  public static MembersInjector<ReminderActionReceiver> create(
      Provider<CompleteReminderUseCase> completeReminderUseCaseProvider,
      Provider<SnoozeReminderUseCase> snoozeReminderUseCaseProvider) {
    return new ReminderActionReceiver_MembersInjector(completeReminderUseCaseProvider, snoozeReminderUseCaseProvider);
  }

  @InjectedFieldSignature("com.cooked.app.alarm.ReminderActionReceiver.completeReminderUseCase")
  public static void injectCompleteReminderUseCase(ReminderActionReceiver instance,
      CompleteReminderUseCase completeReminderUseCase) {
    instance.completeReminderUseCase = completeReminderUseCase;
  }

  @InjectedFieldSignature("com.cooked.app.alarm.ReminderActionReceiver.snoozeReminderUseCase")
  public static void injectSnoozeReminderUseCase(ReminderActionReceiver instance,
      SnoozeReminderUseCase snoozeReminderUseCase) {
    instance.snoozeReminderUseCase = snoozeReminderUseCase;
  }
}
