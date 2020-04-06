package com.viceboy.babble.ui.state

sealed class Resource<out T> {
    class Loading<out T> : Resource<T>()
    data class Success<out T>(val data: T?) : Resource<T>()
    data class Failure<out T>(val message: String?) : Resource<T>()
}

sealed class OtpSendingState<out T> {
    class Loading<out T> : OtpSendingState<T>()
    data class Sent<out T>(val data: String) : OtpSendingState<T>()
    data class Verified<out T>(val data: T?) : OtpSendingState<T>()
    data class Failed<out T>(val message: String?) : OtpSendingState<T>()
}

/**
 * Enum Class to manage Otp verification state
 */
enum class OtpVerificationState {
    SUCCESS, FAILED
}

/**
 * Enum Class to manage button state
 */
enum class ButtonState {
    DISABLE, ACTIVE, CLICKED, ANIMATING, INVISIBLE
}

/**
 * Enum class to manage user search state via email
 */
enum class SearchState {
    IDLE, IN_PROGRESS, SUCCESS, ERROR, FAILED
}

/**
 * Enum class to manage data load state
 */
enum class DataLoad {
    IN_PROGRESS, SUCCESS, ERROR
}
