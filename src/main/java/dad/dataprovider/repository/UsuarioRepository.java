package dad.dataprovider.repository;

import dad.dataprovider.entity.UsuarioEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

  @Query(
      """
      SELECT u
      FROM UsuarioEntity u
      WHERE (
          LOWER(u.nome) LIKE
              LOWER(CONCAT('%', :busca, '%'))
          OR LOWER(u.email) LIKE
              LOWER(CONCAT('%', :busca, '%'))
      )
      AND (:ativo IS NULL OR u.ativo = :ativo)
      AND (
          :tipoUsuarioId IS NULL
          OR u.tipoUsuarioId = :tipoUsuarioId
          OR (u.tipoUsuarioId IN :administradores AND :filtrarAdministradores = TRUE)
      )
      """)
  Page<UsuarioEntity> buscar(
      @Param("busca") String busca,
      @Param("ativo") Boolean ativo,
      @Param("tipoUsuarioId") Integer tipoUsuarioId,
      @Param("administradores") java.util.List<Integer> administradores,
      @Param("filtrarAdministradores") boolean filtrarAdministradores,
      Pageable pageable);

  default Page<UsuarioEntity> buscar(
      String busca, Boolean ativo, Integer tipoUsuarioId, Pageable pageable) {
    return buscar(busca, ativo, tipoUsuarioId, java.util.List.of(-1), false, pageable);
  }

  @Query(
      value =
          """
      SELECT id_tipo_usuario FROM tb_lkp_tipos_usuarios
      WHERE LOWER(TRIM(nome_tipo)) IN ('admin', 'administrador', 'administradora')
      ORDER BY id_tipo_usuario
      """,
      nativeQuery = true)
  java.util.List<Integer> buscarIdsAdministradores();

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      SELECT u
      FROM UsuarioEntity u
      WHERE u.id = :id
      """)
  Optional<UsuarioEntity> buscarComBloqueio(@Param("id") Integer id);

  @Query(
      """
      SELECT COUNT(u) > 0
      FROM UsuarioEntity u
      WHERE LOWER(TRIM(u.email)) = :email
        AND u.id <> :ignorarId
      """)
  boolean existeEmail(@Param("email") String email, @Param("ignorarId") Integer ignorarId);

  boolean existsByCpf(String cpf);

  @Query(
      value =
          """
      SELECT nome_tipo
      FROM tb_lkp_tipos_usuarios
      WHERE id_tipo_usuario = :tipoId
      """,
      nativeQuery = true)
  Optional<String> buscarNomeTipo(@Param("tipoId") Integer tipoId);

  @Query(
      value =
          """
      SELECT condominio_id
      FROM tb_moradores
      WHERE usuario_id = :usuarioId
      """,
      nativeQuery = true)
  Optional<Integer> buscarCondominioMorador(@Param("usuarioId") Integer usuarioId);

  @Query(
      value =
          """
      SELECT id_nivel_confianca
      FROM tb_lkp_niveis_confianca
      WHERE LOWER(TRIM(nome_nivel)) = 'morador_comum'
      ORDER BY id_nivel_confianca
      LIMIT 1
      """,
      nativeQuery = true)
  Optional<Integer> buscarNivelInicial();

  @Modifying
  @Query(
      value =
          """
      INSERT INTO tb_sindicos (usuario_id)
      VALUES (:usuarioId)
      """,
      nativeQuery = true)
  int inserirSindico(@Param("usuarioId") Integer usuarioId);

  @Modifying
  @Query(
      value =
          """
      INSERT INTO tb_moradores (
          usuario_id,
          condominio_id
      )
      VALUES (:usuarioId, :condominioId)
      """,
      nativeQuery = true)
  int inserirMorador(
      @Param("usuarioId") Integer usuarioId, @Param("condominioId") Integer condominioId);

  @Modifying
  @Query(
      value =
          """
      INSERT INTO tb_rel_usuarios_condominios (
          usuario_id,
          condominio_id,
          nivel_confianca_id,
          trust_score,
          postagens_validadas_sem_contestacao,
          denuncias_realizadas,
          denuncias_procedentes
      )
      VALUES (
          :usuarioId,
          :condominioId,
          :nivelId,
          0,
          0,
          0,
          0
      )
      """,
      nativeQuery = true)
  int inserirVinculoMorador(
      @Param("usuarioId") Integer usuarioId,
      @Param("condominioId") Integer condominioId,
      @Param("nivelId") Integer nivelId);

  @Query(
      value =
          """
      SELECT 1
      FROM pg_advisory_xact_lock(23871, 3)
      """,
      nativeQuery = true)
  Integer bloquearEscritas();
}
