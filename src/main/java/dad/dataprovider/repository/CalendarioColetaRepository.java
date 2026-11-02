package dad.dataprovider.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CalendarioColetaRepository {
  private final EntityManager entityManager;

  public CalendarioColetaRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  private static final String BASE =
      """
        WITH parametros AS (
            SELECT CAST(:inicio AS timestamptz) AS inicio, CAST(:fim AS timestamptz) AS fim
        ), candidatos AS (
            SELECT a.* FROM tb_agendamentos_coletas a CROSS JOIN parametros p
            WHERE (CAST(:condominioId AS integer) IS NULL OR a.condominio_id = :condominioId)
              AND (CAST(:cooperativaId AS integer) IS NULL OR a.cooperativa_id = :cooperativaId)
              AND (CAST(:statusId AS integer) IS NULL OR a.status_agendamento_id = :statusId)
              AND a.data_inicio < p.fim
              AND (a.possui_recorrencia = FALSE OR a.data_fim IS NULL
                   OR (a.data_fim AT TIME ZONE 'America/Sao_Paulo')::date
                      >= (p.inicio AT TIME ZONE 'America/Sao_Paulo')::date)
        ), ocorrencias AS (
            SELECT a.id_agendamento_coleta AS id, a.data_inicio AS ocorrencia_em, NULL::timestamptz AS limite, a.data_inicio AS primeiro_instante
            FROM candidatos a WHERE a.possui_recorrencia = FALSE
            UNION ALL
            SELECT a.id_agendamento_coleta,
                   (d.dia::date + (a.data_inicio AT TIME ZONE 'America/Sao_Paulo')::time)
                       AT TIME ZONE 'America/Sao_Paulo', a.data_fim, a.data_inicio
            FROM candidatos a CROSS JOIN parametros p
            CROSS JOIN LATERAL generate_series(
                GREATEST((a.data_inicio AT TIME ZONE 'America/Sao_Paulo')::date,
                         (p.inicio AT TIME ZONE 'America/Sao_Paulo')::date)::timestamp,
                LEAST(COALESCE((a.data_fim AT TIME ZONE 'America/Sao_Paulo')::date,
                               (p.fim AT TIME ZONE 'America/Sao_Paulo')::date),
                      (p.fim AT TIME ZONE 'America/Sao_Paulo')::date)::timestamp,
                interval '1 day') AS d(dia)
            WHERE a.possui_recorrencia = TRUE
              AND EXISTS (
                SELECT 1 FROM tb_rel_recorrencias_agendamentos r
                JOIN tb_lkp_dias_semanas s ON s.id_dia_semana = r.dia_semana_id
                WHERE r.agendamento_coleta_id = a.id_agendamento_coleta
                  AND EXTRACT(ISODOW FROM d.dia) = CASE
                    split_part(replace(translate(lower(trim(s.nome_dia)),
                       'áàâãéêíóôõúç', 'aaaaeeiooouc'), '_', '-'), '-', 1)
                    WHEN 'segunda' THEN 1 WHEN 'terca' THEN 2 WHEN 'quarta' THEN 3
                    WHEN 'quinta' THEN 4 WHEN 'sexta' THEN 5 WHEN 'sabado' THEN 6
                    WHEN 'domingo' THEN 7 END
              )
        ), filtradas AS (
            SELECT o.* FROM ocorrencias o CROSS JOIN parametros p
            WHERE o.ocorrencia_em >= p.inicio AND o.ocorrencia_em < p.fim
              AND o.ocorrencia_em >= o.primeiro_instante
              AND (o.limite IS NULL OR o.ocorrencia_em <= o.limite)
        )
        """;

  public long contar(
      OffsetDateTime inicio,
      OffsetDateTime fim,
      Integer condominioId,
      Integer cooperativaId,
      Integer statusId) {
    return ((Number)
            consulta(
                    "SELECT COUNT(*) FROM filtradas",
                    inicio,
                    fim,
                    condominioId,
                    cooperativaId,
                    statusId)
                .getSingleResult())
        .longValue();
  }

  @SuppressWarnings("unchecked")
  public List<Object[]> listar(
      OffsetDateTime inicio,
      OffsetDateTime fim,
      Integer condominioId,
      Integer cooperativaId,
      Integer statusId,
      int pagina,
      int tamanho) {
    return consulta(
            "SELECT id, ocorrencia_em FROM filtradas ORDER BY ocorrencia_em, id LIMIT :limite OFFSET :deslocamento",
            inicio,
            fim,
            condominioId,
            cooperativaId,
            statusId)
        .setParameter("limite", tamanho)
        .setParameter("deslocamento", (long) pagina * tamanho)
        .getResultList();
  }

  private Query consulta(
      String finalSql,
      OffsetDateTime inicio,
      OffsetDateTime fim,
      Integer condominioId,
      Integer cooperativaId,
      Integer statusId) {
    return entityManager
        .createNativeQuery(BASE + finalSql)
        .setParameter("inicio", inicio)
        .setParameter("fim", fim)
        .setParameter("condominioId", condominioId)
        .setParameter("cooperativaId", cooperativaId)
        .setParameter("statusId", statusId);
  }
}
