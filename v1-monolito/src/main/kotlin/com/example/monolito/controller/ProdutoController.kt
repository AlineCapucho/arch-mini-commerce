package com.example.monolito.controller

import com.example.monolito.dto.ProdutoRequest
import com.example.monolito.model.Produto
import com.example.monolito.service.ProdutoService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/produtos")
class ProdutoController(private val produtoService: ProdutoService) {

    @PostMapping
    fun criar(@Valid @RequestBody req: ProdutoRequest): ResponseEntity<Produto> {
        val produto = produtoService.criarProduto(req)
        return ResponseEntity.status(201).body(produto)
    }

    @GetMapping
    fun listar(): List<Produto> = produtoService.listarProdutos()
}
