@file:Suppress("MagicNumber")

package rfm.hillsongptapp.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Hillsong Brand Colors
 * 
 * Core Brand Palette:
 * - Black: Primary brand color, represents strength and elegance
 * - White: Clean, pure, represents hope and clarity  
 * - Gold: Premium accent, represents excellence and divine connection
 * 
 * Extended Palette:
 * - Warm grays for subtle backgrounds and borders
 * - Success green for positive actions
 * - Warning amber for caution states
 * - Error red for critical states
 */

// === CORE BRAND COLORS ===
val HillsongBlack = Color(0xFF000000)
val HillsongWhite = Color(0xFFFFFFFF)
val HillsongGold = Color(0xFFD4AF37)        // Classic gold
val HillsongGoldLight = Color(0xFFE6C547)   // Lighter gold for containers
val HillsongGoldDark = Color(0xFFB8941F)    // Darker gold for emphasis

// === NEUTRAL PALETTE ===
val Gray50 = Color(0xFFFAFAFA)
val Gray100 = Color(0xFFF5F5F5)
val Gray200 = Color(0xFFEEEEEE)
val Gray300 = Color(0xFFE0E0E0)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

// === SEMANTIC COLORS ===
val SuccessGreen = Color(0xFF4CAF50)
val SuccessGreenLight = Color(0xFFE8F5E8)
val WarningAmber = Color(0xFFFF9800)
val WarningAmberLight = Color(0xFFFFF3E0)
val ErrorRed = Color(0xFFD32F2F)
val ErrorRedLight = Color(0xFFFFEBEE)
val InfoBlue = Color(0xFF1976D2)
val InfoBlueLight = Color(0xFFE3F2FD)

// === LIGHT THEME COLORS ===
val LightPrimary = HillsongBlack
val LightOnPrimary = HillsongWhite
val LightPrimaryContainer = Gray100
val LightOnPrimaryContainer = HillsongBlack

val LightSecondary = HillsongGold
val LightOnSecondary = HillsongBlack
val LightSecondaryContainer = HillsongGoldLight
val LightOnSecondaryContainer = HillsongBlack

val LightTertiary = Gray600
val LightOnTertiary = HillsongWhite
val LightTertiaryContainer = Gray200
val LightOnTertiaryContainer = Gray800

val LightError = ErrorRed
val LightOnError = HillsongWhite
val LightErrorContainer = ErrorRedLight
val LightOnErrorContainer = ErrorRed

val LightBackground = HillsongWhite
val LightOnBackground = HillsongBlack
val LightSurface = HillsongWhite
val LightOnSurface = HillsongBlack
val LightSurfaceVariant = Gray100
val LightOnSurfaceVariant = Gray700
val LightOutline = Gray400
val LightInverseOnSurface = HillsongWhite
val LightInverseSurface = HillsongBlack
val LightPrimaryInverse = HillsongGold

// === DARK THEME COLORS ===
val DarkPrimary = HillsongGold
val DarkOnPrimary = HillsongBlack
val DarkPrimaryContainer = HillsongGoldDark
val DarkOnPrimaryContainer = HillsongWhite

val DarkSecondary = HillsongWhite
val DarkOnSecondary = HillsongBlack
val DarkSecondaryContainer = Gray800
val DarkOnSecondaryContainer = HillsongWhite

val DarkTertiary = Gray400
val DarkOnTertiary = HillsongBlack
val DarkTertiaryContainer = Gray700
val DarkOnTertiaryContainer = Gray200

val DarkError = Color(0xFFFF6B6B)
val DarkOnError = HillsongBlack
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkBackground = HillsongBlack
val DarkOnBackground = HillsongWhite
val DarkSurface = Gray900
val DarkOnSurface = HillsongWhite
val DarkSurfaceVariant = Gray800
val DarkOnSurfaceVariant = Gray300
val DarkOutline = Gray600
val DarkInverseOnSurface = HillsongBlack
val DarkInverseSurface = HillsongWhite
val DarkPrimaryInverse = HillsongBlack

// === MATERIAL 3 COLOR SCHEMES ===

val hillsongPtAppLightColorScheme = ColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightPrimaryInverse,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    outline = LightOutline,
    outlineVariant = Gray300,
    scrim = HillsongBlack,
    surfaceTint = LightPrimary,
)

val hillsongPtAppDarkColorScheme = ColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkPrimaryInverse,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = Gray700,
    scrim = HillsongBlack,
    surfaceTint = DarkPrimary,
)

/**
 * Custom Hillsong Colors for direct use
 * Use these when you need specific brand colors outside of Material 3 theming
 */
object HillsongColors {
    // Brand Colors
    val Black = HillsongBlack
    val White = HillsongWhite
    val Gold = HillsongGold
    val GoldLight = HillsongGoldLight
    val GoldDark = HillsongGoldDark
    
    // Semantic Colors
    val Success = SuccessGreen
    val SuccessContainer = SuccessGreenLight
    val Warning = WarningAmber
    val WarningContainer = WarningAmberLight
    val Error = ErrorRed
    val ErrorContainer = ErrorRedLight
    val Info = InfoBlue
    val InfoContainer = InfoBlueLight
    
    // Neutral Grays
    val Gray50 = rfm.hillsongptapp.core.designsystem.theme.Gray50
    val Gray100 = rfm.hillsongptapp.core.designsystem.theme.Gray100
    val Gray200 = rfm.hillsongptapp.core.designsystem.theme.Gray200
    val Gray300 = rfm.hillsongptapp.core.designsystem.theme.Gray300
    val Gray400 = rfm.hillsongptapp.core.designsystem.theme.Gray400
    val Gray500 = rfm.hillsongptapp.core.designsystem.theme.Gray500
    val Gray600 = rfm.hillsongptapp.core.designsystem.theme.Gray600
    val Gray700 = rfm.hillsongptapp.core.designsystem.theme.Gray700
    val Gray800 = rfm.hillsongptapp.core.designsystem.theme.Gray800
    val Gray900 = rfm.hillsongptapp.core.designsystem.theme.Gray900
}
