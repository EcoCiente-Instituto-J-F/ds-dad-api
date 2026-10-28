package dad.core.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Pagina;
import dad.core.domain.Quiz;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.QuizGateway;

@Service
@Transactional
public class QuizService {

  private final QuizGateway gateway;

  public QuizService(QuizGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Quiz> listar(
      String busca,
      int pagina,
      int tamanho) {
    if (pagina < 0 || tamanho < 1 || tamanho > 100) {
      throw new RegraNegocioException(
          "Página deve ser maior ou igual a zero "
              + "e tamanho deve estar entre 1 e 100");
    }

    return gateway.listar(
        busca,
        pagina,
        tamanho);
  }

  @Transactional(readOnly = true)
  public Quiz buscar(Integer id) {
    return gateway.buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Quiz não encontrado"));
  }

  public Quiz criar(Quiz quiz) {
    validar(quiz);

    return gateway.criar(quiz);
  }

  public Quiz atualizar(Quiz quiz) {
    validar(quiz);

    return gateway.atualizar(quiz);
  }

  public void excluir(Integer id) {
    gateway.excluir(id);
  }

  private void validar(Quiz quiz) {
    if (quiz.titulo() == null
        || quiz.titulo().isBlank()) {
      throw new RegraNegocioException(
          "Informe o título do quiz");
    }

    if (quiz.cursoId() == null
        || quiz.aulaId() == null) {
      throw new RegraNegocioException(
          "Informe o curso e a aula");
    }

    if (quiz.pontos() == null || quiz.pontos() <= 0) {
      throw new RegraNegocioException(
          "Os pontos devem ser maiores que zero");
    }

    if (quiz.notaMinima() == null
        || quiz.notaMinima().compareTo(
            BigDecimal.ZERO) < 0
        || quiz.notaMinima().compareTo(
            new BigDecimal("100")) > 0) {
      throw new RegraNegocioException(
          "A nota mínima deve estar entre 0 e 100");
    }

    if (quiz.perguntas() == null
        || quiz.perguntas().isEmpty()) {
      throw new RegraNegocioException(
          "Informe pelo menos uma pergunta");
    }

    for (var pergunta : quiz.perguntas()) {
      if (pergunta == null
          || pergunta.enunciado() == null
          || pergunta.enunciado().isBlank()) {
        throw new RegraNegocioException(
            "Informe o enunciado de cada pergunta");
      }

      if (pergunta.alternativas() == null
          || pergunta.alternativas().size() < 2) {
        throw new RegraNegocioException(
            "Cada pergunta precisa de pelo menos duas alternativas");
      }

      long corretas = 0;

      for (var alternativa : pergunta.alternativas()) {
        if (alternativa == null
            || alternativa.texto() == null
            || alternativa.texto().isBlank()) {
          throw new RegraNegocioException(
              "Informe o texto de cada alternativa");
        }

        if (alternativa.correta()) {
          corretas++;
        }
      }

      if (corretas != 1) {
        throw new RegraNegocioException(
            "Cada pergunta precisa de uma única alternativa correta");
      }
    }
  }
}