package br.unifan.produtos.repository;

import br.unifan.produtos.model.Categoria;

import java.util.List;
import java.util.Optional;

/**
 * Categorias disponíveis para cadastro. São fixas para simplificar o sistema
 * (o foco do trabalho é o cadastro de produtos).
 */
public class CategoriaRepository {

    private final List<Categoria> categorias = List.of(
            new Categoria(1, "Laticínios", "Leites, queijos e iogurtes", "Refrigerados"),
            new Categoria(2, "Carnes", "Carnes bovinas, suínas e aves", "Refrigerados"),
            new Categoria(3, "Grãos", "Arroz, feijão e cereais", "Mercearia"),
            new Categoria(4, "Massas", "Macarrão e massas secas", "Mercearia"),
            new Categoria(5, "Bebidas", "Sucos, refrigerantes e águas", "Bebidas"),
            new Categoria(6, "Limpeza", "Produtos de limpeza doméstica", "Limpeza"),
            new Categoria(7, "Higiene", "Higiene pessoal", "Higiene e Beleza"),
            new Categoria(8, "Hortifruti", "Frutas, verduras e legumes", "Hortifruti")
    );

    public List<Categoria> listarTodas() {
        return categorias;
    }

    public Optional<Categoria> buscarPorId(int id) {
        return categorias.stream().filter(c -> c.getId() == id).findFirst();
    }
}
