package dad.dataprovider.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dad.dataprovider.entity.ColetaEntity;
import jakarta.persistence.LockModeType;

public interface ColetaRepository
    extends JpaRepository<ColetaEntity, Integer> {

  @Query("""
      SELECT a
      FROM ColetaEntity a
      JOIN CondominioEntity c ON c.id = a.condominioId
      JOIN CooperativaEntity p ON p.id = a.cooperativaId
      WHERE (
          LOWER(c.nome) LIKE
              LOWER(CONCAT('%', :busca, '%'))
          OR LOWER(p.nome) LIKE
              LOWER(CONCAT('%', :busca, '%'))
      )
      AND (:statusId IS NULL OR a.statusId = :statusId)
      AND (
          :inicio IS NULL
          OR a.dataFim IS NULL
          OR a.dataFim >= :inicio
      )
      AND (:fim IS NULL OR a.dataInicio <= :fim)
      """)
  Page<ColetaEntity> buscar(
      @Param("busca") String busca,
      @Param("statusId") Integer statusId,
      @Param("inicio") OffsetDateTime inicio,
      @Param("fim") OffsetDateTime fim,
      Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT a
      FROM ColetaEntity a
      WHERE a.id = :id
      """)
  Optional<ColetaEntity> buscarComBloqueio(
      @Param("id") Integer id);

  @Query(value = """
      SELECT nome_status
      FROM tb_lkp_status_agendamentos
      WHERE id_status = :statusId
      """, nativeQuery = true)
  Optional<String> buscarNomeStatus(
      @Param("statusId") Integer statusId);

  @Query(value = """
      SELECT id_status
      FROM tb_lkp_status_agendamentos
      WHERE UPPER(TRIM(nome_status))
          IN ('CANCELADO', 'CANCELADA')
      ORDER BY id_status
      LIMIT 1
      """, nativeQuery = true)
  Optional<Integer> buscarStatusCancelado();

  @Query(value = """
      SELECT nome_dia
      FROM tb_lkp_dias_semanas
      WHERE id_dia_semana = :diaId
      """, nativeQuery = true)
  Optional<String> buscarNomeDia(
      @Param("diaId") Integer diaId);

  @Query(value = """
      SELECT dia_semana_id
      FROM tb_rel_recorrencias_agendamentos
      WHERE agendamento_coleta_id = :coletaId
      ORDER BY dia_semana_id
      """, nativeQuery = true)
  List<Integer> buscarDias(
      @Param("coletaId") Integer coletaId);

  @Modifying
  @Query(value = """
      DELETE FROM tb_rel_recorrencias_agendamentos
      WHERE agendamento_coleta_id = :coletaId
      """, nativeQuery = true)
  int excluirDias(
      @Param("coletaId") Integer coletaId);

  @Modifying
  @Query(value = """
      INSERT INTO tb_rel_recorrencias_agendamentos (
          agendamento_coleta_id,
          dia_semana_id
      )
      VALUES (:coletaId, :diaId)
      """, nativeQuery = true)
  int inserirDia(
      @Param("coletaId") Integer coletaId,
      @Param("diaId") Integer diaId);
}