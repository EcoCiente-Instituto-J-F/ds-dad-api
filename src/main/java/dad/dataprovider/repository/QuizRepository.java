package dad.dataprovider.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.QuizEntity;
import jakarta.persistence.LockModeType;

public interface QuizRepository
    extends JpaRepository<QuizEntity, Integer> {

  @Query("""
      SELECT q
      FROM QuizEntity q
      WHERE LOWER(q.titulo) LIKE
          LOWER(CONCAT('%', :busca, '%'))
      """)
  Page<QuizEntity> buscar(
      @Param("busca") String busca,
      Pageable pageable);

  boolean existsByAulaId(Integer aulaId);

  boolean existsByAulaIdAndIdNot(
      Integer aulaId,
      Integer id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT q
      FROM QuizEntity q
      WHERE q.id = :id
      """)
  Optional<QuizEntity> buscarComBloqueio(
      @Param("id") Integer id);

  @Query(value = """
      SELECT COUNT(*)
      FROM tb_tentativas_quiz
      WHERE quiz_id = :quizId
      """, nativeQuery = true)
  long contarTentativas(
      @Param("quizId") Integer quizId);
}