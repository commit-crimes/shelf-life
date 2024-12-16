//package com.android.shelfLife.di
//
//import com.android.shelfLife.model.invitations.Invitation
//import com.android.shelfLife.viewmodel.invitations.InvitationViewModel
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.components.SingletonComponent
//import dagger.hilt.testing.TestInstallIn
//import kotlinx.coroutines.flow.MutableStateFlow
//import javax.inject.Singleton
//
//@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [YourOriginalViewModelModule::class]
//)
//object TestViewModelModule {
//
//    @Provides
//    @Singleton
//    fun provideTestInvitationViewModel(): InvitationViewModel {
//        val mockInvitations = MutableStateFlow<List<Invitation>>(emptyList())
//        return object : InvitationViewModel() {
//            override val invitations = mockInvitations
//        }
//    }
//}