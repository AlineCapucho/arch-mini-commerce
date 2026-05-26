package com.example.ddd.domain.entities

import com.example.ddd.domain.valueobjects.Preco

data class ItemPedido(
    val produtoId: Long,
    val preco: Preco,
    val quantidade: Int
) {
    fun subtotal(): Preco = preco * quantidade
}
