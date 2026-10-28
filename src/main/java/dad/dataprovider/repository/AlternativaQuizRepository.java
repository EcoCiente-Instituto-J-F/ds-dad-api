package dad.dataprovider.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.AlternativaEntity;

public interface AlternativaQuizRepository
    extends JpaRepository<AlternativaEntity, Integer> {

  List<AlternativaEntity> findByPerguntaIdOrderByIdAsc(
      Integer perguntaId);

  @Modifying
  @Query(value = """
      DELETE FROM tb_alternativas_quiz
      WHERE pergunta_id IN (
          SELECT id_pergunta
          FROM tb_perguntas_quiz
          WHERE quiz_id = :quizId
      )
      """, nativeQuery = true)
  int excluirPorQuiz(
      @Param("quizId") Integer quizId);
}