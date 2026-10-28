package dad.core.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dad.core.exception.RegraNegocioException;
import dad.core.gateway.ApoioGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApoioServiceTest {

  @Mock
  private ApoioGateway gateway;

  @InjectMocks
  private ApoioService service;

  @ParameterizedTest
  @ValueSource(ints = { -1, 0, 25 })
  void deveBloquearPeriodoForaDosLimites(int meses) {
    assertThrows(
        RegraNegocioException.class,
        () -> service.buscarDashboard(meses));

    verifyNoInteractions(gateway);
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 24 })
  void deveAceitarLimitesDoPeriodo(int meses) {
    service.buscarDashboard(meses);

    verify(gateway).buscarDashboard(meses);
  }
}