package br.unifan.produtos.repository;

import br.unifan.produtos.model.Categoria;
import br.unifan.produtos.model.PersistenciaException;
import br.unifan.produtos.model.Produto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê e grava os produtos em um arquivo CSV separado por ponto-e-vírgula.
 *
 * Formato de cada linha (a categoria vai junto na mesma linha):
 * codigo;nome;descricao;dataFabricacao;dataValidade;precoCompra;precoVenda;
 * quantidadeEstoque;categoriaId;categoriaNome;categoriaDescricao;categoriaSetor
 */
public class ProdutoCsvRepository {

    private static final String SEPARADOR = ";";
    private static final String CABECALHO = "codigo;nome;descricao;dataFabricacao;dataValidade;"
            + "precoCompra;precoVenda;quantidadeEstoque;categoriaId;categoriaNome;categoriaDescricao;categoriaSetor";
    private static final int TOTAL_CAMPOS = 12;

    private final Path arquivo;
    private final List<String> avisosLeitura = new ArrayList<>();

    public ProdutoCsvRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    /** Carrega todos os produtos do arquivo. Se o arquivo não existir, retorna lista vazia. */
    public List<Produto> carregar() {
        avisosLeitura.clear();
        List<Produto> produtos = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return produtos;
        }

        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            int numeroLinha = 0;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                if (numeroLinha == 1 || linha.isBlank()) {
                    continue; // pula o cabeçalho e linhas vazias
                }
                try {
                    produtos.add(converterLinha(linha));
                } catch (RuntimeException e) {
                    // Uma linha corrompida não impede de carregar as demais
                    avisosLeitura.add("Linha " + numeroLinha + " ignorada: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler o arquivo " + arquivo, e);
        }
        return produtos;
    }

    /** Regrava o arquivo inteiro com a lista atual de produtos. */
    public void salvar(List<Produto> produtos) {
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
                escritor.write(CABECALHO);
                escritor.newLine();
                for (Produto p : produtos) {
                    escritor.write(converterProduto(p));
                    escritor.newLine();
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + arquivo, e);
        }
    }

    /** Problemas encontrados na última leitura (linhas inválidas). */
    public List<String> getAvisosLeitura() {
        return List.copyOf(avisosLeitura);
    }

    public Path getArquivo() {
        return arquivo;
    }

    private Produto converterLinha(String linha) {
        // limite -1 mantém campos vazios no final (ex.: descrição em branco)
        String[] c = linha.split(SEPARADOR, -1);
        if (c.length != TOTAL_CAMPOS) {
            throw new IllegalArgumentException("esperados " + TOTAL_CAMPOS + " campos, encontrados " + c.length);
        }
        Categoria categoria = new Categoria(Integer.parseInt(c[8].trim()), c[9], c[10], c[11]);
        return new Produto(
                c[0], c[1], c[2],
                LocalDate.parse(c[3]), LocalDate.parse(c[4]),
                new BigDecimal(c[5]), new BigDecimal(c[6]),
                Integer.parseInt(c[7].trim()),
                categoria);
    }

    private String converterProduto(Produto p) {
        Categoria cat = p.getCategoria();
        return String.join(SEPARADOR,
                p.getCodigo(),
                p.getNome(),
                textoOuVazio(p.getDescricao()),
                p.getDataFabricacao().toString(),
                p.getDataValidade().toString(),
                p.getPrecoCompra().toPlainString(),
                p.getPrecoVenda().toPlainString(),
                String.valueOf(p.getQuantidadeEstoque()),
                String.valueOf(cat.getId()),
                cat.getNome(),
                textoOuVazio(cat.getDescricao()),
                cat.getSetor());
    }

    private String textoOuVazio(String texto) {
        return texto == null ? "" : texto;
    }
}
