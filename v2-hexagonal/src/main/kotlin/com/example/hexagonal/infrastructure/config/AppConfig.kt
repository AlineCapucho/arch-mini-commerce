package com.example.hexagonal.infrastructure.config

import com.example.hexagonal.application.ports.PedidoRepositoryPort
import com.example.hexagonal.application.ports.ProdutoRepositoryPort
import com.example.hexagonal.application.usecases.CriarPedidoUseCase
import com.example.hexagonal.application.usecases.CriarProdutoUseCase
import com.example.hexagonal.application.usecases.ListarPedidosUseCase
import com.example.hexagonal.application.usecases.ListarProdutosUseCase
import com.example.hexagonal.application.usecases.PagarPedidoUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean
    fun criarPedidoUseCase(
        produtoRepository: ProdutoRepositoryPort,
        pedidoRepository: PedidoRepositoryPort
    ): CriarPedidoUseCase = CriarPedidoUseCase(produtoRepository, pedidoRepository)

    @Bean
    fun pagarPedidoUseCase(pedidoRepository: PedidoRepositoryPort): PagarPedidoUseCase =
        PagarPedidoUseCase(pedidoRepository)

    @Bean
    fun criarProdutoUseCase(produtoRepository: ProdutoRepositoryPort): CriarProdutoUseCase =
        CriarProdutoUseCase(produtoRepository)

    @Bean
    fun listarProdutosUseCase(produtoRepository: ProdutoRepositoryPort): ListarProdutosUseCase =
        ListarProdutosUseCase(produtoRepository)

    @Bean
    fun listarPedidosUseCase(pedidoRepository: PedidoRepositoryPort): ListarPedidosUseCase =
        ListarPedidosUseCase(pedidoRepository)
}
