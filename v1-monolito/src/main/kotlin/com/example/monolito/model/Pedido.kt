package com.example.monolito.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "pedidos")
class Pedido(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var clienteNome: String,
    var clienteCpf: String,
    @Enumerated(EnumType.STRING)
    var status: StatusPedido = StatusPedido.PENDENTE,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var itens: MutableList<ItemPedido> = mutableListOf(),
    var valorTotal: BigDecimal = BigDecimal.ZERO
)
