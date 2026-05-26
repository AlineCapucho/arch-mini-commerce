package com.example.monolito.repository

import com.example.monolito.model.Produto
import org.springframework.data.jpa.repository.JpaRepository

interface ProdutoRepository : JpaRepository<Produto, Long>
