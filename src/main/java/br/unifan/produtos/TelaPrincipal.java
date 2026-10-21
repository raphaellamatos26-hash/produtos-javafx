package br.unifan.produtos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Aplicação JavaFX: carrega a tela (FXML + CSS) e mostra a janela.
 */
public class TelaPrincipal extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(TelaPrincipal.class.getResource("principal.fxml"));
        Scene cena = new Scene(loader.load());
        cena.getStylesheets().add(TelaPrincipal.class.getResource("estilo.css").toExternalForm());

        stage.setTitle("Cadastro de Produtos - Supermercado");
        stage.setScene(cena);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.show();
    }
}
