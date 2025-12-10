package com.princess.student_activity.helpers

import com.princess.student_activity.dto.ErrorResponseDTO
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ErrorResponseDTO =
        ErrorResponseDTO(
            error = ex.bindingResult.fieldErrors
                .map { error -> "In ${error.field}, ${error.defaultMessage}" }
                .toList()
        )

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        DuplicateKeyException::class,
        HttpMessageNotReadableException::class,
        IllegalArgumentException::class,
        MethodArgumentTypeMismatchException::class,
        InvalidCredentialsException::class
    )
    fun badRequestException(ex: Exception): ErrorResponseDTO =
        ErrorResponseDTO(error = listOf(ex.message ?: "Bad request."))

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException::class)
    fun notFoundException(ex: Exception): ErrorResponseDTO =
        ErrorResponseDTO(error = listOf(ex.message ?: "Resource not found."))

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidLoginException::class, UnauthorizedUserException::class)
    fun unauthorizedException(ex: Exception): ErrorResponseDTO =
        ErrorResponseDTO(error = listOf(ex.message ?: "Unauthorized access."))
}

class ResourceNotFoundException(message: String) : RuntimeException(message)
class DuplicateKeyException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class InvalidLoginException(message: String) : RuntimeException(message)
class UnauthorizedUserException(message: String) : RuntimeException(message)
