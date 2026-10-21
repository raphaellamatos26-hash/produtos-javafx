package br.unifan.produtos.model;

/**
 * Erro ao ler ou gravar o arquivo CSV.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
