package com.cooked.app.core.di;

import com.cooked.app.data.dao.ReminderDao;
import com.cooked.app.data.db.CookedDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideReminderDaoFactory implements Factory<ReminderDao> {
  private final Provider<CookedDatabase> databaseProvider;

  private DatabaseModule_ProvideReminderDaoFactory(Provider<CookedDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ReminderDao get() {
    return provideReminderDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideReminderDaoFactory create(
      Provider<CookedDatabase> databaseProvider) {
    return new DatabaseModule_ProvideReminderDaoFactory(databaseProvider);
  }

  public static ReminderDao provideReminderDao(CookedDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideReminderDao(database));
  }
}
