package com.cooked.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.cooked.app.alarm.AlarmSchedulerImpl;
import com.cooked.app.alarm.BootReceiver;
import com.cooked.app.alarm.BootReceiver_MembersInjector;
import com.cooked.app.alarm.NagDismissReceiver;
import com.cooked.app.alarm.NagDismissReceiver_MembersInjector;
import com.cooked.app.alarm.ReminderActionReceiver;
import com.cooked.app.alarm.ReminderActionReceiver_MembersInjector;
import com.cooked.app.alarm.ReminderAlarmReceiver;
import com.cooked.app.alarm.ReminderAlarmReceiver_MembersInjector;
import com.cooked.app.data.local.CookedDatabase;
import com.cooked.app.data.local.dao.ReminderDao;
import com.cooked.app.data.preferences.UserPreferencesRepository;
import com.cooked.app.data.repository.OfflineReminderRepository;
import com.cooked.app.di.DatabaseModule_ProvideCookedDatabaseFactory;
import com.cooked.app.di.DatabaseModule_ProvideReminderDaoFactory;
import com.cooked.app.di.UseCaseModule_ProvideCalculateCookedScoreUseCaseFactory;
import com.cooked.app.di.UseCaseModule_ProvideCalculatePressureUseCaseFactory;
import com.cooked.app.di.UseCaseModule_ProvideCompleteReminderUseCaseFactory;
import com.cooked.app.di.UseCaseModule_ProvideDeleteReminderUseCaseFactory;
import com.cooked.app.di.UseCaseModule_ProvideDismissNagUseCaseFactory;
import com.cooked.app.di.UseCaseModule_ProvideSaveReminderUseCaseFactory;
import com.cooked.app.di.UseCaseModule_ProvideSnoozeReminderUseCaseFactory;
import com.cooked.app.domain.usecase.CalculateCookedScoreUseCase;
import com.cooked.app.domain.usecase.CalculatePressureUseCase;
import com.cooked.app.domain.usecase.CompleteReminderUseCase;
import com.cooked.app.domain.usecase.DeleteReminderUseCase;
import com.cooked.app.domain.usecase.DismissNagUseCase;
import com.cooked.app.domain.usecase.SaveReminderUseCase;
import com.cooked.app.domain.usecase.SnoozeReminderUseCase;
import com.cooked.app.ui.addedit.AddEditReminderViewModel;
import com.cooked.app.ui.addedit.AddEditReminderViewModel_HiltModules;
import com.cooked.app.ui.addedit.AddEditReminderViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.cooked.app.ui.addedit.AddEditReminderViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.cooked.app.ui.deadline.DeadlineDashboardViewModel;
import com.cooked.app.ui.deadline.DeadlineDashboardViewModel_HiltModules;
import com.cooked.app.ui.deadline.DeadlineDashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.cooked.app.ui.deadline.DeadlineDashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.cooked.app.ui.home.HomeViewModel;
import com.cooked.app.ui.home.HomeViewModel_HiltModules;
import com.cooked.app.ui.home.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.cooked.app.ui.home.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerCookedApplication_HiltComponents_SingletonC {
  private DaggerCookedApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public CookedApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements CookedApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public CookedApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements CookedApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public CookedApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements CookedApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public CookedApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements CookedApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CookedApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements CookedApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public CookedApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements CookedApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public CookedApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements CookedApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public CookedApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends CookedApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends CookedApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends CookedApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends CookedApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    Map keySetMapOfClassOfObjectAndBooleanBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, Boolean>newMapBuilder(3);
      mapBuilder.put(AddEditReminderViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditReminderViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(DeadlineDashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DeadlineDashboardViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide());
      return mapBuilder.build();
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(keySetMapOfClassOfObjectAndBooleanBuilder());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends CookedApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<AddEditReminderViewModel> addEditReminderViewModelProvider;

    Provider<DeadlineDashboardViewModel> deadlineDashboardViewModelProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    Map hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(3);
      mapBuilder.put(AddEditReminderViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (addEditReminderViewModelProvider)));
      mapBuilder.put(DeadlineDashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (deadlineDashboardViewModelProvider)));
      mapBuilder.put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider)));
      return mapBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addEditReminderViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.deadlineDashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(hiltViewModelMapMapOfClassOfObjectAndProviderOfViewModelBuilder());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.cooked.app.ui.addedit.AddEditReminderViewModel
          return (T) new AddEditReminderViewModel(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.provideSaveReminderUseCaseProvider.get(), singletonCImpl.provideDeleteReminderUseCaseProvider.get(), viewModelCImpl.savedStateHandle);

          case 1: // com.cooked.app.ui.deadline.DeadlineDashboardViewModel
          return (T) new DeadlineDashboardViewModel(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.provideCalculatePressureUseCaseProvider.get(), singletonCImpl.provideCalculateCookedScoreUseCaseProvider.get(), singletonCImpl.provideCompleteReminderUseCaseProvider.get(), singletonCImpl.provideSnoozeReminderUseCaseProvider.get());

          case 2: // com.cooked.app.ui.home.HomeViewModel
          return (T) new HomeViewModel(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.provideCalculatePressureUseCaseProvider.get(), singletonCImpl.provideCalculateCookedScoreUseCaseProvider.get(), singletonCImpl.provideCompleteReminderUseCaseProvider.get(), singletonCImpl.provideSnoozeReminderUseCaseProvider.get(), singletonCImpl.userPreferencesRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends CookedApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends CookedApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends CookedApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<CookedDatabase> provideCookedDatabaseProvider;

    Provider<OfflineReminderRepository> offlineReminderRepositoryProvider;

    Provider<AlarmSchedulerImpl> alarmSchedulerImplProvider;

    Provider<DismissNagUseCase> provideDismissNagUseCaseProvider;

    Provider<CompleteReminderUseCase> provideCompleteReminderUseCaseProvider;

    Provider<SnoozeReminderUseCase> provideSnoozeReminderUseCaseProvider;

    Provider<SaveReminderUseCase> provideSaveReminderUseCaseProvider;

    Provider<DeleteReminderUseCase> provideDeleteReminderUseCaseProvider;

    Provider<CalculatePressureUseCase> provideCalculatePressureUseCaseProvider;

    Provider<CalculateCookedScoreUseCase> provideCalculateCookedScoreUseCaseProvider;

    Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    ReminderDao reminderDao() {
      return DatabaseModule_ProvideReminderDaoFactory.provideReminderDao(provideCookedDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideCookedDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<CookedDatabase>(singletonCImpl, 1));
      this.offlineReminderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<OfflineReminderRepository>(singletonCImpl, 0));
      this.alarmSchedulerImplProvider = DoubleCheck.provider(new SwitchingProvider<AlarmSchedulerImpl>(singletonCImpl, 2));
      this.provideDismissNagUseCaseProvider = DoubleCheck.provider(new SwitchingProvider<DismissNagUseCase>(singletonCImpl, 3));
      this.provideCompleteReminderUseCaseProvider = DoubleCheck.provider(new SwitchingProvider<CompleteReminderUseCase>(singletonCImpl, 4));
      this.provideSnoozeReminderUseCaseProvider = DoubleCheck.provider(new SwitchingProvider<SnoozeReminderUseCase>(singletonCImpl, 5));
      this.provideSaveReminderUseCaseProvider = DoubleCheck.provider(new SwitchingProvider<SaveReminderUseCase>(singletonCImpl, 6));
      this.provideDeleteReminderUseCaseProvider = DoubleCheck.provider(new SwitchingProvider<DeleteReminderUseCase>(singletonCImpl, 7));
      this.provideCalculatePressureUseCaseProvider = DoubleCheck.provider(new SwitchingProvider<CalculatePressureUseCase>(singletonCImpl, 8));
      this.provideCalculateCookedScoreUseCaseProvider = DoubleCheck.provider(new SwitchingProvider<CalculateCookedScoreUseCase>(singletonCImpl, 9));
      this.userPreferencesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferencesRepository>(singletonCImpl, 10));
    }

    @Override
    public void injectCookedApplication(CookedApplication cookedApplication) {
    }

    @Override
    public void injectBootReceiver(BootReceiver bootReceiver) {
      injectBootReceiver2(bootReceiver);
    }

    @Override
    public void injectNagDismissReceiver(NagDismissReceiver nagDismissReceiver) {
      injectNagDismissReceiver2(nagDismissReceiver);
    }

    @Override
    public void injectReminderActionReceiver(ReminderActionReceiver reminderActionReceiver) {
      injectReminderActionReceiver2(reminderActionReceiver);
    }

    @Override
    public void injectReminderAlarmReceiver(ReminderAlarmReceiver reminderAlarmReceiver) {
      injectReminderAlarmReceiver2(reminderAlarmReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private BootReceiver injectBootReceiver2(BootReceiver instance) {
      BootReceiver_MembersInjector.injectRepository(instance, offlineReminderRepositoryProvider.get());
      BootReceiver_MembersInjector.injectAlarmScheduler(instance, alarmSchedulerImplProvider.get());
      return instance;
    }

    private NagDismissReceiver injectNagDismissReceiver2(NagDismissReceiver instance2) {
      NagDismissReceiver_MembersInjector.injectDismissNagUseCase(instance2, provideDismissNagUseCaseProvider.get());
      return instance2;
    }

    private ReminderActionReceiver injectReminderActionReceiver2(ReminderActionReceiver instance3) {
      ReminderActionReceiver_MembersInjector.injectCompleteReminderUseCase(instance3, provideCompleteReminderUseCaseProvider.get());
      ReminderActionReceiver_MembersInjector.injectSnoozeReminderUseCase(instance3, provideSnoozeReminderUseCaseProvider.get());
      return instance3;
    }

    private ReminderAlarmReceiver injectReminderAlarmReceiver2(ReminderAlarmReceiver instance4) {
      ReminderAlarmReceiver_MembersInjector.injectRepository(instance4, offlineReminderRepositoryProvider.get());
      ReminderAlarmReceiver_MembersInjector.injectAlarmScheduler(instance4, alarmSchedulerImplProvider.get());
      return instance4;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.cooked.app.data.repository.OfflineReminderRepository
          return (T) new OfflineReminderRepository(singletonCImpl.reminderDao());

          case 1: // com.cooked.app.data.local.CookedDatabase
          return (T) DatabaseModule_ProvideCookedDatabaseFactory.provideCookedDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.cooked.app.alarm.AlarmSchedulerImpl
          return (T) new AlarmSchedulerImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.cooked.app.domain.usecase.DismissNagUseCase
          return (T) UseCaseModule_ProvideDismissNagUseCaseFactory.provideDismissNagUseCase(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.alarmSchedulerImplProvider.get());

          case 4: // com.cooked.app.domain.usecase.CompleteReminderUseCase
          return (T) UseCaseModule_ProvideCompleteReminderUseCaseFactory.provideCompleteReminderUseCase(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.alarmSchedulerImplProvider.get());

          case 5: // com.cooked.app.domain.usecase.SnoozeReminderUseCase
          return (T) UseCaseModule_ProvideSnoozeReminderUseCaseFactory.provideSnoozeReminderUseCase(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.alarmSchedulerImplProvider.get());

          case 6: // com.cooked.app.domain.usecase.SaveReminderUseCase
          return (T) UseCaseModule_ProvideSaveReminderUseCaseFactory.provideSaveReminderUseCase(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.alarmSchedulerImplProvider.get());

          case 7: // com.cooked.app.domain.usecase.DeleteReminderUseCase
          return (T) UseCaseModule_ProvideDeleteReminderUseCaseFactory.provideDeleteReminderUseCase(singletonCImpl.offlineReminderRepositoryProvider.get(), singletonCImpl.alarmSchedulerImplProvider.get());

          case 8: // com.cooked.app.domain.usecase.CalculatePressureUseCase
          return (T) UseCaseModule_ProvideCalculatePressureUseCaseFactory.provideCalculatePressureUseCase();

          case 9: // com.cooked.app.domain.usecase.CalculateCookedScoreUseCase
          return (T) UseCaseModule_ProvideCalculateCookedScoreUseCaseFactory.provideCalculateCookedScoreUseCase();

          case 10: // com.cooked.app.data.preferences.UserPreferencesRepository
          return (T) new UserPreferencesRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
