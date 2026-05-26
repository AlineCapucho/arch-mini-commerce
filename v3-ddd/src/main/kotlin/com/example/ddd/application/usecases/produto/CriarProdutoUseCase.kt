package com.example.ddd.application.usecases.produto

import com.example.ddd.application.usecases.dto.ProdutoCommand
import com.example.ddd.domain.entities.Produto
import com.example.ddd.domain.errors.DomainException
import com.example.ddd.domain.repositories.ProdutoRepository
import com.example.ddd.domain.valueobjects.Preco
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class CriarProdutoUseCase(
    private val produtoRepository: ProdutoRepository
) {
    fun executar(command: ProdutoCommand): Produto {
        if (command.nome.isBlank()) {
            throw DomainException("Nome do produto não pode ser em branco")
        }
        if (command.preco <= BigDecimal.ZERO) {
            throw DomainException("Preço do produto deve ser maior que zero")
        }
        if (command.quantidadeEstoque < 0) {
            throw DomainException("Quantidade em estoque não pode ser negativa")
        }

        val produto = Produto(
            id = 0,
            nome = command.nome,
            preco = Preco(command.preco),
            quantidadeEstoque = command.quantidadeEstoque
        )

        return produtoRepository.salvar(produto)
    }
}
