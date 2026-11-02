package dad.core.regra;

import dad.core.exception.RegraNegocioException;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.util.Locale;

public final class DiasSemana {
  private DiasSemana() {}

  public static String normalizar(String valor) {
    return Normalizer.normalize(valor, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .trim()
        .toUpperCase(Locale.ROOT);
  }

  public static DayOfWeek diaDaSemana(String nome) {
    String dia = normalizar(nome);

    return switch (dia) {
      case "DOMINGO" -> DayOfWeek.SUNDAY;
      case "SEGUNDA", "SEGUNDA-FEIRA", "SEGUNDA_FEIRA" -> DayOfWeek.MONDAY;
      case "TERCA", "TERCA-FEIRA", "TERCA_FEIRA" -> DayOfWeek.TUESDAY;
      case "QUARTA", "QUARTA-FEIRA", "QUARTA_FEIRA" -> DayOfWeek.WEDNESDAY;
      case "QUINTA", "QUINTA-FEIRA", "QUINTA_FEIRA" -> DayOfWeek.THURSDAY;
      case "SEXTA", "SEXTA-FEIRA", "SEXTA_FEIRA" -> DayOfWeek.FRIDAY;
      case "SABADO" -> DayOfWeek.SATURDAY;
      default ->
          throw new RegraNegocioException("Nome de dia não reconhecido no catálogo: " + nome);
    };
  }
}
