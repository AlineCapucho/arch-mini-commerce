package com.example.ddd.infrastructure.repositories.entities

import com.example.ddd.domain.entities.StatusPedido
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "pedidos")
class PedidoJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var clienteNome: String,
    var clienteCpf: String,
    @Enumerated(EnumType.STRING)
    var status: StatusPedido = StatusPedido.PENDENTE,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var itens: MutableList<ItemPedidoJpaEntity> = mutableListOf(),
    var valorTotal: BigDecimal = BigDecimal.ZERO
)
