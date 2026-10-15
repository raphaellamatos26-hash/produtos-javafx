package br.unifan.produtos.service;

import br.unifan.produtos.model.Produto;
import br.unifan.produtos.model.ValidacaoException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Regras de validação do produto, conforme os requisitos técnicos.
 */
public class ValidadorProduto {

    /** Exatamente 8 caracteres alfanuméricos (letras sem acento e números). */
    private static final Pattern FORMATO_CODIGO = Pattern.compile("^[A-Za-z0-9]{8}$");
    public static final int TAMANHO_MINIMO_NOME = 3;

    public void validar(Produto p) throws ValidacaoException {
        List<String> erros = new ArrayList<>();

        if (p.getCodigo() == null || !FORMATO_CODIGO.matcher(p.getCodigo()).matches()) {
            erros.add("Código deve ter exatamente 8 caracteres alfanuméricos (ex.: ARZ00001).");
        }

        if (p.getNome() == null || p.getNome().isBlank()) {
            erros.add("Nome é obrigatório.");
        } else if (p.getNome().trim().length() < TAMANHO_MINIMO_NOME) {
            erros.add("Nome deve ter pelo menos " + TAMANHO_MINIMO_NOME + " caracteres.");
        }

        // O ';' é o separador do CSV, então não pode aparecer nos textos
        if (contemSeparador(p.getNome()) || contemSeparador(p.getDescricao())) {
            erros.add("Nome e descrição não podem conter o caractere ';'.");
        }

        if (p.getDataFabricacao() == null) {
            erros.add("Data de fabricação é obrigatória.");
        } else if (p.getDataFabricacao().isAfter(LocalDate.now())) {
            erros.add("Data de fabricação não pode ser posterior à data atual.");
        }

        if (p.getDataValidade() == null) {
            erros.add("Data de validade é obrigatória.");
        } else if (p.getDataFabricacao() != null && p.getDataValidade().isBefore(p.getDataFabricacao())) {
            erros.add("Data de validade não pode ser anterior à data de fabricação.");
        }

        if (p.getPrecoCompra() == null || p.getPrecoCompra().signum() <= 0) {
            erros.add("Preço de compra deve ser um valor positivo.");
        }
        if (p.getPrecoVenda() == null || p.getPrecoVenda().signum() <= 0) {
            erros.add("Preço de venda deve ser um valor positivo.");
        }
        if (p.getPrecoCompra() != null && p.getPrecoVenda() != null
                && p.getPrecoVenda().compareTo(p.getPrecoCompra()) <= 0) {
            erros.add("Preço de venda deve ser maior que o preço de compra.");
        }

        if (p.getQuantidadeEstoque() < 0) {
            erros.add("Quantidade em estoque não pode ser negativa.");
        }

        if (p.getCategoria() == null) {
            erros.add("Selecione uma categoria.");
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    private boolean contemSeparador(String texto) {
        return texto != null && texto.contains(";");
    }
}
