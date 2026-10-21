package br.unifan.produtos;

import javafx.application.Application;

/**
 * Ponto de entrada do sistema.
 *
 * Esta classe NÃO estende Application de propósito: quando a classe que tem o main
 * estende Application, o Java exige o JavaFX configurado como módulo e não roda
 * pelo botão Run da IDE. Assim, o main aqui só manda o JavaFX abrir a TelaPrincipal.
 */
public class App {

    public static void main(String[] args) {
        Application.launch(TelaPrincipal.class, args);
    }
}
