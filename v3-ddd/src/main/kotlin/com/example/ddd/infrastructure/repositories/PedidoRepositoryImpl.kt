package com.example.ddd.infrastructure.repositories

import com.example.ddd.domain.entities.ItemPedido
import com.example.ddd.domain.entities.Pedido
import com.example.ddd.domain.entities.StatusPedido
import com.example.ddd.domain.repositories.PedidoRepository
import com.example.ddd.domain.valueobjects.Cliente
import com.example.ddd.domain.valueobjects.Preco
import com.example.ddd.infrastructure.repositories.entities.ItemPedidoJpaEntity
import com.example.ddd.infrastructure.repositories.entities.PedidoJpaEntity
import org.springframework.stereotype.Repository

@Repository
class PedidoRepositoryImpl(
    private val jpaRepository: PedidoJpaRepository
) : PedidoRepository {

    override fun salvar(pedido: Pedido): Pedido =
        jpaRepository.save(pedido.toEntity()).toDomain()

    override fun buscarPorId(id: Long): Pedido? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun listar(): List<Pedido> =
        jpaRepository.findAll().map { it.toDomain() }

    private fun PedidoJpaEntity.toDomain(): Pedido =
        Pedido(
            id = id,
            cliente = Cliente(nome = clienteNome, cpf = clienteCpf),
            status = status,
            itens = itens.map { it.toDomainItem() },
            valorTotal = Preco(valorTotal)
        )

    private fun ItemPedidoJpaEntity.toDomainItem(): ItemPedido =
        ItemPedido(
            produtoId = produtoId,
            preco = Preco(precoProduto),
            quantidade = quantidade
        )

    private fun Pedido.toEntity(): PedidoJpaEntity =
        PedidoJpaEntity(
            id = id,
            clienteNome = cliente.nome,
            clienteCpf = cliente.cpf,
            status = status,
            itens = itens.map { it.toItemEntity() }.toMutableList(),
            valorTotal = valorTotal.value
        )

    private fun ItemPedido.toItemEntity(): ItemPedidoJpaEntity =
        ItemPedidoJpaEntity(
            produtoId = produtoId,
            precoProduto = preco.value,
            quantidade = quantidade
        )
}
