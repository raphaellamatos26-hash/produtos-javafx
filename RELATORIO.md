# Relatório do Projeto — Cadastro de Produtos

**Aluna:** Raphaella Barbosa Matos<br>
**Disciplina:** Programação Orientada a Objetos III — P1 · **Professor:** Daniel Corrêa da Silva · UNIFAN

## 1. Funcionalidades implementadas

| Requisito | Onde está |
|---|---|
| Cadastrar produto | Formulário à esquerda da aba **Produtos**, botão *Cadastrar* (`ProdutoController.onCadastrar` → `ProdutoService.cadastrar`) |
| Excluir produto | Botão *Excluir selecionado*, com confirmação (`onExcluir` → `ProdutoService.excluir`) |
| Consultar por código | Campo *Consultar por código* + Enter ou botão (`onConsultar` → `ProdutoService.buscarPorCodigo`) |
| Listar todos | Tabela principal e botão *Listar todos* (`onListarTodos`) |
| Persistência em CSV com `;` | `ProdutoCsvRepository` — carrega ao iniciar, regrava após cada operação |
| Relatórios com Stream API | Aba **Relatórios** (`RelatorioService`) |

Extras: os detalhes do produto selecionado aparecem abaixo da tabela (incluindo a margem de lucro), as linhas
da tabela são coloridas (vermelho = vence em até 60 dias, amarelo = estoque baixo), há uma barra de status e
um relatório de resumo geral do estoque.

## 2. Classes do modelo de negócio

- **`Categoria`**: `id`, `nome`, `descricao`, `setor`.
- **`Produto`**: `codigo`, `nome`, `descricao`, `dataFabricacao` e `dataValidade` (`LocalDate`),
  `precoCompra` e `precoVenda` (`BigDecimal`), `quantidadeEstoque` e `categoria` — **composição** com `Categoria`.
  Também tem o método `getMargemLucro()`, que calcula `(venda − compra) / compra × 100`. Assim a regra fica
  dentro do próprio objeto que tem os dados.
- `equals`/`hashCode` usam o identificador (`codigo` no produto, `id` na categoria).

`BigDecimal` foi usado nos preços para evitar os erros de arredondamento do `double` em valores monetários.

## 3. Validação de dados

A classe `ValidadorProduto` junta **todos** os erros em uma lista e lança uma única `ValidacaoException`.
Assim o usuário vê de uma vez tudo o que precisa corrigir:

- Código: exatamente 8 caracteres alfanuméricos (regex `^[A-Za-z0-9]{8}$`), convertido para maiúsculas e
  sem repetição (código é identificador único).
- Nome: obrigatório, com no mínimo 3 caracteres.
- Data de fabricação: obrigatória e não posterior à data atual.
- Data de validade: obrigatória e não anterior à data de fabricação.
- Preços: positivos, com o preço de venda maior que o de compra.
- Estoque: inteiro e não negativo.
- Nome e descrição não podem conter `;`, porque esse é o separador do CSV.

Os erros de **formato** (texto no campo de preço, data digitada errada) são tratados no controlador antes de
criar o objeto. Os campos de preço aceitam `12,50` ou `12.50`.

## 4. Tratamento de exceções

- `ValidacaoException` (checada): erros de regra de negócio, exibidos em um alerta de aviso.
- `PersistenciaException` (não checada): envolve a `IOException` da leitura ou gravação do arquivo e é exibida
  em um alerta de erro, sem fechar o programa.
- Na leitura do CSV, uma linha corrompida não impede o carregamento das demais: ela é ignorada e o usuário
  recebe um aviso indicando o número da linha.

## 5. Relatórios com Stream API

| Relatório | Operações |
|---|---|
| Próximos ao vencimento (60 dias) | `filter` (não vencido e dentro do prazo) → `sorted` por validade → `map` para texto |
| Estoque baixo (< 10) | `filter` → `sorted` por quantidade → `map` |
| Margem média por categoria | `groupingBy` por categoria → `map(Produto::getMargemLucro)` → `reduce(BigDecimal.ZERO, BigDecimal::add)` ÷ quantidade |
| Agrupados por setor | `sorted` → `groupingBy(setor, TreeMap::new, mapping(...))` |
| Resumo do estoque | `map` + `reduce` para somar unidades, valor de custo e valor de venda |

A margem média usa `reduce` com `BigDecimal` em vez de `averagingDouble`, para manter a precisão decimal.
O `TreeMap` deixa as categorias e os setores em ordem alfabética.

## 6. Decisões de design

- **Camadas**: `model` (dados), `repository` (arquivo), `service` (regras e relatórios) e `controller` (tela).
  O controlador não sabe que existe um CSV; ele só conversa com o `ProdutoService`. Trocar o CSV por um banco de
  dados mexeria apenas no repositório.
- **Interface em FXML + CSS**: o layout fica separado do código e os eventos (`onAction="#onCadastrar"`)
  ligam a tela aos métodos do controlador.
- **CSV desnormalizado**: cada linha de produto já leva os dados da sua categoria. Assim um único arquivo
  basta, como pede o enunciado.
- **Datas em ISO e números com ponto no CSV**: o arquivo é lido do mesmo jeito em qualquer computador,
  independente do idioma do sistema. Na tela, as datas aparecem como `dd/mm/aaaa` e os preços em `R$`.
- **Categorias fixas**: há 8 categorias pré-definidas em `CategoriaRepository`, escolhidas por um ComboBox.
  O foco do trabalho é o cadastro de produtos, e isso evita erros de digitação no setor (que é usado no
  agrupamento do relatório).
- **Gravação após cada operação**: o arquivo é regravado inteiro a cada cadastro ou exclusão. Para o volume
  de um trabalho acadêmico isso é simples e garante que o arquivo sempre reflete o estado atual.
