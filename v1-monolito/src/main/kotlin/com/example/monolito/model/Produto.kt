package com.example.monolito.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "produtos")
class Produto(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var nome: String,
    var preco: BigDecimal,
    var quantidadeEstoque: Int
)
