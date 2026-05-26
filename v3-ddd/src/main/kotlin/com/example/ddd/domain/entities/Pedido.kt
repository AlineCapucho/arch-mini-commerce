package com.example.ddd.domain.entities

import com.example.ddd.domain.errors.DomainException
import com.example.ddd.domain.services.CalculadoraDeDesconto
import com.example.ddd.domain.valueobjects.Cliente
import com.example.ddd.domain.valueobjects.Preco

class Pedido constructor(
    val id: Long = 0,
    val cliente: Cliente,
    var status: StatusPedido = StatusPedido.PENDENTE,
    itens: List<ItemPedido> = emptyList(),
    var valorTotal: Preco = Preco.ZERO
) {
    private val _itens: MutableList<ItemPedido> = mutableListOf()

    val itens: List<ItemPedido> get() = _itens

    init {
        _itens.addAll(itens)
    }

    fun adicionarItem(produto: Produto, quantidade: Int) {
        if (!produto.temEstoque(quantidade)) {
            throw DomainException("Estoque insuficiente para o produto ${produto.id}")
        }
        produto.decrementarEstoque(quantidade)
        _itens.add(ItemPedido(produtoId = produto.id, preco = produto.preco, quantidade = quantidade))
    }

    fun calcularTotal(calculadora: CalculadoraDeDesconto) {
        val valorBruto = _itens.fold(Preco.ZERO) { acc, item -> acc + item.subtotal() }
        valorTotal = calculadora.calcular(valorBruto)
    }

    fun pagar() {
        if (status == StatusPedido.CANCELADO) {
            throw DomainException("Pedido cancelado não pode ser pago")
        }
        status = StatusPedido.PAGO
    }
}
