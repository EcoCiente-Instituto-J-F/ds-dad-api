package dad.dataprovider.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.CooperativaEntity;
import jakarta.persistence.LockModeType;

public interface CooperativaRepository
    extends JpaRepository<CooperativaEntity, Integer> {

  @Query("""
      SELECT c
      FROM CooperativaEntity c
      WHERE LOWER(c.nome) LIKE
          LOWER(CONCAT('%', :busca, '%'))
      OR LOWER(c.cnpj) LIKE
          LOWER(CONCAT('%', :busca, '%'))
      OR LOWER(COALESCE(c.email, '')) LIKE
          LOWER(CONCAT('%', :busca, '%'))
      """)
  Page<CooperativaEntity> buscar(
      @Param("busca") String busca,
      Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT c
      FROM CooperativaEntity c
      WHERE c.id = :id
      """)
  Optional<CooperativaEntity> buscarComBloqueio(
      @Param("id") Integer id);

  @Query(value = """
      SELECT 1
      FROM pg_advisory_xact_lock(23871, 2)
      """, nativeQuery = true)
  Integer bloquearEscritas();

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM tb_cooperativas
          WHERE UPPER(
              REGEXP_REPLACE(
                  cnpj_cooperativa,
                  '[^A-Za-z0-9]',
                  '',
                  'g'
              )
          ) = :cnpj
          AND id_cooperativa <> :ignorarId
      )
      """, nativeQuery = true)
  boolean existeCnpj(
      @Param("cnpj") String cnpj,
      @Param("ignorarId") Integer ignorarId);

  boolean existsByUsuarioIdAndIdNot(
      Integer usuarioId,
      Integer id);

  @Query(value = """
      SELECT id_usuario
      FROM tb_usuarios
      WHERE id_usuario = :usuarioId
      FOR UPDATE
      """, nativeQuery = true)
  Optional<Integer> bloquearUsuario(
      @Param("usuarioId") Integer usuarioId);

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM tb_usuarios u
          JOIN tb_lkp_tipos_usuarios t
              ON t.id_tipo_usuario = u.tipo_usuario_id
          WHERE u.id_usuario = :usuarioId
            AND u.ativo = TRUE
            AND UPPER(TRIM(t.nome_tipo)) = 'COOPERATIVA'
      )
      """, nativeQuery = true)
  boolean usuarioEhCooperativaAtiva(
      @Param("usuarioId") Integer usuarioId);

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM tb_lkp_categorias_residuos
          WHERE id_categoria = :categoriaId
      )
      """, nativeQuery = true)
  boolean existeCategoria(
      @Param("categoriaId") Integer categoriaId);

  @Query(value = """
      SELECT categoria_residuo_id
      FROM tb_rel_cooperativas_categorias_materiais
      WHERE cooperativa_id = :cooperativaId
      ORDER BY categoria_residuo_id
      """, nativeQuery = true)
  List<Integer> buscarCategorias(
      @Param("cooperativaId") Integer cooperativaId);

  @Modifying
  @Query(value = """
      DELETE FROM tb_rel_cooperativas_categorias_materiais
      WHERE cooperativa_id = :cooperativaId
      """, nativeQuery = true)
  int excluirCategorias(
      @Param("cooperativaId") Integer cooperativaId);

  @Modifying
  @Query(value = """
      INSERT INTO tb_rel_cooperativas_categorias_materiais (
          cooperativa_id,
          categoria_residuo_id
      )
      VALUES (:cooperativaId, :categoriaId)
      """, nativeQuery = true)
  int inserirCategoria(
      @Param("cooperativaId") Integer cooperativaId,
      @Param("categoriaId") Integer categoriaId);
}