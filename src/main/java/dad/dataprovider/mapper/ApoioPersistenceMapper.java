package dad.dataprovider.mapper;

import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import dad.core.domain.Catalogos;
import dad.core.domain.Dashboard;
import dad.core.exception.RegraNegocioException;

@Component
public class ApoioPersistenceMapper {

  public Dashboard toDashboard(
      OffsetDateTime agora,
      Object[] totais,
      List<Dashboard.Mes> evolucao,
      List<Object[]> usuariosRecentes) {
    return new Dashboard(
        agora,
        numero(totais[0]),
        numero(totais[1]),
        numero(totais[2]),
        numero(totais[3]),
        numero(totais[4]),
        numero(totais[5]),
        numero(totais[6]),
        numero(totais[7]),
        numero(totais[8]),
        numero(totais[9]),
        evolucao,
        usuariosRecentes.stream()
            .map(this::toUsuarioRecente)
            .toList());
  }

  public Dashboard.Mes toMes(Object[] linha) {
    return new Dashboard.Mes(
        texto(linha[0]),
        numero(linha[1]),
        numero(linha[2]),
        numero(linha[3]));
  }

  public Catalogos.Opcao toOpcao(Object[] linha) {
    return new Catalogos.Opcao(
        inteiro(linha[0]),
        texto(linha[1]));
  }

  public Catalogos.Categoria toCategoria(
      Object[] linha) {
    return new Catalogos.Categoria(
        inteiro(linha[0]),
        texto(linha[1]),
        (Boolean) linha[2],
        texto(linha[3]),
        inteiro(linha[4]),
        inteiro(linha[5]));
  }

  public Catalogos.DiaSemana toDia(Object[] linha) {
    String nome = texto(linha[1]);

    return new Catalogos.DiaSemana(
        inteiro(linha[0]),
        nome,
        numeroDia(nome));
  }

  public Catalogos.Sindico toSindico(Object[] linha) {
    return new Catalogos.Sindico(
        inteiro(linha[0]),
        inteiro(linha[1]),
        texto(linha[2]));
  }

  private Dashboard.UsuarioRecente toUsuarioRecente(
      Object[] linha) {
    return new Dashboard.UsuarioRecente(
        inteiro(linha[0]),
        texto(linha[1]),
        texto(linha[2]),
        texto(linha[3]),
        (Boolean) linha[4],
        data(linha[5]));
  }

  private Integer numeroDia(String nome) {
    String normalizado = Normalizer
        .normalize(nome, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .trim()
        .toUpperCase(Locale.ROOT);

    return switch (normalizado) {
      case "DOMINGO" -> 0;
      case "SEGUNDA", "SEGUNDA-FEIRA",
          "SEGUNDA_FEIRA" ->
        1;
      case "TERCA", "TERCA-FEIRA",
          "TERCA_FEIRA" ->
        2;
      case "QUARTA", "QUARTA-FEIRA",
          "QUARTA_FEIRA" ->
        3;
      case "QUINTA", "QUINTA-FEIRA",
          "QUINTA_FEIRA" ->
        4;
      case "SEXTA", "SEXTA-FEIRA",
          "SEXTA_FEIRA" ->
        5;
      case "SABADO" -> 6;
      default -> throw new RegraNegocioException(
          "Nome de dia não reconhecido no catálogo: "
              + nome);
    };
  }

  private long numero(Object valor) {
    return ((Number) valor).longValue();
  }

  private Integer inteiro(Object valor) {
    return valor == null
        ? null
        : ((Number) valor).intValue();
  }

  private String texto(Object valor) {
    return valor == null ? null : valor.toString();
  }

  private OffsetDateTime data(Object valor) {
    if (valor instanceof OffsetDateTime data) {
      return data;
    }

    if (valor instanceof Instant instante) {
      return instante.atOffset(ZoneOffset.UTC);
    }

    if (valor instanceof Timestamp timestamp) {
      return timestamp.toInstant()
          .atOffset(ZoneOffset.UTC);
    }

    if (valor instanceof ZonedDateTime data) {
      return data.toOffsetDateTime();
    }

    throw new IllegalStateException(
        "Tipo de data não suportado pelo driver");
  }
}