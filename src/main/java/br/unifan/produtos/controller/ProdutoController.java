package br.unifan.produtos.controller;

import br.unifan.produtos.model.Categoria;
import br.unifan.produtos.model.PersistenciaException;
import br.unifan.produtos.model.Produto;
import br.unifan.produtos.model.ValidacaoException;
import br.unifan.produtos.repository.CategoriaRepository;
import br.unifan.produtos.repository.ProdutoCsvRepository;
import br.unifan.produtos.service.ProdutoService;
import br.unifan.produtos.service.RelatorioService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador da tela principal: trata os eventos do formulário,
 * da tabela de listagem e da aba de relatórios.
 */
public class ProdutoController {

    private static final Path ARQUIVO_CSV = Path.of("dados", "produtos.csv");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

    // Formulário
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private DatePicker dpFabricacao;
    @FXML private DatePicker dpValidade;
    @FXML private TextField txtPrecoCompra;
    @FXML private TextField txtPrecoVenda;
    @FXML private TextField txtEstoque;

    // Consulta e tabela
    @FXML private TextField txtConsulta;
    @FXML private TableView<Produto> tabela;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, String> colSetor;
    @FXML private TableColumn<Produto, LocalDate> colFabricacao;
    @FXML private TableColumn<Produto, LocalDate> colValidade;
    @FXML private TableColumn<Produto, BigDecimal> colPrecoCompra;
    @FXML private TableColumn<Produto, BigDecimal> colPrecoVenda;
    @FXML private TableColumn<Produto, Number> colEstoque;
    @FXML private TextArea txtDetalhes;

    // Relatórios e rodapé
    @FXML private TextArea txtRelatorio;
    @FXML private Label lblStatus;
    @FXML private Label lblArquivo;

    private final CategoriaRepository categoriaRepository = new CategoriaRepository();
    private final RelatorioService relatorioService = new RelatorioService();
    private final ObservableList<Produto> itensTabela = FXCollections.observableArrayList();
    private ProdutoService produtoService;

    /** Chamado automaticamente pelo FXMLLoader depois de injetar os campos. */
    @FXML
    private void initialize() {
        configurarDatePickers();
        cbCategoria.setItems(FXCollections.observableArrayList(categoriaRepository.listarTodas()));
        configurarTabela();
        lblArquivo.setText("Arquivo: " + ARQUIVO_CSV.toAbsolutePath());

        try {
            produtoService = new ProdutoService(new ProdutoCsvRepository(ARQUIVO_CSV));
            atualizarTabela(produtoService.listarTodos());
            status(produtoService.listarTodos().size() + " produto(s) carregado(s) do arquivo.");
            List<String> avisos = produtoService.getAvisosCarregamento();
            if (!avisos.isEmpty()) {
                alerta(Alert.AlertType.WARNING, "Algumas linhas do CSV foram ignoradas", String.join("\n", avisos));
            }
        } catch (PersistenciaException e) {
            alerta(Alert.AlertType.ERROR, "Erro ao carregar dados", e.getMessage());
        }
    }

    // ===================== Eventos do formulário =====================

    @FXML
    private void onCadastrar() {
        try {
            Produto produto = lerFormulario();
            produtoService.cadastrar(produto);
            atualizarTabela(produtoService.listarTodos());
            tabela.getSelectionModel().select(produto);
            tabela.scrollTo(produto);
            limparFormulario();
            status("Produto " + produto.getCodigo() + " cadastrado e salvo no arquivo.");
        } catch (ValidacaoException e) {
            alerta(Alert.AlertType.WARNING, "Verifique os dados do produto", "• " + String.join("\n• ", e.getErros()));
        } catch (PersistenciaException e) {
            alerta(Alert.AlertType.ERROR, "Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void onLimpar() {
        limparFormulario();
        status("Formulário limpo.");
    }

    // ===================== Eventos de consulta / tabela =====================

    @FXML
    private void onConsultar() {
        String codigo = txtConsulta.getText();
        if (codigo == null || codigo.isBlank()) {
            alerta(Alert.AlertType.INFORMATION, "Consulta", "Digite o código do produto.");
            return;
        }
        Optional<Produto> encontrado = produtoService.buscarPorCodigo(codigo);
        if (encontrado.isPresent()) {
            Produto p = encontrado.get();
            atualizarTabela(List.of(p));
            tabela.getSelectionModel().select(p);
            status("Produto " + p.getCodigo() + " encontrado.");
        } else {
            alerta(Alert.AlertType.INFORMATION, "Consulta", "Nenhum produto com o código \"" + codigo.trim().toUpperCase() + "\".");
            status("Produto não encontrado.");
        }
    }

    @FXML
    private void onListarTodos() {
        txtConsulta.clear();
        atualizarTabela(produtoService.listarTodos());
        status(itensTabela.size() + " produto(s) listado(s).");
    }

    @FXML
    private void onExcluir() {
        Produto selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            alerta(Alert.AlertType.INFORMATION, "Excluir", "Selecione um produto na tabela (ou consulte pelo código) para excluir.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir o produto " + selecionado + "?", ButtonType.YES, ButtonType.NO);
        confirmacao.setHeaderText("Confirmar exclusão");
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.YES) {
            return;
        }

        try {
            produtoService.excluir(selecionado.getCodigo());
            atualizarTabela(produtoService.listarTodos());
            txtDetalhes.clear();
            status("Produto " + selecionado.getCodigo() + " excluído e arquivo atualizado.");
        } catch (PersistenciaException e) {
            alerta(Alert.AlertType.ERROR, "Erro ao salvar", e.getMessage());
        }
    }

    // ===================== Eventos dos relatórios =====================

    @FXML
    private void onRelatorioVencimento() {
        exibirRelatorio(relatorioService.relatorioVencimento(produtoService.listarTodos(), LocalDate.now()));
    }

    @FXML
    private void onRelatorioEstoqueBaixo() {
        exibirRelatorio(relatorioService.relatorioEstoqueBaixo(produtoService.listarTodos()));
    }

    @FXML
    private void onRelatorioMargem() {
        exibirRelatorio(relatorioService.relatorioMargemPorCategoria(produtoService.listarTodos()));
    }

    @FXML
    private void onRelatorioSetor() {
        exibirRelatorio(relatorioService.relatorioPorSetor(produtoService.listarTodos()));
    }

    @FXML
    private void onRelatorioResumo() {
        exibirRelatorio(relatorioService.relatorioResumo(produtoService.listarTodos()));
    }

    // ===================== Configuração da tela =====================

    private void configurarTabela() {
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCategoria.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria().getNome()));
        colSetor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoria().getSetor()));
        colFabricacao.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDataFabricacao()));
        colValidade.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDataValidade()));
        colPrecoCompra.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrecoCompra()));
        colPrecoVenda.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrecoVenda()));
        colEstoque.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getQuantidadeEstoque()));

        colFabricacao.setCellFactory(col -> celulaFormatada(d -> d.format(DATA_BR)));
        colValidade.setCellFactory(col -> celulaFormatada(d -> d.format(DATA_BR)));
        colPrecoCompra.setCellFactory(col -> celulaFormatada(MOEDA::format));
        colPrecoVenda.setCellFactory(col -> celulaFormatada(MOEDA::format));

        // Destaca produtos vencendo (vermelho claro) e com estoque baixo (amarelo claro)
        tabela.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Produto p, boolean vazio) {
                super.updateItem(p, vazio);
                getStyleClass().removeAll("vencendo", "estoque-baixo");
                if (vazio || p == null) {
                    return;
                }
                LocalDate hoje = LocalDate.now();
                if (!p.getDataValidade().isAfter(hoje.plusDays(RelatorioService.DIAS_VENCIMENTO))) {
                    getStyleClass().add("vencendo");
                } else if (p.getQuantidadeEstoque() < RelatorioService.LIMITE_ESTOQUE_BAIXO) {
                    getStyleClass().add("estoque-baixo");
                }
            }
        });

        tabela.setItems(itensTabela);
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> mostrarDetalhes(novo));
    }

    private <T> TableCell<Produto, T> celulaFormatada(java.util.function.Function<T, String> formatador) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T valor, boolean vazio) {
                super.updateItem(valor, vazio);
                setText(vazio || valor == null ? null : formatador.apply(valor));
            }
        };
    }

    private void configurarDatePickers() {
        StringConverter<LocalDate> conversor = new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : data.format(DATA_BR);
            }

            @Override
            public LocalDate fromString(String texto) {
                return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), DATA_BR);
            }
        };
        dpFabricacao.setConverter(conversor);
        dpValidade.setConverter(conversor);
    }

    // ===================== Auxiliares =====================

    /** Converte os campos da tela em um Produto. Erros de formato viram ValidacaoException. */
    private Produto lerFormulario() throws ValidacaoException {
        List<String> erros = new java.util.ArrayList<>();

        LocalDate fabricacao = lerData(dpFabricacao, "Data de fabricação", erros);
        LocalDate validade = lerData(dpValidade, "Data de validade", erros);
        BigDecimal precoCompra = lerDecimal(txtPrecoCompra, "Preço de compra", erros);
        BigDecimal precoVenda = lerDecimal(txtPrecoVenda, "Preço de venda", erros);

        int estoque = 0;
        try {
            estoque = Integer.parseInt(txtEstoque.getText().trim());
        } catch (NumberFormatException e) {
            erros.add("Estoque deve ser um número inteiro.");
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        return new Produto(
                txtCodigo.getText(),
                txtNome.getText() == null ? null : txtNome.getText().trim(),
                txtDescricao.getText() == null ? "" : txtDescricao.getText().trim().replace("\n", " "),
                fabricacao, validade, precoCompra, precoVenda, estoque,
                cbCategoria.getValue());
    }

    /** Lê o texto digitado no DatePicker (mesmo que o usuário não tenha apertado Enter). */
    private LocalDate lerData(DatePicker picker, String campo, List<String> erros) {
        String texto = picker.getEditor().getText();
        if (texto == null || texto.isBlank()) {
            return null; // obrigatoriedade é tratada no ValidadorProduto
        }
        try {
            LocalDate data = picker.getConverter().fromString(texto);
            picker.setValue(data);
            return data;
        } catch (DateTimeParseException e) {
            erros.add(campo + " inválida. Use o formato dd/mm/aaaa.");
            return null;
        }
    }

    /** Aceita "12,50" ou "12.50". */
    private BigDecimal lerDecimal(TextField campoTexto, String campo, List<String> erros) {
        String texto = campoTexto.getText();
        if (texto == null || texto.isBlank()) {
            return null; // obrigatoriedade é tratada no ValidadorProduto
        }
        try {
            String normalizado = texto.trim().replace("R$", "").trim();
            if (normalizado.contains(",")) {
                normalizado = normalizado.replace(".", "").replace(",", ".");
            }
            return new BigDecimal(normalizado);
        } catch (NumberFormatException e) {
            erros.add(campo + " inválido. Use apenas números, ex.: 12,50.");
            return null;
        }
    }

    private void mostrarDetalhes(Produto p) {
        if (p == null) {
            txtDetalhes.clear();
            return;
        }
        Categoria c = p.getCategoria();
        txtDetalhes.setText(String.format(
                "%s — %s%nDescrição: %s%nCategoria: %s (%s) — setor %s%n"
                        + "Fabricação: %s   Validade: %s%nCompra: %s   Venda: %s   Margem: %s%%   Estoque: %d un.",
                p.getCodigo(), p.getNome(),
                p.getDescricao() == null || p.getDescricao().isBlank() ? "-" : p.getDescricao(),
                c.getNome(), c.getDescricao(), c.getSetor(),
                p.getDataFabricacao().format(DATA_BR), p.getDataValidade().format(DATA_BR),
                MOEDA.format(p.getPrecoCompra()), MOEDA.format(p.getPrecoVenda()),
                p.getMargemLucro().toPlainString(), p.getQuantidadeEstoque()));
    }

    private void atualizarTabela(List<Produto> produtos) {
        itensTabela.setAll(produtos);
    }

    private void limparFormulario() {
        txtCodigo.clear();
        txtNome.clear();
        txtDescricao.clear();
        cbCategoria.getSelectionModel().clearSelection();
        dpFabricacao.setValue(null);
        dpFabricacao.getEditor().clear();
        dpValidade.setValue(null);
        dpValidade.getEditor().clear();
        txtPrecoCompra.clear();
        txtPrecoVenda.clear();
        txtEstoque.clear();
        txtCodigo.requestFocus();
    }

    private void exibirRelatorio(String texto) {
        txtRelatorio.setText(texto);
        status("Relatório gerado.");
    }

    private void status(String mensagem) {
        lblStatus.setText(mensagem);
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
