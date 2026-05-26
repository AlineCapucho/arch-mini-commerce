package com.example.monolito.repository

import com.example.monolito.model.Pedido
import org.springframework.data.jpa.repository.JpaRepository

interface PedidoRepository : JpaRepository<Pedido, Long>
