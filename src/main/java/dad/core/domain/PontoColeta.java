package dad.core.domain;

import java.time.LocalTime;
import java.util.List;

public record PontoColeta(
    Integer id,
    String nome,
    LocalTime abertura,
    LocalTime fechamento,
    boolean ativo,
    Endereco endereco,
    List<Integer> categoriaIds) {
}