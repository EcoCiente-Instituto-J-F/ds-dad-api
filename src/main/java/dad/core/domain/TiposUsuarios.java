package dad.core.domain;

import java.util.Locale;

public final class TiposUsuarios {
  private TiposUsuarios() {}

  public static boolean administrador(String nome) {
    if (nome == null) return false;
    return switch (nome.trim().toLowerCase(Locale.ROOT)) {
      case "admin", "administrador", "administradora" -> true;
      default -> false;
    };
  }

  public static String nomeExibicao(String nome) {
    return administrador(nome) ? "Administrador" : nome;
  }
}
