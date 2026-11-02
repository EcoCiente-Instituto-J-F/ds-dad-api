package dad.core.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import dad.core.domain.Coleta;
import dad.core.exception.UsuarioNaoAutenticadoException;
import dad.core.gateway.ColetaGateway;
import org.junit.jupiter.api.Test;

class ColetaSolicitanteServiceTest {
  @Test
  void solicitanteInvalidoNaoChegaAoGateway() {
    var gateway = mock(ColetaGateway.class);
    var service = new ColetaService(gateway);
    for (Integer id : new Integer[] {null, 0, -1})
      assertThrows(
          UsuarioNaoAutenticadoException.class, () -> service.criar(new Coleta.Criacao(null, id)));
    assertThrows(UsuarioNaoAutenticadoException.class, () -> service.criar(null));
    assertThrows(UsuarioNaoAutenticadoException.class, () -> service.salvar(null, null));
    verifyNoInteractions(gateway);
  }
}
