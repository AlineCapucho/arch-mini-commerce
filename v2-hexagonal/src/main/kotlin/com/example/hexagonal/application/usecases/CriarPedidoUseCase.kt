package com.example.hexagonal.application.usecases

import com.example.hexagonal.application.ports.PedidoRepositoryPort
import com.example.hexagonal.application.ports.ProdutoRepositoryPort
import com.example.hexagonal.domain.entities.ItemPedido
import com.example.hexagonal.domain.entities.Pedido
import com.example.hexagonal.domain.entities.Produto
import com.example.hexagonal.domain.entities.StatusPedido
import com.example.hexagonal.domain.errors.DomainException
import com.example.hexagonal.domain.services.CalculadoraDeDesconto
import java.math.BigDecimal

class CriarPedidoUseCase(
    private val produtoRepository: ProdutoRepositoryPort,
    private val pedidoRepository: PedidoRepositoryPort
) {
    fun executar(clienteNome: String, clienteCpf: String, produtoIds: List<Long>): Pedido {
        if (clienteNome.isBlank()) {
            throw DomainException("Nome do cliente não pode ser vazio")
        }
        if (clienteCpf.isBlank()) {
            throw DomainException("CPF do cliente não pode ser vazio")
        }

        // Load all products first
        val produtos: List<Produto> = produtoIds.map { id ->
            produtoRepository.buscarPorId(id)
                ?: throw DomainException("Produto $id não encontrado")
        }

        // Verify ALL products have sufficient stock BEFORE decrementing any
        produtos.forEach { produto ->
            if (produto.quantidadeEstoque < 1) {
                throw DomainException("Estoque insuficiente para o produto ${produto.id}")
            }
        }

        // Decrement stock of each product
        val produtosAtualizados: List<Produto> = produtos.map { produto ->
            produto.copy(quantidadeEstoque = produto.quantidadeEstoque - 1)
        }

        // Build items list: one item per product with quantity = 1
        val itens: MutableList<ItemPedido> = produtosAtualizados.map { produto ->
            ItemPedido(produto = produto, quantidade = 1)
        }.toMutableList()

        // Calculate total: sum of (preco * quantidade) for each item
        val valorBruto: BigDecimal = itens.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.produto.preco.multiply(BigDecimal.valueOf(item.quantidade.toLong()))
        }

        // Apply discount via CalculadoraDeDesconto
        val valorTotal = CalculadoraDeDesconto.calcular(valorBruto)

        // Create pedido with PENDENTE status
        val pedido = Pedido(
            clienteNome = clienteNome,
            clienteCpf = clienteCpf,
            status = StatusPedido.PENDENTE,
            itens = itens,
            valorTotal = valorTotal
        )

        // Persist updated stock
        produtoRepository.salvarTodos(produtosAtualizados)

        // Persist and return pedido
        return pedidoRepository.salvar(pedido)
    }
}
