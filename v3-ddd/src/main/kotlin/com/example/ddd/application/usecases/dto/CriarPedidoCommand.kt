package com.example.ddd.application.usecases.dto

data class CriarPedidoCommand(
    val clienteNome: String,
    val clienteCpf: String,
    val produtoIds: List<Long>
)
