package dad.core.regra;

import dad.core.domain.Catalogos;
import java.util.Comparator;

public final class RegrasCatalogos {
  private RegrasCatalogos() {}

  public static java.util.List<Catalogos.Opcao> agruparTiposUsuarios(
      java.util.List<Catalogos.Opcao> opcoes) {
    var admin =
        opcoes.stream()
            .filter(o -> dad.core.domain.TiposUsuarios.administrador(o.nome()))
            .min(Comparator.comparing(Catalogos.Opcao::id));
    return opcoes.stream()
        .filter(
            o ->
                !dad.core.domain.TiposUsuarios.administrador(o.nome())
                    || admin.map(a -> a.id().equals(o.id())).orElse(false))
        .map(
            o ->
                new Catalogos.Opcao(
                    o.id(),
                    dad.core.domain.TiposUsuarios.nomeExibicao(o.nome()),
                    dad.core.domain.TiposUsuarios.administrador(o.nome())
                        ? opcoes.stream()
                            .filter(a -> dad.core.domain.TiposUsuarios.administrador(a.nome()))
                            .map(Catalogos.Opcao::id)
                            .sorted()
                            .toList()
                        : java.util.List.of(o.id())))
        .sorted(Comparator.comparing(Catalogos.Opcao::nome).thenComparing(Catalogos.Opcao::id))
        .toList();
  }
}
