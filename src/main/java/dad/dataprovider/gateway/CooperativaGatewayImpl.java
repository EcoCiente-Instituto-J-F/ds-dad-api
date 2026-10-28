package dad.dataprovider.gateway;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Cooperativa;
import dad.core.domain.Endereco;
import dad.core.domain.Pagina;
import dad.core.domain.PontoColeta;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CooperativaGateway;
import dad.dataprovider.entity.CooperativaEntity;
import dad.dataprovider.entity.EnderecoEntity;
import dad.dataprovider.entity.PontoEntity;
import dad.dataprovider.mapper.CooperativaPersistenceMapper;
import dad.dataprovider.repository.CooperativaRepository;
import dad.dataprovider.repository.EnderecoRepository;
import dad.dataprovider.repository.PontoColetaRepository;

@Component
@Transactional
public class CooperativaGatewayImpl
    implements CooperativaGateway {

  private final CooperativaRepository repository;
  private final PontoColetaRepository pontoRepository;
  private final EnderecoRepository enderecoRepository;
  private final CooperativaPersistenceMapper mapper;

  public CooperativaGatewayImpl(
      CooperativaRepository repository,
      PontoColetaRepository pontoRepository,
      EnderecoRepository enderecoRepository,
      CooperativaPersistenceMapper mapper) {
    this.repository = repository;
    this.pontoRepository = pontoRepository;
    this.enderecoRepository = enderecoRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Pagina<Cooperativa> listar(
      String busca,
      int pagina,
      int tamanho) {
    var pageable = PageRequest.of(
        pagina,
        tamanho,
        Sort.by("nome").ascending()
            .and(Sort.by("id").ascending()));

    var resultado = repository.buscar(
        busca == null ? "" : busca.trim(),
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
  public Optional<Cooperativa> buscarPorId(Integer id) {
    return repository.findById(id)
        .map(this::toDomain);
  }

  @Override
  public Cooperativa criar(Cooperativa cooperativa) {
    repository.bloquearEscritas();

    validarReferencias(cooperativa);
    validarDuplicidades(cooperativa);

    if (cooperativa.pontos().stream()
        .anyMatch(ponto -> ponto.id() != null)) {
      throw new RegraNegocioException(
          "Pontos de uma nova cooperativa não devem possuir ID");
    }

    var entity = new CooperativaEntity();

    mapper.atualizar(entity, cooperativa);

    entity.criadoEm = OffsetDateTime.now();
    entity.enderecoId = salvarEndereco(
        null,
        cooperativa.endereco());

    repository.saveAndFlush(entity);

    salvarCategorias(
        entity.id,
        cooperativa.categoriaIds());

    salvarPontos(
        entity.id,
        cooperativa.pontos(),
        Map.of());

    return toDomain(entity);
  }

  @Override
  public Cooperativa atualizar(
      Cooperativa cooperativa) {
    repository.bloquearEscritas();

    var entity = repository
        .buscarComBloqueio(cooperativa.id())
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Cooperativa não encontrada"));

    validarReferencias(cooperativa);
    validarDuplicidades(cooperativa);

    var existentes = pontoRepository
        .findByCooperativaIdOrderByNomeAsc(entity.id);

    Map<Integer, PontoEntity> porId = existentes
        .stream()
        .collect(Collectors.toMap(
            ponto -> ponto.id,
            Function.identity()));

    Set<Integer> recebidos = new HashSet<>();

    for (var ponto : cooperativa.pontos()) {
      if (ponto.id() == null) {
        continue;
      }

      if (!recebidos.add(ponto.id())) {
        throw new RegraNegocioException(
            "Ponto informado mais de uma vez");
      }

      if (!porId.containsKey(ponto.id())) {
        throw new RegraNegocioException(
            "Ponto não pertence à cooperativa");
      }
    }

    var removidos = existentes.stream()
        .filter(ponto -> !recebidos.contains(ponto.id))
        .toList();

    for (var ponto : removidos) {
      pontoRepository.excluirCategorias(ponto.id);
    }

    pontoRepository.deleteAll(removidos);
    pontoRepository.flush();

    mapper.atualizar(entity, cooperativa);

    entity.enderecoId = salvarEndereco(
        entity.enderecoId,
        cooperativa.endereco());

    repository.flush();

    salvarCategorias(
        entity.id,
        cooperativa.categoriaIds());

    salvarPontos(
        entity.id,
        cooperativa.pontos(),
        porId);

    return toDomain(entity);
  }

  private void validarReferencias(
      Cooperativa cooperativa) {
    if (cooperativa.usuarioId() != null) {
      repository.bloquearUsuario(
          cooperativa.usuarioId()).orElseThrow(
              () -> new RecursoNaoEncontradoException(
                  "Usuário não encontrado"));

      if (!repository.usuarioEhCooperativaAtiva(
          cooperativa.usuarioId())) {
        throw new RegraNegocioException(
            "O usuário deve estar ativo e possuir tipo Cooperativa");
      }
    }

    validarCategorias(cooperativa.categoriaIds());

    for (var ponto : cooperativa.pontos()) {
      validarCategorias(ponto.categoriaIds());
    }
  }

  private void validarCategorias(
      List<Integer> categorias) {
    for (var categoriaId : categorias) {
      if (!repository.existeCategoria(categoriaId)) {
        throw new RegraNegocioException(
            "Categoria de resíduo não encontrada: "
                + categoriaId);
      }
    }
  }

  private void validarDuplicidades(
      Cooperativa cooperativa) {
    Integer ignorarId = cooperativa.id() == null
        ? 0
        : cooperativa.id();

    if (repository.existeCnpj(
        cooperativa.cnpj(),
        ignorarId)) {
      throw new RegraNegocioException(
          "CNPJ já cadastrado");
    }

    if (cooperativa.usuarioId() != null
        && repository.existsByUsuarioIdAndIdNot(
            cooperativa.usuarioId(),
            ignorarId)) {
      throw new RegraNegocioException(
          "Usuário já vinculado a outra cooperativa");
    }
  }

  private void salvarCategorias(
      Integer cooperativaId,
      List<Integer> categorias) {
    repository.excluirCategorias(cooperativaId);

    for (var categoriaId : categorias) {
      repository.inserirCategoria(
          cooperativaId,
          categoriaId);
    }
  }

  private void salvarPontos(
      Integer cooperativaId,
      List<PontoColeta> pontos,
      Map<Integer, PontoEntity> existentes) {
    for (var ponto : pontos) {
      var entity = ponto.id() == null
          ? new PontoEntity()
          : existentes.get(ponto.id());

      mapper.atualizar(
          entity,
          ponto,
          cooperativaId);

      entity.enderecoId = salvarEndereco(
          entity.enderecoId,
          ponto.endereco());

      pontoRepository.saveAndFlush(entity);
      pontoRepository.excluirCategorias(entity.id);

      for (var categoriaId : ponto.categoriaIds()) {
        pontoRepository.inserirCategoria(
            entity.id,
            categoriaId);
      }
    }
  }

  private Cooperativa toDomain(
      CooperativaEntity entity) {
    var pontos = pontoRepository
        .findByCooperativaIdOrderByNomeAsc(entity.id)
        .stream()
        .map(ponto -> mapper.toDomain(
            ponto,
            buscarEndereco(ponto.enderecoId),
            pontoRepository.buscarCategorias(ponto.id)))
        .toList();

    return mapper.toDomain(
        entity,
        buscarEndereco(entity.enderecoId),
        repository.buscarCategorias(entity.id),
        pontos);
  }

  private EnderecoEntity buscarEndereco(
      Integer enderecoId) {
    if (enderecoId == null) {
      return null;
    }

    return enderecoRepository.findById(enderecoId)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Endereço não encontrado"));
  }

  private Integer salvarEndereco(
      Integer enderecoId,
      Endereco endereco) {
    if (endereco == null) {
      return null;
    }

    if (enderecoId != null) {
      var atual = buscarEndereco(enderecoId);

      if (Objects.equals(
          mapper.toDomain(atual),
          endereco)) {
        return enderecoId;
      }
    }

    return enderecoRepository
        .saveAndFlush(mapper.toEntity(endereco)).id;
  }
}