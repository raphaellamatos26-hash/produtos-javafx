package br.unifan.produtos.service;

import br.unifan.produtos.model.Produto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Relatórios gerados com a Stream API (filter, map, reduce, sorted, groupingBy).
 * Cada método devolve o texto pronto para ser exibido na tela.
 */
public class RelatorioService {

    public static final int DIAS_VENCIMENTO = 60;
    public static final int LIMITE_ESTOQUE_BAIXO = 10;

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ---------- 1. Próximos ao vencimento (filter + sorted + map) ----------

    public List<Produto> proximosAoVencimento(List<Produto> produtos, LocalDate hoje) {
        LocalDate limite = hoje.plusDays(DIAS_VENCIMENTO);
        return produtos.stream()
                .filter(p -> !p.getDataValidade().isBefore(hoje))   // ainda não venceu
                .filter(p -> !p.getDataValidade().isAfter(limite))  // vence em até 60 dias
                .sorted(Comparator.comparing(Produto::getDataValidade))
                .toList();
    }

    public String relatorioVencimento(List<Produto> produtos, LocalDate hoje) {
        List<String> linhas = proximosAoVencimento(produtos, hoje).stream()
                .map(p -> String.format("%-8s  %-28s  vence em %s (%d dias)",
                        p.getCodigo(), p.getNome(), p.getDataValidade().format(DATA_BR),
                        ChronoUnit.DAYS.between(hoje, p.getDataValidade())))
                .toList();
        return montar("PRODUTOS PRÓXIMOS AO VENCIMENTO (próximos " + DIAS_VENCIMENTO + " dias)", linhas);
    }

    // ---------- 2. Estoque baixo (filter + sorted + map) ----------

    public List<Produto> estoqueBaixo(List<Produto> produtos) {
        return produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() < LIMITE_ESTOQUE_BAIXO)
                .sorted(Comparator.comparingInt(Produto::getQuantidadeEstoque))
                .toList();
    }

    public String relatorioEstoqueBaixo(List<Produto> produtos) {
        List<String> linhas = estoqueBaixo(produtos).stream()
                .map(p -> String.format("%-8s  %-28s  %3d unidade(s)",
                        p.getCodigo(), p.getNome(), p.getQuantidadeEstoque()))
                .toList();
        return montar("PRODUTOS COM ESTOQUE BAIXO (menos de " + LIMITE_ESTOQUE_BAIXO + " unidades)", linhas);
    }

    // ---------- 3. Margem de lucro média por categoria (groupingBy + map + reduce) ----------

    public Map<String, BigDecimal> margemMediaPorCategoria(List<Produto> produtos) {
        Map<String, List<Produto>> porCategoria = produtos.stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().getNome(), TreeMap::new, Collectors.toList()));

        Map<String, BigDecimal> resultado = new TreeMap<>();
        porCategoria.forEach((categoria, lista) -> {
            BigDecimal soma = lista.stream()
                    .map(Produto::getMargemLucro)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            resultado.put(categoria, soma.divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP));
        });
        return resultado;
    }

    public String relatorioMargemPorCategoria(List<Produto> produtos) {
        List<String> linhas = margemMediaPorCategoria(produtos).entrySet().stream()
                .map(e -> String.format("%-20s  margem média: %8s%%", e.getKey(), e.getValue().toPlainString()))
                .toList();
        return montar("MARGEM DE LUCRO MÉDIA POR CATEGORIA", linhas);
    }

    // ---------- 4. Produtos agrupados por setor (groupingBy + mapping) ----------

    public Map<String, List<String>> produtosPorSetor(List<Produto> produtos) {
        return produtos.stream()
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria().getSetor(),
                        TreeMap::new,
                        Collectors.mapping(p -> p.getCodigo() + " - " + p.getNome(), Collectors.toList())));
    }

    public String relatorioPorSetor(List<Produto> produtos) {
        List<String> linhas = produtosPorSetor(produtos).entrySet().stream()
                .map(e -> "[" + e.getKey() + "] (" + e.getValue().size() + " produto(s))\n"
                        + e.getValue().stream().map(n -> "    " + n).collect(Collectors.joining("\n")))
                .toList();
        return montar("PRODUTOS AGRUPADOS POR SETOR", linhas);
    }

    // ---------- 5. Resumo geral do estoque (map + reduce) ----------

    public String relatorioResumo(List<Produto> produtos) {
        int totalUnidades = produtos.stream()
                .map(Produto::getQuantidadeEstoque)
                .reduce(0, Integer::sum);

        BigDecimal valorCusto = produtos.stream()
                .map(p -> p.getPrecoCompra().multiply(BigDecimal.valueOf(p.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorVenda = produtos.stream()
                .map(p -> p.getPrecoVenda().multiply(BigDecimal.valueOf(p.getQuantidadeEstoque())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> linhas = List.of(
                "Produtos cadastrados:        " + produtos.size(),
                "Unidades em estoque:         " + totalUnidades,
                "Valor do estoque (custo):    R$ " + valorCusto.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "Valor do estoque (venda):    R$ " + valorVenda.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "Lucro potencial:             R$ " + valorVenda.subtract(valorCusto).setScale(2, RoundingMode.HALF_UP).toPlainString());
        return montar("RESUMO GERAL DO ESTOQUE", linhas);
    }

    private String montar(String titulo, List<String> linhas) {
        String corpo = linhas.isEmpty() ? "Nenhum produto encontrado." : String.join("\n", linhas);
        return titulo + "\n" + "=".repeat(titulo.length()) + "\n\n" + corpo + "\n";
    }
}
