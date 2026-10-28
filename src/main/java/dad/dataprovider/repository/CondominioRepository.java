package dad.dataprovider.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.CondominioEntity;
import jakarta.persistence.LockModeType;

public interface CondominioRepository
    extends JpaRepository<CondominioEntity, Integer> {

  @Query("""
      SELECT c
      FROM CondominioEntity c
      WHERE (
          LOWER(c.nome) LIKE
              LOWER(CONCAT('%', :busca, '%'))
          OR LOWER(COALESCE(c.cnpj, '')) LIKE
              LOWER(CONCAT('%', :busca, '%'))
      )
      AND (:ativo IS NULL OR c.ativo = :ativo)
      """)
  Page<CondominioEntity> buscar(
      @Param("busca") String busca,
      @Param("ativo") Boolean ativo,
      Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT c
      FROM CondominioEntity c
      WHERE c.id = :id
      """)
  Optional<CondominioEntity> buscarComBloqueio(
      @Param("id") Integer id);

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM tb_lkp_tipos_condominios
          WHERE id_tipo_condominio = :tipoId
      )
      """, nativeQuery = true)
  boolean existeTipo(
      @Param("tipoId") Integer tipoId);

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM tb_sindicos s
          JOIN tb_usuarios u
              ON u.id_usuario = s.usuario_id
          WHERE s.id_sindico = :sindicoId
            AND u.ativo = TRUE
      )
      """, nativeQuery = true)
  boolean existeSindicoAtivo(
      @Param("sindicoId") Integer sindicoId);

  @Query(value = """
      SELECT EXISTS (
          SELECT 1
          FROM tb_condominios
          WHERE REGEXP_REPLACE(
              cnpj,
              '[^0-9]',
              '',
              'g'
          ) = :cnpj
          AND id_condominio <> :ignorarId
      )
      """, nativeQuery = true)
  boolean existeCnpj(
      @Param("cnpj") String cnpj,
      @Param("ignorarId") Integer ignorarId);

  @Query("""
      SELECT COUNT(c) > 0
      FROM CondominioEntity c
      WHERE UPPER(TRIM(c.codigoAcesso)) = :codigo
        AND c.id <> :ignorarId
      """)
  boolean existeCodigo(
      @Param("codigo") String codigo,
      @Param("ignorarId") Integer ignorarId);

  @Query(value = """
      SELECT 1
      FROM pg_advisory_xact_lock(23871, 1)
      """, nativeQuery = true)
  Integer bloquearEscritas();
}