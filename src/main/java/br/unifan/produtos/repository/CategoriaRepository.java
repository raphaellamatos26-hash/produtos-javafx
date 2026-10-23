package br.unifan.produtos.repository;

import br.unifan.produtos.model.Categoria;
import br.unifan.produtos.model.PersistenciaException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê e grava as categorias em um arquivo CSV separado por ponto-e-vírgula.
 *
 * Formato de cada linha: id;nome;descricao;setor
 *
 * Se o arquivo ainda não existir, o sistema começa com algumas categorias
 * de supermercado como exemplo.
 */
public class CategoriaRepository {

    private static final String SEPARADOR = ";";
    private static final String CABECALHO = "id;nome;descricao;setor";
    private static final int TOTAL_CAMPOS = 4;

    private final Path arquivo;

    public CategoriaRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    /** Carrega as categorias do arquivo. Se ele não existir, cria com as categorias de exemplo. */
    public List<Categoria> carregar() {
        if (!Files.exists(arquivo)) {
            List<Categoria> iniciais = categoriasIniciais();
            salvar(iniciais);
            return iniciais;
        }

        List<Categoria> categorias = new ArrayList<>();
        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            int numeroLinha = 0;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                if (numeroLinha == 1 || linha.isBlank()) {
                    continue; // pula o cabeçalho e linhas vazias
                }
                String[] c = linha.split(SEPARADOR, -1);
                if (c.length != TOTAL_CAMPOS) {
                    continue; // linha corrompida: ignora e segue
                }
                try {
                    categorias.add(new Categoria(Integer.parseInt(c[0].trim()), c[1], c[2], c[3]));
                } catch (NumberFormatException e) {
                    // id inválido: ignora a linha
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo, e);
        }
        return categorias;
    }

    /** Regrava o arquivo inteiro com a lista atual de categorias. */
    public void salvar(List<Categoria> categorias) {
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
                escritor.write(CABECALHO);
                escritor.newLine();
                for (Categoria c : categorias) {
                    escritor.write(String.join(SEPARADOR,
                            String.valueOf(c.getId()),
                            c.getNome(),
                            c.getDescricao() == null ? "" : c.getDescricao(),
                            c.getSetor()));
                    escritor.newLine();
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + arquivo, e);
        }
    }

    private List<Categoria> categoriasIniciais() {
        return new ArrayList<>(List.of(
                new Categoria(1, "Laticínios", "Leites, queijos e iogurtes", "Refrigerados"),
                new Categoria(2, "Carnes", "Carnes bovinas, suínas e aves", "Refrigerados"),
                new Categoria(3, "Grãos", "Arroz, feijão e cereais", "Mercearia"),
                new Categoria(4, "Massas", "Macarrão e massas secas", "Mercearia"),
                new Categoria(5, "Bebidas", "Sucos, refrigerantes e águas", "Bebidas"),
                new Categoria(6, "Limpeza", "Produtos de limpeza doméstica", "Limpeza"),
                new Categoria(7, "Higiene", "Higiene pessoal", "Higiene e Beleza"),
                new Categoria(8, "Hortifruti", "Frutas, verduras e legumes", "Hortifruti")
        ));
    }
}
