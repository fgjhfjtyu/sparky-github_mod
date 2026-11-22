package com.jackby03.sparky.core.extensions

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

fun <T> Flow<T>.asResult(): Flow<AppResult<T>> =
    map<T, AppResult<T>> { AppResult.Success(it) }
        .catch { throwable ->
            emit(AppResult.Error(AppException.UnknownException(throwable)))
        }

fun <T, R> Flow<AppResult<T>>.mapResult(transform: (T) -> R): Flow<AppResult<R>> =
    map { result ->
        when (result) {
            is AppResult.Success -> AppResult.Success(transform(result.data))
            is AppResult.Error   -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }
