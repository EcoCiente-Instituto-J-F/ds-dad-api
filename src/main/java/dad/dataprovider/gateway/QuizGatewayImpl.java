package dad.dataprovider.gateway;

import dad.core.domain.Pagina;
import dad.core.domain.PerguntaQuiz;
import dad.core.domain.Quiz;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.gateway.QuizGateway;
import dad.core.regra.RegrasQuiz;
import dad.dataprovider.entity.QuizEntity;
import dad.dataprovider.mapper.QuizPersistenceMapper;
import dad.dataprovider.repository.AlternativaQuizRepository;
import dad.dataprovider.repository.AulaRepository;
import dad.dataprovider.repository.PerguntaQuizRepository;
import dad.dataprovider.repository.QuizRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class QuizGatewayImpl implements QuizGateway {

  private final QuizRepository quizRepository;
  private final AulaRepository aulaRepository;
  private final PerguntaQuizRepository perguntaRepository;
  private final AlternativaQuizRepository alternativaRepository;
  private final QuizPersistenceMapper mapper;

  public QuizGatewayImpl(
      QuizRepository quizRepository,
      AulaRepository aulaRepository,
      PerguntaQuizRepository perguntaRepository,
      AlternativaQuizRepository alternativaRepository,
      QuizPersistenceMapper mapper) {
    this.quizRepository = quizRepository;
    this.aulaRepository = aulaRepository;
    this.perguntaRepository = perguntaRepository;
    this.alternativaRepository = alternativaRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Pagina<Quiz> listar(String busca, int pagina, int tamanho) {
    var pageable =
        PageRequest.of(
            pagina, tamanho, Sort.by("titulo").ascending().and(Sort.by("id").ascending()));

    var resultado = quizRepository.buscar(busca == null ? "" : busca.trim(), pageable);

    var quizzes = resultado.getContent().stream().map(this::toDomain).toList();

    return new Pagina<>(
        quizzes,
        resultado.getNumber(),
        resultado.getSize(),
        resultado.getTotalElements(),
        resultado.getTotalPages());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Quiz> buscarPorId(Integer id) {
    return quizRepository.findById(id).map(this::toDomain);
  }

  private boolean existeOutroQuizNaAula(Integer aulaId, Integer quizIdIgnorado) {
    if (quizIdIgnorado == null) {
      return quizRepository.existsByAulaId(aulaId);
    }

    return quizRepository.existsByAulaIdAndIdNot(aulaId, quizIdIgnorado);
  }

  private boolean possuiTentativas(Integer quizId) {
    return quizRepository.contarTentativas(quizId) > 0;
  }

  @Override
  public Quiz criar(Quiz quiz) {
    RegrasQuiz.validar(quiz);
    bloquearEValidarAula(quiz);

    RegrasQuiz.validarUnicidade(existeOutroQuizNaAula(quiz.aulaId(), null), false);

    var entity = mapper.toNovaEntity(quiz);

    quizRepository.saveAndFlush(entity);
    salvarPerguntas(entity.id, quiz.perguntas());

    return toDomain(entity);
  }

  @Override
  public Quiz atualizar(Quiz quiz) {
    RegrasQuiz.validar(quiz);

    var entity = exigirComBloqueio(quiz.id());

    if (possuiTentativas(entity.id)) {
      var atual = toDomain(entity);

      RegrasQuiz.validarEdicaoComTentativas(atual, quiz);

      entity.titulo = quiz.titulo();
      entity.ativo = quiz.ativo();

      quizRepository.flush();

      return toDomain(entity);
    }

    bloquearEValidarAula(quiz);

    RegrasQuiz.validarUnicidade(existeOutroQuizNaAula(quiz.aulaId(), entity.id), true);

    removerPerguntas(entity.id);
    mapper.atualizar(entity, quiz);

    quizRepository.flush();
    salvarPerguntas(entity.id, quiz.perguntas());

    return toDomain(entity);
  }

  @Override
  public void excluir(Integer quizId) {
    var entity = exigirComBloqueio(quizId);

    RegrasQuiz.validarExclusao(possuiTentativas(quizId));

    removerPerguntas(quizId);

    quizRepository.delete(entity);
    quizRepository.flush();
  }

  private QuizEntity exigirComBloqueio(Integer id) {
    return quizRepository
        .buscarComBloqueio(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz não encontrado"));
  }

  private void bloquearEValidarAula(Quiz quiz) {
    var aula =
        aulaRepository
            .buscarComBloqueio(quiz.aulaId())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aula não encontrada"));

    RegrasQuiz.validarAula(Objects.equals(aula.cursoId, quiz.cursoId()));
  }

  private Quiz toDomain(QuizEntity entity) {
    var perguntas =
        perguntaRepository.findByQuizIdOrderByOrdemAsc(entity.id).stream()
            .map(
                pergunta ->
                    mapper.toDomain(
                        pergunta, alternativaRepository.findByPerguntaIdOrderByIdAsc(pergunta.id)))
            .toList();

    var quiz = mapper.toDomain(entity, perguntas);
    return new Quiz(
        quiz.id(),
        quiz.cursoId(),
        quiz.aulaId(),
        quiz.titulo(),
        quiz.notaMinima(),
        quiz.pontos(),
        quiz.ativo(),
        quiz.criadoEm(),
        quiz.perguntas(),
        quizRepository.contarTentativas(entity.id));
  }

  private void salvarPerguntas(Integer quizId, List<PerguntaQuiz> perguntas) {
    for (int indice = 0; indice < perguntas.size(); indice++) {
      var pergunta = perguntas.get(indice);

      var entity = mapper.toNovaEntity(pergunta, quizId, indice + 1);

      perguntaRepository.saveAndFlush(entity);

      var alternativas =
          pergunta.alternativas().stream()
              .map(alternativa -> mapper.toNovaEntity(alternativa, entity.id))
              .toList();

      alternativaRepository.saveAllAndFlush(alternativas);
    }
  }

  private void removerPerguntas(Integer quizId) {
    alternativaRepository.excluirPorQuiz(quizId);
    perguntaRepository.excluirPorQuiz(quizId);
  }
}
