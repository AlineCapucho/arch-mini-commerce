package com.example.monolito.controller

import com.example.monolito.dto.CriarPedidoRequest
import com.example.monolito.model.Pedido
import com.example.monolito.service.PedidoService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pedidos")
class PedidoController(private val pedidoService: PedidoService) {

    @PostMapping
    fun criar(@Valid @RequestBody req: CriarPedidoRequest): ResponseEntity<Pedido> {
        val pedido = pedidoService.criarPedido(req.clienteNome, req.clienteCpf, req.produtoIds)
        return ResponseEntity.status(201).body(pedido)
    }

    @PatchMapping("/{id}/pagar")
    fun pagar(@PathVariable id: Long): ResponseEntity<Pedido> {
        val pedido = pedidoService.pagarPedido(id)
        return ResponseEntity.ok(pedido)
    }

    @GetMapping
    fun listar(): List<Pedido> = pedidoService.listarPedidos()
}
