package dad.core.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dad.core.domain.Coleta;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.ColetaGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColetaServiceTest {

  @Mock
  private ColetaGateway gateway;

  @InjectMocks
  private ColetaService service;

  private final OffsetDateTime inicio = OffsetDateTime.parse(
      "2026-10-01T08:00:00-03:00");

  @Test
  void deveBloquearPeriodoInvertido() {
    var dados = dados(
        false,
        List.of(),
        inicio.minusHours(1));

    assertDadosInvalidos(dados);
  }

  @Test
  void deveExigirDiasQuandoExisteRecorrencia() {
    assertDadosInvalidos(
        dados(
            true,
            List.of(),
            inicio.plusDays(10)));
  }

  @Test
  void deveBloquearDiasSemRecorrencia() {
    assertDadosInvalidos(
        dados(
            false,
            List.of(1),
            inicio.plusHours(1)));
  }

  @Test
  void deveBloquearDiasRepetidos() {
    assertDadosInvalidos(
        dados(
            true,
            List.of(1, 1),
            inicio.plusDays(10)));
  }

  @Test
  void deveBloquearDiaNaoPositivo() {
    assertDadosInvalidos(
        dados(
            true,
            List.of(0),
            inicio.plusDays(10)));
  }

  @Test
  void devePermitirAgendamentoSemDataFinal() {
    var dados = dados(
        false,
        List.of(),
        null);

    service.salvar(null, dados);

    verify(gateway).salvar(null, dados);
  }

  @Test
  void deveBloquearConfirmacaoSemRealizacao() {
    var registro = new Coleta.RegistroVisita(
        inicio,
        false,
        true,
        "Não ocorreu");

    assertThrows(
        RegraNegocioException.class,
        () -> service.registrarVisita(1, registro));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveExigirObservacaoQuandoNaoRealizada() {
    var registro = new Coleta.RegistroVisita(
        inicio,
        false,
        false,
        " ");

    assertThrows(
        RegraNegocioException.class,
        () -> service.registrarVisita(1, registro));

    verifyNoInteractions(gateway);
  }

  @Test
  void devePermitirVisitaRealizadaSemObservacao() {
    var registro = new Coleta.RegistroVisita(
        inicio,
        true,
        false,
        null);

    service.registrarVisita(1, registro);

    verify(gateway).registrarVisita(1, registro);
  }

  @Test
  void devePermitirVisitaNaoRealizadaComObservacao() {
    var registro = new Coleta.RegistroVisita(
        inicio,
        false,
        false,
        "Cooperativa não compareceu");

    service.registrarVisita(1, registro);

    verify(gateway).registrarVisita(1, registro);
  }

  private void assertDadosInvalidos(
      Coleta.Dados dados) {
    assertThrows(
        RegraNegocioException.class,
        () -> service.salvar(null, dados));

    verifyNoInteractions(gateway);
  }

  private Coleta.Dados dados(
      boolean recorrente,
      List<Integer> dias,
      OffsetDateTime fim) {
    return new Coleta.Dados(
        1,
        2,
        3,
        inicio,
        fim,
        recorrente,
        dias);
  }
}