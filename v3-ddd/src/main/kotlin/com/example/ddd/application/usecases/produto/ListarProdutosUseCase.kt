package com.example.ddd.application.usecases.produto

import com.example.ddd.domain.entities.Produto
import com.example.ddd.domain.repositories.ProdutoRepository
import org.springframework.stereotype.Service

@Service
class ListarProdutosUseCase(
    private val produtoRepository: ProdutoRepository
) {
    fun executar(): List<Produto> = produtoRepository.listar()
}
