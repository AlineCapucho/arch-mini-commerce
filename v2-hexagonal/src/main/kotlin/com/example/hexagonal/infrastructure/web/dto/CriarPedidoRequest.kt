package com.example.hexagonal.infrastructure.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CriarPedidoRequest(
    @field:NotBlank(message = "Nome do cliente não pode ser vazio")
    val clienteNome: String,

    @field:NotBlank(message = "CPF do cliente não pode ser vazio")
    val clienteCpf: String,

    @field:NotEmpty(message = "Lista de produtos não pode ser vazia")
    val produtoIds: List<Long>
)
