package dad.core.service;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Pagina;
import dad.core.domain.Usuario;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.UsuarioGateway;
import dad.core.regra.RegrasPaginacao;
import dad.core.regra.RegrasUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

  private final UsuarioGateway gateway;

  public UsuarioService(UsuarioGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Usuario> listar(
      String busca, Boolean ativo, Integer tipoUsuarioId, int pagina, int tamanho) {
    RegrasPaginacao.validar(pagina, tamanho);

    if (tipoUsuarioId != null && tipoUsuarioId <= 0) {
      throw new RegraNegocioException("Tipo de usuário inválido");
    }

    return gateway.listar(busca, ativo, tipoUsuarioId, pagina, tamanho);
  }

  @Transactional(readOnly = true)
  public Usuario buscar(Integer id) {
    return gateway
        .buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
  }

  public Usuario criar(CriarUsuario comando) {
    validarCriacao(comando);
    return gateway.criar(comando);
  }

  public Usuario atualizar(Integer id, EditarUsuario comando) {
    RegrasUsuario.validarDados(comando.nome(), comando.email(), comando.dataNascimento());

    if (comando.endereco() == null) {
      throw new RegraNegocioException("Informe o endereço");
    }

    return gateway.atualizar(id, comando);
  }

  public Usuario alterarStatus(Integer id, boolean ativo) {
    return gateway.alterarStatus(id, ativo);
  }

  public void validarCriacao(CriarUsuario comando) {
    RegrasUsuario.validarCriacao(comando);
  }
}
