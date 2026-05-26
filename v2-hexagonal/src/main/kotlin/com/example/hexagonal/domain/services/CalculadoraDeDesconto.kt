package com.example.hexagonal.domain.services

import java.math.BigDecimal

object CalculadoraDeDesconto {
    private val LIMITE = BigDecimal("500.00")
    private val PERCENTUAL_DESCONTO = BigDecimal("0.10")

    fun calcular(valorBruto: BigDecimal): BigDecimal =
        if (valorBruto > LIMITE)
            valorBruto - (valorBruto * PERCENTUAL_DESCONTO)
        else
            valorBruto
}
