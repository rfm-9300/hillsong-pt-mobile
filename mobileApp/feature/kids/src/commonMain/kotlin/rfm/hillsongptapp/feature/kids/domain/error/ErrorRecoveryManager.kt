package rfm.hillsongptapp.feature.kids.domain.error

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.offline.OfflineHandler
import co.touchlab.kermit.Logger

/**
 * Manages error recovery flows and user guidance for kids management operations
 */
class ErrorRecoveryManager(
    private val errorHandler: ErrorHandler,
    private val offlineHandler: OfflineHandler,
    private val logger: Logger
) {
    
    private val _recoveryState = MutableStateFlow<RecoveryState>(RecoveryState.Idle)
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()
    
    private val _recoveryHistory = MutableStateFlow<List<RecoveryAttempt>>(emptyList())
    val recoveryHistory: StateFlow<List<RecoveryAttempt>> = _recoveryHistory.asStateFlow()
    
    /**
     * Start error recovery process
     */
    suspend fun startRecovery(
        error: Throwable,
        operation: suspend () -> Result<Any>,
        context: RecoveryContext
    ): RecoveryResult {
        logger.i { "Starting error recovery for: ${error.message}" }
        
        val errorInfo = errorHandler.handleError(error)
        _recoveryState.value = RecoveryState.Analyzing(errorInfo)
        
        val recoveryPlan = createRecoveryPlan(errorInfo, context)
        _recoveryState.value = RecoveryState.Executing(recoveryPlan)
        
        val result = executeRecoveryPlan(recoveryPlan, operation, context)
        
        // Record recovery attempt
        recordRecoveryAttempt(RecoveryAttempt(
            error = errorInfo,
            plan = recoveryPlan,
            result = result,
            timestamp = System.currentTimeMillis()
        ))
        
        _recoveryState.value = when (result) {
            is RecoveryResult.Success -> RecoveryState.Completed(result)
            is RecoveryResult.Failed -> RecoveryState.Failed(result)
            is RecoveryResult.RequiresUserAction -> RecoveryState.AwaitingUserAction(result)
        }
        
        return result
    }
    
    /**
     * Create recovery plan based on error type and context
     */
    private fun createRecoveryPlan(errorInfo: ErrorInfo, context: RecoveryContext): RecoveryPlan {
        val steps = mutableListOf<RecoveryStep>()
        
        when (errorInfo.type) {
            ErrorType.NETWORK -> {
                steps.add(RecoveryStep.CheckConnectivity)
                if (errorInfo.isRetryable) {
                    steps.add(RecoveryStep.RetryWithDelay(2000L))
                }
                steps.add(RecoveryStep.FallbackToOffline)
            }
            
            ErrorType.SERVER -> {
                steps.add(RecoveryStep.RetryWithBackoff(maxAttempts = 3))
                steps.add(RecoveryStep.CheckServiceStatus)
                steps.add(RecoveryStep.NotifyUser("Server issues detected"))
            }
            
            ErrorType.VALIDATION -> {
                steps.add(RecoveryStep.HighlightErrors)
                steps.add(RecoveryStep.ProvideGuidance(errorInfo.suggestedAction))
                steps.add(RecoveryStep.RequireUserInput)
            }
            
            ErrorType.BUSINESS_RULE -> {
                steps.add(RecoveryStep.ExplainBusinessRule(errorInfo.userMessage))
                steps.add(RecoveryStep.SuggestAlternatives)
                steps.add(RecoveryStep.RequireUserInput)
            }
            
            ErrorType.NOT_FOUND -> {
                steps.add(RecoveryStep.RefreshData)
                steps.add(RecoveryStep.RetryOperation)
                steps.add(RecoveryStep.SuggestAlternatives)
            }
            
            ErrorType.CONFLICT -> {
                steps.add(RecoveryStep.RefreshData)
                steps.add(RecoveryStep.ResolveConflict)
                steps.add(RecoveryStep.RetryOperation)
            }
            
            ErrorType.AUTHENTICATION -> {
                steps.add(RecoveryStep.RefreshAuth)
                steps.add(RecoveryStep.RequireReauth)
            }
            
            ErrorType.REAL_TIME -> {
                steps.add(RecoveryStep.ReconnectRealTime)
                steps.add(RecoveryStep.FallbackToPolling)
                steps.add(RecoveryStep.NotifyLimitedFunctionality)
            }
            
            else -> {
                steps.add(RecoveryStep.LogError)
                steps.add(RecoveryStep.RetryWithDelay(1000L))
                steps.add(RecoveryStep.NotifyUser("Unexpected error occurred"))
            }
        }
        
        return RecoveryPlan(
            errorInfo = errorInfo,
            steps = steps,
            context = context,
            estimatedDuration = estimateRecoveryDuration(steps)
        )
    }
    
    /**
     * Execute recovery plan
     */
    private suspend fun executeRecoveryPlan(
        plan: RecoveryPlan,
        operation: suspend () -> Result<Any>,
        context: RecoveryContext
    ): RecoveryResult {
        logger.d { "Executing recovery plan with ${plan.steps.size} steps" }
        
        for ((index, step) in plan.steps.withIndex()) {
            logger.d { "Executing recovery step ${index + 1}/${plan.steps.size}: $step" }
            
            val stepResult = executeRecoveryStep(step, operation, context)
            
            when (stepResult) {
                is StepResult.Success -> {
                    if (step is RecoveryStep.RetryOperation || step is RecoveryStep.RetryWithDelay || step is RecoveryStep.RetryWithBackoff) {
                        return RecoveryResult.Success("Operation completed successfully after recovery")
                    }
                    continue
                }
                
                is StepResult.Failed -> {
                    logger.w { "Recovery step failed: ${stepResult.reason}" }
                    continue
                }
                
                is StepResult.RequiresUserAction -> {
                    return RecoveryResult.RequiresUserAction(
                        message = stepResult.message,
                        actions = stepResult.actions,
                        context = context
                    )
                }
                
                is StepResult.Skip -> {
                    logger.d { "Skipping recovery step: ${stepResult.reason}" }
                    continue
                }
            }
        }
        
        return RecoveryResult.Failed(
            reason = "All recovery steps completed but operation still failed",
            canRetry = plan.errorInfo.isRetryable
        )
    }
    
    /**
     * Execute individual recovery step
     */
    private suspend fun executeRecoveryStep(
        step: RecoveryStep,
        operation: suspend () -> Result<Any>,
        context: RecoveryContext
    ): StepResult {
        return when (step) {
            is RecoveryStep.CheckConnectivity -> {
                if (offlineHandler.isOffline.value) {
                    StepResult.Failed("Device is offline")
                } else {
                    StepResult.Success
                }
            }
            
            is RecoveryStep.RetryWithDelay -> {
                delay(step.delayMs)
                val result = operation()
                if (result.isSuccess) StepResult.Success else StepResult.Failed("Retry failed")
            }
            
            is RecoveryStep.RetryWithBackoff -> {
                var lastError: Throwable? = null
                repeat(step.maxAttempts) { attempt ->
                    val delayMs = step.baseDelayMs * (1L shl attempt) // Exponential backoff
                    if (attempt > 0) delay(delayMs)
                    
                    val result = operation()
                    if (result.isSuccess) return StepResult.Success
                    lastError = result.exceptionOrNull()
                }
                StepResult.Failed("All retry attempts failed: ${lastError?.message}")
            }
            
            is RecoveryStep.RefreshData -> {
                // This would trigger data refresh in the actual implementation
                StepResult.Success
            }
            
            is RecoveryStep.FallbackToOffline -> {
                offlineHandler.setOfflineStatus(true)
                StepResult.RequiresUserAction(
                    message = "Working offline with limited functionality",
                    actions = listOf("Continue Offline", "Retry Connection")
                )
            }
            
            is RecoveryStep.RequireUserInput -> {
                StepResult.RequiresUserAction(
                    message = "Please review and correct the information",
                    actions = listOf("Review", "Cancel")
                )
            }
            
            is RecoveryStep.NotifyUser -> {
                StepResult.RequiresUserAction(
                    message = step.message,
                    actions = listOf("OK")
                )
            }
            
            is RecoveryStep.ProvideGuidance -> {
                StepResult.RequiresUserAction(
                    message = step.guidance,
                    actions = listOf("Got it", "Need Help")
                )
            }
            
            else -> StepResult.Success
        }
    }
    
    /**
     * Record recovery attempt for analysis
     */
    private fun recordRecoveryAttempt(attempt: RecoveryAttempt) {
        val currentHistory = _recoveryHistory.value.toMutableList()
        currentHistory.add(attempt)
        
        // Keep only last 50 attempts
        if (currentHistory.size > 50) {
            currentHistory.removeAt(0)
        }
        
        _recoveryHistory.value = currentHistory
    }
    
    /**
     * Estimate recovery duration
     */
    private fun estimateRecoveryDuration(steps: List<RecoveryStep>): Long {
        return steps.sumOf { step ->
            when (step) {
                is RecoveryStep.RetryWithDelay -> step.delayMs
                is RecoveryStep.RetryWithBackoff -> step.baseDelayMs * step.maxAttempts
                is RecoveryStep.CheckConnectivity -> 1000L
                is RecoveryStep.RefreshData -> 2000L
                else -> 500L
            }
        }
    }
    
    /**
     * Get recovery suggestions based on error history
     */
    fun getRecoverySuggestions(errorType: ErrorType): List<String> {
        val recentAttempts = _recoveryHistory.value.takeLast(10)
        val frequentErrors = recentAttempts.groupBy { it.error.type }.mapValues { it.value.size }
        
        return when (errorType) {
            ErrorType.NETWORK -> {
                if (frequentErrors[ErrorType.NETWORK] ?: 0 > 3) {
                    listOf(
                        "Check your internet connection",
                        "Try switching between WiFi and mobile data",
                        "Contact your network provider if issues persist"
                    )
                } else {
                    listOf("Check your internet connection", "Try again in a moment")
                }
            }
            
            ErrorType.VALIDATION -> listOf(
                "Review the highlighted fields",
                "Check for required information",
                "Ensure all formats are correct"
            )
            
            ErrorType.BUSINESS_RULE -> listOf(
                "Review the operation requirements",
                "Check child's current status",
                "Try an alternative service if available"
            )
            
            else -> listOf("Try again", "Contact support if the problem persists")
        }
    }
    
    /**
     * Reset recovery state
     */
    fun resetRecoveryState() {
        _recoveryState.value = RecoveryState.Idle
    }
}

/**
 * Recovery state tracking
 */
sealed class RecoveryState {
    data object Idle : RecoveryState()
    data class Analyzing(val error: ErrorInfo) : RecoveryState()
    data class Executing(val plan: RecoveryPlan) : RecoveryState()
    data class Completed(val result: RecoveryResult.Success) : RecoveryState()
    data class Failed(val result: RecoveryResult.Failed) : RecoveryState()
    data class AwaitingUserAction(val result: RecoveryResult.RequiresUserAction) : RecoveryState()
}

/**
 * Recovery plan with steps to resolve errors
 */
data class RecoveryPlan(
    val errorInfo: ErrorInfo,
    val steps: List<RecoveryStep>,
    val context: RecoveryContext,
    val estimatedDuration: Long
)

/**
 * Individual recovery steps
 */
sealed class RecoveryStep {
    data object CheckConnectivity : RecoveryStep()
    data class RetryWithDelay(val delayMs: Long) : RecoveryStep()
    data class RetryWithBackoff(val maxAttempts: Int, val baseDelayMs: Long = 1000L) : RecoveryStep()
    data object RefreshData : RecoveryStep()
    data object RetryOperation : RecoveryStep()
    data object FallbackToOffline : RecoveryStep()
    data object RequireUserInput : RecoveryStep()
    data class NotifyUser(val message: String) : RecoveryStep()
    data class ProvideGuidance(val guidance: String) : RecoveryStep()
    data object HighlightErrors : RecoveryStep()
    data object SuggestAlternatives : RecoveryStep()
    data class ExplainBusinessRule(val explanation: String) : RecoveryStep()
    data object ResolveConflict : RecoveryStep()
    data object RefreshAuth : RecoveryStep()
    data object RequireReauth : RecoveryStep()
    data object ReconnectRealTime : RecoveryStep()
    data object FallbackToPolling : RecoveryStep()
    data object NotifyLimitedFunctionality : RecoveryStep()
    data object CheckServiceStatus : RecoveryStep()
    data object LogError : RecoveryStep()
}

/**
 * Result of recovery step execution
 */
sealed class StepResult {
    data object Success : StepResult()
    data class Failed(val reason: String) : StepResult()
    data class RequiresUserAction(val message: String, val actions: List<String>) : StepResult()
    data class Skip(val reason: String) : StepResult()
}

/**
 * Overall recovery result
 */
sealed class RecoveryResult {
    data class Success(val message: String) : RecoveryResult()
    data class Failed(val reason: String, val canRetry: Boolean) : RecoveryResult()
    data class RequiresUserAction(
        val message: String,
        val actions: List<String>,
        val context: RecoveryContext
    ) : RecoveryResult()
}

/**
 * Context for recovery operations
 */
data class RecoveryContext(
    val operationType: String,
    val userId: String?,
    val childId: String?,
    val serviceId: String?,
    val additionalData: Map<String, Any> = emptyMap()
)

/**
 * Record of recovery attempt
 */
data class RecoveryAttempt(
    val error: ErrorInfo,
    val plan: RecoveryPlan,
    val result: RecoveryResult,
    val timestamp: Long
)