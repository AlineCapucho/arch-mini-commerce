package com.example.monolito.service

import com.example.monolito.model.Pedido
import com.example.monolito.model.Produto
import com.example.monolito.model.StatusPedido
import com.example.monolito.repository.PedidoRepository
import com.example.monolito.repository.ProdutoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Optional

class PedidoServiceTest {

    private val pedidoRepository: PedidoRepository = mock(PedidoRepository::class.java)
    private val produtoRepository: ProdutoRepository = mock(ProdutoRepository::class.java)
    private val pedidoService: PedidoService = PedidoService(pedidoRepository, produtoRepository)

    // Requirements: 3.1, 3.2
    @Test
    fun `quandoValorBrutoMenorOuIgual500_naoAplicaDesconto`() {
        val produto = Produto(id = 1L, nome = "Produto A", preco = BigDecimal("500.00"), quantidadeEstoque = 5)

        whenever(produtoRepository.findById(1L)).thenReturn(Optional.of(produto))
        whenever(produtoRepository.save(any<Produto>())).thenReturn(produto)
        whenever(pedidoRepository.save(any<Pedido>())).thenAnswer { it.arguments[0] as Pedido }

        val pedido = pedidoService.criarPedido("Cliente Teste", "123.456.789-00", listOf(1L))

        assertEquals(BigDecimal("500.00"), pedido.valorTotal)
    }

    // Requirements: 3.1, 3.2
    @Test
    fun `quandoValorBrutoMaiorQue500_aplicaDesconto10Porcento`() {
        val produto = Produto(id = 2L, nome = "Produto B", preco = BigDecimal("600.00"), quantidadeEstoque = 5)

        whenever(produtoRepository.findById(2L)).thenReturn(Optional.of(produto))
        whenever(produtoRepository.save(any<Produto>())).thenReturn(produto)
        whenever(pedidoRepository.save(any<Pedido>())).thenAnswer { it.arguments[0] as Pedido }

        val pedido = pedidoService.criarPedido("Cliente Teste", "123.456.789-00", listOf(2L))

        // 600.00 * 0.90 = 540.00
        assertEquals(0, pedido.valorTotal.compareTo(BigDecimal("540.00"))) {
            "Esperado 540.00, mas foi ${pedido.valorTotal}"
        }
    }

    // Requirements: 7.3
    @Test
    fun `quandoProdutoSemEstoque_rejeita`() {
        val produto = Produto(id = 3L, nome = "Produto Sem Estoque", preco = BigDecimal("100.00"), quantidadeEstoque = 0)

        whenever(produtoRepository.findById(3L)).thenReturn(Optional.of(produto))

        assertThrows<IllegalStateException> {
            pedidoService.criarPedido("Cliente Teste", "123.456.789-00", listOf(3L))
        }
    }

    // Requirements: 4.2, 6.3
    @Test
    fun `quandoPedidoCancelado_naoPodeSerPago`() {
        val pedido = Pedido(
            id = 10L,
            clienteNome = "Cliente Teste",
            clienteCpf = "123.456.789-00",
            status = StatusPedido.CANCELADO,
            itens = mutableListOf(),
            valorTotal = BigDecimal("100.00")
        )

        whenever(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido))

        assertThrows<IllegalStateException> {
            pedidoService.pagarPedido(10L)
        }
    }
}
