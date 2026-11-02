package dad.dataprovider.gateway;

import dad.core.domain.Aula;
import dad.core.domain.Curso;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CursoGateway;
import dad.core.regra.RegrasCurso;
import dad.dataprovider.entity.AulaEntity;
import dad.dataprovider.entity.CursoEntity;
import dad.dataprovider.mapper.CursoPersistenceMapper;
import dad.dataprovider.repository.AulaRepository;
import dad.dataprovider.repository.CursoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CursoGatewayImpl implements CursoGateway {

  private final CursoRepository cursoRepository;
  private final AulaRepository aulaRepository;
  private final CursoPersistenceMapper mapper;

  public CursoGatewayImpl(
      CursoRepository cursoRepository,
      AulaRepository aulaRepository,
      CursoPersistenceMapper mapper) {
    this.cursoRepository = cursoRepository;
    this.aulaRepository = aulaRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Pagina<Curso> listar(String busca, int pagina, int tamanho) {
    var pageable =
        PageRequest.of(
            pagina, tamanho, Sort.by("titulo").ascending().and(Sort.by("id").ascending()));

    var resultado = cursoRepository.buscar(busca == null ? "" : busca.trim(), pageable);

    var cursos = resultado.getContent().stream().map(this::toDomain).toList();

    return new Pagina<>(
        cursos,
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Curso> buscarPorId(Integer id) {
    return cursoRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Curso criar(Curso curso) {
    RegrasCurso.validarListaAulas(curso.aulas());

    RegrasCurso.validarCriacao(curso.aulas());

    var entity = new CursoEntity();

    mapper.atualizar(entity, curso);

    cursoRepository.saveAndFlush(entity);

    salvarAulas(entity.id, curso.aulas(), Map.of());

    return toDomain(entity);
  }

  @Override
  public Curso atualizar(Curso curso) {
    RegrasCurso.validarListaAulas(curso.aulas());

    var entity =
        cursoRepository
            .buscarComBloqueio(curso.id())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado"));

    var existentes = aulaRepository.findByCursoIdOrderByOrdemAsc(entity.id);

    Map<Integer, AulaEntity> porId =
        existentes.stream().collect(Collectors.toMap(aula -> aula.id, Function.identity()));

    Set<Integer> idsRecebidos = RegrasCurso.validarIds(curso.aulas(), porId.keySet());

    var removidas = existentes.stream().filter(aula -> !idsRecebidos.contains(aula.id)).toList();

    validarRemocao(removidas);

    aulaRepository.deleteAll(removidas);
    aulaRepository.flush();

    /*
     * A ordem é única por curso.
     * Move temporariamente as aulas mantidas para
     * posições livres antes de aplicar a nova ordem.
     */
    var mantidas = existentes.stream().filter(aula -> idsRecebidos.contains(aula.id)).toList();

    reservarOrdens(mantidas, curso.aulas().size());

    mapper.atualizar(entity, curso);

    salvarAulas(entity.id, curso.aulas(), porId);

    cursoRepository.flush();

    return toDomain(entity);
  }

  private Curso toDomain(CursoEntity entity) {
    var aulas = aulaRepository.findByCursoIdOrderByOrdemAsc(entity.id);

    return mapper.toDomain(entity, aulas);
  }

  private void validarRemocao(List<AulaEntity> removidas) {
    for (var aula : removidas) {
      RegrasCurso.validarRemocaoComQuiz(aulaRepository.contarQuizzes(aula.id));
      RegrasCurso.validarRemocaoComProgresso(aulaRepository.contarProgressos(aula.id));
    }
  }

  private void reservarOrdens(List<AulaEntity> aulas, int quantidadeRecebida) {
    long maiorOrdem =
        aulas.stream().mapToLong(aula -> aula.ordem == null ? 0 : aula.ordem).max().orElse(0);

    long proxima = Math.max(maiorOrdem, quantidadeRecebida) + 1;

    if (proxima + aulas.size() > Integer.MAX_VALUE) {
      throw new RegraNegocioException("Não foi possível reorganizar as aulas");
    }

    for (var aula : aulas) {
      aula.ordem = (int) proxima++;
    }

    aulaRepository.saveAllAndFlush(aulas);
  }

  private void salvarAulas(Integer cursoId, List<Aula> aulas, Map<Integer, AulaEntity> existentes) {
    List<AulaEntity> entidades = new ArrayList<>();

    for (int indice = 0; indice < aulas.size(); indice++) {
      var aula = aulas.get(indice);

      var entity = aula.id() == null ? new AulaEntity() : existentes.get(aula.id());

      mapper.atualizar(entity, aula, cursoId, indice + 1);

      entidades.add(entity);
    }

    aulaRepository.saveAllAndFlush(entidades);
  }
}
