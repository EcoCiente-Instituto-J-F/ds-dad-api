package dad.core.service;

import dad.core.domain.CriarUsuario;
import dad.core.exception.CamposInvalidosException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.ConsultaCadastroUsuariosGateway;
import dad.core.regra.RegrasUsuario;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportacaoUsuariosService {
  private final UsuarioService usuarios;
  private final ConsultaCadastroUsuariosGateway consultaCadastro;

  public ImportacaoUsuariosService(
      UsuarioService usuarios, ConsultaCadastroUsuariosGateway consultaCadastro) {
    this.usuarios = usuarios;
    this.consultaCadastro = consultaCadastro;
  }

  @Transactional
  public int importar(List<CriarUsuario> comandos) {
    if (comandos == null || comandos.isEmpty() || comandos.size() > 100)
      throw new CamposInvalidosException(
          "Corrija os dados da importação.", Map.of("usuarios", "Envie de 1 a 100 usuários."));
    var erros = new LinkedHashMap<String, String>();
    var emails = new HashMap<String, Integer>();
    var cpfs = new HashMap<String, Integer>();
    for (int i = 0; i < comandos.size(); i++) {
      var usuario = comandos.get(i);
      if (usuario == null) {
        erros.put("usuarios." + i, "Informe o usuário.");
        continue;
      }
      try {
        usuarios.validarCriacao(usuario);
      } catch (RegraNegocioException erro) {
        erros.put("usuarios." + i, erro.getMessage());
      }
      if (usuario.email() != null)
        duplicidade(emails, usuario.email().trim().toLowerCase(Locale.ROOT), i, "email", erros);
      if (usuario.cpf() != null) duplicidade(cpfs, usuario.cpf(), i, "cpf", erros);
    }
    if (comandos.stream().noneMatch(java.util.Objects::isNull)) {
      var fatos = consultaCadastro.consultar(comandos);
      for (int i = 0; i < comandos.size(); i++) {
        String prefixo = "usuarios." + i + ".";
        RegrasUsuario.errosImportacao(comandos.get(i), fatos.get(i))
            .forEach((campo, mensagem) -> erros.put(prefixo + campo, mensagem));
      }
    }
    if (!erros.isEmpty())
      throw new CamposInvalidosException("Corrija os dados da importação.", erros);
    for (var usuario : comandos) usuarios.criar(usuario);
    return comandos.size();
  }

  private void duplicidade(
      Map<String, Integer> vistos,
      String valor,
      int indice,
      String campo,
      Map<String, String> erros) {
    Integer anterior = vistos.putIfAbsent(valor, indice);
    if (anterior != null) {
      erros.put("usuarios." + anterior + "." + campo, "Valor repetido no lote.");
      erros.put("usuarios." + indice + "." + campo, "Valor repetido no lote.");
    }
  }
}
