import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import hillsongptapp.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.Font

/**
 * App Typography System
 * 
 * Font Families:
 * - Andika: Clean, readable font perfect for body text and UI elements
 * - Anta: Modern display font for headings and emphasis
 * - Mogra: Bold decorative font for special headings and branding
 */

@Composable
fun AppTypography(): Typography {
    
    // Andika Font Family - Primary text font (readable, clean)
    val andikaFamily = FontFamily(
        Font(Res.font.Andika_Regular, FontWeight.Normal),
        Font(Res.font.Andika_Italic, FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic),
        Font(Res.font.Andika_Bold, FontWeight.Bold),
        Font(Res.font.Andika_BoldItalic, FontWeight.Bold, androidx.compose.ui.text.font.FontStyle.Italic)
    )
    
    // Anta Font Family - Display font for headings
    val antaFamily = FontFamily(
        Font(Res.font.Anta_Regular, FontWeight.Normal)
    )
    
    // Mogra Font Family - Decorative font for branding/special headings
    val mograFamily = FontFamily(
        Font(Res.font.Mogra_Regular, FontWeight.Normal)
    )

    return Typography(
        // Display styles - Large, prominent text (Mogra for impact)
        displayLarge = TextStyle(
            fontFamily = mograFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = mograFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = antaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        
        // Headline styles - Section headings (Anta for modern look)
        headlineLarge = TextStyle(
            fontFamily = antaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = antaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = antaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        
        // Title styles - Card titles, dialog titles (Andika Bold for clarity)
        titleLarge = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        
        // Body styles - Main content text (Andika for readability)
        bodyLarge = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        
        // Label styles - Buttons, tabs, form labels (Andika for UI elements)
        labelLarge = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = andikaFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

/**
 * Custom font families for direct use when needed
 */
object AppFonts {
    @Composable
    fun andika() = FontFamily(
        Font(Res.font.Andika_Regular, FontWeight.Normal),
        Font(Res.font.Andika_Italic, FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic),
        Font(Res.font.Andika_Bold, FontWeight.Bold),
        Font(Res.font.Andika_BoldItalic, FontWeight.Bold, androidx.compose.ui.text.font.FontStyle.Italic)
    )
    
    @Composable
    fun anta() = FontFamily(
        Font(Res.font.Anta_Regular, FontWeight.Normal)
    )
    
    @Composable
    fun mogra() = FontFamily(
        Font(Res.font.Mogra_Regular, FontWeight.Normal)
    )
}