package com.example.hexagonal.infrastructure.repositories

import com.example.hexagonal.application.ports.ProdutoRepositoryPort
import com.example.hexagonal.domain.entities.Produto
import com.example.hexagonal.infrastructure.repositories.entities.ProdutoJpaEntity
import org.springframework.stereotype.Repository

@Repository
class ProdutoRepositoryAdapter(
    private val jpaRepository: ProdutoJpaRepository
) : ProdutoRepositoryPort {

    override fun salvar(produto: Produto): Produto =
        jpaRepository.save(toEntity(produto)).toDomain()

    override fun buscarPorId(id: Long): Produto? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun listar(): List<Produto> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun salvarTodos(produtos: List<Produto>): List<Produto> =
        jpaRepository.saveAll(produtos.map { toEntity(it) }).map { it.toDomain() }

    private fun ProdutoJpaEntity.toDomain(): Produto =
        Produto(id = id, nome = nome, preco = preco, quantidadeEstoque = quantidadeEstoque)

    private fun toEntity(produto: Produto): ProdutoJpaEntity =
        ProdutoJpaEntity(id = produto.id, nome = produto.nome, preco = produto.preco, quantidadeEstoque = produto.quantidadeEstoque)
}
