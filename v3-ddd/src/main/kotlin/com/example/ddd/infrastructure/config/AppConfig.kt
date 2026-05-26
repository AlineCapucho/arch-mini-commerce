package com.example.ddd.infrastructure.config

import com.example.ddd.domain.services.CalculadoraDeDesconto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun calculadoraDeDesconto(): CalculadoraDeDesconto = CalculadoraDeDesconto()
}
