package com.example.monolito.service

import com.example.monolito.model.Pedido
import com.example.monolito.model.Produto
import com.example.monolito.repository.PedidoRepository
import com.example.monolito.repository.ProdutoRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.positiveInt
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Optional

/**
 * Property 7: Pedido com produto sem estoque é rejeitado sem efeitos colaterais
 *
 * Validates: Requirements 2.4, 7.1, 7.3, 7.4
 *
 * Para qualquer combinação de produtos onde ao menos um tem estoque insuficiente (0):
 *   - A criação do pedido deve ser rejeitada (lançar exceção)
 *   - O estoque de NENHUM produto deve ser alterado (atomicidade)
 *   - O pedido NÃO deve ser persistido
 */
class PedidoServiceAtomicidadePropertyTest : FreeSpec({

    // Gerador de estoque positivo (produto com estoque disponível): 1..100
    val estoqueDisponivel: Arb<Int> = Arb.int(1..100)

    // Gerador de preço como centavos inteiros → BigDecimal com 2 casas decimais
    val precoArb: Arb<BigDecimal> = Arb.positiveInt(max = 99999).map { centavos ->
        BigDecimal(centavos).movePointLeft(2)
    }

    /**
     * Monta um cenário com N produtos onde exatamente o último tem estoque = 0.
     * Retorna (service, produtoRepository, estoqueOriginalPorId).
     */
    fun buildScenarioComUmProdutoSemEstoque(
        numProdutosComEstoque: Int,
        estoques: List<Int>,
        precos: List<BigDecimal>
    ): Triple<PedidoService, ProdutoRepository, Map<Long, Int>> {
        val pedidoRepository: PedidoRepository = mock(PedidoRepository::class.java)
        val produtoRepository: ProdutoRepository = mock(ProdutoRepository::class.java)
        val service = PedidoService(pedidoRepository, produtoRepository)

        val estoqueOriginal = mutableMapOf<Long, Int>()

        // Produtos com estoque disponível (ids 1..numProdutosComEstoque)
        for (i in 0 until numProdutosComEstoque) {
            val id = (i + 1).toLong()
            val estoque = estoques[i]
            val preco = precos[i]
            val produto = Produto(id = id, nome = "Produto $id", preco = preco, quantidadeEstoque = estoque)
            whenever(produtoRepository.findById(id)).thenReturn(Optional.of(produto))
            whenever(produtoRepository.save(any<Produto>())).thenReturn(produto)
            estoqueOriginal[id] = estoque
        }

        // Produto sem estoque (id = numProdutosComEstoque + 1)
        val idSemEstoque = (numProdutosComEstoque + 1).toLong()
        val precoProdutoSemEstoque = precos.last()
        val produtoSemEstoque = Produto(
            id = idSemEstoque,
            nome = "Produto Sem Estoque",
            preco = precoProdutoSemEstoque,
            quantidadeEstoque = 0
        )
        whenever(produtoRepository.findById(idSemEstoque)).thenReturn(Optional.of(produtoSemEstoque))
        estoqueOriginal[idSemEstoque] = 0

        return Triple(service, produtoRepository, estoqueOriginal)
    }

    "Property 7 — pedido com um único produto sem estoque é rejeitado" {
        checkAll(iterations = 100, precoArb) { preco ->
            val pedidoRepository: PedidoRepository = mock(PedidoRepository::class.java)
            val produtoRepository: ProdutoRepository = mock(ProdutoRepository::class.java)
            val service = PedidoService(pedidoRepository, produtoRepository)

            val produto = Produto(id = 1L, nome = "Produto Sem Estoque", preco = preco, quantidadeEstoque = 0)
            whenever(produtoRepository.findById(1L)).thenReturn(Optional.of(produto))

            // A criação deve ser rejeitada
            shouldThrow<IllegalStateException> {
                service.criarPedido("Cliente Teste", "123.456.789-00", listOf(1L))
            }

            // Nenhum produto deve ter sido salvo (estoque inalterado)
            verify(produtoRepository, never()).save(any<Produto>())

            // Nenhum pedido deve ter sido persistido
            verify(pedidoRepository, never()).save(any<Pedido>())
        }
    }

    "Property 7 — pedido com múltiplos produtos onde o último não tem estoque é rejeitado sem alterar estoques" {
        // Gera entre 1 e 4 produtos com estoque, mais 1 sem estoque
        val numProdutosComEstoqueArb = Arb.int(1..4)
        val listaEstoqueArb = Arb.list(estoqueDisponivel, 1..4)
        val listaPrecoArb = Arb.list(precoArb, 2..5)

        checkAll(iterations = 100, numProdutosComEstoqueArb, listaEstoqueArb, listaPrecoArb) { n, estoques, precos ->
            // Garantir que temos estoques e preços suficientes para n produtos + 1 sem estoque
            val numProdutos = minOf(n, estoques.size)
            val totalProdutos = numProdutos + 1 // +1 para o produto sem estoque
            val precosAjustados = if (precos.size >= totalProdutos) precos.take(totalProdutos)
                                  else precos + List(totalProdutos - precos.size) { BigDecimal("10.00") }
            val estoquesAjustados = if (estoques.size >= numProdutos) estoques.take(numProdutos)
                                    else estoques + List(numProdutos - estoques.size) { 5 }

            val (service, produtoRepository, estoqueOriginal) =
                buildScenarioComUmProdutoSemEstoque(numProdutos, estoquesAjustados, precosAjustados)

            val pedidoRepository: PedidoRepository = mock(PedidoRepository::class.java)
            // Reconstruir service com o pedidoRepository correto
            val produtoRepositoryFinal: ProdutoRepository = mock(ProdutoRepository::class.java)
            val serviceFinal = PedidoService(pedidoRepository, produtoRepositoryFinal)

            // Configurar todos os produtos no produtoRepositoryFinal
            for (i in 0 until numProdutos) {
                val id = (i + 1).toLong()
                val estoque = estoquesAjustados[i]
                val preco = precosAjustados[i]
                val produto = Produto(id = id, nome = "Produto $id", preco = preco, quantidadeEstoque = estoque)
                whenever(produtoRepositoryFinal.findById(id)).thenReturn(Optional.of(produto))
                whenever(produtoRepositoryFinal.save(any<Produto>())).thenReturn(produto)
            }

            val idSemEstoque = (numProdutos + 1).toLong()
            val produtoSemEstoque = Produto(
                id = idSemEstoque,
                nome = "Produto Sem Estoque",
                preco = precosAjustados.last(),
                quantidadeEstoque = 0
            )
            whenever(produtoRepositoryFinal.findById(idSemEstoque)).thenReturn(Optional.of(produtoSemEstoque))

            val todosIds = (1..numProdutos).map { it.toLong() } + listOf(idSemEstoque)

            // A criação deve ser rejeitada
            shouldThrow<IllegalStateException> {
                serviceFinal.criarPedido("Cliente Teste", "123.456.789-00", todosIds)
            }

            // Nenhum produto deve ter sido salvo (estoque inalterado)
            verify(produtoRepositoryFinal, never()).save(any<Produto>())

            // Nenhum pedido deve ter sido persistido
            verify(pedidoRepository, never()).save(any<Pedido>())
        }
    }

    "Property 7 — pedido com produto sem estoque no meio da lista é rejeitado sem alterar estoques" {
        checkAll(iterations = 100, estoqueDisponivel, estoqueDisponivel, precoArb, precoArb, precoArb) {
            estoqueA, estoqueC, precoA, precoB, precoC ->

            val pedidoRepository: PedidoRepository = mock(PedidoRepository::class.java)
            val produtoRepository: ProdutoRepository = mock(ProdutoRepository::class.java)
            val service = PedidoService(pedidoRepository, produtoRepository)

            // Produto A: tem estoque
            val produtoA = Produto(id = 1L, nome = "Produto A", preco = precoA, quantidadeEstoque = estoqueA)
            // Produto B: SEM estoque (no meio)
            val produtoB = Produto(id = 2L, nome = "Produto B Sem Estoque", preco = precoB, quantidadeEstoque = 0)
            // Produto C: tem estoque
            val produtoC = Produto(id = 3L, nome = "Produto C", preco = precoC, quantidadeEstoque = estoqueC)

            whenever(produtoRepository.findById(1L)).thenReturn(Optional.of(produtoA))
            whenever(produtoRepository.findById(2L)).thenReturn(Optional.of(produtoB))
            whenever(produtoRepository.findById(3L)).thenReturn(Optional.of(produtoC))

            // A criação deve ser rejeitada
            shouldThrow<IllegalStateException> {
                service.criarPedido("Cliente Teste", "123.456.789-00", listOf(1L, 2L, 3L))
            }

            // Nenhum produto deve ter sido salvo (estoque inalterado — atomicidade)
            verify(produtoRepository, never()).save(any<Produto>())

            // Nenhum pedido deve ter sido persistido
            verify(pedidoRepository, never()).save(any<Pedido>())

            // Verificar que os estoques originais permanecem inalterados
            produtoA.quantidadeEstoque shouldBe estoqueA
            produtoB.quantidadeEstoque shouldBe 0
            produtoC.quantidadeEstoque shouldBe estoqueC
        }
    }
})
