package dad.core.regra;

import dad.core.domain.Aula;
import dad.core.domain.Curso;
import dad.core.exception.RegraNegocioException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RegrasCurso {
  private RegrasCurso() {}

  public static void validar(Curso curso) {
    if (curso.titulo() == null || curso.titulo().isBlank()) {
      throw new RegraNegocioException("Informe o título do curso");
    }

    if (curso.descricao() == null || curso.descricao().isBlank()) {
      throw new RegraNegocioException("Informe a descrição do curso");
    }

    if (curso.aulas() == null) {
      throw new RegraNegocioException("Informe a lista de aulas");
    }

    for (var aula : curso.aulas()) {
      if (aula == null
          || aula.titulo() == null
          || aula.titulo().isBlank()
          || aula.conteudo() == null
          || aula.conteudo().isBlank()) {
        throw new RegraNegocioException("Cada aula precisa de título e conteúdo");
      }
    }
  }

  public static void validarListaAulas(List<Aula> aulas) {
    if (aulas == null) {
      throw new RegraNegocioException("Informe a lista de aulas");
    }

    if (aulas.stream().anyMatch(aula -> aula == null)) {
      throw new RegraNegocioException("A lista de aulas contém um item inválido");
    }
  }

  public static Set<Integer> validarIds(List<Aula> aulas, Set<Integer> existentes) {
    Set<Integer> recebidos = new HashSet<>();

    for (var aula : aulas) {
      if (aula.id() == null) {
        continue;
      }

      if (!recebidos.add(aula.id())) {
        throw new RegraNegocioException("A mesma aula foi informada mais de uma vez");
      }

      if (!existentes.contains(aula.id())) {
        throw new RegraNegocioException("Aula não pertence ao curso informado");
      }
    }

    return recebidos;
  }

  public static void validarCriacao(List<Aula> aulas) {
    if (aulas.stream().anyMatch(a -> a.id() != null))
      throw new RegraNegocioException("Aulas de um novo curso não devem possuir ID");
  }

  public static void validarRemocaoComQuiz(long quantidade) {
    if (quantidade > 0) throw new RegraNegocioException("Não é possível remover uma aula com quiz");
  }

  public static void validarRemocaoComProgresso(long quantidade) {
    if (quantidade > 0)
      throw new RegraNegocioException("Não é possível remover uma aula com progresso registrado");
  }
}
