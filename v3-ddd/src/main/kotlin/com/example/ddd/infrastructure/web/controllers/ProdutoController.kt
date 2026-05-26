package com.example.ddd.infrastructure.web.controllers

import com.example.ddd.application.usecases.dto.ProdutoCommand
import com.example.ddd.application.usecases.produto.CriarProdutoUseCase
import com.example.ddd.application.usecases.produto.ListarProdutosUseCase
import com.example.ddd.infrastructure.web.dto.ProdutoRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/produtos")
class ProdutoController(
    private val criarProdutoUseCase: CriarProdutoUseCase,
    private val listarProdutosUseCase: ListarProdutosUseCase
) {

    @PostMapping
    fun criar(@Valid @RequestBody req: ProdutoRequest): ResponseEntity<Any> {
        val command = ProdutoCommand(req.nome, req.preco, req.quantidadeEstoque)
        val produto = criarProdutoUseCase.executar(command)
        return ResponseEntity.status(201).body(
            mapOf(
                "id" to produto.id,
                "nome" to produto.nome,
                "preco" to produto.preco.value,
                "quantidadeEstoque" to produto.quantidadeEstoque
            )
        )
    }

    @GetMapping
    fun listar(): List<Map<String, Any?>> =
        listarProdutosUseCase.executar().map { produto ->
            mapOf(
                "id" to produto.id,
                "nome" to produto.nome,
                "preco" to produto.preco.value,
                "quantidadeEstoque" to produto.quantidadeEstoque
            )
        }
}
