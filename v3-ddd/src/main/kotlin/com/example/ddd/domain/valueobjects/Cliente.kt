package com.example.ddd.domain.valueobjects

data class Cliente(
    val nome: String,
    val cpf: String
) {
    init {
        require(nome.isNotBlank()) { "Nome do cliente não pode ser em branco" }
        require(cpf.isNotBlank()) { "CPF do cliente não pode ser em branco" }
    }
}
