package dad.core.gateway;

import java.util.Optional;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Pagina;
import dad.core.domain.Usuario;

public interface UsuarioGateway {

  Pagina<Usuario> listar(
      String busca,
      Boolean ativo,
      Integer tipoUsuarioId,
      int pagina,
      int tamanho);

  Optional<Usuario> buscarPorId(Integer id);

  Usuario criar(CriarUsuario comando);

  Usuario atualizar(
      Integer id,
      EditarUsuario comando);

  Usuario alterarStatus(
      Integer id,
      boolean ativo);
}