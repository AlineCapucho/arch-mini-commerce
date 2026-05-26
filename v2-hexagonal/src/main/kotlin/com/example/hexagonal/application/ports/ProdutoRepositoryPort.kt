package com.example.hexagonal.application.ports

import com.example.hexagonal.domain.entities.Produto

interface ProdutoRepositoryPort {
    fun salvar(produto: Produto): Produto
    fun buscarPorId(id: Long): Produto?
    fun listar(): List<Produto>
    fun salvarTodos(produtos: List<Produto>): List<Produto>
}
