package com.example.hexagonal.application.ports

import com.example.hexagonal.domain.entities.Pedido

interface PedidoRepositoryPort {
    fun salvar(pedido: Pedido): Pedido
    fun buscarPorId(id: Long): Pedido?
    fun listar(): List<Pedido>
}
