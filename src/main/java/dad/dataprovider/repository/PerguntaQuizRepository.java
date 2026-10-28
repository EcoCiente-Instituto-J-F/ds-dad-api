package dad.dataprovider.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.PerguntaEntity;

public interface PerguntaQuizRepository
    extends JpaRepository<PerguntaEntity, Integer> {

  List<PerguntaEntity> findByQuizIdOrderByOrdemAsc(
      Integer quizId);

  @Modifying
  @Query(value = """
      DELETE FROM tb_perguntas_quiz
      WHERE quiz_id = :quizId
      """, nativeQuery = true)
  int excluirPorQuiz(
      @Param("quizId") Integer quizId);
}