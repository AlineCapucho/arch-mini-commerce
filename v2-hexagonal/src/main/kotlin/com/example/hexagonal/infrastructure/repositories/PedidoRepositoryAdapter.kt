package com.example.hexagonal.infrastructure.repositories

import com.example.hexagonal.application.ports.PedidoRepositoryPort
import com.example.hexagonal.domain.entities.ItemPedido
import com.example.hexagonal.domain.entities.Pedido
import com.example.hexagonal.domain.entities.Produto
import com.example.hexagonal.infrastructure.repositories.entities.ItemPedidoJpaEntity
import com.example.hexagonal.infrastructure.repositories.entities.PedidoJpaEntity
import org.springframework.stereotype.Repository

@Repository
class PedidoRepositoryAdapter(
    private val jpaRepository: PedidoJpaRepository
) : PedidoRepositoryPort {

    override fun salvar(pedido: Pedido): Pedido =
        jpaRepository.save(toEntity(pedido)).toDomain()

    override fun buscarPorId(id: Long): Pedido? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun listar(): List<Pedido> =
        jpaRepository.findAll().map { it.toDomain() }

    private fun PedidoJpaEntity.toDomain(): Pedido =
        Pedido(
            id = id,
            clienteNome = clienteNome,
            clienteCpf = clienteCpf,
            status = status,
            itens = itens.map { it.toDomainItem() }.toMutableList(),
            valorTotal = valorTotal
        )

    private fun ItemPedidoJpaEntity.toDomainItem(): ItemPedido =
        ItemPedido(
            produto = Produto(id = produtoId, nome = "", preco = precoProduto, quantidadeEstoque = 0),
            quantidade = quantidade
        )

    private fun toEntity(pedido: Pedido): PedidoJpaEntity =
        PedidoJpaEntity(
            id = pedido.id,
            clienteNome = pedido.clienteNome,
            clienteCpf = pedido.clienteCpf,
            status = pedido.status,
            itens = pedido.itens.map { toItemEntity(it) }.toMutableList(),
            valorTotal = pedido.valorTotal
        )

    private fun toItemEntity(item: ItemPedido): ItemPedidoJpaEntity =
        ItemPedidoJpaEntity(
            produtoId = item.produto.id,
            precoProduto = item.produto.preco,
            quantidade = item.quantidade
        )
}
