package example.com.web.components

import example.com.data.db.user.UserProfile
import kotlinx.html.*

/**
 * Gets the correct image path for a user profile image, handling external URLs from Google
 * @param profileImagePath The raw profile image path from database
 * @return The appropriate path to use in img src attribute
 */
fun getProfileImageSrc(profileImagePath: String): String {
    return when {
        profileImagePath.isEmpty() -> "/resources/images/default-user-image.webp"
        profileImagePath.startsWith("http://") || profileImagePath.startsWith("https://") -> profileImagePath
        else -> "/resources/uploads/images/$profileImagePath"
    }
}

/**
 * Renders a user profile image
 * @param profileImagePath The profile image path from database
 * @param alt Alt text for the image
 * @param classes CSS classes to apply to the image
 */
fun FlowContent.userProfileImage(
    profileImagePath: String,
    alt: String = "User profile",
    classes: String = "object-cover w-full h-full"
) {
    img(
        classes = classes,
        src = getProfileImageSrc(profileImagePath),
        alt = alt
    )
}

/**
 * Renders a user profile image
 * @param userProfile The user profile object
 * @param alt Alt text for the image
 * @param classes CSS classes to apply to the image
 */
fun FlowContent.userProfileImage(
    userProfile: UserProfile,
    alt: String = "${userProfile.firstName}'s profile",
    classes: String = "object-cover w-full h-full"
) {
    userProfileImage(
        profileImagePath = userProfile.profileImagePath,
        alt = alt,
        classes = classes
    )
} 