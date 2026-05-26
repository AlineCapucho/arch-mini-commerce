package com.example.ddd.infrastructure.repositories

import com.example.ddd.infrastructure.repositories.entities.ProdutoJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProdutoJpaRepository : JpaRepository<ProdutoJpaEntity, Long>
