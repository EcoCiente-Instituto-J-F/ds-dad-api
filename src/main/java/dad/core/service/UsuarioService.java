package dad.core.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Pagina;
import dad.core.domain.Usuario;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.UsuarioGateway;

@Service
@Transactional
public class UsuarioService {

  private final UsuarioGateway gateway;

  public UsuarioService(UsuarioGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Usuario> listar(
      String busca,
      Boolean ativo,
      Integer tipoUsuarioId,
      int pagina,
      int tamanho) {
    if (pagina < 0 || tamanho < 1 || tamanho > 100) {
      throw new RegraNegocioException(
          "Página deve ser maior ou igual a zero "
              + "e tamanho deve estar entre 1 e 100");
    }

    if (tipoUsuarioId != null && tipoUsuarioId <= 0) {
      throw new RegraNegocioException(
          "Tipo de usuário inválido");
    }

    return gateway.listar(
        busca,
        ativo,
        tipoUsuarioId,
        pagina,
        tamanho);
  }

  @Transactional(readOnly = true)
  public Usuario buscar(Integer id) {
    return gateway.buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Usuário não encontrado"));
  }

  public Usuario criar(CriarUsuario comando) {
    validarDados(
        comando.nome(),
        comando.email(),
        comando.dataNascimento());

    if (comando.endereco() == null) {
      throw new RegraNegocioException(
          "Informe o endereço");
    }

    if (comando.tipoUsuarioId() == null
        || comando.tipoUsuarioId() <= 0) {
      throw new RegraNegocioException(
          "Informe o tipo de usuário");
    }

    if (comando.senha() == null
        || !comando.senha().matches(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])"
                + "(?=.*[^\\p{L}\\p{N}\\s])\\S{8,}$")) {
      throw new RegraNegocioException(
          "Senha precisa de 8 caracteres, "
              + "maiúscula, minúscula, número e especial, "
              + "sem espaços");
    }

    if (comando.senha().getBytes(
        StandardCharsets.UTF_8).length > 72) {
      throw new RegraNegocioException(
          "A senha excede o limite de 72 bytes");
    }

    if (comando.cpf() != null
        && !comando.cpf().matches("\\d{11}")) {
      throw new RegraNegocioException(
          "Informe o CPF com 11 números");
    }

    return gateway.criar(comando);
  }

  public Usuario atualizar(
      Integer id,
      EditarUsuario comando) {
    validarDados(
        comando.nome(),
        comando.email(),
        comando.dataNascimento());

    if (comando.endereco() == null) {
      throw new RegraNegocioException(
          "Informe o endereço");
    }

    return gateway.atualizar(id, comando);
  }

  public Usuario alterarStatus(
      Integer id,
      boolean ativo) {
    return gateway.alterarStatus(id, ativo);
  }

  private void validarDados(
      String nome,
      String email,
      LocalDate nascimento) {
    if (nome == null || nome.isBlank()) {
      throw new RegraNegocioException(
          "Informe o nome do usuário");
    }

    if (email == null || email.isBlank()) {
      throw new RegraNegocioException(
          "Informe o e-mail");
    }

    if (nascimento != null
        && !nascimento.isBefore(LocalDate.now())) {
      throw new RegraNegocioException(
          "A data de nascimento deve estar no passado");
    }
  }
}