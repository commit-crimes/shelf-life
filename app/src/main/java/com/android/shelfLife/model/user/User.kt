package com.android.shelfLife.model.user

data class User(
    val uid: String,
    val username: String,
    val email: String,
    val householdUIDs: List<String> = emptyList(),
    val selectedHouseholdUID: String? = null,
    val recipeUIDs: List<String> = emptyList(),
    val invitationUIDs: List<String> = emptyList()
    // TODO add macros param if we decide to do it
) {
  override fun toString(): String {
    return "User:(" +
        "UID: ${uid}\n" +
        "Username: ${username}\n" +
        "Email: ${email}\n" +
        "Household UIDs: ${householdUIDs.toString()}\n" +
        "Selected Household UID: ${selectedHouseholdUID}\n" +
        "Recipe UIDs: ${recipeUIDs.toString()}\n" + "Invitation UIDs: ${invitationUIDs.toString()}\n)"
  }
}
