package dad.dataprovider.gateway;

import dad.core.domain.Catalogos;
import dad.core.domain.Dashboard;
import dad.core.gateway.ApoioGateway;
import dad.core.regra.RegrasCatalogos;
import dad.dataprovider.mapper.ApoioPersistenceMapper;
import dad.dataprovider.repository.ApoioRepository;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class ApoioGatewayImpl implements ApoioGateway {

  private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

  private final ApoioRepository repository;
  private final ApoioPersistenceMapper mapper;
  private final dad.dataprovider.repository.CalendarioColetaRepository calendario;

  public ApoioGatewayImpl(
      ApoioRepository repository,
      ApoioPersistenceMapper mapper,
      dad.dataprovider.repository.CalendarioColetaRepository calendario) {
    this.repository = repository;
    this.mapper = mapper;
    this.calendario = calendario;
  }

  @Override
  public Dashboard buscarDashboard(int meses) {
    var agora = ZonedDateTime.now(ZONA);

    var primeiroMes = YearMonth.from(agora).minusMonths(meses - 1L);

    var inicioPeriodo = primeiroMes.atDay(1).atStartOfDay(ZONA).toOffsetDateTime();

    var inicioHoje = agora.toLocalDate().atStartOfDay(ZONA).toOffsetDateTime();

    var instante = agora.toOffsetDateTime();

    var totais = repository.buscarTotais(inicioHoje, instante);

    var porMes =
        repository.buscarEvolucao(inicioPeriodo, instante).stream()
            .map(mapper::toMes)
            .collect(Collectors.toMap(Dashboard.Mes::mes, Function.identity()));

    var evolucao = new ArrayList<Dashboard.Mes>();

    for (int indice = 0; indice < meses; indice++) {
      String chave = primeiroMes.plusMonths(indice).toString();

      evolucao.add(porMes.getOrDefault(chave, new Dashboard.Mes(chave, 0, 0, 0)));
    }

    return mapper.toDashboard(
        instante,
        totais,
        evolucao,
        repository.buscarUsuariosRecentes(),
        calendario.contar(
            inicioHoje,
            agora.toLocalDate().plusDays(1).atStartOfDay(ZONA).toOffsetDateTime(),
            null,
            null,
            null));
  }

  @Override
  public Catalogos buscarCatalogos() {
    return new Catalogos(
        tiposUsuarios(),
        repository.buscarTiposCondominios().stream().map(mapper::toOpcao).toList(),
        repository.buscarCategorias().stream().map(mapper::toCategoria).toList(),
        repository.buscarDiasSemana().stream()
            .map(mapper::toDia)
            .sorted(Comparator.comparing(Catalogos.DiaSemana::numero))
            .toList(),
        repository.buscarStatusAgendamentos().stream().map(mapper::toOpcao).toList(),
        repository.buscarSindicos().stream().map(mapper::toSindico).toList());
  }

  private java.util.List<Catalogos.Opcao> tiposUsuarios() {
    return RegrasCatalogos.agruparTiposUsuarios(
        repository.buscarTiposUsuarios().stream().map(mapper::toOpcao).toList());
  }
}
