package com.example.ddd.domain

import com.example.ddd.application.usecases.dto.CriarPedidoCommand
import com.example.ddd.application.usecases.pedido.CriarPedidoUseCase
import com.example.ddd.application.usecases.pedido.PagarPedidoUseCase
import com.example.ddd.domain.entities.Pedido
import com.example.ddd.domain.entities.Produto
import com.example.ddd.domain.entities.StatusPedido
import com.example.ddd.domain.errors.DomainException
import com.example.ddd.domain.repositories.PedidoRepository
import com.example.ddd.domain.repositories.ProdutoRepository
import com.example.ddd.domain.services.CalculadoraDeDesconto
import com.example.ddd.domain.valueobjects.Cliente
import com.example.ddd.domain.valueobjects.Preco
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

/**
 * Unit tests for V3 DDD use cases and domain entities.
 *
 * Task 7.8
 * Requirements: 3.1, 3.2, 4.2, 6.3, 7.3
 */
class UseCasesTest {

    // ─── Pedido.pagar() ───────────────────────────────────────────────────────

    @Test
    fun `Pedido pagar - transicao PENDENTE para PAGO`() {
        val pedido = Pedido(
            id = 1L,
            cliente = Cliente(nome = "Ana", cpf = "111.222.333-44"),
            status = StatusPedido.PENDENTE
        )

        pedido.pagar()

        assertEquals(StatusPedido.PAGO, pedido.status)
    }

    @Test
    fun `Pedido pagar - pedido CANCELADO lanca DomainException`() {
        val pedido = Pedido(
            id = 2L,
            cliente = Cliente(nome = "Bob", cpf = "222.333.444-55"),
            status = StatusPedido.CANCELADO
        )

        assertThrows<DomainException> {
            pedido.pagar()
        }
    }

    // ─── Produto.decrementarEstoque() ─────────────────────────────────────────

    @Test
    fun `Produto decrementarEstoque - decremento valido reduz estoque corretamente`() {
        val produto = Produto(
            id = 1L,
            nome = "Notebook",
            preco = Preco(BigDecimal("1500.00")),
            quantidadeEstoque = 5
        )

        produto.decrementarEstoque(3)

        assertEquals(2, produto.quantidadeEstoque)
    }

    @Test
    fun `Produto decrementarEstoque - estoque insuficiente lanca DomainException`() {
        val produto = Produto(
            id = 2L,
            nome = "Mouse",
            preco = Preco(BigDecimal("50.00")),
            quantidadeEstoque = 0
        )

        assertThrows<DomainException> {
            produto.decrementarEstoque(1)
        }
    }

    @Test
    fun `Produto decrementarEstoque - quantidade maior que estoque lanca DomainException`() {
        val produto = Produto(
            id = 3L,
            nome = "Teclado",
            preco = Preco(BigDecimal("80.00")),
            quantidadeEstoque = 2
        )

        assertThrows<DomainException> {
            produto.decrementarEstoque(5)
        }
    }

    // ─── CalculadoraDeDesconto ────────────────────────────────────────────────

    @Test
    fun `CalculadoraDeDesconto - valor igual a 500 nao recebe desconto`() {
        val calculadora = CalculadoraDeDesconto()
        val preco = Preco(BigDecimal("500.00"))

        val resultado = calculadora.calcular(preco)

        assertEquals(0, resultado.value.compareTo(BigDecimal("500.00")))
    }

    @Test
    fun `CalculadoraDeDesconto - valor abaixo de 500 nao recebe desconto`() {
        val calculadora = CalculadoraDeDesconto()
        val preco = Preco(BigDecimal("300.00"))

        val resultado = calculadora.calcular(preco)

        assertEquals(0, resultado.value.compareTo(BigDecimal("300.00")))
    }

    @Test
    fun `CalculadoraDeDesconto - valor acima de 500 recebe desconto de 10 porcento`() {
        val calculadora = CalculadoraDeDesconto()
        val preco = Preco(BigDecimal("600.00"))

        val resultado = calculadora.calcular(preco)

        // 600.00 * 0.90 = 540.00
        assertEquals(0, resultado.value.compareTo(BigDecimal("540.00")))
    }

    // ─── CriarPedidoUseCase ───────────────────────────────────────────────────

    private val produtoRepository: ProdutoRepository = mock()
    private val pedidoRepository: PedidoRepository = mock()
    private val calculadora = CalculadoraDeDesconto()
    private val criarPedidoUseCase = CriarPedidoUseCase(produtoRepository, pedidoRepository, calculadora)

    @Test
    fun `CriarPedidoUseCase - cria pedido PENDENTE com produto disponivel`() {
        val produto = Produto(
            id = 1L,
            nome = "TV",
            preco = Preco(BigDecimal("300.00")),
            quantidadeEstoque = 5
        )

        whenever(produtoRepository.buscarPorId(1L)).thenReturn(produto)
        whenever(produtoRepository.salvarTodos(any())).thenAnswer { it.arguments[0] }
        whenever(pedidoRepository.salvar(any())).thenAnswer { it.arguments[0] as Pedido }

        val command = CriarPedidoCommand(
            clienteNome = "Carlos",
            clienteCpf = "333.444.555-66",
            produtoIds = listOf(1L)
        )

        val pedido = criarPedidoUseCase.executar(command)

        assertEquals(StatusPedido.PENDENTE, pedido.status)
        assertEquals("Carlos", pedido.cliente.nome)
        assertEquals(1, pedido.itens.size)
        // 300.00 <= 500.00 → no discount
        assertEquals(0, pedido.valorTotal.value.compareTo(BigDecimal("300.00")))
    }

    @Test
    fun `CriarPedidoUseCase - produto sem estoque lanca DomainException sem persistir`() {
        val produtoSemEstoque = Produto(
            id = 2L,
            nome = "Câmera",
            preco = Preco(BigDecimal("200.00")),
            quantidadeEstoque = 0
        )

        whenever(produtoRepository.buscarPorId(2L)).thenReturn(produtoSemEstoque)

        val command = CriarPedidoCommand(
            clienteNome = "Diana",
            clienteCpf = "444.555.666-77",
            produtoIds = listOf(2L)
        )

        assertThrows<DomainException> {
            criarPedidoUseCase.executar(command)
        }

        verify(produtoRepository, never()).salvarTodos(any())
        verify(pedidoRepository, never()).salvar(any())
    }

    @Test
    fun `CriarPedidoUseCase - produto inexistente lanca DomainException`() {
        whenever(produtoRepository.buscarPorId(99L)).thenReturn(null)

        val command = CriarPedidoCommand(
            clienteNome = "Eduardo",
            clienteCpf = "555.666.777-88",
            produtoIds = listOf(99L)
        )

        assertThrows<DomainException> {
            criarPedidoUseCase.executar(command)
        }

        verify(pedidoRepository, never()).salvar(any())
    }

    // ─── PagarPedidoUseCase ───────────────────────────────────────────────────

    private val pedidoRepositoryPagar: PedidoRepository = mock()
    private val pagarPedidoUseCase = PagarPedidoUseCase(pedidoRepositoryPagar)

    @Test
    fun `PagarPedidoUseCase - pedido PENDENTE transiciona para PAGO`() {
        val pedidoPendente = Pedido(
            id = 10L,
            cliente = Cliente(nome = "Fernanda", cpf = "666.777.888-99"),
            status = StatusPedido.PENDENTE
        )

        whenever(pedidoRepositoryPagar.buscarPorId(10L)).thenReturn(pedidoPendente)
        whenever(pedidoRepositoryPagar.salvar(any())).thenAnswer { it.arguments[0] as Pedido }

        val pedidoPago = pagarPedidoUseCase.executar(10L)

        assertEquals(StatusPedido.PAGO, pedidoPago.status)
    }

    @Test
    fun `PagarPedidoUseCase - pedido CANCELADO lanca DomainException sem persistir`() {
        val pedidoCancelado = Pedido(
            id = 20L,
            cliente = Cliente(nome = "Gabriel", cpf = "777.888.999-00"),
            status = StatusPedido.CANCELADO
        )

        whenever(pedidoRepositoryPagar.buscarPorId(20L)).thenReturn(pedidoCancelado)

        assertThrows<DomainException> {
            pagarPedidoUseCase.executar(20L)
        }

        verify(pedidoRepositoryPagar, never()).salvar(any())
    }

    @Test
    fun `PagarPedidoUseCase - pedido inexistente lanca NoSuchElementException`() {
        whenever(pedidoRepositoryPagar.buscarPorId(999L)).thenReturn(null)

        assertThrows<NoSuchElementException> {
            pagarPedidoUseCase.executar(999L)
        }
    }
}
