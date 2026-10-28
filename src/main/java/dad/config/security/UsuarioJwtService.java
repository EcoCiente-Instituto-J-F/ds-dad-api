package dad.config.security;

import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dad.dataprovider.repository.UsuarioRepository;

@Service
public class UsuarioJwtService {

  private final UsuarioRepository repository;

  public UsuarioJwtService(
      UsuarioRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public UsuarioAutenticado buscar(
      Integer usuarioId,
      String emailToken) {
    var usuario = repository.findById(usuarioId)
        .orElseThrow(() -> new BadCredentialsException(
            "Usuário não encontrado"));

    if (!usuario.ativo) {
      throw new BadCredentialsException(
          "Usuário inativo");
    }

    if (!usuario.email.equalsIgnoreCase(emailToken)) {
      throw new BadCredentialsException(
          "Identificação do token desatualizada");
    }

    String tipo = repository
        .buscarNomeTipo(usuario.tipoUsuarioId)
        .orElseThrow(() -> new BadCredentialsException(
            "Tipo de usuário não encontrado"))
        .trim()
        .toLowerCase(Locale.ROOT);

    boolean administrador = tipo.equals("admin")
        || tipo.equals("administrador")
        || tipo.equals("administradora");

    return new UsuarioAutenticado(
        usuario.id,
        usuario.nome,
        usuario.email,
        administrador);
  }
}