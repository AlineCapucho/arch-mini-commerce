package com.example.ddd.application.usecases.pedido

import com.example.ddd.domain.entities.Pedido
import com.example.ddd.domain.repositories.PedidoRepository
import org.springframework.stereotype.Service

@Service
class ListarPedidosUseCase(
    private val pedidoRepository: PedidoRepository
) {
    fun executar(): List<Pedido> = pedidoRepository.listar()
}
