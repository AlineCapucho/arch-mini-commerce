package com.example.ddd.domain

import com.example.ddd.domain.entities.ItemPedido
import com.example.ddd.domain.entities.Pedido
import com.example.ddd.domain.entities.Produto
import com.example.ddd.domain.entities.StatusPedido
import com.example.ddd.domain.errors.DomainException
import com.example.ddd.domain.services.CalculadoraDeDesconto
import com.example.ddd.domain.valueobjects.Cliente
import com.example.ddd.domain.valueobjects.Preco
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Property-based tests for V3 DDD Value Objects and business rules.
 *
 * Task 7.9
 * Validates: Requirements 3.1, 3.2, 3.3, 4.1, 4.2, 6.2, 6.3, 8.3
 */
class DomainPropertyTest : FreeSpec({

    // ─── Generators ──────────────────────────────────────────────────────────

    /** Non-blank string generator (at least 1 non-whitespace character) */
    val nonBlankStringArb: Arb<String> = Arb.string(1..30)
        .filter { it.isNotBlank() }

    /** Blank string generator: empty or whitespace-only */
    val blankStringArb: Arb<String> = Arb.int(0..5).map { n -> " ".repeat(n) }

    /** Preco generator: positive values as integer cents → BigDecimal with 2 decimal places */
    val precoPositivoArb: Arb<Preco> = Arb.int(1..999999).map { centavos ->
        Preco(BigDecimal(centavos).movePointLeft(2))
    }

    /** Preco generator: values strictly above 500.00 (50001..999999 cents) */
    val precoAcimaLimiarArb: Arb<Preco> = Arb.int(50001..999999).map { centavos ->
        Preco(BigDecimal(centavos).movePointLeft(2))
    }

    /** Preco generator: values at or below 500.00 (1..50000 cents) */
    val precoAbaixoLimiarArb: Arb<Preco> = Arb.int(1..50000).map { centavos ->
        Preco(BigDecimal(centavos).movePointLeft(2))
    }

    /** Negative BigDecimal generator (for Preco rejection tests) */
    val negativoArb: Arb<BigDecimal> = Arb.int(1..999999).map { centavos ->
        BigDecimal(centavos).movePointLeft(2).negate()
    }

    /** Positive stock quantity generator */
    val estoquePositivoArb: Arb<Int> = Arb.int(1..100)

    fun criarProduto(id: Long = 1L, preco: Preco = Preco(BigDecimal("100.00")), estoque: Int = 10): Produto =
        Produto(id = id, nome = "Produto $id", preco = preco, quantidadeEstoque = estoque)

    fun criarPedidoPendente(id: Long = 1L): Pedido =
        Pedido(
            id = id,
            cliente = Cliente(nome = "Cliente Teste", cpf = "123.456.789-00"),
            status = StatusPedido.PENDENTE
        )

    // ─── Property 12: Value Objects rejeitam valores inválidos ───────────────

    "Property 12 — Cliente com nome em branco lança IllegalArgumentException" {
        checkAll(iterations = 100, blankStringArb, nonBlankStringArb) { nomeEmBranco, cpfValido ->
            shouldThrow<IllegalArgumentException> {
                Cliente(nome = nomeEmBranco, cpf = cpfValido)
            }
        }
    }

    "Property 12 — Cliente com CPF em branco lança IllegalArgumentException" {
        checkAll(iterations = 100, nonBlankStringArb, blankStringArb) { nomeValido, cpfEmBranco ->
            shouldThrow<IllegalArgumentException> {
                Cliente(nome = nomeValido, cpf = cpfEmBranco)
            }
        }
    }

    "Property 12 — Cliente com nome e CPF válidos é criado com sucesso" {
        checkAll(iterations = 100, nonBlankStringArb, nonBlankStringArb) { nome, cpf ->
            val cliente = Cliente(nome = nome, cpf = cpf)
            cliente.nome shouldBe nome
            cliente.cpf shouldBe cpf
        }
    }

    "Property 12 — Preco com valor negativo lança IllegalArgumentException" {
        checkAll(iterations = 100, negativoArb) { valorNegativo ->
            shouldThrow<IllegalArgumentException> {
                Preco(valorNegativo)
            }
        }
    }

    "Property 12 — Preco com valor zero ou positivo é criado com sucesso" {
        checkAll(iterations = 100, precoPositivoArb) { preco ->
            preco.value shouldBe preco.value
        }
        // Zero is also valid
        val precoZero = Preco(BigDecimal.ZERO)
        precoZero.value.compareTo(BigDecimal.ZERO) shouldBe 0
    }

    // ─── Property 5: Desconto aplicado corretamente ──────────────────────────

    "Property 5 — CalculadoraDeDesconto retorna exatamente 90% para Preco > 500" {
        val calculadora = CalculadoraDeDesconto()
        checkAll(iterations = 100, precoAcimaLimiarArb) { preco ->
            val resultado = calculadora.calcular(preco)
            val esperado = preco.value.multiply(BigDecimal("0.90"))
            resultado.value.compareTo(esperado) shouldBe 0
        }
    }

    "Property 5 — CalculadoraDeDesconto retorna valor inalterado para Preco <= 500" {
        val calculadora = CalculadoraDeDesconto()
        checkAll(iterations = 100, precoAbaixoLimiarArb) { preco ->
            val resultado = calculadora.calcular(preco)
            resultado.value.compareTo(preco.value) shouldBe 0
        }
    }

    "Property 5 — regra de desconto é mutuamente exclusiva para qualquer Preco" {
        val calculadora = CalculadoraDeDesconto()
        checkAll(iterations = 200, precoPositivoArb) { preco ->
            val resultado = calculadora.calcular(preco)
            if (preco.value > CalculadoraDeDesconto.LIMITE) {
                val esperado = preco.value.multiply(BigDecimal("0.90"))
                resultado.value.compareTo(esperado) shouldBe 0
            } else {
                resultado.value.compareTo(preco.value) shouldBe 0
            }
        }
    }

    // ─── Property 7: Atomicidade de estoque ──────────────────────────────────

    "Property 7 — adicionarItem falha por estoque insuficiente: estoque do Produto permanece inalterado" {
        checkAll(iterations = 100, precoPositivoArb) { preco ->
            val estoqueOriginal = 0
            val produto = criarProduto(preco = preco, estoque = estoqueOriginal)
            val pedido = criarPedidoPendente()

            shouldThrow<DomainException> {
                pedido.adicionarItem(produto, 1)
            }

            // Estoque deve permanecer inalterado
            produto.quantidadeEstoque shouldBe estoqueOriginal
        }
    }

    "Property 7 — adicionarItem falha quando quantidade solicitada excede estoque: estoque permanece inalterado" {
        checkAll(iterations = 100, estoquePositivoArb, precoPositivoArb) { estoque, preco ->
            val quantidadeExcessiva = estoque + 1
            val produto = criarProduto(preco = preco, estoque = estoque)
            val pedido = criarPedidoPendente()

            shouldThrow<DomainException> {
                pedido.adicionarItem(produto, quantidadeExcessiva)
            }

            // Estoque deve permanecer inalterado
            produto.quantidadeEstoque shouldBe estoque
        }
    }

    "Property 7 — adicionarItem com estoque suficiente decrementa corretamente" {
        checkAll(iterations = 100, estoquePositivoArb, precoPositivoArb) { estoque, preco ->
            val quantidade = 1
            val produto = criarProduto(preco = preco, estoque = estoque)
            val pedido = criarPedidoPendente()

            pedido.adicionarItem(produto, quantidade)

            produto.quantidadeEstoque shouldBe (estoque - quantidade)
        }
    }

    // ─── Property 8/9: Transições de estado ──────────────────────────────────

    "Property 8 — Pedido.pagar() altera apenas status e preserva todos os outros campos" {
        checkAll(iterations = 100, nonBlankStringArb, nonBlankStringArb, precoPositivoArb) { nome, cpf, preco ->
            val cliente = Cliente(nome = nome, cpf = cpf)
            val item = ItemPedido(produtoId = 1L, preco = preco, quantidade = 2)
            val pedido = Pedido(
                id = 42L,
                cliente = cliente,
                status = StatusPedido.PENDENTE,
                itens = listOf(item),
                valorTotal = preco * 2
            )

            // Capture state before pagar()
            val idAntes = pedido.id
            val clienteAntes = pedido.cliente
            val itensAntes = pedido.itens.toList()
            val valorTotalAntes = pedido.valorTotal

            pedido.pagar()

            // Only status should change
            pedido.status shouldBe StatusPedido.PAGO
            pedido.id shouldBe idAntes
            pedido.cliente shouldBe clienteAntes
            pedido.itens shouldBe itensAntes
            pedido.valorTotal shouldBe valorTotalAntes
        }
    }

    "Property 9 — Pedido com status CANCELADO sempre lança exceção ao chamar pagar()" {
        checkAll(iterations = 100, nonBlankStringArb, nonBlankStringArb) { nome, cpf ->
            val pedido = Pedido(
                id = 1L,
                cliente = Cliente(nome = nome, cpf = cpf),
                status = StatusPedido.CANCELADO
            )

            shouldThrow<DomainException> {
                pedido.pagar()
            }
        }
    }

    "Property 8 — Pedido PAGO pode chamar pagar() novamente (idempotente para PAGO)" {
        // Verifica que pagar() em pedido já PAGO não lança exceção (transição PAGO -> PAGO)
        checkAll(iterations = 50, nonBlankStringArb, nonBlankStringArb) { nome, cpf ->
            val pedido = Pedido(
                id = 1L,
                cliente = Cliente(nome = nome, cpf = cpf),
                status = StatusPedido.PAGO
            )

            // pagar() em pedido PAGO deve funcionar (não é CANCELADO)
            pedido.pagar()
            pedido.status shouldBe StatusPedido.PAGO
        }
    }
})
