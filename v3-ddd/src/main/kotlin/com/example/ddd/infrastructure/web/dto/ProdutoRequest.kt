package com.example.ddd.infrastructure.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class ProdutoRequest(
    @field:NotBlank(message = "Nome não pode ser vazio")
    val nome: String,
    @field:Positive(message = "Preço deve ser maior que zero")
    val preco: BigDecimal,
    @field:PositiveOrZero(message = "Quantidade em estoque não pode ser negativa")
    val quantidadeEstoque: Int
)
