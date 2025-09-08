package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.feature.kids.domain.validation.ValidationResult

/**
 * Enhanced text field with real-time validation
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    supportingText: String? = null,
    onValidationChange: ((ValidationResult) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val isError = validationResult.isInvalid
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                // Trigger validation change if provided
                onValidationChange?.invoke(validationResult)
            },
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() }
            ),
            supportingText = {
                when {
                    isError -> {
                        Text(
                            text = validationResult.errorMessage ?: "Invalid input",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    supportingText != null -> {
                        Text(
                            text = supportingText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Phone number input field with formatting and validation
 */
@Composable
fun ValidatedPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ValidatedTextField(
        value = value,
        onValueChange = { newValue ->
            // Format phone number as user types
            val digitsOnly = newValue.filter { it.isDigit() }
            val formatted = formatPhoneNumber(digitsOnly)
            onValueChange(formatted)
        },
        label = label,
        validationResult = validationResult,
        keyboardType = KeyboardType.Phone,
        placeholder = "(123) 456-7890",
        supportingText = "Enter a valid phone number",
        modifier = modifier,
        enabled = enabled
    )
}

/**
 * Date input field with validation
 */
@Composable
fun ValidatedDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDatePickerClick: (() -> Unit)? = null
) {
    ValidatedTextField(
        value = value,
        onValueChange = { newValue ->
            // Format date as user types
            val formatted = formatDateInput(newValue)
            onValueChange(formatted)
        },
        label = label,
        validationResult = validationResult,
        keyboardType = KeyboardType.Number,
        placeholder = "YYYY-MM-DD",
        supportingText = "Enter date in YYYY-MM-DD format",
        modifier = modifier,
        enabled = enabled
    )
}

/**
 * Dropdown field with validation for predefined options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidatedDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val isError = validationResult.isInvalid
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded && enabled }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { },
                readOnly = true,
                label = { Text(label) },
                placeholder = placeholder?.let { { Text(it) } },
                isError = isError,
                enabled = enabled,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                supportingText = {
                    if (isError) {
                        Text(
                            text = validationResult.errorMessage ?: "Please select an option",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Multi-line text field with validation for longer text inputs
 */
@Composable
fun ValidatedMultilineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validationResult: ValidationResult,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    maxLines: Int = 4,
    maxCharacters: Int? = null,
    enabled: Boolean = true
) {
    val isError = validationResult.isInvalid
    val characterCount = value.length
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxCharacters == null || newValue.length <= maxCharacters) {
                    onValueChange(newValue)
                }
            },
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            enabled = enabled,
            singleLine = false,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isError) {
                            validationResult.errorMessage ?: "Invalid input"
                        } else {
                            ""
                        },
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (maxCharacters != null) {
                        Text(
                            text = "$characterCount/$maxCharacters",
                            color = if (characterCount > maxCharacters * 0.9) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Form validation summary component
 */
@Composable
fun FormValidationSummary(
    isValid: Boolean,
    errorCount: Int,
    modifier: Modifier = Modifier
) {
    if (!isValid && errorCount > 0) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Please fix $errorCount validation error${if (errorCount > 1) "s" else ""} before continuing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Real-time validation indicator
 */
@Composable
fun ValidationIndicator(
    validationResult: ValidationResult,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (validationResult.isValid) "✓" else "✗",
            color = if (validationResult.isValid) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
        
        if (validationResult.isInvalid) {
            Text(
                text = validationResult.errorMessage ?: "Invalid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Helper function to format phone number input
 */
private fun formatPhoneNumber(digits: String): String {
    return when {
        digits.length <= 3 -> digits
        digits.length <= 6 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        digits.length <= 10 -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6, 10)}"
    }
}

/**
 * Helper function to format date input
 */
private fun formatDateInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    return when {
        digitsOnly.length <= 4 -> digitsOnly
        digitsOnly.length <= 6 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4)}"
        digitsOnly.length <= 8 -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}-${digitsOnly.substring(6)}"
        else -> "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4, 6)}-${digitsOnly.substring(6, 8)}"
    }
}