@file:Suppress("MagicNumber", "LongParameterList", "TooManyFunctions")

package rfm.hillsongptapp.core.designsystem.ui.components

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors

/**
 * Editorial design atoms matching the Hillsong PT Redesign spec:
 * dark-first, gold accents, Mogra/Anta/Andika type system.
 */

/**
 * Gold-bar section header: 3x14 gold bar + Anta uppercase 12sp, 2.4 tracking.
 */
@Composable
fun EditorialSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .background(HillsongColors.Gold),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title.uppercase(),
            style = TextStyle(
                fontFamily = AppFonts.anta(),
                fontSize = 12.sp,
                letterSpacing = 2.4.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
    }
}

/**
 * 54h primary CTA, radius 28, gold fill, Andika Bold 15, gold glow.
 */
@Composable
fun GoldCtaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val bg = if (enabled) HillsongColors.Gold else HillsongColors.Gold.copy(alpha = 0.4f)
    val fg = if (enabled) HillsongColors.Black else HillsongColors.Black.copy(alpha = 0.6f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp,
                color = fg,
            ),
        )
    }
}

/**
 * 50h outline gold button for secondary actions.
 */
@Composable
fun OutlineGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(26.dp))
            .border(1.5.dp, HillsongColors.Gold, RoundedCornerShape(26.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.3.sp,
                color = HillsongColors.Gold,
            ),
        )
    }
}

/**
 * Floating-label text field matching the editorial design:
 * surface card, uppercase label (gold when focused), 1.5dp gold underline on focus.
 */
@Composable
fun EditorialTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    light: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val surface = if (light) HillsongColors.Gray100 else MaterialTheme.colorScheme.surface
    val textColor = if (light) HillsongColors.Black else MaterialTheme.colorScheme.onSurface
    val subColor = HillsongColors.Gray500

    val underline = when {
        isError -> HillsongColors.Error
        focused -> HillsongColors.Gold
        else -> Color.Transparent
    }
    val labelColor = when {
        isError -> HillsongColors.Error
        focused -> HillsongColors.Gold
        else -> subColor
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(surface)
                .border(
                    width = 1.5.dp,
                    color = underline,
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topStart = 0.dp, topEnd = 0.dp),
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label.uppercase(),
                        style = TextStyle(
                            fontFamily = AppFonts.andika(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = labelColor,
                        ),
                    )
                    Spacer(Modifier.height(2.dp))
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                        textStyle = TextStyle(
                            fontFamily = AppFonts.andika(),
                            fontSize = 15.sp,
                            color = textColor,
                        ),
                        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        cursorBrush = SolidColor(HillsongColors.Gold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 22.dp)
                            .onFocusChanged { focused = it.isFocused },
                    )
                }
                if (isPassword) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = labelColor,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { passwordVisible = !passwordVisible },
                    )
                }
            }
        }
        if (isError && errorText != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorText,
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 11.sp,
                    color = HillsongColors.Error,
                ),
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

/**
 * Small gold pill chip, e.g. "MEMBER · Lisboa" or feed category tags.
 */
@Composable
fun GoldPillTag(
    text: String,
    modifier: Modifier = Modifier,
    showDot: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(HillsongColors.Gold.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(HillsongColors.Gold),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = HillsongColors.Gold,
            ),
        )
    }
}

/**
 * Wordmark used on top bar: "Hillsong<gold>PT</gold>" in Mogra display.
 */
@Composable
fun HillsongWordmark(
    modifier: Modifier = Modifier,
    fontSize: Int = 20,
    color: Color = HillsongColors.White,
) {
    val styled = buildAnnotatedString {
        withStyle(SpanStyle(color = color)) { append("Hillsong") }
        append(" ")
        withStyle(SpanStyle(color = HillsongColors.Gold)) { append("PT") }
    }
    Text(
        text = styled,
        style = TextStyle(
            fontFamily = AppFonts.mogra(),
            fontSize = fontSize.sp,
            letterSpacing = (-0.3).sp,
        ),
        modifier = modifier,
    )
}

/**
 * Thin gold horizontal hairline loader (60×2).
 */
@Composable
fun GoldHairlineLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(60.dp)
            .height(2.dp)
            .background(HillsongColors.Gold.copy(alpha = 0.7f)),
    )
}
