package com.example.monolito.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import jakarta.validation.constraints.Positive

data class ProdutoRequest(
    @field:NotBlank(message = "Nome não pode ser vazio")
    val nome: String,

    @field:Positive(message = "Preço deve ser maior que zero")
    val preco: BigDecimal,

    @field:PositiveOrZero(message = "Quantidade em estoque não pode ser negativa")
    val quantidadeEstoque: Int
)
