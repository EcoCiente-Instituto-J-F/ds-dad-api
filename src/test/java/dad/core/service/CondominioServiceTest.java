package dad.core.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dad.core.domain.Condominio;
import dad.core.domain.Torre;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CondominioGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CondominioServiceTest {

  @Mock
  private CondominioGateway gateway;

  @InjectMocks
  private CondominioService service;

  @Test
  void deveBloquearTorresRepetidasIgnorandoCaixaEEspacos() {
    var condominio = condominio(
        1,
        List.of(
            new Torre(null, "Torre A"),
            new Torre(null, " torre a ")));

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(condominio));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveBloquearTorreSemNome() {
    var condominio = condominio(
        1,
        List.of(
            new Torre(null, " ")));

    assertThrows(
        RegraNegocioException.class,
        () -> service.atualizar(condominio));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveBloquearTipoAusente() {
    var condominio = condominio(
        null,
        List.of());

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(condominio));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveBloquearListaDeTorresNula() {
    var condominio = condominio(1, null);

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(condominio));

    verifyNoInteractions(gateway);
  }

  @Test
  void devePermitirCondominioSemTorresEEndereco() {
    var condominio = condominio(
        1,
        List.of());

    when(gateway.criar(condominio))
        .thenReturn(condominio);

    assertEquals(
        condominio,
        service.criar(condominio));

    verify(gateway).criar(condominio);
  }

  private Condominio condominio(
      Integer tipoId,
      List<Torre> torres) {
    return new Condominio(
        null,
        "Condomínio",
        null,
        null,
        true,
        tipoId,
        null,
        null,
        torres);
  }
}