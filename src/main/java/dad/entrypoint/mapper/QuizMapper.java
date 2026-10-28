package dad.entrypoint.mapper;

import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import dad.core.domain.AlternativaQuiz;
import dad.core.domain.Pagina;
import dad.core.domain.PerguntaQuiz;
import dad.core.domain.Quiz;
import dad.entrypoint.dto.request.QuizRequest;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.dto.response.QuizResponse;

@Component
public class QuizMapper {

  public Quiz toDomain(
      Integer id,
      QuizRequest request) {
    var perguntas = IntStream
        .range(0, request.perguntas().size())
        .mapToObj(indice -> {
          var pergunta = request.perguntas()
              .get(indice);

          var alternativas = pergunta.alternativas()
              .stream()
              .map(alternativa -> new AlternativaQuiz(
                  null,
                  null,
                  alternativa.texto().trim(),
                  alternativa.correta()))
              .toList();

          return new PerguntaQuiz(
              null,
              id,
              pergunta.enunciado().trim(),
              indice + 1,
              alternativas);
        })
        .toList();

    return new Quiz(
        id,
        request.cursoId(),
        request.aulaId(),
        request.titulo().trim(),
        request.notaMinima(),
        request.pontos(),
        request.ativo(),
        null,
        perguntas);
  }

  public QuizResponse toResponse(Quiz quiz) {
    return new QuizResponse(
        quiz.id(),
        quiz.cursoId(),
        quiz.aulaId(),
        quiz.titulo(),
        quiz.notaMinima(),
        quiz.pontos(),
        quiz.ativo(),
        quiz.criadoEm(),
        quiz.perguntas()
            .stream()
            .map(this::toResponse)
            .toList());
  }

  public PaginaResponse<QuizResponse> toResponse(
      Pagina<Quiz> pagina) {
    return new PaginaResponse<>(
        pagina.itens()
            .stream()
            .map(this::toResponse)
            .toList(),
        pagina.pagina(),
        pagina.tamanho(),
        pagina.totalItens(),
        pagina.totalPaginas());
  }

  private QuizResponse.PerguntaResponse toResponse(
      PerguntaQuiz pergunta) {
    return new QuizResponse.PerguntaResponse(
        pergunta.id(),
        pergunta.quizId(),
        pergunta.enunciado(),
        pergunta.ordem(),
        pergunta.alternativas()
            .stream()
            .map(this::toResponse)
            .toList());
  }

  private QuizResponse.AlternativaResponse toResponse(
      AlternativaQuiz alternativa) {
    return new QuizResponse.AlternativaResponse(
        alternativa.id(),
        alternativa.perguntaId(),
        alternativa.texto(),
        alternativa.correta());
  }
}