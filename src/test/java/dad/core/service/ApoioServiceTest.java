package dad.core.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import dad.core.exception.RegraNegocioException;
import dad.core.gateway.ApoioGateway;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApoioServiceTest {

  @Mock private ApoioGateway gateway;

  @InjectMocks private ApoioService service;

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 25})
  void deveBloquearPeriodoForaDosLimites(int meses) {
    assertThrows(RegraNegocioException.class, () -> service.buscarDashboard(meses));

    verifyNoInteractions(gateway);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 24})
  void deveAceitarLimitesDoPeriodo(int meses) {
    service.buscarDashboard(meses);

    verify(gateway).buscarDashboard(meses);
  }
}
