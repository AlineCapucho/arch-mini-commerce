package com.example.ddd.domain.services

import com.example.ddd.domain.valueobjects.Preco
import java.math.BigDecimal

class CalculadoraDeDesconto {

    companion object {
        val LIMITE: BigDecimal = BigDecimal("500.00")
        val TAXA: BigDecimal = BigDecimal("0.10")
    }

    fun calcular(preco: Preco): Preco {
        return if (preco.value > LIMITE) {
            preco.aplicarDesconto(TAXA)
        } else {
            preco
        }
    }
}
