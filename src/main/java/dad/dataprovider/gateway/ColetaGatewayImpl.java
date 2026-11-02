package dad.dataprovider.gateway;

import dad.core.domain.Coleta;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.exception.UsuarioNaoAutenticadoException;
import dad.core.gateway.ColetaGateway;
import dad.core.regra.DiasSemana;
import dad.core.regra.RegrasColeta;
import dad.dataprovider.entity.ColetaEntity;
import dad.dataprovider.entity.VisitaEntity;
import dad.dataprovider.mapper.ColetaPersistenceMapper;
import dad.dataprovider.repository.ColetaRepository;
import dad.dataprovider.repository.CondominioRepository;
import dad.dataprovider.repository.CooperativaRepository;
import dad.dataprovider.repository.UsuarioRepository;
import dad.dataprovider.repository.VisitaColetaRepository;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ColetaGatewayImpl implements ColetaGateway {

  private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

  private final ColetaRepository repository;
  private final VisitaColetaRepository visitaRepository;
  private final CondominioRepository condominioRepository;
  private final CooperativaRepository cooperativaRepository;
  private final ColetaPersistenceMapper mapper;
  private final UsuarioRepository usuarioRepository;

  public ColetaGatewayImpl(
      ColetaRepository repository,
      VisitaColetaRepository visitaRepository,
      CondominioRepository condominioRepository,
      CooperativaRepository cooperativaRepository,
      ColetaPersistenceMapper mapper,
      UsuarioRepository usuarioRepository) {
    this.repository = repository;
    this.visitaRepository = visitaRepository;
    this.condominioRepository = condominioRepository;
    this.cooperativaRepository = cooperativaRepository;
    this.mapper = mapper;
    this.usuarioRepository = usuarioRepository;
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
    var pageable =
        PageRequest.of(
            pagina, tamanho, Sort.by("dataInicio").descending().and(Sort.by("id").descending()));

    var resultado =
        repository.buscar(busca == null ? "" : busca.trim(), statusId, inicio, fim, pageable);

    return new Pagina<>(
        resultado.getContent().stream().map(this::toDomain).toList(),
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Coleta> buscarPorId(Integer id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public Coleta criar(Coleta.Criacao criacao) {
    if (criacao == null) throw new UsuarioNaoAutenticadoException();
    RegrasColeta.exigirSolicitante(criacao.solicitadoPorUsuarioId());
    usuarioRepository
        .buscarComBloqueio(criacao.solicitadoPorUsuarioId())
        .filter(usuario -> usuario.ativo)
        .orElseThrow(UsuarioNaoAutenticadoException::new);
    validarReferencias(criacao.dados());
    var entity = mapper.toNovaEntity(criacao);
    repository.saveAndFlush(entity);
    salvarDias(entity.id, criacao.dados());
    return toDomain(entity);
  }

  @Override
  public Coleta salvar(Integer id, Coleta.Dados dados) {
    if (id == null) throw new UsuarioNaoAutenticadoException();
    var entity = exigirComBloqueio(id);

    boolean possuiVisitas = id != null && visitaRepository.countByColetaId(id) > 0;

    if (possuiVisitas) {
      RegrasColeta.validarEdicaoComVisitas(programacao(entity), dados);

      nomeStatus(dados.statusId());

      entity.statusId = dados.statusId();

      repository.flush();

      return toDomain(entity);
    }

    validarReferencias(dados);

    mapper.atualizar(entity, dados);

    repository.saveAndFlush(entity);

    salvarDias(entity.id, dados);

    return toDomain(entity);
  }

  @Override
  public Coleta cancelar(Integer id) {
    var entity = exigirComBloqueio(id);

    RegrasColeta.validarCancelamento(nomeStatus(entity.statusId));

    entity.statusId =
        repository
            .buscarStatusCancelado()
            .orElseThrow(
                () -> new RegraNegocioException("Status Cancelado não encontrado no banco"));

    repository.flush();

    return toDomain(entity);
  }

  @Override
  public Coleta registrarVisita(Integer id, Coleta.RegistroVisita registro) {
    var coleta = exigirComBloqueio(id);

    RegrasColeta.validarStatusVisita(nomeStatus(coleta.statusId));
    RegrasColeta.validarDataVisita(programacao(coleta), registro.data(), OffsetDateTime.now());
    if (coleta.recorrente) {
      var dia = registro.data().atZoneSameInstant(ZONA).toLocalDate().getDayOfWeek();
      boolean permitido =
          repository.buscarDias(coleta.id).stream()
              .anyMatch(
                  diaId ->
                      DiasSemana.diaDaSemana(
                              repository
                                  .buscarNomeDia(diaId)
                                  .orElseThrow(
                                      () ->
                                          new RegraNegocioException(
                                              "Dia da recorrência não encontrado")))
                          == dia);
      RegrasColeta.validarDiaPermitido(permitido);
    }

    var dataLocal = registro.data().atZoneSameInstant(ZONA).toLocalDate();

    var inicioDia = dataLocal.atStartOfDay(ZONA).toOffsetDateTime();

    var fimDia = dataLocal.plusDays(1).atStartOfDay(ZONA).toOffsetDateTime();

    var existentes =
        visitaRepository.findByColetaIdAndDataVisitaGreaterThanEqualAndDataVisitaLessThan(
            id, inicioDia, fimDia);

    RegrasColeta.validarQuantidadeVisitas(existentes.size());

    VisitaEntity entity;

    if (existentes.isEmpty()) {
      entity = new VisitaEntity();
      entity.coletaId = id;
    } else {
      entity = existentes.get(0);

      RegrasColeta.validarVisitaSemResultado(
          entity.realizada, entity.confirmada, entity.confirmadoEm, entity.observacao);
    }

    entity.dataVisita = registro.data();
    entity.realizada = registro.realizada();
    entity.confirmada = registro.confirmada();
    entity.confirmadoEm = registro.confirmada() ? OffsetDateTime.now() : null;
    entity.observacao = registro.observacao();

    visitaRepository.saveAndFlush(entity);

    return toDomain(coleta);
  }

  private ColetaEntity exigirComBloqueio(Integer id) {
    return repository
        .buscarComBloqueio(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));
  }

  private Coleta toDomain(ColetaEntity entity) {
    return mapper.toDomain(
        entity,
        nomeStatus(entity.statusId),
        repository.buscarDias(entity.id),
        visitaRepository.findByColetaIdOrderByDataVisitaAsc(entity.id));
  }

  private String nomeStatus(Integer statusId) {
    return repository
        .buscarNomeStatus(statusId)
        .orElseThrow(() -> new RegraNegocioException("Status de agendamento não encontrado"));
  }

  private void validarReferencias(Coleta.Dados dados) {
    var condominio =
        condominioRepository
            .findById(dados.condominioId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Condomínio não encontrado"));

    RegrasColeta.validarCondominioAtivo(condominio.ativo);

    if (!cooperativaRepository.existsById(dados.cooperativaId())) {
      throw new RecursoNaoEncontradoException("Cooperativa não encontrada");
    }

    nomeStatus(dados.statusId());

    for (var diaId : dados.diasSemanaIds()) {
      var nome =
          repository
              .buscarNomeDia(diaId)
              .orElseThrow(() -> new RegraNegocioException("Dia da semana não encontrado"));

      DiasSemana.diaDaSemana(nome);
    }
  }

  private Coleta.Dados programacao(ColetaEntity entity) {
    return new Coleta.Dados(
        entity.condominioId,
        entity.cooperativaId,
        entity.statusId,
        entity.dataInicio,
        entity.dataFim,
        entity.recorrente,
        repository.buscarDias(entity.id));
  }

  private void salvarDias(Integer id, Coleta.Dados dados) {
    repository.excluirDias(id);
    for (var diaId : dados.diasSemanaIds()) repository.inserirDia(id, diaId);
  }
}
