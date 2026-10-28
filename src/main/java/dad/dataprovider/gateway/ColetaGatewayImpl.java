package dad.dataprovider.gateway;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Coleta;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.ColetaGateway;
import dad.dataprovider.entity.ColetaEntity;
import dad.dataprovider.entity.VisitaEntity;
import dad.dataprovider.mapper.ColetaPersistenceMapper;
import dad.dataprovider.repository.ColetaRepository;
import dad.dataprovider.repository.CondominioRepository;
import dad.dataprovider.repository.CooperativaRepository;
import dad.dataprovider.repository.VisitaColetaRepository;

@Component
@Transactional
public class ColetaGatewayImpl implements ColetaGateway {

  private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

  private final ColetaRepository repository;
  private final VisitaColetaRepository visitaRepository;
  private final CondominioRepository condominioRepository;
  private final CooperativaRepository cooperativaRepository;
  private final ColetaPersistenceMapper mapper;

  public ColetaGatewayImpl(
      ColetaRepository repository,
      VisitaColetaRepository visitaRepository,
      CondominioRepository condominioRepository,
      CooperativaRepository cooperativaRepository,
      ColetaPersistenceMapper mapper) {
    this.repository = repository;
    this.visitaRepository = visitaRepository;
    this.condominioRepository = condominioRepository;
    this.cooperativaRepository = cooperativaRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Pagina<Coleta> listar(
      String busca,
      Integer statusId,
      OffsetDateTime inicio,
      OffsetDateTime fim,
      int pagina,
      int tamanho) {
    var pageable = PageRequest.of(
        pagina,
        tamanho,
        Sort.by("dataInicio").descending()
            .and(Sort.by("id").descending()));

    var resultado = repository.buscar(
        busca == null ? "" : busca.trim(),
        statusId,
        inicio,
        fim,
        pageable);

    return new Pagina<>(
        resultado.getContent()
            .stream()
            .map(this::toDomain)
            .toList(),
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Coleta> buscarPorId(Integer id) {
    return repository.findById(id)
        .map(this::toDomain);
  }

  @Override
  public Coleta salvar(
      Integer id,
      Coleta.Dados dados) {
    var entity = id == null
        ? new ColetaEntity()
        : exigirComBloqueio(id);

    boolean possuiVisitas = id != null
        && visitaRepository.countByColetaId(id) > 0;

    if (possuiVisitas) {
      if (!mesmaProgramacao(entity, dados)) {
        throw new RegraNegocioException(
            "O agendamento possui visitas. "
                + "Preserve participantes, datas e recorrência");
      }

      nomeStatus(dados.statusId());

      entity.statusId = dados.statusId();

      repository.flush();

      return toDomain(entity);
    }

    validarReferencias(dados);

    mapper.atualizar(entity, dados);

    repository.saveAndFlush(entity);

    repository.excluirDias(entity.id);

    for (var diaId : dados.diasSemanaIds()) {
      repository.inserirDia(entity.id, diaId);
    }

    return toDomain(entity);
  }

  @Override
  public Coleta cancelar(Integer id) {
    var entity = exigirComBloqueio(id);

    String status = normalizar(
        nomeStatus(entity.statusId));

    if (status.equals("REALIZADO")
        || status.equals("REALIZADA")
        || status.equals("CONCLUIDO")
        || status.equals("CONCLUIDA")) {
      throw new RegraNegocioException(
          "O agendamento já foi concluído");
    }

    entity.statusId = repository
        .buscarStatusCancelado()
        .orElseThrow(() -> new RegraNegocioException(
            "Status Cancelado não encontrado no banco"));

    repository.flush();

    return toDomain(entity);
  }

  @Override
  public Coleta registrarVisita(
      Integer id,
      Coleta.RegistroVisita registro) {
    var coleta = exigirComBloqueio(id);

    String status = normalizar(
        nomeStatus(coleta.statusId));

    if (status.equals("CANCELADO")
        || status.equals("CANCELADA")
        || status.equals("RECUSADO")
        || status.equals("RECUSADA")) {
      throw new RegraNegocioException(
          "O status do agendamento não permite registrar visita");
    }

    validarDataVisita(coleta, registro.data());

    var dataLocal = registro.data()
        .atZoneSameInstant(ZONA)
        .toLocalDate();

    var inicioDia = dataLocal
        .atStartOfDay(ZONA)
        .toOffsetDateTime();

    var fimDia = dataLocal.plusDays(1)
        .atStartOfDay(ZONA)
        .toOffsetDateTime();

    var existentes = visitaRepository
        .findByColetaIdAndDataVisitaGreaterThanEqualAndDataVisitaLessThan(
            id,
            inicioDia,
            fimDia);

    if (existentes.size() > 1) {
      throw new RegraNegocioException(
          "Existem várias visitas para esse dia. "
              + "É necessário identificar a visita antes de registrar");
    }

    VisitaEntity entity;

    if (existentes.isEmpty()) {
      entity = new VisitaEntity();
      entity.coletaId = id;
    } else {
      entity = existentes.get(0);

      if (entity.realizada
          || entity.confirmada
          || entity.confirmadoEm != null
          || (entity.observacao != null
              && !entity.observacao.isBlank())) {
        throw new RegraNegocioException(
            "A visita desse dia já possui resultado ou observação");
      }
    }

    entity.dataVisita = registro.data();
    entity.realizada = registro.realizada();
    entity.confirmada = registro.confirmada();
    entity.confirmadoEm = registro.confirmada()
        ? OffsetDateTime.now()
        : null;
    entity.observacao = registro.observacao();

    visitaRepository.saveAndFlush(entity);

    return toDomain(coleta);
  }

  private ColetaEntity exigirComBloqueio(Integer id) {
    return repository.buscarComBloqueio(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Agendamento não encontrado"));
  }

  private Coleta toDomain(ColetaEntity entity) {
    return mapper.toDomain(
        entity,
        nomeStatus(entity.statusId),
        repository.buscarDias(entity.id),
        visitaRepository
            .findByColetaIdOrderByDataVisitaAsc(entity.id));
  }

  private String nomeStatus(Integer statusId) {
    return repository.buscarNomeStatus(statusId)
        .orElseThrow(() -> new RegraNegocioException(
            "Status de agendamento não encontrado"));
  }

  private void validarReferencias(Coleta.Dados dados) {
    var condominio = condominioRepository
        .findById(dados.condominioId())
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Condomínio não encontrado"));

    if (!condominio.ativo) {
      throw new RegraNegocioException(
          "Condomínio inativo");
    }

    if (!cooperativaRepository.existsById(
        dados.cooperativaId())) {
      throw new RecursoNaoEncontradoException(
          "Cooperativa não encontrada");
    }

    nomeStatus(dados.statusId());

    for (var diaId : dados.diasSemanaIds()) {
      var nome = repository.buscarNomeDia(diaId)
          .orElseThrow(() -> new RegraNegocioException(
              "Dia da semana não encontrado"));

      diaDaSemana(nome);
    }
  }

  private boolean mesmaProgramacao(
      ColetaEntity entity,
      Coleta.Dados dados) {
    return Objects.equals(
        entity.condominioId,
        dados.condominioId())
        && Objects.equals(
            entity.cooperativaId,
            dados.cooperativaId())
        && mesmoInstante(
            entity.dataInicio,
            dados.dataInicio())
        && mesmoInstante(
            entity.dataFim,
            dados.dataFim())
        && entity.recorrente == dados.recorrente()
        && new HashSet<>(
            repository.buscarDias(entity.id)).equals(
                new HashSet<>(
                    dados.diasSemanaIds()));
  }

  private boolean mesmoInstante(
      OffsetDateTime primeiro,
      OffsetDateTime segundo) {
    if (primeiro == null || segundo == null) {
      return primeiro == segundo;
    }

    return primeiro.toInstant()
        .equals(segundo.toInstant());
  }

  private void validarDataVisita(
      ColetaEntity coleta,
      OffsetDateTime data) {
    if (data.isAfter(OffsetDateTime.now())) {
      throw new RegraNegocioException(
          "Não registre resultado de uma visita futura");
    }

    if (!coleta.recorrente) {
      if (data.isBefore(coleta.dataInicio)
          || (coleta.dataFim != null
              && data.isAfter(coleta.dataFim))) {
        throw new RegraNegocioException(
            "Visita fora do período do agendamento");
      }

      return;
    }

    var dia = data.atZoneSameInstant(ZONA)
        .toLocalDate();

    var inicio = coleta.dataInicio
        .atZoneSameInstant(ZONA)
        .toLocalDate();

    var fim = coleta.dataFim == null
        ? null
        : coleta.dataFim
            .atZoneSameInstant(ZONA)
            .toLocalDate();

    if (dia.isBefore(inicio)
        || (fim != null && dia.isAfter(fim))) {
      throw new RegraNegocioException(
          "Visita fora do período da recorrência");
    }

    boolean permitido = repository
        .buscarDias(coleta.id)
        .stream()
        .anyMatch(diaId -> {
          var nome = repository
              .buscarNomeDia(diaId)
              .orElseThrow(() -> new RegraNegocioException(
                  "Dia da recorrência não encontrado"));

          return diaDaSemana(nome) == dia.getDayOfWeek();
        });

    if (!permitido) {
      throw new RegraNegocioException(
          "A visita não corresponde a um dia da recorrência");
    }
  }

  private DayOfWeek diaDaSemana(String nome) {
    String dia = normalizar(nome);

    return switch (dia) {
      case "DOMINGO" -> DayOfWeek.SUNDAY;
      case "SEGUNDA", "SEGUNDA-FEIRA",
          "SEGUNDA_FEIRA" ->
        DayOfWeek.MONDAY;
      case "TERCA", "TERCA-FEIRA",
          "TERCA_FEIRA" ->
        DayOfWeek.TUESDAY;
      case "QUARTA", "QUARTA-FEIRA",
          "QUARTA_FEIRA" ->
        DayOfWeek.WEDNESDAY;
      case "QUINTA", "QUINTA-FEIRA",
          "QUINTA_FEIRA" ->
        DayOfWeek.THURSDAY;
      case "SEXTA", "SEXTA-FEIRA",
          "SEXTA_FEIRA" ->
        DayOfWeek.FRIDAY;
      case "SABADO" -> DayOfWeek.SATURDAY;
      default -> throw new RegraNegocioException(
          "Nome de dia não reconhecido no catálogo: "
              + nome);
    };
  }

  private String normalizar(String valor) {
    return Normalizer
        .normalize(valor, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .trim()
        .toUpperCase(Locale.ROOT);
  }
}