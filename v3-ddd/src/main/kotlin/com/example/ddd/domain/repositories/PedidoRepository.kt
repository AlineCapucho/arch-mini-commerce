package com.example.ddd.domain.repositories

import com.example.ddd.domain.entities.Pedido

interface PedidoRepository {
    fun salvar(pedido: Pedido): Pedido
    fun buscarPorId(id: Long): Pedido?
    fun listar(): List<Pedido>
}
