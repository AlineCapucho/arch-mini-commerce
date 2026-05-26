package com.example.ddd.application.usecases.pedido

import com.example.ddd.application.usecases.dto.CriarPedidoCommand
import com.example.ddd.domain.entities.Pedido
import com.example.ddd.domain.errors.DomainException
import com.example.ddd.domain.repositories.PedidoRepository
import com.example.ddd.domain.repositories.ProdutoRepository
import com.example.ddd.domain.services.CalculadoraDeDesconto
import com.example.ddd.domain.valueobjects.Cliente
import org.springframework.stereotype.Service

@Service
class CriarPedidoUseCase(
    private val produtoRepository: ProdutoRepository,
    private val pedidoRepository: PedidoRepository,
    private val calculadoraDeDesconto: CalculadoraDeDesconto
) {
    fun executar(command: CriarPedidoCommand): Pedido {
        val cliente = Cliente(nome = command.clienteNome, cpf = command.clienteCpf)

        val produtos = command.produtoIds.map { id ->
            produtoRepository.buscarPorId(id)
                ?: throw DomainException("Produto $id não encontrado")
        }

        val pedido = Pedido(cliente = cliente)

        for (produto in produtos) {
            pedido.adicionarItem(produto, 1)
        }

        pedido.calcularTotal(calculadoraDeDesconto)

        produtoRepository.salvarTodos(produtos)

        return pedidoRepository.salvar(pedido)
    }
}
