package dad.dataprovider.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.TorreEntity;

public interface TorreRepository
    extends JpaRepository<TorreEntity, Integer> {

  List<TorreEntity> findByCondominioIdOrderByNomeAsc(
      Integer condominioId);

  @Query(value = """
      SELECT
          (
              SELECT COUNT(*)
              FROM tb_postagens
              WHERE torre_id = :torreId
          )
          +
          (
              SELECT COUNT(*)
              FROM tb_tentativas_quiz
              WHERE torre_id = :torreId
          )
          +
          (
              SELECT COUNT(*)
              FROM tb_movimentacoes_pontos
              WHERE torre_id = :torreId
          )
      """, nativeQuery = true)
  long contarReferencias(
      @Param("torreId") Integer torreId);
}