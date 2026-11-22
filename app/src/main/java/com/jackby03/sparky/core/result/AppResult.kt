package com.jackby03.sparky.core.result

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: AppException) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class DatabaseException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class SimulationException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class ValidationException(message: String) : AppException(message)
    class ExportException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class UnknownException(cause: Throwable) : AppException(cause.message ?: "Unknown error", cause)
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (AppException) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(exception)
    return this
}

inline fun <T> AppResult<T>.onLoading(action: () -> Unit): AppResult<T> {
    if (this is AppResult.Loading) action()
    return this
}

fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error   -> this
    is AppResult.Loading -> AppResult.Loading
}
