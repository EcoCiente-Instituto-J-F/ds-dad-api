package dad.core.domain;

public record Aula(
    Integer id,
    Integer cursoId,
    String titulo,
    String conteudo,
    Integer ordem
) {
}