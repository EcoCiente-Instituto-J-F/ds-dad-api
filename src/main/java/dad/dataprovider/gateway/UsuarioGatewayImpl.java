package dad.dataprovider.gateway;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.CriarUsuario;
import dad.core.domain.EditarUsuario;
import dad.core.domain.Endereco;
import dad.core.domain.Pagina;
import dad.core.domain.Usuario;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.UsuarioGateway;
import dad.dataprovider.entity.EnderecoEntity;
import dad.dataprovider.entity.UsuarioEntity;
import dad.dataprovider.mapper.UsuarioPersistenceMapper;
import dad.dataprovider.repository.CondominioRepository;
import dad.dataprovider.repository.EnderecoRepository;
import dad.dataprovider.repository.UsuarioRepository;

@Component
@Transactional
public class UsuarioGatewayImpl implements UsuarioGateway {

  private final UsuarioRepository repository;
  private final EnderecoRepository enderecoRepository;
  private final CondominioRepository condominioRepository;
  private final UsuarioPersistenceMapper mapper;
  private final PasswordEncoder passwordEncoder;

  public UsuarioGatewayImpl(
      UsuarioRepository repository,
      EnderecoRepository enderecoRepository,
      CondominioRepository condominioRepository,
      UsuarioPersistenceMapper mapper,
      PasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.enderecoRepository = enderecoRepository;
    this.condominioRepository = condominioRepository;
    this.mapper = mapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional(readOnly = true)
  public Pagina<Usuario> listar(
      String busca,
      Boolean ativo,
      Integer tipoUsuarioId,
      int pagina,
      int tamanho) {
    var pageable = PageRequest.of(
        pagina,
        tamanho,
        Sort.by("nome").ascending()
            .and(Sort.by("id").ascending()));

    var resultado = repository.buscar(
        busca == null ? "" : busca.trim(),
        ativo,
        tipoUsuarioId,
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
  public Optional<Usuario> buscarPorId(Integer id) {
    return repository.findById(id)
        .map(this::toDomain);
  }

  @Override
  public Usuario criar(CriarUsuario comando) {
    repository.bloquearEscritas();

    validarEmail(comando.email(), 0);

    if (comando.cpf() != null
        && repository.existsByCpf(comando.cpf())) {
      throw new RegraNegocioException(
          "CPF já cadastrado");
    }

    String tipo = buscarTipo(
        comando.tipoUsuarioId());

    String codigoTipo = normalizarTipo(tipo);

    if (codigoTipo.equals("MORADOR")) {
      validarCondominio(comando.condominioId());
    } else if (comando.condominioId() != null) {
      throw new RegraNegocioException(
          "Condomínio no cadastro é exclusivo do morador");
    }

    Integer enderecoId = salvarEndereco(
        null,
        comando.endereco());

    var entity = mapper.toNovaEntity(
        comando,
        passwordEncoder.encode(comando.senha()),
        enderecoId);

    repository.saveAndFlush(entity);

    if (codigoTipo.equals("SINDICO")) {
      repository.inserirSindico(entity.id);
    }

    if (codigoTipo.equals("MORADOR")) {
      Integer nivelId = repository
          .buscarNivelInicial()
          .orElseThrow(() -> new RegraNegocioException(
              "Nível morador_comum não encontrado no banco"));

      repository.inserirMorador(
          entity.id,
          comando.condominioId());

      repository.inserirVinculoMorador(
          entity.id,
          comando.condominioId(),
          nivelId);
    }

    return toDomain(entity);
  }

  @Override
  public Usuario atualizar(
      Integer id,
      EditarUsuario comando) {
    repository.bloquearEscritas();

    var entity = exigirComBloqueio(id);

    validarEmail(comando.email(), id);

    mapper.atualizar(entity, comando);

    entity.enderecoId = salvarEndereco(
        entity.enderecoId,
        comando.endereco());

    repository.flush();

    return toDomain(entity);
  }

  @Override
  public Usuario alterarStatus(
      Integer id,
      boolean ativo) {
    repository.bloquearEscritas();

    var entity = exigirComBloqueio(id);

    entity.ativo = ativo;

    repository.flush();

    return toDomain(entity);
  }

  private UsuarioEntity exigirComBloqueio(Integer id) {
    return repository.buscarComBloqueio(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Usuário não encontrado"));
  }

  private void validarEmail(
      String email,
      Integer ignorarId) {
    if (repository.existeEmail(email, ignorarId)) {
      throw new RegraNegocioException(
          "E-mail já cadastrado");
    }
  }

  private String buscarTipo(Integer tipoId) {
    return repository.buscarNomeTipo(tipoId)
        .orElseThrow(() -> new RegraNegocioException(
            "Tipo de usuário não encontrado"));
  }

  private void validarCondominio(Integer id) {
    if (id == null) {
      throw new RegraNegocioException(
          "Informe o condomínio do morador");
    }

    var condominio = condominioRepository
        .buscarComBloqueio(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Condomínio não encontrado"));

    if (!condominio.ativo) {
      throw new RegraNegocioException(
          "O condomínio está inativo");
    }
  }

  private Usuario toDomain(UsuarioEntity entity) {
    var endereco = buscarEndereco(
        entity.enderecoId);

    return mapper.toDomain(
        entity,
        endereco,
        buscarTipo(entity.tipoUsuarioId),
        repository.buscarCondominioMorador(entity.id)
            .orElse(null));
  }

  private EnderecoEntity buscarEndereco(Integer id) {
    return enderecoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Endereço do usuário não encontrado"));
  }

  private Integer salvarEndereco(
      Integer enderecoId,
      Endereco endereco) {
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

  private String normalizarTipo(String tipo) {
    return Normalizer
        .normalize(tipo, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .trim()
        .toUpperCase(Locale.ROOT)
        .replaceAll("[\\s-]+", "_");
  }
}