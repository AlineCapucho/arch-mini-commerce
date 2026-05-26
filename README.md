# Mini Commerce

Sistema simplificado de gestão de pedidos implementado em **três versões arquiteturais** com Kotlin + Spring Boot + H2. O objetivo é comparar na prática como a mesma lógica de negócio se organiza em arquiteturas diferentes.

---

## O que o sistema faz

### Produtos
- Cadastrar um produto com nome, preço e quantidade em estoque
- Listar todos os produtos cadastrados

### Pedidos
- Criar um pedido informando o nome do cliente, CPF e os IDs dos produtos desejados
- Pagar um pedido existente
- Listar todos os pedidos com seus itens e valor total

---

## Regras de negócio

| Regra | Detalhe |
|---|---|
| **Estoque** | Cada produto precisa ter pelo menos 1 unidade em estoque para entrar em um pedido. O estoque é decrementado no momento da criação do pedido. |
| **Atomicidade** | O estoque de **todos** os produtos é verificado antes de qualquer alteração. Se um produto estiver sem estoque, nenhum estoque é alterado e o pedido não é criado. |
| **Desconto** | Se o valor total do pedido for maior que R$ 500,00, é aplicado automaticamente um desconto de 10%. |
| **Pagamento** | Um pedido só pode ser pago se estiver com status `PENDENTE`. Pedidos com status `CANCELADO` não podem ser pagos. |
| **Status do pedido** | Os status possíveis são: `PENDENTE` → `PAGO`. Pedidos cancelados são um estado terminal. |
| **Validações de entrada** | Preço do produto deve ser maior que zero. Estoque não pode ser negativo. Nome e CPF do cliente são obrigatórios. A lista de produtos do pedido não pode ser vazia. |

---

## As três versões

### V1 — Monolito (`v1-monolito`, porta 8081)
Arquitetura "Big Ball of Mud": Controller → Service → Repository. Toda a lógica de negócio vive dentro dos Services, junto com acesso ao banco. Simples e direto, sem separação de camadas.

### V2 — Hexagonal (`v2-hexagonal`, porta 8082)
Arquitetura Hexagonal (Ports & Adapters). O núcleo da aplicação (domain + use cases) é completamente isolado de frameworks. A comunicação com o banco e com a web acontece por meio de interfaces (ports) implementadas por adapters na camada de infraestrutura.

```
domain/          → entidades puras, sem Spring ou JPA
application/     → use cases + interfaces de repositório (ports)
infrastructure/  → adapters JPA, controllers Spring MVC
```

### V3 — DDD (`v3-ddd`, porta 8083)
Domain-Driven Design. Introduz Value Objects (`Cliente`, `Preco`), um Aggregate Root (`Pedido`) com comportamento encapsulado, e um Domain Service (`CalculadoraDeDesconto`). A camada Application orquestra os use cases sem conhecer detalhes de infraestrutura.

```
domain/value-objects/  → Cliente, Preco (com autovalidação)
domain/entities/       → Produto, Pedido (Aggregate Root), ItemPedido
domain/services/       → CalculadoraDeDesconto
domain/repositories/   → interfaces de repositório
application/usecases/  → orquestração dos casos de uso
infrastructure/        → JPA entities, adapters, controllers
```

---

## Pré-requisitos

- **Java 17+** instalado e no PATH
- **Make** instalado (Linux/macOS: nativo; Windows: via [Chocolatey](https://chocolatey.org/) com `choco install make` ou [Git Bash](https://gitforwindows.org/))

Não é necessário instalar Gradle — cada projeto usa o Gradle Wrapper (`gradlew`).

---

## Como executar

### Usando Make (recomendado)

```bash
# Subir uma versão específica
make run-v1   # V1 em http://localhost:8081
make run-v2   # V2 em http://localhost:8082
make run-v3   # V3 em http://localhost:8083

# Rodar os testes
make test-v1
make test-v2
make test-v3
make test-all   # testa as três versões em sequência
```

### Sem Make (diretamente com Gradle)

```bash
# V1
cd v1-monolito
./gradlew bootRun        # sobe o servidor
./gradlew test           # roda os testes

# V2
cd v2-hexagonal
./gradlew bootRun
./gradlew test

# V3
cd v3-ddd
./gradlew bootRun
./gradlew test
```

> **Windows sem Git Bash:** substitua `./gradlew` por `gradlew.bat` nos comandos acima.

---

## Acessando a aplicação

Após subir qualquer versão, abra no navegador:

```
http://localhost:808X/
```

A interface web permite criar produtos, criar pedidos, pagar pedidos e visualizar as listas — tudo sem recarregar a página.

---

## Endpoints da API

Todos os endpoints são idênticos nas três versões.

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/produtos` | Cadastra um produto |
| `GET` | `/api/produtos` | Lista todos os produtos |
| `POST` | `/api/pedidos` | Cria um pedido |
| `PATCH` | `/api/pedidos/{id}/pagar` | Paga um pedido |
| `GET` | `/api/pedidos` | Lista todos os pedidos |

### Exemplos de payload

**Criar produto:**
```json
{
  "nome": "Teclado Mecânico",
  "preco": 350.00,
  "quantidadeEstoque": 10
}
```

**Criar pedido:**
```json
{
  "clienteNome": "Maria Silva",
  "clienteCpf": "123.456.789-00",
  "produtoIds": [1, 2]
}
```

---

## Testes

Cada versão possui três camadas de testes:

| Tipo | O que valida |
|---|---|
| **Unitários** | Regras de negócio isoladas (desconto, estoque, transições de status) |
| **Property-based** | Propriedades universais com centenas de inputs gerados automaticamente (ex: desconto sempre correto, atomicidade de estoque, round-trip de mapeamento) |
| **ArchUnit** (V2 e V3) | Regras de dependência entre camadas — garante que o domain nunca depende de Spring ou JPA |
