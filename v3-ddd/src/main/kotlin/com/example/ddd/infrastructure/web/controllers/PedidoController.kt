package com.example.ddd.infrastructure.web.controllers

import com.example.ddd.application.usecases.dto.CriarPedidoCommand
import com.example.ddd.application.usecases.pedido.CriarPedidoUseCase
import com.example.ddd.application.usecases.pedido.ListarPedidosUseCase
import com.example.ddd.application.usecases.pedido.PagarPedidoUseCase
import com.example.ddd.infrastructure.web.dto.CriarPedidoRequest
import com.example.ddd.infrastructure.web.presenters.PedidoPresenter
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
        val command = CriarPedidoCommand(req.clienteNome, req.clienteCpf, req.produtoIds)
        val pedido = criarPedidoUseCase.executar(command)
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
