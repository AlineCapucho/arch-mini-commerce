package com.example.ddd.application.usecases.pedido

import com.example.ddd.domain.entities.Pedido
import com.example.ddd.domain.repositories.PedidoRepository
import org.springframework.stereotype.Service

@Service
class PagarPedidoUseCase(
    private val pedidoRepository: PedidoRepository
) {
    fun executar(pedidoId: Long): Pedido {
        val pedido = pedidoRepository.buscarPorId(pedidoId)
            ?: throw NoSuchElementException("Pedido $pedidoId não encontrado")

        pedido.pagar()

        return pedidoRepository.salvar(pedido)
    }
}
