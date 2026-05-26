package com.example.ddd.domain.repositories

import com.example.ddd.domain.entities.Produto

interface ProdutoRepository {
    fun salvar(produto: Produto): Produto
    fun buscarPorId(id: Long): Produto?
    fun listar(): List<Produto>
    fun salvarTodos(produtos: List<Produto>): List<Produto>
}
