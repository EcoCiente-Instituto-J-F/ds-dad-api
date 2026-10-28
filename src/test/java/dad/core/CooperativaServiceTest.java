package dad.core;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dad.core.domain.Cooperativa;
import dad.core.domain.PontoColeta;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CooperativaGateway;
import dad.core.service.CooperativaService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CooperativaServiceTest {

  @Mock
  private CooperativaGateway gateway;

  @InjectMocks
  private CooperativaService service;

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {
      "123",
      "1122233300018",
      "112223330001811"
  })
  void deveBloquearFormatoInvalidoDoCnpj(String cnpj) {
    assertInvalida(
        cooperativa(
            cnpj,
            List.of(),
            List.of()));
  }

  @Test
  void deveBloquearCategoriasRepetidas() {
    assertInvalida(
        cooperativa(
            "11222333000181",
            List.of(1, 1),
            List.of()));
  }

  @Test
  void deveBloquearCategoriaNula() {
    assertInvalida(
        cooperativa(
            "11222333000181",
            Arrays.asList(1, null),
            List.of()));
  }

  @Test
  void deveBloquearCategoriaNaoPositiva() {
    assertInvalida(
        cooperativa(
            "11222333000181",
            List.of(0),
            List.of()));
  }

  @Test
  void deveValidarCategoriasDosPontosTambem() {
    var ponto = new PontoColeta(
        null,
        "Ponto",
        null,
        null,
        true,
        null,
        List.of(1, 1));

    assertInvalida(
        cooperativa(
            "11222333000181",
            List.of(1),
            List.of(ponto)));
  }

  @Test
  void deveBloquearPontoSemNome() {
    var ponto = new PontoColeta(
        null,
        " ",
        null,
        null,
        true,
        null,
        List.of());

    assertInvalida(
        cooperativa(
            "11222333000181",
            List.of(),
            List.of(ponto)));
  }

  @Test
  void devePermitirListasVaziasConformeBanco() {
    var cooperativa = cooperativa(
        "11222333000181",
        List.of(),
        List.of());

    when(gateway.criar(cooperativa))
        .thenReturn(cooperativa);

    assertEquals(
        cooperativa,
        service.criar(cooperativa));

    verify(gateway).criar(cooperativa);
  }

  private void assertInvalida(
      Cooperativa cooperativa) {
    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(cooperativa));

    verifyNoInteractions(gateway);
  }

  private Cooperativa cooperativa(
      String cnpj,
      List<Integer> categorias,
      List<PontoColeta> pontos) {
    return new Cooperativa(
        null,
        "Cooperativa",
        cnpj,
        null,
        null,
        null,
        null,
        null,
        categorias,
        pontos);
  }
}