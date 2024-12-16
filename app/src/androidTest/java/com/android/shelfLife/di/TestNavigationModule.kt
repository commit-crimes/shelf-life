//import androidx.navigation.NavHostController
//import com.android.shelfLife.ui.navigation.NavigationActions
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import io.mockk.mockk
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object TestNavigationModule {
//
//    @Provides
//    @Singleton
//    fun provideNavHostController(): NavHostController = mockk(relaxed = true)
//
//    @Provides
//    @Singleton
//    fun provideNavigationActions(navController: NavHostController): NavigationActions {
//        return NavigationActions(navController)
//    }
//}