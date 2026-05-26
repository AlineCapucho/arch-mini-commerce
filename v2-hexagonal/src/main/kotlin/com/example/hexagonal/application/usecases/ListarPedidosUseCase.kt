package com.example.hexagonal.application.usecases

import com.example.hexagonal.application.ports.PedidoRepositoryPort
import com.example.hexagonal.domain.entities.Pedido

class ListarPedidosUseCase(
    private val pedidoRepository: PedidoRepositoryPort
) {
    fun executar(): List<Pedido> = pedidoRepository.listar()
}
