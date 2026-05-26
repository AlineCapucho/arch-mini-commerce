package com.example.hexagonal.application.usecases

import com.example.hexagonal.application.ports.ProdutoRepositoryPort
import com.example.hexagonal.domain.entities.Produto

class ListarProdutosUseCase(
    private val produtoRepository: ProdutoRepositoryPort
) {
    fun executar(): List<Produto> = produtoRepository.listar()
}
