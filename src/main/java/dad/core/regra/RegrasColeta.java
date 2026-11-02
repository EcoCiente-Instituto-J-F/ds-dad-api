package dad.core.regra;

import dad.core.domain.Coleta;
import dad.core.exception.RegraNegocioException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;

public final class RegrasColeta {
  private RegrasColeta() {}

  private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

  private static boolean mesmoInstante(OffsetDateTime primeiro, OffsetDateTime segundo) {
    if (primeiro == null || segundo == null) {
      return primeiro == segundo;
    }

    return primeiro.toInstant().equals(segundo.toInstant());
  }

  private static boolean mesmaProgramacao(Coleta.Dados atual, Coleta.Dados dados) {
    return Objects.equals(atual.condominioId(), dados.condominioId())
        && Objects.equals(atual.cooperativaId(), dados.cooperativaId())
        && mesmoInstante(atual.dataInicio(), dados.dataInicio())
        && mesmoInstante(atual.dataFim(), dados.dataFim())
        && atual.recorrente() == dados.recorrente()
        && new HashSet<>(atual.diasSemanaIds()).equals(new HashSet<>(dados.diasSemanaIds()));
  }

  public static void validarDataVisita(
      Coleta.Dados coleta, OffsetDateTime data, OffsetDateTime agora) {
    if (data.isAfter(agora)) {
      throw new RegraNegocioException("Não registre resultado de uma visita futura");
    }

    if (!coleta.recorrente()) {
      if (data.isBefore(coleta.dataInicio())
          || (coleta.dataFim() != null && data.isAfter(coleta.dataFim()))) {
        throw new RegraNegocioException("Visita fora do período do agendamento");
      }

      return;
    }

    var dia = data.atZoneSameInstant(ZONA).toLocalDate();

    var inicio = coleta.dataInicio().atZoneSameInstant(ZONA).toLocalDate();

    var fim =
        coleta.dataFim() == null ? null : coleta.dataFim().atZoneSameInstant(ZONA).toLocalDate();

    if (dia.isBefore(inicio) || (fim != null && dia.isAfter(fim))) {
      throw new RegraNegocioException("Visita fora do período da recorrência");
    }
  }

  public static void validarEdicaoComVisitas(Coleta.Dados atual, Coleta.Dados recebido) {
    if (!mesmaProgramacao(atual, recebido))
      throw new RegraNegocioException(
          "O agendamento possui visitas. Preserve participantes, datas e recorrência");
  }

  public static void validarCancelamento(String nomeStatus) {
    String status = DiasSemana.normalizar(nomeStatus);
    if (status.equals("REALIZADO")
        || status.equals("REALIZADA")
        || status.equals("CONCLUIDO")
        || status.equals("CONCLUIDA"))
      throw new RegraNegocioException("O agendamento já foi concluído");
  }

  public static void validarStatusVisita(String nomeStatus) {
    String status = DiasSemana.normalizar(nomeStatus);
    if (status.equals("CANCELADO")
        || status.equals("CANCELADA")
        || status.equals("RECUSADO")
        || status.equals("RECUSADA"))
      throw new RegraNegocioException("O status do agendamento não permite registrar visita");
  }

  public static void validarDiaPermitido(boolean permitido) {
    if (!permitido)
      throw new RegraNegocioException("A visita não corresponde a um dia da recorrência");
  }

  public static void validarQuantidadeVisitas(int quantidade) {
    if (quantidade > 1)
      throw new RegraNegocioException(
          "Existem várias visitas para esse dia. É necessário identificar a visita antes de registrar");
  }

  public static void validarVisitaSemResultado(
      boolean realizada, boolean confirmada, OffsetDateTime confirmadoEm, String observacao) {
    if (realizada
        || confirmada
        || confirmadoEm != null
        || (observacao != null && !observacao.isBlank()))
      throw new RegraNegocioException("A visita desse dia já possui resultado ou observação");
  }

  public static void validarCondominioAtivo(boolean ativo) {
    if (!ativo) throw new RegraNegocioException("Condomínio inativo");
  }
}
