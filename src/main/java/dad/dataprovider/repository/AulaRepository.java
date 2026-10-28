package dad.dataprovider.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.AulaEntity;

public interface AulaRepository
    extends JpaRepository<AulaEntity, Integer> {

  List<AulaEntity> findByCursoIdOrderByOrdemAsc(
      Integer cursoId);

  @Query(value = """
      SELECT COUNT(*)
      FROM tb_quizzes
      WHERE aula_id = :aulaId
      """, nativeQuery = true)
  long contarQuizzes(
      @Param("aulaId") Integer aulaId);

  @Query(value = """
      SELECT COUNT(*)
      FROM tb_rel_usuarios_cursos
      WHERE aula_id = :aulaId
      """, nativeQuery = true)
  long contarProgressos(
      @Param("aulaId") Integer aulaId);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT a
      FROM AulaEntity a
      WHERE a.id = :id
      """)
  java.util.Optional<AulaEntity> buscarComBloqueio(
      @Param("id") Integer id);
}