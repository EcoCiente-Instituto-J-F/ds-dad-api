package dad.core.gateway;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Pagina;
import dad.core.domain.Usuario;
import java.util.Optional;

public interface UsuarioGateway {

  Pagina<Usuario> listar(
      String busca, Boolean ativo, Integer tipoUsuarioId, int pagina, int tamanho);

  Optional<Usuario> buscarPorId(Integer id);

  Usuario criar(CriarUsuario comando);

  Usuario atualizar(Integer id, EditarUsuario comando);

  Usuario alterarStatus(Integer id, boolean ativo);
}
