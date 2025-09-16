package rfm.com.exception

/**
 * Base class for all business logic exceptions
 */
abstract class BusinessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception thrown when a requested entity is not found
 */
class EntityNotFoundException(
    entityType: String,
    identifier: Any
) : BusinessException("$entityType with identifier '$identifier' not found")

/**
 * Exception thrown when a user tries to perform an action they're not authorized for
 */
class UnauthorizedActionException(
    action: String,
    reason: String? = null
) : BusinessException("Unauthorized to perform action: $action${reason?.let { " - $it" } ?: ""}")

/**
 * Exception thrown when a business rule is violated
 */
class BusinessRuleViolationException(
    rule: String,
    details: String? = null
) : BusinessException("Business rule violation: $rule${details?.let { " - $it" } ?: ""}")

/**
 * Exception thrown when an entity already exists and shouldn't be duplicated
 */
class EntityAlreadyExistsException(
    entityType: String,
    identifier: Any
) : BusinessException("$entityType with identifier '$identifier' already exists")

/**
 * Exception thrown when an operation conflicts with current state
 */
class ConflictException(
    operation: String,
    reason: String
) : BusinessException("Cannot perform operation '$operation': $reason")

/**
 * Exception thrown when required data is invalid or missing
 */
class InvalidDataException(
    field: String,
    value: Any?,
    reason: String
) : BusinessException("Invalid data for field '$field' with value '$value': $reason")

/**
 * Exception thrown when a service operation fails
 */
class ServiceOperationException(
    service: String,
    operation: String,
    reason: String,
    cause: Throwable? = null
) : BusinessException("$service service failed to perform '$operation': $reason", cause)

/**
 * Exception thrown when external service integration fails
 */
class ExternalServiceException(
    serviceName: String,
    operation: String,
    cause: Throwable? = null
) : BusinessException("External service '$serviceName' failed during '$operation'", cause)

/**
 * Exception thrown when file operations fail
 */
class FileOperationException(
    operation: String,
    fileName: String,
    reason: String,
    cause: Throwable? = null
) : BusinessException("File operation '$operation' failed for file '$fileName': $reason", cause)

/**
 * Exception thrown when capacity limits are exceeded
 */
class CapacityExceededException(
    resource: String,
    currentCount: Int,
    maxCapacity: Int
) : BusinessException("Capacity exceeded for '$resource': $currentCount/$maxCapacity")

/**
 * Exception thrown when an event or service is not available for the requested action
 */
class UnavailableException(
    resource: String,
    reason: String
) : BusinessException("$resource is not available: $reason")