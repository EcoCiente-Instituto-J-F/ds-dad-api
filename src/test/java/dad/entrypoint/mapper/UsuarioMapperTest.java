package dad.entrypoint.mapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import dad.core.domain.Endereco;
import dad.core.domain.Usuario;
import dad.entrypoint.dto.request.EnderecoRequest;
import dad.entrypoint.dto.request.UsuarioCriarRequest;
import dad.entrypoint.dto.request.UsuarioEditarRequest;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

  private final UsuarioMapper mapper = new UsuarioMapper();

  @Test
  void deveNormalizarEmailENomeSemAlterarSenha() {
    var request = new UsuarioCriarRequest(
        " Pessoa Teste ",
        " PESSOA@TESTE.COM ",
        "Teste@123",
        1,
        LocalDate.of(2000, 1, 1),
        "52998224725",
        null,
        enderecoRequest(),
        null);

    var comando = mapper.toDomain(request);

    assertEquals(
        "Pessoa Teste",
        comando.nome());

    assertEquals(
        "pessoa@teste.com",
        comando.email());

    assertEquals(
        "Teste@123",
        comando.senha());
  }

  @Test
  void deveConverterAvatarEmBrancoParaNull() {
    var request = new UsuarioEditarRequest(
        "Pessoa",
        "pessoa@teste.com",
        null,
        " ",
        enderecoRequest());

    var comando = mapper.toDomain(request);

    assertNull(comando.avatar());
    assertNull(comando.endereco().complemento());
  }

  @Test
  void deveRetornarSomenteCpfMascarado() {
    var usuario = new Usuario(
        1,
        "Pessoa",
        "pessoa@teste.com",
        2,
        "Comum",
        true,
        null,
        "52998224725",
        null,
        OffsetDateTime.now(),
        endereco(),
        null);

    var response = mapper.toResponse(usuario);

    assertEquals(
        "***.***.***-25",
        response.cpfMascarado());
  }

  @Test
  void deveManterCpfMascaradoNuloQuandoAusente() {
    var usuario = new Usuario(
        1,
        "Pessoa",
        "pessoa@teste.com",
        2,
        "Comum",
        true,
        null,
        null,
        null,
        OffsetDateTime.now(),
        endereco(),
        null);

    assertNull(
        mapper.toResponse(usuario).cpfMascarado());
  }

  private EnderecoRequest enderecoRequest() {
    return new EnderecoRequest(
        "01001000",
        "SP",
        " São Paulo ",
        " Rua dos Testes ",
        " 10 ",
        " ");
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