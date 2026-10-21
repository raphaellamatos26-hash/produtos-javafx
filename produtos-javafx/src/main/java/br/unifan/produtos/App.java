package br.unifan.produtos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação JavaFX.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("principal.fxml"));
        Scene cena = new Scene(loader.load());
        cena.getStylesheets().add(App.class.getResource("estilo.css").toExternalForm());

        stage.setTitle("Cadastro de Produtos - Supermercado");
        stage.setScene(cena);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
