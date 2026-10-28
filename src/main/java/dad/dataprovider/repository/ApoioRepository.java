package dad.dataprovider.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

@Repository
public class ApoioRepository {

  private final EntityManager entityManager;

  public ApoioRepository(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Object[] buscarTotais(
      OffsetDateTime inicioHoje,
      OffsetDateTime agora) {
    return (Object[]) entityManager
        .createNativeQuery("""
            SELECT
                (SELECT COUNT(*) FROM tb_usuarios),

                (
                    SELECT COUNT(*)
                    FROM tb_usuarios
                    WHERE ativo = TRUE
                ),

                (SELECT COUNT(*) FROM tb_condominios),

                (
                    SELECT COUNT(*)
                    FROM tb_condominios
                    WHERE ativo = TRUE
                ),

                (SELECT COUNT(*) FROM tb_cooperativas),

                (SELECT COUNT(*) FROM tb_cursos),

                (SELECT COUNT(*) FROM tb_quizzes),

                (
                    SELECT COUNT(*)
                    FROM tb_agendamentos_coletas
                ),

                (
                    SELECT COUNT(*)
                    FROM tb_usuarios
                    WHERE registro_em >= :inicioHoje
                      AND registro_em < :agora
                ),

                (
                    SELECT COUNT(*)
                    FROM tb_postagens
                    WHERE data_postagem >= :inicioHoje
                      AND data_postagem < :agora
                )
            """)
        .setParameter("inicioHoje", inicioHoje)
        .setParameter("agora", agora)
        .getSingleResult();
  }

  @SuppressWarnings("unchecked")
  public List<Object[]> buscarEvolucao(
      OffsetDateTime inicio,
      OffsetDateTime fim) {
    return entityManager
        .createNativeQuery("""
            SELECT
                mes,
                SUM(usuarios),
                SUM(cooperativas),
                SUM(postagens)
            FROM (
                SELECT
                    TO_CHAR(
                        registro_em AT TIME ZONE
                            'America/Sao_Paulo',
                        'YYYY-MM'
                    ) AS mes,
                    COUNT(*) AS usuarios,
                    0::BIGINT AS cooperativas,
                    0::BIGINT AS postagens
                FROM tb_usuarios
                WHERE registro_em >= :inicio
                  AND registro_em < :fim
                GROUP BY 1

                UNION ALL

                SELECT
                    TO_CHAR(
                        data_cadastro AT TIME ZONE
                            'America/Sao_Paulo',
                        'YYYY-MM'
                    ) AS mes,
                    0::BIGINT AS usuarios,
                    COUNT(*) AS cooperativas,
                    0::BIGINT AS postagens
                FROM tb_cooperativas
                WHERE data_cadastro >= :inicio
                  AND data_cadastro < :fim
                GROUP BY 1

                UNION ALL

                SELECT
                    TO_CHAR(
                        data_postagem AT TIME ZONE
                            'America/Sao_Paulo',
                        'YYYY-MM'
                    ) AS mes,
                    0::BIGINT AS usuarios,
                    0::BIGINT AS cooperativas,
                    COUNT(*) AS postagens
                FROM tb_postagens
                WHERE data_postagem >= :inicio
                  AND data_postagem < :fim
                GROUP BY 1
            ) dados
            GROUP BY mes
            ORDER BY mes
            """)
        .setParameter("inicio", inicio)
        .setParameter("fim", fim)
        .getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<Object[]> buscarUsuariosRecentes() {
    return entityManager
        .createNativeQuery("""
            SELECT
                u.id_usuario,
                u.nome_usuario,
                u.email_usuario,
                t.nome_tipo,
                u.ativo,
                u.registro_em
            FROM tb_usuarios u
            JOIN tb_lkp_tipos_usuarios t
                ON t.id_tipo_usuario = u.tipo_usuario_id
            ORDER BY
                u.registro_em DESC,
                u.id_usuario DESC
            LIMIT 5
            """)
        .getResultList();
  }

  public List<Object[]> buscarTiposUsuarios() {
    return consultar("""
        SELECT id_tipo_usuario, nome_tipo
        FROM tb_lkp_tipos_usuarios
        ORDER BY nome_tipo, id_tipo_usuario
        """);
  }

  public List<Object[]> buscarTiposCondominios() {
    return consultar("""
        SELECT id_tipo_condominio, nome_tipo
        FROM tb_lkp_tipos_condominios
        ORDER BY nome_tipo, id_tipo_condominio
        """);
  }

  public List<Object[]> buscarCategorias() {
    return consultar("""
        SELECT
            id_categoria,
            nome_categoria,
            permite_reciclagem,
            cor_identificacao,
            pontos_base,
            limite_pontos_diario
        FROM tb_lkp_categorias_residuos
        ORDER BY nome_categoria, id_categoria
        """);
  }

  public List<Object[]> buscarDiasSemana() {
    return consultar("""
        SELECT id_dia_semana, nome_dia
        FROM tb_lkp_dias_semanas
        ORDER BY id_dia_semana
        """);
  }

  public List<Object[]> buscarStatusAgendamentos() {
    return consultar("""
        SELECT id_status, nome_status
        FROM tb_lkp_status_agendamentos
        ORDER BY nome_status, id_status
        """);
  }

  public List<Object[]> buscarSindicos() {
    return consultar("""
        SELECT
            s.id_sindico,
            u.id_usuario,
            u.nome_usuario
        FROM tb_sindicos s
        JOIN tb_usuarios u
            ON u.id_usuario = s.usuario_id
        WHERE u.ativo = TRUE
        ORDER BY u.nome_usuario, s.id_sindico
        """);
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> consultar(String sql) {
    return entityManager
        .createNativeQuery(sql)
        .getResultList();
  }
}