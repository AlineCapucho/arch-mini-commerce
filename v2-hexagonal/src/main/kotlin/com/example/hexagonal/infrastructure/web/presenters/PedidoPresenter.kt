package com.example.hexagonal.infrastructure.web.presenters

import com.example.hexagonal.domain.entities.Pedido

object PedidoPresenter {

    fun toResponse(pedido: Pedido): Map<String, Any?> = mapOf(
        "id" to pedido.id,
        "clienteNome" to pedido.clienteNome,
        "clienteCpf" to pedido.clienteCpf,
        "status" to pedido.status.name,
        "valorTotal" to pedido.valorTotal,
        "itens" to pedido.itens.map { item ->
            mapOf(
                "produtoId" to item.produto.id,
                "nomeProduto" to item.produto.nome,
                "precoProduto" to item.produto.preco,
                "quantidade" to item.quantidade
            )
        }
    )

    fun toResponseList(pedidos: List<Pedido>): List<Map<String, Any?>> =
        pedidos.map { toResponse(it) }
}
