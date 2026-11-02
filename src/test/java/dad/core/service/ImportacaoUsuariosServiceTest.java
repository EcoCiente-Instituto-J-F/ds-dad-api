package dad.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dad.core.domain.CriarUsuario;
import dad.core.exception.CamposInvalidosException;
import dad.core.gateway.ConsultaCadastroUsuariosGateway;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportacaoUsuariosServiceTest {
  private final UsuarioService usuarios = mock(UsuarioService.class);
  private final ConsultaCadastroUsuariosGateway validacao =
      mock(ConsultaCadastroUsuariosGateway.class);
  private final ImportacaoUsuariosService service =
      new ImportacaoUsuariosService(usuarios, validacao);

  private CriarUsuario usuario(String email, String cpf) {
    return new CriarUsuario("Pessoa", email, "Teste@123", 1, null, cpf, null, null, null);
  }

  @Test
  void rejeitaLoteVazioEAcimaDoLimite() {
    assertThrows(CamposInvalidosException.class, () -> service.importar(List.of()));
    assertThrows(
        CamposInvalidosException.class,
        () -> service.importar(java.util.Collections.nCopies(101, usuario("a@teste.com", null))));
    verifyNoInteractions(usuarios, validacao);
  }

  @Test
  void informaIndicesDuplicadosESemPersistir() {
    var lote =
        List.of(usuario(" A@TESTE.COM ", "11111111111"), usuario("a@teste.com", "11111111111"));
    when(validacao.consultar(lote))
        .thenReturn(
            lote.stream()
                .map(
                    u ->
                        new dad.core.domain.ReferenciasCadastroUsuario(
                            false, false, "Comum", null, true))
                .toList());
    var erro = assertThrows(CamposInvalidosException.class, () -> service.importar(lote));
    assertTrue(erro.getCampos().containsKey("usuarios.0.email"));
    assertTrue(erro.getCampos().containsKey("usuarios.1.cpf"));
    verify(usuarios, never()).criar(any());
  }

  @Test
  void reutilizaCadastroIndividualSomenteAposValidacaoCompleta() {
    var lote = List.of(usuario("a@teste.com", null), usuario("b@teste.com", null));
    when(validacao.consultar(lote))
        .thenReturn(
            lote.stream()
                .map(
                    u ->
                        new dad.core.domain.ReferenciasCadastroUsuario(
                            false, false, "Comum", null, true))
                .toList());
    assertEquals(2, service.importar(lote));
    var ordem = inOrder(usuarios, validacao);
    ordem.verify(usuarios).validarCriacao(lote.get(0));
    ordem.verify(usuarios).validarCriacao(lote.get(1));
    ordem.verify(validacao).consultar(lote);
    ordem.verify(usuarios).criar(lote.get(0));
    ordem.verify(usuarios).criar(lote.get(1));
  }
}
