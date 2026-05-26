package com.example.ddd.infrastructure.web.presenters

import com.example.ddd.domain.entities.Pedido

object PedidoPresenter {

    fun toResponse(pedido: Pedido): Map<String, Any?> = mapOf(
        "id" to pedido.id,
        "clienteNome" to pedido.cliente.nome,
        "clienteCpf" to pedido.cliente.cpf,
        "status" to pedido.status.name,
        "valorTotal" to pedido.valorTotal.value,
        "itens" to pedido.itens.map { item ->
            mapOf(
                "produtoId" to item.produtoId,
                "preco" to item.preco.value,
                "quantidade" to item.quantidade
            )
        }
    )

    fun toResponseList(pedidos: List<Pedido>): List<Map<String, Any?>> =
        pedidos.map { toResponse(it) }
}
