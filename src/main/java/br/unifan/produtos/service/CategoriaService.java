package br.unifan.produtos.service;

import br.unifan.produtos.model.Categoria;
import br.unifan.produtos.model.ValidacaoException;
import br.unifan.produtos.repository.CategoriaRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Regras do cadastro de categorias. Mantém as categorias em memória e
 * grava no CSV após cada operação que altera os dados.
 */
public class CategoriaService {

    public static final int TAMANHO_MINIMO_NOME = 3;

    private final CategoriaRepository repositorio;
    private final List<Categoria> categorias;

    public CategoriaService(CategoriaRepository repositorio) {
        this.repositorio = repositorio;
        this.categorias = new ArrayList<>(repositorio.carregar()); // carrega ao iniciar
    }

    /** Cria uma categoria nova. O id é gerado automaticamente (maior id + 1). */
    public Categoria cadastrar(String nome, String descricao, String setor) throws ValidacaoException {
        nome = nome == null ? "" : nome.trim();
        descricao = descricao == null ? "" : descricao.trim();
        setor = setor == null ? "" : setor.trim();

        List<String> erros = new ArrayList<>();
        if (nome.length() < TAMANHO_MINIMO_NOME) {
            erros.add("Nome da categoria deve ter pelo menos " + TAMANHO_MINIMO_NOME + " caracteres.");
        }
        if (setor.isBlank()) {
            erros.add("Setor é obrigatório.");
        }
        if (nome.contains(";") || descricao.contains(";") || setor.contains(";")) {
            erros.add("Os campos não podem conter o caractere ';'.");
        }
        String nomeFinal = nome;
        if (categorias.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nomeFinal))) {
            erros.add("Já existe uma categoria com o nome " + nome + ".");
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        int novoId = categorias.stream()
                .mapToInt(Categoria::getId)
                .max()
                .orElse(0) + 1;

        Categoria nova = new Categoria(novoId, nome, descricao, setor);
        categorias.add(nova);
        repositorio.salvar(categorias);
        return nova;
    }

    /** @return true se a categoria existia e foi removida */
    public boolean excluir(int id) {
        boolean removida = categorias.removeIf(c -> c.getId() == id);
        if (removida) {
            repositorio.salvar(categorias);
        }
        return removida;
    }

    public Optional<Categoria> buscarPorId(int id) {
        return categorias.stream().filter(c -> c.getId() == id).findFirst();
    }

    public List<Categoria> listarTodas() {
        return categorias.stream()
                .sorted(Comparator.comparing(Categoria::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Setores já usados, sem repetir, para sugerir no formulário. */
    public List<String> listarSetores() {
        return categorias.stream()
                .map(Categoria::getSetor)
                .distinct()
                .sorted()
                .toList();
    }
}
