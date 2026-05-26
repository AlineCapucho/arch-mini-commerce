package com.example.monolito.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(val status: Int, val error: String, val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Recurso não encontrado"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleUnprocessableEntity(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = "Unprocessable Entity",
            message = ex.message ?: "Operação não permitida"
        )
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Requisição inválida"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        val body = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = details.ifBlank { "Dados de entrada inválidos" }
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}
