package rfm.hillsongptapp.feature.kids.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
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
import rfm.hillsongptapp.core.data.model.Child

/**
 * Screen for editing existing child information with pre-populated form
 * Includes real-time validation, optimistic updates, and change confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildEditScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    onUpdateSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChildEditViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize form with child data
    LaunchedEffect(childId) {
        viewModel.initializeWithChild(childId)
    }
    
    // Handle update success
    LaunchedEffect(uiState.isUpdateSuccessful) {
        if (uiState.isUpdateSuccessful) {
            onUpdateSuccess()
        }
    }
    
    // Handle back navigation with unsaved changes
    val handleBackNavigation = {
        if (uiState.hasChanges && !uiState.isSaving) {
            viewModel.showDiscardChangesDialog()
        } else {
            onNavigateBack()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.edit_child_title, uiState.childName),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = handleBackNavigation) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(Res.string.back)
                    )
                }
            },
            actions = {
                // Reset button
                if (uiState.hasChanges) {
                    IconButton(
                        onClick = viewModel::resetForm,
                        enabled = !uiState.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Changes"
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
                EditForm(
                    uiState = uiState,
                    onChildNameChange = viewModel::updateChildName,
                    onDateOfBirthChange = viewModel::updateDateOfBirth,
                    onMedicalInfoChange = viewModel::updateMedicalInfo,
                    onDietaryRestrictionsChange = viewModel::updateDietaryRestrictions,
                    onEmergencyContactNameChange = viewModel::updateEmergencyContactName,
                    onEmergencyContactPhoneChange = viewModel::updateEmergencyContactPhone,
                    onEmergencyContactRelationshipChange = viewModel::updateEmergencyContactRelationship,
                    onDatePickerClick = viewModel::showDatePicker,
                    onSaveClick = viewModel::saveChildInformation
                )
            }
        }
    }
    
    // Date Picker Dialog
    if (uiState.showDatePicker) {
        DatePickerDialog(
            currentDate = uiState.dateOfBirth,
            onDateSelected = { date ->
                viewModel.updateDateOfBirth(date)
                viewModel.hideDatePicker()
            },
            onDismiss = viewModel::hideDatePicker
        )
    }
    
    // Discard Changes Dialog
    if (uiState.showDiscardChangesDialog) {
        DiscardChangesDialog(
            onConfirm = {
                viewModel.hideDiscardChangesDialog()
                onNavigateBack()
            },
            onDismiss = viewModel::hideDiscardChangesDialog
        )
    }
    
    // Success Dialog
    if (uiState.showSuccessDialog) {
        SuccessDialog(
            onDismiss = {
                viewModel.hideSuccessDialog()
                onNavigateBack()
            }
        )
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, show snackbar here
            // Edit error occurred
            viewModel.clearError()
        }
    }
}

/**
 * Loading content displayed during data loading
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
                text = "Loading child information...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Main edit form with pre-populated data
 */
@Composable
private fun EditForm(
    uiState: ChildEditUiState,
    onChildNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onMedicalInfoChange: (String) -> Unit,
    onDietaryRestrictionsChange: (String) -> Unit,
    onEmergencyContactNameChange: (String) -> Unit,
    onEmergencyContactPhoneChange: (String) -> Unit,
    onEmergencyContactRelationshipChange: (String) -> Unit,
    onDatePickerClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Changes indicator
        if (uiState.hasChanges) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "You have unsaved changes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
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
            singleLine = true,
            enabled = !uiState.isSaving
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
                IconButton(
                    onClick = onDatePickerClick,
                    enabled = !uiState.isSaving
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.isSaving
        )
        
        // Medical Information Field (Optional)
        OutlinedTextField(
            value = uiState.medicalInfo,
            onValueChange = onMedicalInfoChange,
            label = { Text("Medical Information") },
            placeholder = { Text("Any medical conditions, allergies, or medications...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            enabled = !uiState.isSaving
        )
        
        // Dietary Restrictions Field (Optional)
        OutlinedTextField(
            value = uiState.dietaryRestrictions,
            onValueChange = onDietaryRestrictionsChange,
            label = { Text("Dietary Restrictions") },
            placeholder = { Text("Any food allergies or dietary requirements...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            enabled = !uiState.isSaving
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
            singleLine = true,
            enabled = !uiState.isSaving
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
            singleLine = true,
            enabled = !uiState.isSaving
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
            singleLine = true,
            enabled = !uiState.isSaving
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Save Button
        Button(
            onClick = onSaveClick,
            enabled = uiState.isFormValid && uiState.hasChanges && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...")
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes")
            }
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
        } else if (!uiState.hasChanges) {
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
                        text = "No Changes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Make changes to the form to enable saving",
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
 * Date picker dialog with current date pre-filled
 */
@Composable
private fun DatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var dateInput by remember { mutableStateOf(currentDate) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Date of Birth") },
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
}

/**
 * Dialog to confirm discarding unsaved changes
 */
@Composable
private fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Discard Changes?") },
        text = {
            Text("You have unsaved changes. Are you sure you want to discard them and go back?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Discard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep Editing")
            }
        }
    )
}

/**
 * Success dialog shown after successful update
 */
@Composable
private fun SuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Changes Saved") },
        text = {
            Text("The child's information has been successfully updated.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildEditContent(
    uiState: ChildEditUiState,
    childId: String,
    onNavigateBack: () -> Unit = {},
    onUpdateSuccess: () -> Unit = {},
    onInitializeWithChild: (String) -> Unit = {},
    onChildNameChange: (String) -> Unit = {},
    onDateOfBirthChange: (String) -> Unit = {},
    onMedicalInfoChange: (String) -> Unit = {},
    onDietaryRestrictionsChange: (String) -> Unit = {},
    onEmergencyContactNameChange: (String) -> Unit = {},
    onEmergencyContactPhoneChange: (String) -> Unit = {},
    onEmergencyContactRelationshipChange: (String) -> Unit = {},
    onShowDatePicker: () -> Unit = {},
    onHideDatePicker: () -> Unit = {},
    onUpdateChild: () -> Unit = {},
    onShowDiscardDialog: () -> Unit = {},
    onHideDiscardDialog: () -> Unit = {},
    onConfirmDiscard: () -> Unit = {},
    onHideSuccessDialog: () -> Unit = {},
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
                    text = if (uiState.originalChild != null) {
                        "Edit ${uiState.originalChild!!.name}"
                    } else {
                        "Edit Child"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (uiState.hasChanges) {
                            onShowDiscardDialog()
                        } else {
                            onNavigateBack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                if (uiState.hasChanges) {
                    IconButton(
                        onClick = { onInitializeWithChild(childId) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Changes"
                        )
                    }
                }
            }
        )
        
        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
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
                                text = if (uiState.isSaving) "Saving changes..." else "Loading child information...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                else -> {
                    // Form content would go here - simplified for preview
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Child Edit Form",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        
                        if (uiState.hasChanges) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(
                                    text = "You have unsaved changes",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Button(
                            onClick = onUpdateChild,
                            enabled = uiState.isFormValid && !uiState.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (uiState.showDatePicker) {
        DatePickerDialog(
            currentDate = uiState.dateOfBirth,
            onDateSelected = { date ->
                onDateOfBirthChange(date)
                onHideDatePicker()
            },
            onDismiss = onHideDatePicker
        )
    }
    
    if (uiState.showDiscardChangesDialog) {
        DiscardChangesDialog(
            onConfirm = {
                onConfirmDiscard()
                onNavigateBack()
            },
            onDismiss = onHideDiscardDialog
        )
    }
    
    if (uiState.showSuccessDialog) {
        SuccessDialog(
            onDismiss = {
                onHideSuccessDialog()
                onUpdateSuccess()
            }
        )
    }
}

// MARK: - Preview

@Preview
@Composable
private fun ChildEditContentPreview() {
    MaterialTheme {
        Surface {
            ChildEditContent(
                uiState = ChildEditUiState(
                    originalChild = null,
                    childName = "",
                    dateOfBirth = "",
                    isLoading = false,
                    // hasChanges is computed property
                ),
                childId = "preview-child"
            )
        }
    }
}