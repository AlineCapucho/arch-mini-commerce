package com.example.ddd.infrastructure.repositories

import com.example.ddd.domain.entities.Produto
import com.example.ddd.domain.repositories.ProdutoRepository
import com.example.ddd.domain.valueobjects.Preco
import com.example.ddd.infrastructure.repositories.entities.ProdutoJpaEntity
import org.springframework.stereotype.Repository

@Repository
class ProdutoRepositoryImpl(
    private val jpaRepository: ProdutoJpaRepository
) : ProdutoRepository {

    override fun salvar(produto: Produto): Produto =
        jpaRepository.save(produto.toEntity()).toDomain()

    override fun buscarPorId(id: Long): Produto? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun listar(): List<Produto> =
        jpaRepository.findAll().map { it.toDomain() }

    override fun salvarTodos(produtos: List<Produto>): List<Produto> =
        jpaRepository.saveAll(produtos.map { it.toEntity() }).map { it.toDomain() }

    private fun ProdutoJpaEntity.toDomain(): Produto =
        Produto(
            id = id,
            nome = nome,
            preco = Preco(preco),
            quantidadeEstoque = quantidadeEstoque
        )

    private fun Produto.toEntity(): ProdutoJpaEntity =
        ProdutoJpaEntity(
            id = id,
            nome = nome,
            preco = preco.value,
            quantidadeEstoque = quantidadeEstoque
        )
}
