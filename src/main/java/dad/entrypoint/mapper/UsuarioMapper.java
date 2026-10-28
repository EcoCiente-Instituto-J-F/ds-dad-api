package dad.entrypoint.mapper;

import java.util.Locale;

import org.springframework.stereotype.Component;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Endereco;
import dad.core.domain.Pagina;
import dad.core.domain.Usuario;
import dad.entrypoint.dto.request.EnderecoRequest;
import dad.entrypoint.dto.request.UsuarioCriarRequest;
import dad.entrypoint.dto.request.UsuarioEditarRequest;
import dad.entrypoint.dto.response.EnderecoResponse;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.dto.response.UsuarioResponse;

@Component
public class UsuarioMapper {

  public CriarUsuario toDomain(
      UsuarioCriarRequest request) {
    return new CriarUsuario(
        request.nome().trim(),
        normalizarEmail(request.email()),
        request.senha(),
        request.tipoUsuarioId(),
        request.dataNascimento(),
        request.cpf(),
        limpar(request.avatar()),
        toDomain(request.endereco()),
        request.condominioId());
  }

  public EditarUsuario toDomain(
      UsuarioEditarRequest request) {
    return new EditarUsuario(
        request.nome().trim(),
        normalizarEmail(request.email()),
        request.dataNascimento(),
        limpar(request.avatar()),
        toDomain(request.endereco()));
  }

  public UsuarioResponse toResponse(Usuario usuario) {
    var endereco = usuario.endereco();

    return new UsuarioResponse(
        usuario.id(),
        usuario.nome(),
        usuario.email(),
        usuario.tipoUsuarioId(),
        usuario.tipoUsuario(),
        usuario.ativo(),
        usuario.dataNascimento(),
        mascararCpf(usuario.cpf()),
        usuario.avatar(),
        usuario.registroEm(),
        new EnderecoResponse(
            endereco.cep(),
            endereco.uf(),
            endereco.cidade(),
            endereco.logradouro(),
            endereco.numero(),
            endereco.complemento()),
        usuario.condominioId());
  }

  public PaginaResponse<UsuarioResponse> toResponse(
      Pagina<Usuario> pagina) {
    return new PaginaResponse<>(
        pagina.itens()
            .stream()
            .map(this::toResponse)
            .toList(),
        pagina.pagina(),
        pagina.tamanho(),
        pagina.totalItens(),
        pagina.totalPaginas());
  }

  private Endereco toDomain(
      EnderecoRequest request) {
    return new Endereco(
        request.cep(),
        request.uf(),
        request.cidade().trim(),
        request.logradouro().trim(),
        request.numero().trim(),
        limpar(request.complemento()));
  }

  private String normalizarEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String limpar(String valor) {
    return valor == null || valor.isBlank()
        ? null
        : valor.trim();
  }

  private String mascararCpf(String cpf) {
    if (cpf == null || cpf.length() != 11) {
      return null;
    }

    return "***.***.***-" + cpf.substring(9);
  }
}