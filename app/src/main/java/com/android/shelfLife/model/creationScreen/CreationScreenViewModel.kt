package com.android.shelfLife.model.creationScreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreationScreenViewModel : ViewModel() {
  private val _emailList = MutableStateFlow<Set<String>>(emptySet())
  val emailList: StateFlow<Set<String>> = _emailList.asStateFlow()

  fun setEmails(emails: Set<String>) {
    _emailList.value = emails
  }

  fun addEmail(email: String) {
    _emailList.value += email
  }

  fun removeEmail(email: String) {
    _emailList.value -= email
  }
}
