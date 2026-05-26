package com.example.hexagonal.infrastructure.repositories

import com.example.hexagonal.infrastructure.repositories.entities.ProdutoJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProdutoJpaRepository : JpaRepository<ProdutoJpaEntity, Long>
