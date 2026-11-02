package dad.core.regra;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dad.core.domain.Coleta;
import dad.core.exception.RegraNegocioException;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasColetaTest {
  private final OffsetDateTime inicio = OffsetDateTime.parse("2026-10-05T12:00:00Z");

  private Coleta.Dados dados(OffsetDateTime data, List<Integer> dias) {
    return new Coleta.Dados(1, 2, 3, data, null, true, dias);
  }

  @Test
  void mesmaProgramacaoComOffsetsEquivalentesEOutraOrdemDosDias() {
    assertDoesNotThrow(
        () ->
            RegrasColeta.validarEdicaoComVisitas(
                dados(inicio, List.of(10, 20)),
                dados(
                    inicio.withOffsetSameInstant(java.time.ZoneOffset.ofHours(-3)),
                    List.of(20, 10))));
  }

  @Test
  void bloqueiaMudancaDaProgramacaoAposVisita() {
    assertThrows(
        RegraNegocioException.class,
        () ->
            RegrasColeta.validarEdicaoComVisitas(
                dados(inicio, List.of(10)), dados(inicio.plusDays(1), List.of(10))));
  }

  @Test
  void naoPermiteCancelarColetaConcluida() {
    assertThrows(RegraNegocioException.class, () -> RegrasColeta.validarCancelamento("Concluída"));
    assertDoesNotThrow(() -> RegrasColeta.validarCancelamento("Agendado"));
  }

  @Test
  void rejeitaVisitaEmAgendamentoCanceladoOuRecusado() {
    assertThrows(RegraNegocioException.class, () -> RegrasColeta.validarStatusVisita("Cancelado"));
    assertThrows(RegraNegocioException.class, () -> RegrasColeta.validarStatusVisita("Recusada"));
  }

  @Test
  void rejeitaVisitaFuturaComRelogioExplicito() {
    assertThrows(
        RegraNegocioException.class,
        () ->
            RegrasColeta.validarDataVisita(
                dados(inicio, List.of(10)), inicio.plusDays(2), inicio.plusDays(1)));
  }

  @Test
  void preservaIntervaloExatoDeVisitaNaoRecorrente() {
    var coleta = new Coleta.Dados(1, 2, 3, inicio, inicio.plusHours(1), false, List.of());
    assertDoesNotThrow(
        () -> RegrasColeta.validarDataVisita(coleta, inicio.plusMinutes(30), inicio.plusDays(1)));
    assertThrows(
        RegraNegocioException.class,
        () -> RegrasColeta.validarDataVisita(coleta, inicio.plusHours(2), inicio.plusDays(1)));
  }

  @Test
  void naoSobrescreveResultadoDeVisitaJaRegistrada() {
    assertThrows(
        RegraNegocioException.class,
        () -> RegrasColeta.validarVisitaSemResultado(true, false, null, null));
    assertThrows(
        RegraNegocioException.class,
        () -> RegrasColeta.validarVisitaSemResultado(false, false, null, "Ausente"));
    assertDoesNotThrow(() -> RegrasColeta.validarVisitaSemResultado(false, false, null, null));
  }

  @Test
  void impedeEscolhaAmbiguaEntreDuasVisitas() {
    assertThrows(RegraNegocioException.class, () -> RegrasColeta.validarQuantidadeVisitas(2));
  }
}
