package dad.dataprovider.gateway;

import dad.core.domain.CriarUsuario;
import dad.core.domain.ReferenciasCadastroUsuario;
import dad.core.gateway.ConsultaCadastroUsuariosGateway;
import dad.dataprovider.repository.CondominioRepository;
import dad.dataprovider.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ConsultaCadastroUsuariosGatewayImpl implements ConsultaCadastroUsuariosGateway {
  private final UsuarioRepository usuarios;
  private final CondominioRepository condominios;

  public ConsultaCadastroUsuariosGatewayImpl(
      UsuarioRepository usuarios, CondominioRepository condominios) {
    this.usuarios = usuarios;
    this.condominios = condominios;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public List<ReferenciasCadastroUsuario> consultar(List<CriarUsuario> lote) {
    usuarios.bloquearEscritas();
    boolean nivelInicial = usuarios.buscarNivelInicial().isPresent();
    var resultado = new ArrayList<ReferenciasCadastroUsuario>();
    for (var u : lote) {
      String tipo =
          u.tipoUsuarioId() == null
              ? null
              : usuarios.buscarNomeTipo(u.tipoUsuarioId()).orElse(null);
      Boolean ativo =
          u.condominioId() == null
              ? null
              : condominios.buscarComBloqueio(u.condominioId()).map(c -> c.ativo).orElse(false);
      resultado.add(
          new ReferenciasCadastroUsuario(
              u.email() != null
                  && usuarios.existeEmail(u.email().trim().toLowerCase(Locale.ROOT), 0),
              u.cpf() != null && usuarios.existsByCpf(u.cpf()),
              tipo,
              ativo,
              nivelInicial));
    }
    return List.copyOf(resultado);
  }
}
