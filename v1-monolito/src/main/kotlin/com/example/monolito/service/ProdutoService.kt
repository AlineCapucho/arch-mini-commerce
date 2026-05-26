package com.example.monolito.service

import com.example.monolito.dto.ProdutoRequest
import com.example.monolito.model.Produto
import com.example.monolito.repository.ProdutoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProdutoService(
    private val produtoRepository: ProdutoRepository
) {

    fun criarProduto(req: ProdutoRequest): Produto {
        if (req.preco <= java.math.BigDecimal.ZERO) {
            throw IllegalArgumentException("Preço deve ser maior que zero")
        }
        if (req.quantidadeEstoque < 0) {
            throw IllegalArgumentException("Quantidade em estoque não pode ser negativa")
        }

        val produto = Produto(
            nome = req.nome,
            preco = req.preco,
            quantidadeEstoque = req.quantidadeEstoque
        )
        return produtoRepository.save(produto)
    }

    @Transactional(readOnly = true)
    fun listarProdutos(): List<Produto> {
        return produtoRepository.findAll()
    }
}
