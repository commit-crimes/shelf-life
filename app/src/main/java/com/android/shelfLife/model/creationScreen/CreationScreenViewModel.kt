package com.android.shelfLife.model.creationScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreationScreenViewModel(initialEmails: Set<String> = emptySet()) : ViewModel() {
  private val _emailList = MutableStateFlow<Set<String>>(initialEmails)
  val emailList: StateFlow<Set<String>> = _emailList.asStateFlow()

  fun setEmails(emails: Set<String>) {
    _emailList.value = emails
  }

  fun addEmail(email: String) {
    _emailList.value = _emailList.value + email
  }

  fun removeEmail(email: String) {
    _emailList.value = _emailList.value - email
  }
}

class CreationScreenViewModelFactory(private val initialEmails: Set<String>) :
    ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(CreationScreenViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST") return CreationScreenViewModel(initialEmails) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
