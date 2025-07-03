package example.com.plugins

// Logger.kt

/**
 * A simple Logger class to handle colored debug and error messages.
 */
object Logger {
    // ANSI escape codes for colors
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"

    /**
     * Prints a debug message in red.
     *
     * @param message The debug message to be printed.
     */
    fun d(message: String) {
        println("$RED[DEBUG] $message$RESET")
    }

    /**
     * Prints an error message in red.
     *
     * @param message The error message to be printed.
     */
    fun error(message: String) {
        println("$RED[ERROR] $message$RESET")
    }

    /**
     * Prints an info message in green.
     *
     * @param message The info message to be printed.
     */
    fun info(message: String) {
        println("$GREEN[INFO] $message$RESET")
    }

    /**
     * Prints a warning message in yellow.
     *
     * @param message The warning message to be printed.
     */
    fun warn(message: String) {
        println("$YELLOW[WARN] $message$RESET")
    }

    /**
     * Prints a success message in blue.
     *
     * @param message The success message to be printed.
     */
    fun success(message: String) {
        println("$BLUE[SUCCESS] $message$RESET")
    }
}