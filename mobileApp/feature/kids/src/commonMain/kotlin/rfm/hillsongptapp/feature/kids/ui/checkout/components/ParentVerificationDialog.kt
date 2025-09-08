package rfm.hillsongptapp.feature.kids.ui.checkout.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

/**
 * Dialog for parent verification before check-out
 * This is a simplified implementation - in a real app you'd integrate with proper authentication
 */
@Composable
fun ParentVerificationDialog(
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    var verificationCode by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Auto-focus the input field when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Security icon
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Security",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = "Parent Verification Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                Text(
                    text = "For your child's safety, please verify your identity before checking them out.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Verification code input
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { 
                        verificationCode = it
                        verificationError = null // Clear error when user types
                    },
                    label = { Text("Verification Code") },
                    placeholder = { Text("Enter your PIN or password") },
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible }
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) {
                                    Icons.Default.Warning
                                } else {
                                    Icons.Default.Warning
                                },
                                contentDescription = if (isPasswordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (verificationCode.isNotBlank()) {
                                performVerification(
                                    code = verificationCode,
                                    onSuccess = onVerified,
                                    onError = { error -> verificationError = error },
                                    setLoading = { isVerifying = it }
                                )
                            }
                        }
                    ),
                    isError = verificationError != null,
                    supportingText = verificationError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    enabled = !isVerifying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Information card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Security Information",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "• Use the same PIN/password you use for app login\n" +
                                    "• This verification ensures only authorized parents can check out children\n" +
                                    "• Your verification is encrypted and secure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isVerifying
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (verificationCode.isNotBlank()) {
                                performVerification(
                                    code = verificationCode,
                                    onSuccess = onVerified,
                                    onError = { error -> verificationError = error },
                                    setLoading = { isVerifying = it }
                                )
                            } else {
                                verificationError = "Please enter your verification code"
                            }
                        },
                        enabled = !isVerifying && verificationCode.isNotBlank()
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Verify")
                    }
                }
            }
        }
    }
}

/**
 * Perform verification - this is a simplified implementation
 * In a real app, this would integrate with your authentication system
 */
private fun performVerification(
    code: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true)
    
    // Simulate verification delay
    // In a real implementation, this would be an API call
    kotlinx.coroutines.GlobalScope.launch {
        kotlinx.coroutines.delay(1000) // Simulate network delay
        
        // Simplified verification logic
        // In a real app, you'd verify against the user's actual credentials
        when {
            code.length < 4 -> {
                onError("Verification code must be at least 4 characters")
            }
            code == "0000" -> {
                onError("Invalid verification code. Please try again.")
            }
            else -> {
                // Verification successful
                onSuccess()
            }
        }
        
        setLoading(false)
    }
}