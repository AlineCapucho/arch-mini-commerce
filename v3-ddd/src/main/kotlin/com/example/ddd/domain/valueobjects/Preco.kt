package com.example.ddd.domain.valueobjects

import java.math.BigDecimal

data class Preco(
    val value: BigDecimal
) {
    init {
        require(value >= BigDecimal.ZERO) { "Preço não pode ser negativo" }
    }

    operator fun plus(other: Preco): Preco = Preco(this.value + other.value)

    operator fun times(factor: Int): Preco = Preco(this.value * BigDecimal(factor))

    fun aplicarDesconto(percentual: BigDecimal): Preco =
        Preco(value * (BigDecimal.ONE - percentual))

    companion object {
        val ZERO: Preco = Preco(BigDecimal.ZERO)
    }
}
