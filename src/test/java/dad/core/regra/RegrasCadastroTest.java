package dad.core.regra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dad.core.domain.Catalogos;
import dad.core.domain.CriarUsuario;
import dad.core.domain.ReferenciasCadastroUsuario;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasCadastroTest {
  @Test
  void agregaFatosDeCadastroSemDependenciaDoBanco() {
    var u =
        new CriarUsuario(
            "Pessoa", "pessoa@teste.com", "Teste@123", 99, null, null, null, null, null);
    var erros =
        RegrasUsuario.errosImportacao(
            u, new ReferenciasCadastroUsuario(true, true, null, null, false));
    assertTrue(erros.containsKey("email"));
    assertTrue(erros.containsKey("cpf"));
    assertTrue(erros.containsKey("tipoUsuarioId"));
  }

  @Test
  void moradorExigeCondominioAtivoENivelConfigurado() {
    var u =
        new CriarUsuario("Pessoa", "pessoa@teste.com", "Teste@123", 1, null, null, null, null, 7);
    var erros =
        RegrasUsuario.errosImportacao(
            u, new ReferenciasCadastroUsuario(false, false, "Morador", false, false));
    assertTrue(erros.containsKey("condominioId"));
    assertTrue(erros.containsKey("tipoUsuarioId"));
  }

  @Test
  void agrupaAdministradoresPreservandoTodosOsIdsLegados() {
    var tipos =
        RegrasCatalogos.agruparTiposUsuarios(
            List.of(
                new Catalogos.Opcao(9, "Admin"), new Catalogos.Opcao(2, "Administrador"),
                new Catalogos.Opcao(10, "Administradora"), new Catalogos.Opcao(3, "Morador")));
    assertEquals(2, tipos.size());
    var admin =
        tipos.stream().filter(t -> t.nome().equals("Administrador")).findFirst().orElseThrow();
    assertEquals(2, admin.id());
    assertEquals(List.of(2, 9, 10), admin.idsEquivalentes());
  }
}
