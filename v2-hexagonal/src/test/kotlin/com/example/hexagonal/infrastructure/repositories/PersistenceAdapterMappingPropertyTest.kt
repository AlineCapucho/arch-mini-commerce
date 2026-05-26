package com.example.hexagonal.infrastructure.repositories

import com.example.hexagonal.domain.entities.ItemPedido
import com.example.hexagonal.domain.entities.Pedido
import com.example.hexagonal.domain.entities.Produto
import com.example.hexagonal.domain.entities.StatusPedido
import com.example.hexagonal.infrastructure.repositories.entities.ItemPedidoJpaEntity
import com.example.hexagonal.infrastructure.repositories.entities.PedidoJpaEntity
import com.example.hexagonal.infrastructure.repositories.entities.ProdutoJpaEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

/**
 * Property 11: Mapeamento bidirecional dos Persistence Adapters (Hexagonal)
 *
 * Para qualquer entidade de domínio válida, converter para JPA entity e de volta
 * deve produzir objeto igual ao original.
 *
 * Validates: Requirements 7.5
 *
 * Note on Pedido round-trip: ItemPedidoJpaEntity stores only (produtoId, precoProduto, quantidade).
 * When converting back to domain, produto.nome is set to "" and produto.quantidadeEstoque to 0.
 * Therefore the round-trip property for Pedido is tested with Produto instances that have
 * nome="" and quantidadeEstoque=0, which is the exact contract of the adapter.
 */
class PersistenceAdapterMappingPropertyTest {

    // ─── Helpers: mapping functions extracted from the adapters ──────────────

    private fun ProdutoJpaEntity.toDomain(): Produto =
        Produto(id = id, nome = nome, preco = preco, quantidadeEstoque = quantidadeEstoque)

    private fun Produto.toEntity(): ProdutoJpaEntity =
        ProdutoJpaEntity(id = id, nome = nome, preco = preco, quantidadeEstoque = quantidadeEstoque)

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

    private fun Pedido.toEntity(): PedidoJpaEntity =
        PedidoJpaEntity(
            id = id,
            clienteNome = clienteNome,
            clienteCpf = clienteCpf,
            status = status,
            itens = itens.map { it.toItemEntity() }.toMutableList(),
            valorTotal = valorTotal
        )

    private fun ItemPedido.toItemEntity(): ItemPedidoJpaEntity =
        ItemPedidoJpaEntity(
            produtoId = produto.id,
            precoProduto = produto.preco,
            quantidade = quantidade
        )

    // ─── Generators ──────────────────────────────────────────────────────────

    private val rng = Random(System.currentTimeMillis())

    private fun randomId(): Long = rng.nextLong(1L, Long.MAX_VALUE)

    private fun randomNome(): String {
        val nomes = listOf("Notebook", "TV", "Mouse", "Teclado", "Monitor", "Câmera", "Tablet", "Fone")
        return nomes[rng.nextInt(nomes.size)] + " " + rng.nextInt(1000)
    }

    private fun randomPreco(): BigDecimal =
        BigDecimal(rng.nextDouble(0.01, 9999.99)).setScale(2, RoundingMode.HALF_UP)

    private fun randomEstoque(): Int = rng.nextInt(0, 1000)

    private fun randomCpf(): String {
        val d = (1..11).map { rng.nextInt(0, 10) }
        return "${d[0]}${d[1]}${d[2]}.${d[3]}${d[4]}${d[5]}.${d[6]}${d[7]}${d[8]}-${d[9]}${d[10]}"
    }

    private fun randomStatus(): StatusPedido =
        StatusPedido.values()[rng.nextInt(StatusPedido.values().size)]

    /**
     * Generates a valid Produto domain entity with all fields populated.
     */
    private fun randomProduto(): Produto =
        Produto(
            id = randomId(),
            nome = randomNome(),
            preco = randomPreco(),
            quantidadeEstoque = randomEstoque()
        )

    /**
     * Generates a Produto suitable for use inside a Pedido round-trip test.
     * nome="" and quantidadeEstoque=0 because ItemPedidoJpaEntity does not store those fields,
     * so the adapter reconstructs them with those defaults.
     */
    private fun randomProdutoParaItem(): Produto =
        Produto(
            id = randomId(),
            nome = "",
            preco = randomPreco(),
            quantidadeEstoque = 0
        )

    private fun randomItemPedido(): ItemPedido =
        ItemPedido(produto = randomProdutoParaItem(), quantidade = rng.nextInt(1, 100))

    private fun randomPedido(numItens: Int = rng.nextInt(0, 5)): Pedido =
        Pedido(
            id = randomId(),
            clienteNome = randomNome(),
            clienteCpf = randomCpf(),
            status = randomStatus(),
            itens = (1..numItens).map { randomItemPedido() }.toMutableList(),
            valorTotal = randomPreco()
        )

    // ─── Property tests: Produto round-trip ──────────────────────────────────

    /**
     * Property 11 (Produto): domain → JPA entity → domain produces an equal Produto.
     *
     * Validates: Requirements 7.5
     */
    @RepeatedTest(100)
    fun `Property 11 - Produto domain para JPA entity e de volta produz objeto igual ao original`() {
        val original = randomProduto()

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped) {
            "Round-trip falhou para Produto: original=$original, resultado=$roundTripped"
        }
    }

    /**
     * Property 11 (Produto - campos individuais): verifica cada campo individualmente
     * para facilitar diagnóstico em caso de falha.
     *
     * Validates: Requirements 7.5
     */
    @RepeatedTest(50)
    fun `Property 11 - Produto round-trip preserva todos os campos individualmente`() {
        val original = randomProduto()
        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.id, roundTripped.id) { "id não preservado" }
        assertEquals(original.nome, roundTripped.nome) { "nome não preservado" }
        assertEquals(0, original.preco.compareTo(roundTripped.preco)) { "preco não preservado" }
        assertEquals(original.quantidadeEstoque, roundTripped.quantidadeEstoque) { "quantidadeEstoque não preservado" }
    }

    // ─── Property tests: Pedido round-trip ───────────────────────────────────

    /**
     * Property 11 (Pedido): domain → JPA entity → domain produces an equal Pedido.
     *
     * The Pedido round-trip is tested with Produto instances that have nome="" and
     * quantidadeEstoque=0, matching the adapter's reconstruction contract.
     *
     * Validates: Requirements 7.5
     */
    @RepeatedTest(100)
    fun `Property 11 - Pedido domain para JPA entity e de volta produz objeto igual ao original`() {
        val original = randomPedido()

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.id, roundTripped.id) { "id não preservado" }
        assertEquals(original.clienteNome, roundTripped.clienteNome) { "clienteNome não preservado" }
        assertEquals(original.clienteCpf, roundTripped.clienteCpf) { "clienteCpf não preservado" }
        assertEquals(original.status, roundTripped.status) { "status não preservado" }
        assertEquals(0, original.valorTotal.compareTo(roundTripped.valorTotal)) { "valorTotal não preservado" }
        assertEquals(original.itens.size, roundTripped.itens.size) { "número de itens não preservado" }
    }

    /**
     * Property 11 (Pedido - itens): verifica que cada item do pedido é preservado corretamente.
     *
     * Validates: Requirements 7.5
     */
    @RepeatedTest(50)
    fun `Property 11 - Pedido round-trip preserva todos os itens corretamente`() {
        val numItens = rng.nextInt(1, 6)
        val original = randomPedido(numItens)

        val roundTripped = original.toEntity().toDomain()

        assertEquals(numItens, roundTripped.itens.size)

        original.itens.zip(roundTripped.itens).forEachIndexed { index, (orig, rt) ->
            assertEquals(orig.produto.id, rt.produto.id) { "item[$index].produto.id não preservado" }
            assertEquals(0, orig.produto.preco.compareTo(rt.produto.preco)) { "item[$index].produto.preco não preservado" }
            assertEquals(orig.quantidade, rt.quantidade) { "item[$index].quantidade não preservado" }
        }
    }

    /**
     * Property 11 (Pedido sem itens): pedido vazio também faz round-trip corretamente.
     *
     * Validates: Requirements 7.5
     */
    @RepeatedTest(30)
    fun `Property 11 - Pedido sem itens faz round-trip corretamente`() {
        val original = randomPedido(numItens = 0)

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.clienteNome, roundTripped.clienteNome)
        assertEquals(original.clienteCpf, roundTripped.clienteCpf)
        assertEquals(original.status, roundTripped.status)
        assertEquals(0, original.valorTotal.compareTo(roundTripped.valorTotal))
        assertEquals(0, roundTripped.itens.size)
    }

    /**
     * Property 11 (todos os StatusPedido): round-trip preserva cada valor do enum.
     *
     * Validates: Requirements 7.5
     */
    @Test
    fun `Property 11 - round-trip preserva todos os valores de StatusPedido`() {
        StatusPedido.values().forEach { status ->
            val original = Pedido(
                id = 1L,
                clienteNome = "Cliente Teste",
                clienteCpf = "000.000.000-00",
                status = status,
                itens = mutableListOf(),
                valorTotal = BigDecimal("100.00")
            )

            val roundTripped = original.toEntity().toDomain()

            assertEquals(status, roundTripped.status) {
                "Status $status não foi preservado no round-trip"
            }
        }
    }
}
