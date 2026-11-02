package dad.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dad.core.domain.Pagina;
import dad.core.exception.CamposInvalidosException;
import dad.core.gateway.CalendarioColetaGateway;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalendarioColetaServiceTest {
  private final CalendarioColetaGateway gateway = mock(CalendarioColetaGateway.class);
  private final CalendarioColetaService service = new CalendarioColetaService(gateway);

  @Test
  void rejeitaPeriodoInvertidoSemConsultarBanco() {
    var inicio = OffsetDateTime.parse("2026-11-02T12:00:00Z");
    var erro =
        assertThrows(
            CamposInvalidosException.class,
            () -> service.listar(inicio, inicio.minusDays(1), null, null, null, 0, 50));
    assertTrue(erro.getCampos().containsKey("fim"));
    verifyNoInteractions(gateway);
  }

  @Test
  void agregaFiltrosInvalidos() {
    var erro =
        assertThrows(
            CamposInvalidosException.class, () -> service.listar(null, null, 0, -1, -2, -1, 101));
    assertEquals(7, erro.getCampos().size());
    verifyNoInteractions(gateway);
  }

  @Test
  void encaminhaPeriodoEFiltrosSemPaginarAgendamentos() {
    var inicio = OffsetDateTime.parse("2026-11-01T00:00:00Z");
    var fim = inicio.plusMonths(1);
    when(gateway.listar(inicio, fim, 1, 2, 3, 4, 50))
        .thenReturn(new Pagina<>(List.of(), 4, 50, 0, 0));
    var resultado = service.listar(inicio, fim, 1, 2, 3, 4, 50);
    assertEquals(4, resultado.pagina());
    verify(gateway).listar(inicio, fim, 1, 2, 3, 4, 50);
  }
}
