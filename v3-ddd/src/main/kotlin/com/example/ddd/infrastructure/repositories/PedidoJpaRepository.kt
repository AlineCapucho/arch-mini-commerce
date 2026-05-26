package com.example.ddd.infrastructure.repositories

import com.example.ddd.infrastructure.repositories.entities.PedidoJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PedidoJpaRepository : JpaRepository<PedidoJpaEntity, Long>
