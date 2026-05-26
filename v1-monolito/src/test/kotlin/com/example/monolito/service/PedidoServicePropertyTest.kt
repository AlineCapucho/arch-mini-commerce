package com.example.monolito.service

import com.example.monolito.model.Pedido
import com.example.monolito.model.Produto
import com.example.monolito.repository.PedidoRepository
import com.example.monolito.repository.ProdutoRepository
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Optional

/**
 * Property 5: Criação de pedido aplica desconto corretamente
 *
 * Validates: Requirements 2.2, 3.1, 3.2, 3.3
 *
 * Para qualquer pedido válido:
 *   - Se valorBruto > 500.00 → valorTotal = valorBruto * 0.90
 *   - Se valorBruto ≤ 500.00 → valorTotal = valorBruto (sem desconto)
 *
 * Estratégia: gerar preços como inteiros de centavos (1..999999) e converter para BigDecimal
 * com 2 casas decimais. Isso evita o custo de filtrar BigDecimals arbitrários.
 */
class PedidoServicePropertyTest : FreeSpec({

    // Gerador de preços como centavos inteiros → BigDecimal com 2 casas decimais
    // Faixa: 0.01 a 9999.99 (1 a 999999 centavos)
    val precoArb: Arb<BigDecimal> = Arb.int(1..999999).map { centavos ->
        BigDecimal(centavos).movePointLeft(2)
    }

    // Preços acima do limiar de desconto: > 500.00 (50001 a 999999 centavos)
    val precoAcimaLimiarArb: Arb<BigDecimal> = Arb.int(50001..999999).map { centavos ->
        BigDecimal(centavos).movePointLeft(2)
    }

    // Preços no limiar ou abaixo: ≤ 500.00 (1 a 50000 centavos)
    val precoAbaixoLimiarArb: Arb<BigDecimal> = Arb.int(1..50000).map { centavos ->
        BigDecimal(centavos).movePointLeft(2)
    }

    fun buildService(preco: BigDecimal): PedidoService {
        val pedidoRepository: PedidoRepository = mock(PedidoRepository::class.java)
        val produtoRepository: ProdutoRepository = mock(ProdutoRepository::class.java)
        val service = PedidoService(pedidoRepository, produtoRepository)

        val produto = Produto(id = 1L, nome = "Produto Teste", preco = preco, quantidadeEstoque = 100)

        whenever(produtoRepository.findById(1L)).thenReturn(Optional.of(produto))
        whenever(produtoRepository.save(any<Produto>())).thenReturn(produto)
        whenever(pedidoRepository.save(any<Pedido>())).thenAnswer { it.arguments[0] as Pedido }

        return service
    }

    "Property 5 — desconto de 10% é aplicado quando valorBruto > 500.00" {
        checkAll(iterations = 100, precoAcimaLimiarArb) { preco ->
            val service = buildService(preco)

            val pedido = service.criarPedido("Cliente Teste", "123.456.789-00", listOf(1L))

            // valorBruto = preco × 1 (um produto, quantidade 1)
            val valorEsperado = preco.multiply(BigDecimal("0.90")).stripTrailingZeros()
            val valorObtido = pedido.valorTotal.stripTrailingZeros()

            valorObtido shouldBeEqualComparingTo valorEsperado
        }
    }

    "Property 5 — nenhum desconto é aplicado quando valorBruto ≤ 500.00" {
        checkAll(iterations = 100, precoAbaixoLimiarArb) { preco ->
            val service = buildService(preco)

            val pedido = service.criarPedido("Cliente Teste", "123.456.789-00", listOf(1L))

            // valorBruto = preco × 1 (um produto, quantidade 1)
            val valorEsperado = preco.stripTrailingZeros()
            val valorObtido = pedido.valorTotal.stripTrailingZeros()

            valorObtido shouldBeEqualComparingTo valorEsperado
        }
    }

    "Property 5 — para qualquer preço, a regra de desconto é mutuamente exclusiva" {
        checkAll(iterations = 200, precoArb) { preco ->
            val service = buildService(preco)

            val pedido = service.criarPedido("Cliente Teste", "123.456.789-00", listOf(1L))

            val valorObtido = pedido.valorTotal

            if (preco > BigDecimal("500.00")) {
                // Deve ter desconto de 10%
                val valorComDesconto = preco.multiply(BigDecimal("0.90"))
                valorObtido.stripTrailingZeros() shouldBeEqualComparingTo valorComDesconto.stripTrailingZeros()
            } else {
                // Sem desconto
                valorObtido.stripTrailingZeros() shouldBeEqualComparingTo preco.stripTrailingZeros()
            }
        }
    }
})
