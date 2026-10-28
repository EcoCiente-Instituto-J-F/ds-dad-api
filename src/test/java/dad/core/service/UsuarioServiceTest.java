package dad.core.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Endereco;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.UsuarioGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

  @Mock
  private UsuarioGateway gateway;

  @InjectMocks
  private UsuarioService service;

  @ParameterizedTest
  @ValueSource(strings = {
      "curta",
      "teste@123",
      "TESTE@123",
      "Teste@@@@",
      "Teste1234",
      "Teste @123"
  })
  void deveBloquearSenhaForaDoPadrao(String senha) {
    var comando = criar(
        senha,
        LocalDate.of(2000, 1, 1),
        "52998224725",
        endereco());

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(comando));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveBloquearSenhaQueExcede72BytesUtf8() {
    String senha = "Aa1!" + "é".repeat(35);

    var comando = criar(
        senha,
        LocalDate.of(2000, 1, 1),
        null,
        endereco());

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(comando));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveBloquearNascimentoFuturo() {
    var comando = criar(
        "Teste@123",
        LocalDate.now().plusDays(1),
        null,
        endereco());

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(comando));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveBloquearNascimentoNaDataAtual() {
    var comando = criar(
        "Teste@123",
        LocalDate.now(),
        null,
        endereco());

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(comando));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveExigirEnderecoNoCadastro() {
    var comando = criar(
        "Teste@123",
        null,
        null,
        null);

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(comando));

    verifyNoInteractions(gateway);
  }

  @Test
  void deveExigirEnderecoNaEdicao() {
    var comando = new EditarUsuario(
        "Pessoa",
        "pessoa@teste.com",
        null,
        null,
        null);

    assertThrows(
        RegraNegocioException.class,
        () -> service.atualizar(1, comando));

    verifyNoInteractions(gateway);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "123",
      "5299822472A",
      "529.982.247-25"
  })
  void deveExigirCpfComOnzeNumeros(String cpf) {
    var comando = criar(
        "Teste@123",
        null,
        cpf,
        endereco());

    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(comando));

    verifyNoInteractions(gateway);
  }

  @Test
  void devePermitirNascimentoECpfAusentes() {
    var comando = criar(
        "Teste@123",
        null,
        null,
        endereco());

    service.criar(comando);

    verify(gateway).criar(comando);
  }

  private CriarUsuario criar(
      String senha,
      LocalDate nascimento,
      String cpf,
      Endereco endereco) {
    return new CriarUsuario(
        "Pessoa",
        "pessoa@teste.com",
        senha,
        1,
        nascimento,
        cpf,
        null,
        endereco,
        null);
  }

  private Endereco endereco() {
    return new Endereco(
        "01001000",
        "SP",
        "São Paulo",
        "Rua dos Testes",
        "10",
        null);
  }
}