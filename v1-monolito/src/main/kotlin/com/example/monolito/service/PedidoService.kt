package com.example.monolito.service

import com.example.monolito.model.ItemPedido
import com.example.monolito.model.Pedido
import com.example.monolito.model.StatusPedido
import com.example.monolito.repository.PedidoRepository
import com.example.monolito.repository.ProdutoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class PedidoService(
    private val pedidoRepository: PedidoRepository,
    private val produtoRepository: ProdutoRepository
) {

    fun criarPedido(clienteNome: String, clienteCpf: String, produtoIds: List<Long>): Pedido {
        // Carregar todos os produtos, lançando exceção se algum não for encontrado
        val produtos = produtoIds.map { id ->
            produtoRepository.findById(id).orElseThrow {
                NoSuchElementException("Produto $id não encontrado")
            }
        }

        // Verificar estoque de TODOS os produtos antes de qualquer alteração (atomicidade)
        produtos.forEach { produto ->
            if (produto.quantidadeEstoque < 1) {
                throw IllegalStateException("Produto ${produto.nome} sem estoque")
            }
        }

        // Decrementar estoque e construir itens
        val itens = produtos.map { produto ->
            produto.quantidadeEstoque -= 1
            produtoRepository.save(produto)
            ItemPedido(
                produtoId = produto.id,
                precoProduto = produto.preco,
                quantidade = 1
            )
        }

        // Calcular valorTotal (soma de preco × quantidade) e aplicar desconto
        val valorBruto = itens.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.precoProduto.multiply(item.quantidade.toBigDecimal())
        }
        val valorTotal = aplicarDesconto(valorBruto)

        // Criar pedido com status PENDENTE e persistir
        val pedido = Pedido(
            clienteNome = clienteNome,
            clienteCpf = clienteCpf,
            status = StatusPedido.PENDENTE,
            itens = itens.toMutableList(),
            valorTotal = valorTotal
        )
        return pedidoRepository.save(pedido)
    }

    fun pagarPedido(pedidoId: Long): Pedido {
        val pedido = pedidoRepository.findById(pedidoId).orElseThrow {
            NoSuchElementException("Pedido $pedidoId não encontrado")
        }
        if (pedido.status == StatusPedido.CANCELADO) {
            throw IllegalStateException("Pedido cancelado não pode ser pago")
        }
        pedido.status = StatusPedido.PAGO
        return pedidoRepository.save(pedido)
    }

    fun listarPedidos(): List<Pedido> {
        return pedidoRepository.findAll()
    }

    private fun aplicarDesconto(total: BigDecimal): BigDecimal {
        return if (total > BigDecimal("500.00")) {
            total.multiply(BigDecimal("0.90"))
        } else {
            total
        }
    }
}
