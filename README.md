# Cadastro de Produtos — JavaFX

Sistema desktop em Java + JavaFX para gerenciar o cadastro de produtos de um supermercado.

- **Aluna:** Raphaella Barbosa Matos
- **Disciplina:** Programação Orientada a Objetos III — Avaliação P1
- **Professor:** Daniel Corrêa da Silva
- **Instituição:** Centro Universitário Alfredo Nasser (UNIFAN)

## Funcionalidades

- **Cadastrar** produto com todos os dados (com validação)
- **Excluir** produto (selecionando na tabela, com confirmação)
- **Consultar** produto por código
- **Listar** todos os produtos em uma tabela
- **Relatórios** com Stream API:
  1. Produtos próximos ao vencimento (próximos 60 dias)
  2. Produtos com estoque baixo (menos de 10 unidades)
  3. Margem de lucro média por categoria
  4. Produtos agrupados por setor da categoria
  5. Resumo geral do estoque (extra)
- Dados salvos em `dados/produtos.csv` (separado por `;`), carregados ao iniciar e
  regravados após cada cadastro ou exclusão.

## Requisitos

- **JDK 21 ou superior** (testado com o JDK 25)
- Não é preciso instalar o Maven nem o JavaFX: o projeto usa o Maven Wrapper (`mvnw`),
  que baixa tudo sozinho na primeira execução (precisa de internet nessa primeira vez).

## Como executar

### Pelo terminal

Na pasta do projeto:

Windows:
```
mvnw.cmd javafx:run
```

Linux / macOS:
```
./mvnw javafx:run
```

### Pelo IntelliJ IDEA

1. `File > Open` e selecione a pasta do projeto (o IntelliJ reconhece o `pom.xml`).
2. Aguarde o download das dependências.
3. Abra a aba **Maven** (lado direito) → `Plugins > javafx > javafx:run` (duplo clique).

   Ou abra a classe `br.unifan.produtos.App` e clique na setinha verde (Run).

> A pasta de trabalho deve ser a raiz do projeto, pois o arquivo de dados fica em
> `dados/produtos.csv` (caminho relativo). Se o arquivo não existir, ele é criado no primeiro cadastro.

## Estrutura do projeto

```
src/main/java/br/unifan/produtos/
├── App.java                      # main: ponto de entrada (rode esta classe)
├── TelaPrincipal.java            # extends Application: carrega a tela (FXML)
├── model/
│   ├── Categoria.java
│   ├── Produto.java              # composição com Categoria + cálculo da margem
│   ├── ValidacaoException.java
│   └── PersistenciaException.java
├── repository/
│   ├── CategoriaRepository.java  # categorias disponíveis
│   └── ProdutoCsvRepository.java # leitura/gravação do CSV
├── service/
│   ├── ValidadorProduto.java     # regras de validação
│   ├── ProdutoService.java       # cadastrar, excluir, consultar, listar
│   └── RelatorioService.java     # relatórios com Stream API
└── controller/
    └── ProdutoController.java    # eventos da tela
src/main/resources/br/unifan/produtos/
├── principal.fxml                # layout da tela
└── estilo.css
dados/produtos.csv                # dados de exemplo
```

## Formato do CSV

```
codigo;nome;descricao;dataFabricacao;dataValidade;precoCompra;precoVenda;quantidadeEstoque;categoriaId;categoriaNome;categoriaDescricao;categoriaSetor
LAT00001;Leite Integral 1L;Leite UHT integral caixa 1 litro;2026-09-20;2026-11-20;4.10;5.99;48;1;Laticínios;Leites, queijos e iogurtes;Refrigerados
```

Datas no formato ISO (`aaaa-mm-dd`) e valores com ponto decimal, para a leitura não depender do idioma do computador.

Veja também o [RELATORIO.md](RELATORIO.md) com as decisões de design.
