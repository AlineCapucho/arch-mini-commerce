package com.example.hexagonal.application.usecases

import com.example.hexagonal.application.ports.PedidoRepositoryPort
import com.example.hexagonal.domain.entities.Pedido
import com.example.hexagonal.domain.entities.StatusPedido
import com.example.hexagonal.domain.errors.DomainException

class PagarPedidoUseCase(
    private val pedidoRepository: PedidoRepositoryPort
) {
    fun executar(pedidoId: Long): Pedido {
        val pedido = pedidoRepository.buscarPorId(pedidoId)
            ?: throw NoSuchElementException("Pedido $pedidoId não encontrado")

        if (pedido.status == StatusPedido.CANCELADO) {
            throw DomainException("Pedido cancelado não pode ser pago")
        }

        val pedidoPago = pedido.copy(status = StatusPedido.PAGO)
        return pedidoRepository.salvar(pedidoPago)
    }
}
