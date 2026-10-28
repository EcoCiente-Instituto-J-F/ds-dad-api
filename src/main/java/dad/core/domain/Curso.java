package dad.core.domain;

import java.util.List;

public record Curso(
    Integer id,
    String titulo,
    String descricao,
    boolean ativo,
    List<Aula> aulas
) {
}