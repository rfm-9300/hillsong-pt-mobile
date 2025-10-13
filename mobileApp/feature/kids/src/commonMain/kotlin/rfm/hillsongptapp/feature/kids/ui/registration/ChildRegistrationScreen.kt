package rfm.hillsongptapp.feature.kids.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import hillsongptapp.feature.kids.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Screen for registering a new child with form validation
 * Includes required fields: name, date of birth, emergency contact
 * Optional fields: medical information, dietary restrictions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildRegistrationScreen(
    onNavigateBack: (() -> Unit)? = null,
    onRegistrationSuccess: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: ChildRegistrationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle registration success
    LaunchedEffect(uiState.isRegistrationSuccessful) {
        if (uiState.isRegistrationSuccessful) {
            onRegistrationSuccess?.invoke()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.register_child_title),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        )
        
        // Form Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading) {
                LoadingContent()
            } else {
                RegistrationForm(
                    uiState = uiState,
                    onChildNameChange = viewModel::updateChildName,
                    onDateOfBirthChange = viewModel::updateDateOfBirth,
                    onMedicalInfoChange = viewModel::updateMedicalInfo,
                    onDietaryRestrictionsChange = viewModel::updateDietaryRestrictions,
                    onEmergencyContactNameChange = viewModel::updateEmergencyContactName,
                    onEmergencyContactPhoneChange = viewModel::updateEmergencyContactPhone,
                    onEmergencyContactRelationshipChange = viewModel::updateEmergencyContactRelationship,
                    onDatePickerClick = viewModel::showDatePicker,
                    onRegisterClick = viewModel::registerChild,
                    // Debug functions for testing
                    onFillDebugData = viewModel::fillWithDebugData,
                    onFillAlternativeData = viewModel::fillWithAlternativeDebugData,
                    onFillMinimalData = viewModel::fillWithMinimalDebugData,
                    onClearData = viewModel::clearDebugData,
                    isDebugMode = viewModel.isInDebugMode()
                )
            }
        }
    }
    
    // Date Picker Dialog
    if (uiState.showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.updateDateOfBirth(date)
                viewModel.hideDatePicker()
            },
            onDismiss = viewModel::hideDatePicker
        )
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, show snackbar here
            // Registration error occurred
            viewModel.clearError()
        }
    }
}

/**
 * Loading content displayed during registration
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Registering child...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Main registration form
 */
@Composable
private fun RegistrationForm(
    uiState: ChildRegistrationUiState,
    onChildNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onMedicalInfoChange: (String) -> Unit,
    onDietaryRestrictionsChange: (String) -> Unit,
    onEmergencyContactNameChange: (String) -> Unit,
    onEmergencyContactPhoneChange: (String) -> Unit,
    onEmergencyContactRelationshipChange: (String) -> Unit,
    onDatePickerClick: () -> Unit,
    onRegisterClick: () -> Unit,
    // Debug functions
    onFillDebugData: () -> Unit = {},
    onFillAlternativeData: () -> Unit = {},
    onFillMinimalData: () -> Unit = {},
    onClearData: () -> Unit = {},
    isDebugMode: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Debug Controls (only shown in debug mode)
        if (isDebugMode) {
            DebugControls(
                onFillDebugData = onFillDebugData,
                onFillAlternativeData = onFillAlternativeData,
                onFillMinimalData = onFillMinimalData,
                onClearData = onClearData
            )
        }
        
        // Child Information Section
        SectionHeader(title = "Child Information")
        
        // Child Name Field
        OutlinedTextField(
            value = uiState.childName,
            onValueChange = onChildNameChange,
            label = { Text("Child Name *") },
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Date of Birth Field
        OutlinedTextField(
            value = uiState.dateOfBirth,
            onValueChange = onDateOfBirthChange,
            label = { Text("Date of Birth *") },
            placeholder = { Text("YYYY-MM-DD") },
            isError = uiState.dateOfBirthError != null,
            supportingText = {
                Column {
                    uiState.dateOfBirthError?.let { Text(it) }
                    uiState.getCalculatedAge()?.let { age ->
                        Text(
                            text = "Age: $age years",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            trailingIcon = {
                IconButton(onClick = onDatePickerClick) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Medical Information Field (Optional)
        OutlinedTextField(
            value = uiState.medicalInfo,
            onValueChange = onMedicalInfoChange,
            label = { Text("Medical Information") },
            placeholder = { Text("Any medical conditions, allergies, or medications...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        // Dietary Restrictions Field (Optional)
        OutlinedTextField(
            value = uiState.dietaryRestrictions,
            onValueChange = onDietaryRestrictionsChange,
            label = { Text("Dietary Restrictions") },
            placeholder = { Text("Any food allergies or dietary requirements...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Emergency Contact Section
        SectionHeader(title = "Emergency Contact")
        
        // Emergency Contact Name Field
        OutlinedTextField(
            value = uiState.emergencyContactName,
            onValueChange = onEmergencyContactNameChange,
            label = { Text("Contact Name *") },
            isError = uiState.emergencyContactNameError != null,
            supportingText = uiState.emergencyContactNameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Emergency Contact Phone Field
        OutlinedTextField(
            value = uiState.emergencyContactPhone,
            onValueChange = onEmergencyContactPhoneChange,
            label = { Text("Phone Number *") },
            placeholder = { Text("(555) 123-4567") },
            isError = uiState.emergencyContactPhoneError != null,
            supportingText = uiState.emergencyContactPhoneError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Emergency Contact Relationship Field
        OutlinedTextField(
            value = uiState.emergencyContactRelationship,
            onValueChange = onEmergencyContactRelationshipChange,
            label = { Text("Relationship *") },
            placeholder = { Text("Parent, Guardian, Grandparent, etc.") },
            isError = uiState.emergencyContactRelationshipError != null,
            supportingText = uiState.emergencyContactRelationshipError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Register Button
        Button(
            onClick = onRegisterClick,
            enabled = uiState.isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register Child")
        }
        
        // Form validation summary
        if (!uiState.areRequiredFieldsFilled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Required Fields",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please fill in all required fields marked with *",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Section header component
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

/**
 * Simple date picker dialog
 * In a real implementation, you'd use a proper date picker component
 */
@Composable
private fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var dateInput by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date of Birth") },
        text = {
            Column {
                Text("Please enter the date in YYYY-MM-DD format:")
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // In a real implementation, you'd validate the date here
                    onDateSelected(dateInput)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}/**

 * Pure UI content for Child Registration that doesn't depend on ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildRegistrationContent(
    uiState: ChildRegistrationUiState,
    onNavigateBack: (() -> Unit)? = null,
    onRegistrationSuccess: (() -> Unit)? = null,
    onChildNameChange: (String) -> Unit = {},
    onDateOfBirthChange: (String) -> Unit = {},
    onMedicalInfoChange: (String) -> Unit = {},
    onDietaryRestrictionsChange: (String) -> Unit = {},
    onEmergencyContactNameChange: (String) -> Unit = {},
    onEmergencyContactPhoneChange: (String) -> Unit = {},
    onEmergencyContactRelationshipChange: (String) -> Unit = {},
    onShowDatePicker: () -> Unit = {},
    onHideDatePicker: () -> Unit = {},
    onRegisterChild: () -> Unit = {},
    onClearError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Register Child",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        )
        
        // Form Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Registering child...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                // Form content - simplified for preview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Child Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = uiState.childName,
                        onValueChange = onChildNameChange,
                        label = { Text("Child Name *") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = uiState.dateOfBirth,
                        onValueChange = onDateOfBirthChange,
                        label = { Text("Date of Birth *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        isError = uiState.dateOfBirthError != null,
                        supportingText = {
                            Column {
                                uiState.dateOfBirthError?.let { Text(it) }
                                uiState.getCalculatedAge()?.let { age ->
                                    Text(
                                        text = "Age: $age years",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = onShowDatePicker) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Select Date"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onRegisterChild,
                        enabled = uiState.isFormValid,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Register Child")
                    }
                    
                    if (!uiState.areRequiredFieldsFilled) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Required Fields",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Please fill in all required fields marked with *",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (uiState.showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                onDateOfBirthChange(date)
                onHideDatePicker()
            },
            onDismiss = onHideDatePicker
        )
    }
}

/**
 * Debug controls for easy testing - pre-fills form with test data
 */
@Composable
private fun DebugControls(
    onFillDebugData: () -> Unit,
    onFillAlternativeData: () -> Unit,
    onFillMinimalData: () -> Unit,
    onClearData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Debug Mode - Test Data",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFillDebugData,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Fill Emma", style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = onFillAlternativeData,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Fill Liam", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFillMinimalData,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Fill Zoe", style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedButton(
                    onClick = onClearData,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// MARK: - Preview

@Preview
@Composable
private fun ChildRegistrationContentPreview() {
    MaterialTheme {
        Surface {
            ChildRegistrationContent(
                uiState = ChildRegistrationUiState(
                    childName = "",
                    dateOfBirth = "",
                    isLoading = false
                    // areRequiredFieldsFilled and isFormValid are computed properties
                )
            )
        }
    }
}