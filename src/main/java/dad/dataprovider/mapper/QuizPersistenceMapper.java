package dad.dataprovider.mapper;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import dad.core.domain.AlternativaQuiz;
import dad.core.domain.PerguntaQuiz;
import dad.core.domain.Quiz;
import dad.dataprovider.entity.AlternativaEntity;
import dad.dataprovider.entity.PerguntaEntity;
import dad.dataprovider.entity.QuizEntity;

@Component
public class QuizPersistenceMapper {

  public AlternativaQuiz toDomain(
      AlternativaEntity entity) {
    return new AlternativaQuiz(
        entity.id,
        entity.perguntaId,
        entity.texto,
        entity.correta);
  }

  public PerguntaQuiz toDomain(
      PerguntaEntity entity,
      List<AlternativaEntity> alternativas) {
    return new PerguntaQuiz(
        entity.id,
        entity.quizId,
        entity.enunciado,
        entity.ordem,
        alternativas.stream()
            .map(this::toDomain)
            .toList());
  }

  public Quiz toDomain(
      QuizEntity entity,
      List<PerguntaQuiz> perguntas) {
    return new Quiz(
        entity.id,
        entity.cursoId,
        entity.aulaId,
        entity.titulo,
        entity.notaMinima,
        entity.pontos,
        entity.ativo,
        entity.criadoEm,
        perguntas);
  }

  public QuizEntity toNovaEntity(Quiz quiz) {
    var entity = new QuizEntity();

    atualizar(entity, quiz);
    entity.criadoEm = OffsetDateTime.now();

    return entity;
  }

  public void atualizar(
      QuizEntity entity,
      Quiz quiz) {
    entity.cursoId = quiz.cursoId();
    entity.aulaId = quiz.aulaId();
    entity.titulo = quiz.titulo();
    entity.notaMinima = quiz.notaMinima();
    entity.pontos = quiz.pontos();
    entity.ativo = quiz.ativo();
  }

  public PerguntaEntity toNovaEntity(
      PerguntaQuiz pergunta,
      Integer quizId,
      int ordem) {
    var entity = new PerguntaEntity();

    entity.quizId = quizId;
    entity.enunciado = pergunta.enunciado();
    entity.ordem = ordem;

    return entity;
  }

  public AlternativaEntity toNovaEntity(
      AlternativaQuiz alternativa,
      Integer perguntaId) {
    var entity = new AlternativaEntity();

    entity.perguntaId = perguntaId;
    entity.texto = alternativa.texto();
    entity.correta = alternativa.correta();

    return entity;
  }
}