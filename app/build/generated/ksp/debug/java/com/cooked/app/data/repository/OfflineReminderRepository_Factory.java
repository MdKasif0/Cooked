package com.cooked.app.data.repository;

import com.cooked.app.data.dao.ReminderDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class OfflineReminderRepository_Factory implements Factory<OfflineReminderRepository> {
  private final Provider<ReminderDao> reminderDaoProvider;

  private OfflineReminderRepository_Factory(Provider<ReminderDao> reminderDaoProvider) {
    this.reminderDaoProvider = reminderDaoProvider;
  }

  @Override
  public OfflineReminderRepository get() {
    return newInstance(reminderDaoProvider.get());
  }

  public static OfflineReminderRepository_Factory create(
      Provider<ReminderDao> reminderDaoProvider) {
    return new OfflineReminderRepository_Factory(reminderDaoProvider);
  }

  public static OfflineReminderRepository newInstance(ReminderDao reminderDao) {
    return new OfflineReminderRepository(reminderDao);
  }
}
