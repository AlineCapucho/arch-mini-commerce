package com.example.ddd.application.usecases.dto

import java.math.BigDecimal

data class ProdutoCommand(
    val nome: String,
    val preco: BigDecimal,
    val quantidadeEstoque: Int
)
