package com.cooked.app.alarm;

import com.cooked.app.domain.usecase.DismissNagUseCase;
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
public final class NagDismissReceiver_MembersInjector implements MembersInjector<NagDismissReceiver> {
  private final Provider<DismissNagUseCase> dismissNagUseCaseProvider;

  private NagDismissReceiver_MembersInjector(
      Provider<DismissNagUseCase> dismissNagUseCaseProvider) {
    this.dismissNagUseCaseProvider = dismissNagUseCaseProvider;
  }

  @Override
  public void injectMembers(NagDismissReceiver instance) {
    injectDismissNagUseCase(instance, dismissNagUseCaseProvider.get());
  }

  public static MembersInjector<NagDismissReceiver> create(
      Provider<DismissNagUseCase> dismissNagUseCaseProvider) {
    return new NagDismissReceiver_MembersInjector(dismissNagUseCaseProvider);
  }

  @InjectedFieldSignature("com.cooked.app.alarm.NagDismissReceiver.dismissNagUseCase")
  public static void injectDismissNagUseCase(NagDismissReceiver instance,
      DismissNagUseCase dismissNagUseCase) {
    instance.dismissNagUseCase = dismissNagUseCase;
  }
}
