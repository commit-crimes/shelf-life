package com.android.shelfLife.model.user

/**
 * Data class representing a user in the Shelf Life app.
 *
 * This class contains information about a user, including their unique identifier, username,
 * email, profile photo URL, and references to their associated households, recipes, and invitations.
 *
 * @property uid The unique identifier for the user.
 * @property username The display name of the user.
 * @property email The email address of the user.
 * @property photoUrl The URL of the user's profile photo (optional).
 * @property selectedHouseholdUID The UID of the currently selected household, if any.
 * @property householdUIDs A list of UIDs for all households the user is part of.
 * @property recipeUIDs A list of UIDs for recipes created or associated with the user.
 * @property invitationUIDs A list of UIDs for invitations received by the user.
 */
data class User(
    val uid: String, // Unique identifier for the user
    val username: String, // User's display name
    val email: String, // User's email address
    val photoUrl: String? = null, // URL of the user's profile photo (optional)
    val selectedHouseholdUID: String?, // UID of the currently selected household
    val householdUIDs: List<String> = emptyList(), // UIDs of all households the user is part of
    val recipeUIDs: List<String> = emptyList(), // UIDs of recipes associated with the user
    val invitationUIDs: List<String> = emptyList() // UIDs of invitations received by the user
) {
    /**
     * Provides a detailed string representation of the user object.
     *
     * @return A string summarizing the user's information, including their UID, username,
     * email, photo URL, and associated UIDs for households, recipes, and invitations.
     */
    override fun toString(): String {
        return "User:(" +
                "UID: $uid\n" +
                "Username: $username\n" +
                "Email: $email\n" +
                "Photo URL: $photoUrl\n" +
                "Selected Household UID: $selectedHouseholdUID\n" +
                "Household UIDs: $householdUIDs\n" +
                "Recipe UIDs: $recipeUIDs\n" +
                "Invitation UIDs: $invitationUIDs\n)"
    }
}