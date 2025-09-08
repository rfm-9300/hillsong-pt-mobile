package rfm.hillsongptapp.feature.kids.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import rfm.hillsongptapp.core.designsystem.theme.AppTheme

/**
 * Kids feature theme integration with the app's design system
 * Provides consistent theming for all kids management screens
 */
@Composable
fun KidsTheme(
    content: @Composable () -> Unit
) {
    AppTheme(content = content)
}

/**
 * Kids-specific color extensions for status indicators
 */
object KidsColors {
    
    /**
     * Colors for check-in status indicators
     */
    val CheckedInColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary
    
    val CheckedOutColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline
    
    val NotInServiceColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceVariant
    
    /**
     * Colors for service capacity indicators
     */
    val CapacityAvailableColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.tertiary
    
    val CapacityLowColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.secondary
    
    val CapacityFullColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error
    
    /**
     * Colors for connection status
     */
    val ConnectedColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.tertiary
    
    val DisconnectedColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error
    
    val ConnectingColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.secondary
    
    /**
     * Colors for age group indicators
     */
    val InfantColor: Color
        @Composable
        @ReadOnlyComposable
        get() = Color(0xFF81C784) // Light Green
    
    val ToddlerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = Color(0xFF64B5F6) // Light Blue
    
    val PreschoolColor: Color
        @Composable
        @ReadOnlyComposable
        get() = Color(0xFFFFB74D) // Light Orange
    
    val ElementaryColor: Color
        @Composable
        @ReadOnlyComposable
        get() = Color(0xFFBA68C8) // Light Purple
    
    /**
     * Colors for staff features
     */
    val StaffActionColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primaryContainer
    
    val AdminActionColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.tertiaryContainer
}

/**
 * Extension functions for consistent color usage
 */
@Composable
@ReadOnlyComposable
fun getStatusColor(isCheckedIn: Boolean): Color {
    return if (isCheckedIn) {
        KidsColors.CheckedInColor
    } else {
        KidsColors.CheckedOutColor
    }
}

@Composable
@ReadOnlyComposable
fun getCapacityColor(currentCapacity: Int, maxCapacity: Int): Color {
    val percentage = if (maxCapacity > 0) currentCapacity.toFloat() / maxCapacity else 0f
    
    return when {
        percentage >= 1.0f -> KidsColors.CapacityFullColor
        percentage >= 0.8f -> KidsColors.CapacityLowColor
        else -> KidsColors.CapacityAvailableColor
    }
}

@Composable
@ReadOnlyComposable
fun getAgeGroupColor(age: Int): Color {
    return when {
        age < 2 -> KidsColors.InfantColor
        age < 4 -> KidsColors.ToddlerColor
        age < 6 -> KidsColors.PreschoolColor
        else -> KidsColors.ElementaryColor
    }
}