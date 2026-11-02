package dad.dataprovider.gateway;

import dad.core.domain.Condominio;
import dad.core.domain.Endereco;
import dad.core.domain.Pagina;
import dad.core.domain.Torre;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.gateway.CondominioGateway;
import dad.core.regra.RegrasCondominio;
import dad.dataprovider.entity.CondominioEntity;
import dad.dataprovider.entity.EnderecoEntity;
import dad.dataprovider.entity.TorreEntity;
import dad.dataprovider.mapper.CondominioPersistenceMapper;
import dad.dataprovider.repository.CondominioRepository;
import dad.dataprovider.repository.EnderecoRepository;
import dad.dataprovider.repository.TorreRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CondominioGatewayImpl implements CondominioGateway {

  private final CondominioRepository repository;
  private final EnderecoRepository enderecoRepository;
  private final TorreRepository torreRepository;
  private final CondominioPersistenceMapper mapper;

  public CondominioGatewayImpl(
      CondominioRepository repository,
      EnderecoRepository enderecoRepository,
      TorreRepository torreRepository,
      CondominioPersistenceMapper mapper) {
    this.repository = repository;
    this.enderecoRepository = enderecoRepository;
    this.torreRepository = torreRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Pagina<Condominio> listar(String busca, Boolean ativo, int pagina, int tamanho) {
    var pageable =
        PageRequest.of(pagina, tamanho, Sort.by("nome").ascending().and(Sort.by("id").ascending()));

    var resultado = repository.buscar(busca == null ? "" : busca.trim(), ativo, pageable);

    return new Pagina<>(
        resultado.getContent().stream().map(this::toDomain).toList(),
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Condominio> buscarPorId(Integer id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public Condominio criar(Condominio condominio) {
    repository.bloquearEscritas();

    validarReferencias(condominio);
    validarDuplicidades(condominio);

    RegrasCondominio.validarIdNovo(
        condominio.torres().stream().anyMatch(item -> item.id() != null));

    var entity = new CondominioEntity();

    mapper.atualizar(entity, condominio);

    entity.enderecoId = salvarEndereco(null, condominio.endereco());

    repository.saveAndFlush(entity);

    salvarTorres(entity.id, condominio.torres(), Map.of());

    return toDomain(entity);
  }

  @Override
  public Condominio atualizar(Condominio condominio) {
    repository.bloquearEscritas();

    var entity = exigirComBloqueio(condominio.id());

    validarReferencias(condominio);
    validarDuplicidades(condominio);

    var existentes = torreRepository.findByCondominioIdOrderByNomeAsc(entity.id);

    Map<Integer, TorreEntity> porId =
        existentes.stream().collect(Collectors.toMap(torre -> torre.id, Function.identity()));

    Set<Integer> recebidos = RegrasCondominio.validarIds(condominio.torres(), porId.keySet());

    var removidas = existentes.stream().filter(torre -> !recebidos.contains(torre.id)).toList();

    for (var torre : removidas) {
      RegrasCondominio.validarRemocaoTorre(torreRepository.contarReferencias(torre.id));
    }

    torreRepository.deleteAll(removidas);
    torreRepository.flush();

    var mantidas = existentes.stream().filter(torre -> recebidos.contains(torre.id)).toList();

    for (var torre : mantidas) {
      torre.nome = "__temporario_" + UUID.randomUUID();
    }

    torreRepository.saveAllAndFlush(mantidas);

    mapper.atualizar(entity, condominio);

    entity.enderecoId = salvarEndereco(entity.enderecoId, condominio.endereco());

    salvarTorres(entity.id, condominio.torres(), porId);

    repository.flush();

    return toDomain(entity);
  }

  @Override
  public Condominio alterarStatus(Integer id, boolean ativo) {
    repository.bloquearEscritas();

    var entity = exigirComBloqueio(id);

    entity.ativo = ativo;

    repository.flush();

    return toDomain(entity);
  }

  private CondominioEntity exigirComBloqueio(Integer id) {
    return repository
        .buscarComBloqueio(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Condomínio não encontrado"));
  }

  private Condominio toDomain(CondominioEntity entity) {
    EnderecoEntity endereco = null;

    if (entity.enderecoId != null) {
      endereco =
          enderecoRepository
              .findById(entity.enderecoId)
              .orElseThrow(
                  () -> new RecursoNaoEncontradoException("Endereço do condomínio não encontrado"));
    }

    var torres = torreRepository.findByCondominioIdOrderByNomeAsc(entity.id);

    return mapper.toDomain(entity, endereco, torres);
  }

  private void validarReferencias(Condominio condominio) {
    RegrasCondominio.validarTipo(repository.existeTipo(condominio.tipoId()));
    if (condominio.sindicoId() != null)
      RegrasCondominio.validarSindico(repository.existeSindicoAtivo(condominio.sindicoId()));
  }

  private void validarDuplicidades(Condominio condominio) {
    Integer ignorarId = condominio.id() == null ? 0 : condominio.id();
    if (condominio.cnpj() != null)
      RegrasCondominio.validarCnpj(repository.existeCnpj(condominio.cnpj(), ignorarId));
    if (condominio.codigoAcesso() != null)
      RegrasCondominio.validarCodigo(repository.existeCodigo(condominio.codigoAcesso(), ignorarId));
  }

  private Integer salvarEndereco(Integer enderecoId, Endereco endereco) {
    if (endereco == null) {
      return null;
    }

    if (enderecoId != null) {
      var atual =
          enderecoRepository
              .findById(enderecoId)
              .orElseThrow(() -> new RecursoNaoEncontradoException("Endereço não encontrado"));

      if (Objects.equals(mapper.toDomain(atual), endereco)) {
        return enderecoId;
      }
    }

    var novo = mapper.toEntity(endereco);

    return enderecoRepository.saveAndFlush(novo).id;
  }

  private void salvarTorres(
      Integer condominioId, List<Torre> torres, Map<Integer, TorreEntity> existentes) {
    var entidades =
        torres.stream()
            .map(
                torre -> {
                  var entity = torre.id() == null ? new TorreEntity() : existentes.get(torre.id());

                  entity.nome = torre.nome();
                  entity.condominioId = condominioId;

                  return entity;
                })
            .toList();

    torreRepository.saveAllAndFlush(entidades);
  }
}
