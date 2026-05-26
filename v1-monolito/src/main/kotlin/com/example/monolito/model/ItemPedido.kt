package com.example.monolito.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "itens_pedido")
class ItemPedido(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var produtoId: Long,
    var precoProduto: BigDecimal,
    var quantidade: Int
)
