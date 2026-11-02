package dad.core.gateway;

import dad.core.domain.CriarUsuario;
import dad.core.domain.ReferenciasCadastroUsuario;
import java.util.List;

public interface ConsultaCadastroUsuariosGateway {
  List<ReferenciasCadastroUsuario> consultar(List<CriarUsuario> usuarios);
}
