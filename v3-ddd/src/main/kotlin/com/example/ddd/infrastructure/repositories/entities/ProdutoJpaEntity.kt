package com.example.ddd.infrastructure.repositories.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "produtos")
class ProdutoJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var nome: String,
    var preco: BigDecimal,
    var quantidadeEstoque: Int
)
