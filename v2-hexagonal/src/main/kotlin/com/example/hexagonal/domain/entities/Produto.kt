package com.example.hexagonal.domain.entities

import java.math.BigDecimal

data class Produto(
    val id: Long = 0,
    val nome: String,
    val preco: BigDecimal,
    val quantidadeEstoque: Int
)
