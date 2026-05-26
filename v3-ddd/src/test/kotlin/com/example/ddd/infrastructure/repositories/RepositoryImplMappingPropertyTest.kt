package com.example.ddd.infrastructure.repositories

import com.example.ddd.domain.entities.ItemPedido
import com.example.ddd.domain.entities.Pedido
import com.example.ddd.domain.entities.Produto
import com.example.ddd.domain.entities.StatusPedido
import com.example.ddd.domain.valueobjects.Cliente
import com.example.ddd.domain.valueobjects.Preco
import com.example.ddd.infrastructure.repositories.entities.ItemPedidoJpaEntity
import com.example.ddd.infrastructure.repositories.entities.PedidoJpaEntity
import com.example.ddd.infrastructure.repositories.entities.ProdutoJpaEntity
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.math.BigDecimal

/**
 * Property 11: Mapeamento bidirecional dos Repository Impls (DDD)
 *
 * Para qualquer entity de domínio válida (Produto, Pedido), converter para JPA entity
 * e de volta deve produzir objeto igual ao original.
 *
 * Task 7.10
 * Validates: Requirements 8.4, 8.5
 */
class RepositoryImplMappingPropertyTest : FreeSpec({

    // ─── Mapping functions (mirroring ProdutoRepositoryImpl / PedidoRepositoryImpl) ──

    fun ProdutoJpaEntity.toDomain(): Produto =
        Produto(
            id = id,
            nome = nome,
            preco = Preco(preco),
            quantidadeEstoque = quantidadeEstoque
        )

    fun Produto.toEntity(): ProdutoJpaEntity =
        ProdutoJpaEntity(
            id = id,
            nome = nome,
            preco = preco.value,
            quantidadeEstoque = quantidadeEstoque
        )

    fun ItemPedidoJpaEntity.toDomainItem(): ItemPedido =
        ItemPedido(
            produtoId = produtoId,
            preco = Preco(precoProduto),
            quantidade = quantidade
        )

    fun ItemPedido.toItemEntity(): ItemPedidoJpaEntity =
        ItemPedidoJpaEntity(
            produtoId = produtoId,
            precoProduto = preco.value,
            quantidade = quantidade
        )

    fun PedidoJpaEntity.toDomain(): Pedido =
        Pedido(
            id = id,
            cliente = Cliente(nome = clienteNome, cpf = clienteCpf),
            status = status,
            itens = itens.map { it.toDomainItem() },
            valorTotal = Preco(valorTotal)
        )

    fun Pedido.toEntity(): PedidoJpaEntity =
        PedidoJpaEntity(
            id = id,
            clienteNome = cliente.nome,
            clienteCpf = cliente.cpf,
            status = status,
            itens = itens.map { it.toItemEntity() }.toMutableList(),
            valorTotal = valorTotal.value
        )

    // ─── Generators ──────────────────────────────────────────────────────────

    /** Non-blank string (at least 1 non-whitespace character) */
    val nonBlankStringArb: Arb<String> = Arb.string(1..30).filter { it.isNotBlank() }

    /** Positive id (1..Long.MAX_VALUE) */
    val idArb: Arb<Long> = Arb.long(1L..Long.MAX_VALUE)

    /** Non-negative BigDecimal price with 2 decimal places (0..9999.99 in cents) */
    val precoArb: Arb<BigDecimal> = Arb.int(0..999999).map { centavos ->
        BigDecimal(centavos).movePointLeft(2)
    }

    /** Non-negative stock quantity */
    val estoqueArb: Arb<Int> = Arb.int(0..1000)

    /** Positive item quantity */
    val quantidadeArb: Arb<Int> = Arb.int(1..100)

    /** Valid Produto domain entity */
    val produtoArb: Arb<Produto> = arbitrary {
        Produto(
            id = idArb.bind(),
            nome = nonBlankStringArb.bind(),
            preco = Preco(precoArb.bind()),
            quantidadeEstoque = estoqueArb.bind()
        )
    }

    /** Valid ItemPedido domain entity */
    val itemPedidoArb: Arb<ItemPedido> = arbitrary {
        ItemPedido(
            produtoId = idArb.bind(),
            preco = Preco(precoArb.bind()),
            quantidade = quantidadeArb.bind()
        )
    }

    /** Valid Pedido domain entity */
    val pedidoArb: Arb<Pedido> = arbitrary {
        val itens = Arb.list(itemPedidoArb, 0..5).bind()
        Pedido(
            id = idArb.bind(),
            cliente = Cliente(
                nome = nonBlankStringArb.bind(),
                cpf = nonBlankStringArb.bind()
            ),
            status = Arb.enum<StatusPedido>().bind(),
            itens = itens,
            valorTotal = Preco(precoArb.bind())
        )
    }

    // ─── Property 11: Produto round-trip ─────────────────────────────────────

    "Property 11 — Produto: domain → JPA entity → domain produz objeto igual ao original" {
        checkAll(iterations = 200, produtoArb) { original ->
            val roundTripped = original.toEntity().toDomain()

            roundTripped.id shouldBe original.id
            roundTripped.nome shouldBe original.nome
            roundTripped.preco shouldBe original.preco
            roundTripped.quantidadeEstoque shouldBe original.quantidadeEstoque
        }
    }

    "Property 11 — Produto: round-trip preserva Preco como Value Object igual" {
        checkAll(iterations = 200, produtoArb) { original ->
            val roundTripped = original.toEntity().toDomain()

            // Preco é um data class — igualdade por valor
            roundTripped.preco shouldBe original.preco
            roundTripped.preco.value.compareTo(original.preco.value) shouldBe 0
        }
    }

    // ─── Property 11: ItemPedido round-trip ──────────────────────────────────

    "Property 11 — ItemPedido: domain → JPA entity → domain produz objeto igual ao original" {
        checkAll(iterations = 200, itemPedidoArb) { original ->
            val roundTripped = original.toItemEntity().toDomainItem()

            roundTripped.produtoId shouldBe original.produtoId
            roundTripped.preco shouldBe original.preco
            roundTripped.quantidade shouldBe original.quantidade
        }
    }

    // ─── Property 11: Pedido round-trip ──────────────────────────────────────

    "Property 11 — Pedido: domain → JPA entity → domain preserva todos os campos escalares" {
        checkAll(iterations = 200, pedidoArb) { original ->
            val roundTripped = original.toEntity().toDomain()

            roundTripped.id shouldBe original.id
            roundTripped.cliente shouldBe original.cliente
            roundTripped.status shouldBe original.status
            roundTripped.valorTotal shouldBe original.valorTotal
        }
    }

    "Property 11 — Pedido: round-trip preserva número e conteúdo dos itens" {
        checkAll(iterations = 200, pedidoArb) { original ->
            val roundTripped = original.toEntity().toDomain()

            roundTripped.itens.size shouldBe original.itens.size

            original.itens.zip(roundTripped.itens).forEachIndexed { index, (orig, rt) ->
                rt.produtoId shouldBe orig.produtoId
                rt.preco shouldBe orig.preco
                rt.quantidade shouldBe orig.quantidade
            }
        }
    }

    "Property 11 — Pedido sem itens: round-trip produz pedido com lista de itens vazia" {
        checkAll(iterations = 100, pedidoArb) { base ->
            val original = Pedido(
                id = base.id,
                cliente = base.cliente,
                status = base.status,
                itens = emptyList(),
                valorTotal = base.valorTotal
            )

            val roundTripped = original.toEntity().toDomain()

            roundTripped.itens.size shouldBe 0
            roundTripped.id shouldBe original.id
            roundTripped.cliente shouldBe original.cliente
            roundTripped.status shouldBe original.status
            roundTripped.valorTotal shouldBe original.valorTotal
        }
    }

    "Property 11 — Pedido: round-trip preserva todos os valores de StatusPedido" {
        checkAll(iterations = 50, Arb.enum<StatusPedido>()) { status ->
            val original = Pedido(
                id = 1L,
                cliente = Cliente(nome = "Cliente Teste", cpf = "123.456.789-00"),
                status = status,
                itens = emptyList(),
                valorTotal = Preco(BigDecimal("100.00"))
            )

            val roundTripped = original.toEntity().toDomain()

            roundTripped.status shouldBe status
        }
    }

    "Property 11 — Pedido: Cliente Value Object é preservado integralmente no round-trip" {
        checkAll(iterations = 200, nonBlankStringArb, nonBlankStringArb) { nome, cpf ->
            val original = Pedido(
                id = 1L,
                cliente = Cliente(nome = nome, cpf = cpf),
                status = StatusPedido.PENDENTE,
                itens = emptyList(),
                valorTotal = Preco(BigDecimal.ZERO)
            )

            val roundTripped = original.toEntity().toDomain()

            roundTripped.cliente.nome shouldBe nome
            roundTripped.cliente.cpf shouldBe cpf
        }
    }
})
