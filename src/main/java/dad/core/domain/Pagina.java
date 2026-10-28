package dad.core.domain;

import java.util.List;

public record Pagina<T>(
    List<T> itens,
    int pagina,
    int tamanho,
    long totalItens,
    int totalPaginas
) {
}