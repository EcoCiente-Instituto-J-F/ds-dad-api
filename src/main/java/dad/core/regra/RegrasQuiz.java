package dad.core.regra;

import dad.core.domain.Quiz;
import dad.core.exception.RegraNegocioException;
import java.math.BigDecimal;
import java.util.Objects;

public final class RegrasQuiz {
  private RegrasQuiz() {}

  public static void validar(Quiz quiz) {
    if (quiz.titulo() == null || quiz.titulo().isBlank()) {
      throw new RegraNegocioException("Informe o título do quiz");
    }

    if (quiz.cursoId() == null || quiz.aulaId() == null) {
      throw new RegraNegocioException("Informe o curso e a aula");
    }

    if (quiz.pontos() == null || quiz.pontos() <= 0) {
      throw new RegraNegocioException("Os pontos devem ser maiores que zero");
    }

    if (quiz.notaMinima() == null
        || quiz.notaMinima().compareTo(BigDecimal.ZERO) < 0
        || quiz.notaMinima().compareTo(new BigDecimal("100")) > 0) {
      throw new RegraNegocioException("A nota mínima deve estar entre 0 e 100");
    }

    if (quiz.perguntas() == null || quiz.perguntas().isEmpty()) {
      throw new RegraNegocioException("Informe pelo menos uma pergunta");
    }

    for (var pergunta : quiz.perguntas()) {
      if (pergunta == null || pergunta.enunciado() == null || pergunta.enunciado().isBlank()) {
        throw new RegraNegocioException("Informe o enunciado de cada pergunta");
      }

      if (pergunta.alternativas() == null || pergunta.alternativas().size() < 2) {
        throw new RegraNegocioException("Cada pergunta precisa de pelo menos duas alternativas");
      }

      long corretas = 0;

      for (var alternativa : pergunta.alternativas()) {
        if (alternativa == null || alternativa.texto() == null || alternativa.texto().isBlank()) {
          throw new RegraNegocioException("Informe o texto de cada alternativa");
        }

        if (alternativa.correta()) {
          corretas++;
        }
      }

      if (corretas != 1) {
        throw new RegraNegocioException("Cada pergunta precisa de uma única alternativa correta");
      }
    }
  }

  private static boolean mesmaEstrutura(Quiz atual, Quiz recebido) {
    if (!Objects.equals(atual.cursoId(), recebido.cursoId())
        || !Objects.equals(atual.aulaId(), recebido.aulaId())
        || !Objects.equals(atual.pontos(), recebido.pontos())
        || atual.notaMinima() == null
        || recebido.notaMinima() == null
        || atual.notaMinima().compareTo(recebido.notaMinima()) != 0
        || atual.perguntas().size() != recebido.perguntas().size()) {
      return false;
    }

    for (int i = 0; i < atual.perguntas().size(); i++) {
      var primeira = atual.perguntas().get(i);
      var segunda = recebido.perguntas().get(i);

      if (!Objects.equals(primeira.enunciado(), segunda.enunciado())
          || primeira.alternativas().size() != segunda.alternativas().size()) {
        return false;
      }

      for (int j = 0; j < primeira.alternativas().size(); j++) {
        var alternativaAtual = primeira.alternativas().get(j);

        var alternativaRecebida = segunda.alternativas().get(j);

        if (!Objects.equals(alternativaAtual.texto(), alternativaRecebida.texto())
            || alternativaAtual.correta() != alternativaRecebida.correta()) {
          return false;
        }
      }
    }

    return true;
  }

  public static void validarUnicidade(boolean existe, boolean atualizacao) {
    if (existe)
      throw new RegraNegocioException(
          atualizacao ? "A aula já possui outro quiz" : "A aula já possui um quiz");
  }

  public static void validarExclusao(boolean possuiTentativas) {
    if (possuiTentativas)
      throw new RegraNegocioException("Não é possível excluir um quiz com tentativas");
  }

  public static void validarAula(boolean pertence) {
    if (!pertence) throw new RegraNegocioException("A aula não pertence ao curso informado");
  }

  public static void validarEdicaoComTentativas(Quiz atual, Quiz recebido) {
    if (!mesmaEstrutura(atual, recebido))
      throw new RegraNegocioException(
          "O quiz possui tentativas: preserve curso, aula, perguntas, nota e pontos");
  }
}
