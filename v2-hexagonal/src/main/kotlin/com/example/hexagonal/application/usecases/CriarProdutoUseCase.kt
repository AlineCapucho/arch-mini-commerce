package com.example.hexagonal.application.usecases

import com.example.hexagonal.application.ports.ProdutoRepositoryPort
import com.example.hexagonal.domain.entities.Produto
import java.math.BigDecimal

class CriarProdutoUseCase(
    private val produtoRepository: ProdutoRepositoryPort
) {
    fun executar(nome: String, preco: BigDecimal, quantidadeEstoque: Int): Produto {
        val produto = Produto(nome = nome, preco = preco, quantidadeEstoque = quantidadeEstoque)
        return produtoRepository.salvar(produto)
    }
}
