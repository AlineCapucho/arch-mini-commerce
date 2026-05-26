package com.example.hexagonal.application.usecases

import com.example.hexagonal.application.ports.PedidoRepositoryPort
import com.example.hexagonal.application.ports.ProdutoRepositoryPort
import com.example.hexagonal.domain.entities.ItemPedido
import com.example.hexagonal.domain.entities.Pedido
import com.example.hexagonal.domain.entities.Produto
import com.example.hexagonal.domain.entities.StatusPedido
import com.example.hexagonal.domain.errors.DomainException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

/**
 * Unit tests for V2 use cases using mocks of output ports.
 *
 * Requirements: 4.2, 6.3, 7.3
 */
class UseCasesTest {

    // ─── CriarPedidoUseCase ───────────────────────────────────────────────────

    private val produtoRepositoryPort: ProdutoRepositoryPort = mock()
    private val pedidoRepositoryPort: PedidoRepositoryPort = mock()
    private val criarPedidoUseCase = CriarPedidoUseCase(produtoRepositoryPort, pedidoRepositoryPort)

    /**
     * Requirements: 2.1, 2.2, 2.3
     * Happy path: CriarPedidoUseCase creates a PENDENTE order and persists it.
     */
    @Test
    fun `criarPedido com produto disponivel cria pedido PENDENTE`() {
        val produto = Produto(id = 1L, nome = "Notebook", preco = BigDecimal("300.00"), quantidadeEstoque = 5)

        whenever(produtoRepositoryPort.buscarPorId(1L)).thenReturn(produto)
        whenever(produtoRepositoryPort.salvarTodos(any())).thenAnswer { it.arguments[0] }
        whenever(pedidoRepositoryPort.salvar(any())).thenAnswer { it.arguments[0] as Pedido }

        val pedido = criarPedidoUseCase.executar("Ana", "111.222.333-44", listOf(1L))

        assertEquals(StatusPedido.PENDENTE, pedido.status)
        assertEquals("Ana", pedido.clienteNome)
        assertEquals("111.222.333-44", pedido.clienteCpf)
        assertEquals(1, pedido.itens.size)
        // 300.00 <= 500.00 → no discount
        assertEquals(0, pedido.valorTotal.compareTo(BigDecimal("300.00")))
    }

    /**
     * Requirements: 3.1, 3.2
     * Discount is applied when valorTotal > 500.
     */
    @Test
    fun `criarPedido com valor acima de 500 aplica desconto de 10 porcento`() {
        val produto = Produto(id = 2L, nome = "TV", preco = BigDecimal("600.00"), quantidadeEstoque = 3)

        whenever(produtoRepositoryPort.buscarPorId(2L)).thenReturn(produto)
        whenever(produtoRepositoryPort.salvarTodos(any())).thenAnswer { it.arguments[0] }
        whenever(pedidoRepositoryPort.salvar(any())).thenAnswer { it.arguments[0] as Pedido }

        val pedido = criarPedidoUseCase.executar("Bob", "999.888.777-66", listOf(2L))

        // 600.00 * 0.90 = 540.00
        assertEquals(0, pedido.valorTotal.compareTo(BigDecimal("540.00"))) {
            "Esperado 540.00, mas foi ${pedido.valorTotal}"
        }
    }

    /**
     * Requirements: 7.3
     * When a product has insufficient stock, the order is rejected and NO stock is altered.
     */
    @Test
    fun `criarPedido com estoque insuficiente rejeita pedido sem alterar estoque`() {
        val produtoSemEstoque = Produto(id = 3L, nome = "Câmera", preco = BigDecimal("200.00"), quantidadeEstoque = 0)

        whenever(produtoRepositoryPort.buscarPorId(3L)).thenReturn(produtoSemEstoque)

        assertThrows<DomainException> {
            criarPedidoUseCase.executar("Carlos", "000.111.222-33", listOf(3L))
        }

        // salvarTodos must NOT be called — no stock should be altered
        verify(produtoRepositoryPort, never()).salvarTodos(any())
        // pedido must NOT be persisted
        verify(pedidoRepositoryPort, never()).salvar(any())
    }

    /**
     * Requirements: 7.3
     * When one product in a multi-product order has insufficient stock,
     * the entire order is rejected and no stock is altered.
     */
    @Test
    fun `criarPedido com um produto sem estoque em lista mista rejeita sem alterar nenhum estoque`() {
        val produtoComEstoque = Produto(id = 4L, nome = "Mouse", preco = BigDecimal("50.00"), quantidadeEstoque = 10)
        val produtoSemEstoque = Produto(id = 5L, nome = "Teclado", preco = BigDecimal("80.00"), quantidadeEstoque = 0)

        whenever(produtoRepositoryPort.buscarPorId(4L)).thenReturn(produtoComEstoque)
        whenever(produtoRepositoryPort.buscarPorId(5L)).thenReturn(produtoSemEstoque)

        assertThrows<DomainException> {
            criarPedidoUseCase.executar("Diana", "444.555.666-77", listOf(4L, 5L))
        }

        // No stock should be saved
        verify(produtoRepositoryPort, never()).salvarTodos(any())
        verify(pedidoRepositoryPort, never()).salvar(any())
    }

    /**
     * Requirements: 2.5
     * When a product ID does not exist, the order is rejected.
     */
    @Test
    fun `criarPedido com produto inexistente lanca DomainException`() {
        whenever(produtoRepositoryPort.buscarPorId(99L)).thenReturn(null)

        assertThrows<DomainException> {
            criarPedidoUseCase.executar("Eduardo", "777.888.999-00", listOf(99L))
        }

        verify(pedidoRepositoryPort, never()).salvar(any())
    }

    /**
     * Requirements: 2.6
     * When clienteNome is blank, the order is rejected.
     */
    @Test
    fun `criarPedido com nome em branco lanca DomainException`() {
        assertThrows<DomainException> {
            criarPedidoUseCase.executar("   ", "111.222.333-44", listOf(1L))
        }
    }

    /**
     * Requirements: 2.7
     * When clienteCpf is blank, the order is rejected.
     */
    @Test
    fun `criarPedido com CPF em branco lanca DomainException`() {
        assertThrows<DomainException> {
            criarPedidoUseCase.executar("Fernanda", "  ", listOf(1L))
        }
    }

    // ─── PagarPedidoUseCase ───────────────────────────────────────────────────

    private val pedidoRepositoryPortPagar: PedidoRepositoryPort = mock()
    private val pagarPedidoUseCase = PagarPedidoUseCase(pedidoRepositoryPortPagar)

    /**
     * Requirements: 4.1, 6.2
     * Happy path: paying a PENDENTE order transitions it to PAGO.
     */
    @Test
    fun `pagarPedido com pedido PENDENTE transiciona para PAGO`() {
        val pedidoPendente = Pedido(
            id = 10L,
            clienteNome = "Gabi",
            clienteCpf = "123.456.789-00",
            status = StatusPedido.PENDENTE,
            itens = mutableListOf(),
            valorTotal = BigDecimal("100.00")
        )

        whenever(pedidoRepositoryPortPagar.buscarPorId(10L)).thenReturn(pedidoPendente)
        whenever(pedidoRepositoryPortPagar.salvar(any())).thenAnswer { it.arguments[0] as Pedido }

        val pedidoPago = pagarPedidoUseCase.executar(10L)

        assertEquals(StatusPedido.PAGO, pedidoPago.status)
        assertEquals(10L, pedidoPago.id)
    }

    /**
     * Requirements: 4.2, 6.3
     * Paying a CANCELADO order must throw DomainException without modifying the order.
     */
    @Test
    fun `pagarPedido com pedido CANCELADO lanca DomainException`() {
        val pedidoCancelado = Pedido(
            id = 20L,
            clienteNome = "Hugo",
            clienteCpf = "987.654.321-00",
            status = StatusPedido.CANCELADO,
            itens = mutableListOf(),
            valorTotal = BigDecimal("200.00")
        )

        whenever(pedidoRepositoryPortPagar.buscarPorId(20L)).thenReturn(pedidoCancelado)

        assertThrows<DomainException> {
            pagarPedidoUseCase.executar(20L)
        }

        // The cancelled order must NOT be saved
        verify(pedidoRepositoryPortPagar, never()).salvar(any())
    }

    /**
     * Requirements: 4.3
     * Paying a non-existent order must throw NoSuchElementException.
     */
    @Test
    fun `pagarPedido com pedido inexistente lanca NoSuchElementException`() {
        whenever(pedidoRepositoryPortPagar.buscarPorId(999L)).thenReturn(null)

        assertThrows<NoSuchElementException> {
            pagarPedidoUseCase.executar(999L)
        }
    }
}
