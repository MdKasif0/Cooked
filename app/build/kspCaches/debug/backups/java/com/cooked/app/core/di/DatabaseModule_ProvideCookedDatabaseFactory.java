package com.cooked.app.core.di;

import android.content.Context;
import com.cooked.app.data.db.CookedDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseModule_ProvideCookedDatabaseFactory implements Factory<CookedDatabase> {
  private final Provider<Context> contextProvider;

  private DatabaseModule_ProvideCookedDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CookedDatabase get() {
    return provideCookedDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideCookedDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideCookedDatabaseFactory(contextProvider);
  }

  public static CookedDatabase provideCookedDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCookedDatabase(context));
  }
}
