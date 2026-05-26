package com.example.hexagonal.infrastructure.repositories

import com.example.hexagonal.infrastructure.repositories.entities.PedidoJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PedidoJpaRepository : JpaRepository<PedidoJpaEntity, Long>
