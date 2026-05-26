package com.example.hexagonal.infrastructure.web.controllers

import com.example.hexagonal.application.usecases.CriarProdutoUseCase
import com.example.hexagonal.application.usecases.ListarProdutosUseCase
import com.example.hexagonal.domain.entities.Produto
import com.example.hexagonal.infrastructure.web.dto.ProdutoRequest
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
    fun criar(@Valid @RequestBody req: ProdutoRequest): ResponseEntity<Produto> {
        val produto = criarProdutoUseCase.executar(req.nome, req.preco, req.quantidadeEstoque)
        return ResponseEntity.status(201).body(produto)
    }

    @GetMapping
    fun listar(): List<Produto> = listarProdutosUseCase.executar()
}
