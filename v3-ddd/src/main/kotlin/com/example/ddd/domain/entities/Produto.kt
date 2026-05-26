package com.example.ddd.domain.entities

import com.example.ddd.domain.errors.DomainException
import com.example.ddd.domain.valueobjects.Preco

class Produto(
    val id: Long,
    val nome: String,
    val preco: Preco,
    var quantidadeEstoque: Int
) {
    init {
        require(nome.isNotBlank()) { "Nome do produto não pode ser em branco" }
        require(quantidadeEstoque >= 0) { "Quantidade em estoque não pode ser negativa" }
    }

    fun temEstoque(qtd: Int): Boolean = quantidadeEstoque >= qtd

    fun decrementarEstoque(qtd: Int) {
        if (!temEstoque(qtd)) {
            throw DomainException("Estoque insuficiente para o produto $id")
        }
        quantidadeEstoque -= qtd
    }
}
