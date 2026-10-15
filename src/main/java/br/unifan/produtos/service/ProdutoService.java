package br.unifan.produtos.service;

import br.unifan.produtos.model.Produto;
import br.unifan.produtos.model.ValidacaoException;
import br.unifan.produtos.repository.ProdutoCsvRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio do cadastro. Mantém os produtos em memória e
 * grava no CSV após cada operação que altera os dados.
 */
public class ProdutoService {

    private final ProdutoCsvRepository repositorio;
    private final ValidadorProduto validador = new ValidadorProduto();
    private final List<Produto> produtos;

    public ProdutoService(ProdutoCsvRepository repositorio) {
        this.repositorio = repositorio;
        this.produtos = new ArrayList<>(repositorio.carregar()); // carrega ao iniciar
    }

    public void cadastrar(Produto produto) throws ValidacaoException {
        if (produto.getCodigo() != null) {
            produto.setCodigo(produto.getCodigo().trim().toUpperCase());
        }
        validador.validar(produto);
        if (buscarPorCodigo(produto.getCodigo()).isPresent()) {
            throw new ValidacaoException(List.of("Já existe um produto com o código " + produto.getCodigo() + "."));
        }
        produtos.add(produto);
        repositorio.salvar(produtos);
    }

    /** @return true se o produto existia e foi removido */
    public boolean excluir(String codigo) {
        boolean removido = produtos.removeIf(p -> p.getCodigo().equalsIgnoreCase(codigo.trim()));
        if (removido) {
            repositorio.salvar(produtos);
        }
        return removido;
    }

    public Optional<Produto> buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return produtos.stream()
                .filter(p -> p.getCodigo().equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }

    public List<Produto> listarTodos() {
        return produtos.stream()
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<String> getAvisosCarregamento() {
        return repositorio.getAvisosLeitura();
    }
}
