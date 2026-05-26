package com.example.hexagonal.infrastructure.web.controllers

import com.example.hexagonal.application.usecases.CriarPedidoUseCase
import com.example.hexagonal.application.usecases.ListarPedidosUseCase
import com.example.hexagonal.application.usecases.PagarPedidoUseCase
import com.example.hexagonal.infrastructure.web.dto.CriarPedidoRequest
import com.example.hexagonal.infrastructure.web.presenters.PedidoPresenter
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pedidos")
class PedidoController(
    private val criarPedidoUseCase: CriarPedidoUseCase,
    private val pagarPedidoUseCase: PagarPedidoUseCase,
    private val listarPedidosUseCase: ListarPedidosUseCase
) {

    @PostMapping
    fun criar(@Valid @RequestBody req: CriarPedidoRequest): ResponseEntity<Any> {
        val pedido = criarPedidoUseCase.executar(req.clienteNome, req.clienteCpf, req.produtoIds)
        return ResponseEntity.status(201).body(PedidoPresenter.toResponse(pedido))
    }

    @PatchMapping("/{id}/pagar")
    fun pagar(@PathVariable id: Long): ResponseEntity<Any> {
        val pedido = pagarPedidoUseCase.executar(id)
        return ResponseEntity.ok(PedidoPresenter.toResponse(pedido))
    }

    @GetMapping
    fun listar(): List<Map<String, Any?>> =
        PedidoPresenter.toResponseList(listarPedidosUseCase.executar())
}
