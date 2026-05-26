package com.example.hexagonal.domain.entities

import java.math.BigDecimal

data class Pedido(
    val id: Long = 0,
    val clienteNome: String,
    val clienteCpf: String,
    val status: StatusPedido = StatusPedido.PENDENTE,
    val itens: MutableList<ItemPedido> = mutableListOf(),
    val valorTotal: BigDecimal = BigDecimal.ZERO
)
