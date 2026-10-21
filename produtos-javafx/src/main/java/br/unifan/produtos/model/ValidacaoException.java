package br.unifan.produtos.model;

import java.util.List;

/**
 * Lançada quando os dados de um produto não passam na validação.
 * Guarda a lista de todos os erros encontrados para exibir de uma vez na tela.
 */
public class ValidacaoException extends Exception {

    private final List<String> erros;

    public ValidacaoException(List<String> erros) {
        super(String.join("\n", erros));
        this.erros = List.copyOf(erros);
    }

    public List<String> getErros() {
        return erros;
    }
}
