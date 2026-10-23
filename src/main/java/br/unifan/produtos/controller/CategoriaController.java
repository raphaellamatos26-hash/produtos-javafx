package br.unifan.produtos.controller;

import br.unifan.produtos.model.Categoria;
import br.unifan.produtos.model.PersistenciaException;
import br.unifan.produtos.model.ValidacaoException;
import br.unifan.produtos.service.CategoriaService;
import br.unifan.produtos.service.ProdutoService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador da aba Categorias: cadastrar, excluir, consultar por ID e listar.
 *
 * Os serviços são entregues pelo ProdutoController (método configurar),
 * para as duas abas trabalharem com os mesmos dados.
 */
public class CategoriaController {

    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private ComboBox<String> cbSetor;
    @FXML private TextField txtConsulta;
    @FXML private TableView<Categoria> tabela;
    @FXML private TableColumn<Categoria, Number> colId;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, String> colDescricao;
    @FXML private TableColumn<Categoria, String> colSetor;
    @FXML private TableColumn<Categoria, Number> colProdutos;

    private final ObservableList<Categoria> itensTabela = FXCollections.observableArrayList();
    private CategoriaService categoriaService;
    private ProdutoService produtoService;
    private Runnable aoAlterarCategorias = () -> { };
    private java.util.function.Consumer<String> status = texto -> { };

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colSetor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSetor()));
        // Quantos produtos usam cada categoria
        colProdutos.setCellValueFactory(c -> new SimpleObjectProperty<>(
                produtoService == null ? 0 : produtoService.contarPorCategoria(c.getValue().getId())));
        tabela.setItems(itensTabela);
    }

    /**
     * Chamado pelo ProdutoController depois que a tela carrega.
     *
     * @param aoAlterarCategorias avisa a aba Produtos para recarregar a lista de categorias
     * @param status              escreve mensagens na barra de status da janela
     */
    public void configurar(CategoriaService categoriaService, ProdutoService produtoService,
                           Runnable aoAlterarCategorias, java.util.function.Consumer<String> status) {
        this.categoriaService = categoriaService;
        this.produtoService = produtoService;
        this.aoAlterarCategorias = aoAlterarCategorias;
        this.status = status;
        atualizar();
    }

    /** Recarrega a tabela e a lista de setores (também usado quando um produto muda). */
    public void atualizar() {
        itensTabela.setAll(categoriaService.listarTodas());
        tabela.refresh(); // atualiza a coluna "Produtos"
        cbSetor.setItems(FXCollections.observableArrayList(categoriaService.listarSetores()));
    }

    // ===================== Eventos =====================

    @FXML
    private void onCadastrar() {
        // No ComboBox editável, o texto digitado fica no editor
        String setor = cbSetor.getEditor().getText();
        try {
            Categoria nova = categoriaService.cadastrar(txtNome.getText(), txtDescricao.getText(), setor);
            atualizar();
            tabela.getSelectionModel().select(nova);
            limpar();
            aoAlterarCategorias.run();
            status.accept("Categoria " + nova.getNome() + " cadastrada com ID " + nova.getId() + ".");
        } catch (ValidacaoException e) {
            alerta(Alert.AlertType.WARNING, "Verifique os dados da categoria", "• " + String.join("\n• ", e.getErros()));
        } catch (PersistenciaException e) {
            alerta(Alert.AlertType.ERROR, "Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void onLimpar() {
        limpar();
    }

    @FXML
    private void onConsultar() {
        String texto = txtConsulta.getText() == null ? "" : txtConsulta.getText().trim();
        int id;
        try {
            id = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.INFORMATION, "Consulta", "Digite o ID da categoria (um número).");
            return;
        }

        Optional<Categoria> encontrada = categoriaService.buscarPorId(id);
        if (encontrada.isPresent()) {
            itensTabela.setAll(List.of(encontrada.get()));
            tabela.getSelectionModel().select(encontrada.get());
            status.accept("Categoria " + id + " encontrada.");
        } else {
            alerta(Alert.AlertType.INFORMATION, "Consulta", "Nenhuma categoria com o ID " + id + ".");
        }
    }

    @FXML
    private void onListarTodas() {
        txtConsulta.clear();
        atualizar();
        status.accept(itensTabela.size() + " categoria(s) listada(s).");
    }

    @FXML
    private void onExcluir() {
        Categoria selecionada = tabela.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            alerta(Alert.AlertType.INFORMATION, "Excluir", "Selecione uma categoria na tabela para excluir.");
            return;
        }

        // Não deixa excluir categoria que ainda tem produtos (eles ficariam sem categoria)
        long emUso = produtoService.contarPorCategoria(selecionada.getId());
        if (emUso > 0) {
            alerta(Alert.AlertType.WARNING, "Categoria em uso",
                    "A categoria " + selecionada.getNome() + " tem " + emUso
                            + " produto(s). Exclua ou troque esses produtos antes.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente excluir a categoria " + selecionada.getNome() + "?", ButtonType.YES, ButtonType.NO);
        confirmacao.setHeaderText("Confirmar exclusão");
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.YES) {
            return;
        }

        try {
            categoriaService.excluir(selecionada.getId());
            atualizar();
            aoAlterarCategorias.run();
            status.accept("Categoria " + selecionada.getNome() + " excluída.");
        } catch (PersistenciaException e) {
            alerta(Alert.AlertType.ERROR, "Erro ao salvar", e.getMessage());
        }
    }

    // ===================== Auxiliares =====================

    private void limpar() {
        txtNome.clear();
        txtDescricao.clear();
        cbSetor.getSelectionModel().clearSelection();
        cbSetor.getEditor().clear();
        txtNome.requestFocus();
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
