package dad.dataprovider.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.PontoEntity;

public interface PontoColetaRepository
    extends JpaRepository<PontoEntity, Integer> {

  List<PontoEntity> findByCooperativaIdOrderByNomeAsc(
      Integer cooperativaId);

  @Query(value = """
      SELECT categoria_residuo_id
      FROM tb_rel_pontos_coletas_categorias
      WHERE ponto_coleta_id = :pontoId
      ORDER BY categoria_residuo_id
      """, nativeQuery = true)
  List<Integer> buscarCategorias(
      @Param("pontoId") Integer pontoId);

  @Modifying
  @Query(value = """
      DELETE FROM tb_rel_pontos_coletas_categorias
      WHERE ponto_coleta_id = :pontoId
      """, nativeQuery = true)
  int excluirCategorias(
      @Param("pontoId") Integer pontoId);

  @Modifying
  @Query(value = """
      INSERT INTO tb_rel_pontos_coletas_categorias (
          ponto_coleta_id,
          categoria_residuo_id
      )
      VALUES (:pontoId, :categoriaId)
      """, nativeQuery = true)
  int inserirCategoria(
      @Param("pontoId") Integer pontoId,
      @Param("categoriaId") Integer categoriaId);
}