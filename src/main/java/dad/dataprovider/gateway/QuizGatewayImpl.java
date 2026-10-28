package dad.dataprovider.gateway;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Pagina;
import dad.core.domain.PerguntaQuiz;
import dad.core.domain.Quiz;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.QuizGateway;
import dad.dataprovider.entity.QuizEntity;
import dad.dataprovider.mapper.QuizPersistenceMapper;
import dad.dataprovider.repository.AlternativaQuizRepository;
import dad.dataprovider.repository.AulaRepository;
import dad.dataprovider.repository.PerguntaQuizRepository;
import dad.dataprovider.repository.QuizRepository;

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
  public Pagina<Quiz> listar(
      String busca,
      int pagina,
      int tamanho) {
    var pageable = PageRequest.of(
        pagina,
        tamanho,
        Sort.by("titulo").ascending()
            .and(Sort.by("id").ascending()));

    var resultado = quizRepository.buscar(
        busca == null ? "" : busca.trim(),
        pageable);

    var quizzes = resultado.getContent()
        .stream()
        .map(this::toDomain)
        .toList();

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
    return quizRepository.findById(id)
        .map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean aulaPertenceAoCurso(
      Integer aulaId,
      Integer cursoId) {
    return aulaRepository.findById(aulaId)
        .map(aula -> Objects.equals(aula.cursoId, cursoId))
        .orElse(false);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existeOutroQuizNaAula(
      Integer aulaId,
      Integer quizIdIgnorado) {
    if (quizIdIgnorado == null) {
      return quizRepository.existsByAulaId(aulaId);
    }

    return quizRepository.existsByAulaIdAndIdNot(
        aulaId,
        quizIdIgnorado);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean possuiTentativas(Integer quizId) {
    return quizRepository.contarTentativas(quizId) > 0;
  }

  @Override
  public Quiz criar(Quiz quiz) {
    validarEstrutura(quiz);
    bloquearEValidarAula(quiz);

    if (existeOutroQuizNaAula(quiz.aulaId(), null)) {
      throw new RegraNegocioException(
          "A aula já possui um quiz");
    }

    var entity = mapper.toNovaEntity(quiz);

    quizRepository.saveAndFlush(entity);
    salvarPerguntas(entity.id, quiz.perguntas());

    return toDomain(entity);
  }

  @Override
  public Quiz atualizar(Quiz quiz) {
    validarEstrutura(quiz);

    var entity = exigirComBloqueio(quiz.id());

    if (possuiTentativas(entity.id)) {
      var atual = toDomain(entity);

      if (!mesmaEstrutura(atual, quiz)) {
        throw new RegraNegocioException(
            "O quiz possui tentativas: preserve "
                + "curso, aula, perguntas, nota e pontos");
      }

      entity.titulo = quiz.titulo();
      entity.ativo = quiz.ativo();

      quizRepository.flush();

      return toDomain(entity);
    }

    bloquearEValidarAula(quiz);

    if (existeOutroQuizNaAula(
        quiz.aulaId(),
        entity.id)) {
      throw new RegraNegocioException(
          "A aula já possui outro quiz");
    }

    removerPerguntas(entity.id);
    mapper.atualizar(entity, quiz);

    quizRepository.flush();
    salvarPerguntas(entity.id, quiz.perguntas());

    return toDomain(entity);
  }

  @Override
  public void excluir(Integer quizId) {
    var entity = exigirComBloqueio(quizId);

    if (possuiTentativas(quizId)) {
      throw new RegraNegocioException(
          "Não é possível excluir um quiz com tentativas");
    }

    removerPerguntas(quizId);

    quizRepository.delete(entity);
    quizRepository.flush();
  }

  private QuizEntity exigirComBloqueio(Integer id) {
    return quizRepository.buscarComBloqueio(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Quiz não encontrado"));
  }

  private void bloquearEValidarAula(Quiz quiz) {
    var aula = aulaRepository
        .buscarComBloqueio(quiz.aulaId())
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Aula não encontrada"));

    if (!Objects.equals(aula.cursoId, quiz.cursoId())) {
      throw new RegraNegocioException(
          "A aula não pertence ao curso informado");
    }
  }

  private Quiz toDomain(QuizEntity entity) {
    var perguntas = perguntaRepository
        .findByQuizIdOrderByOrdemAsc(entity.id)
        .stream()
        .map(pergunta -> mapper.toDomain(
            pergunta,
            alternativaRepository
                .findByPerguntaIdOrderByIdAsc(pergunta.id)))
        .toList();

    return mapper.toDomain(entity, perguntas);
  }

  private void salvarPerguntas(
      Integer quizId,
      List<PerguntaQuiz> perguntas) {
    for (int indice = 0; indice < perguntas.size(); indice++) {
      var pergunta = perguntas.get(indice);

      var entity = mapper.toNovaEntity(
          pergunta,
          quizId,
          indice + 1);

      perguntaRepository.saveAndFlush(entity);

      var alternativas = pergunta.alternativas()
          .stream()
          .map(alternativa -> mapper.toNovaEntity(
              alternativa,
              entity.id))
          .toList();

      alternativaRepository.saveAllAndFlush(
          alternativas);
    }
  }

  private void removerPerguntas(Integer quizId) {
    alternativaRepository.excluirPorQuiz(quizId);
    perguntaRepository.excluirPorQuiz(quizId);
  }

  private void validarEstrutura(Quiz quiz) {
    if (quiz.perguntas() == null
        || quiz.perguntas().isEmpty()) {
      throw new RegraNegocioException(
          "Informe pelo menos uma pergunta");
    }

    for (var pergunta : quiz.perguntas()) {
      if (pergunta == null
          || pergunta.alternativas() == null
          || pergunta.alternativas().size() < 2) {
        throw new RegraNegocioException(
            "Cada pergunta precisa de pelo menos duas alternativas");
      }

      if (pergunta.alternativas()
          .stream()
          .anyMatch(Objects::isNull)) {
        throw new RegraNegocioException(
            "Alternativa inválida");
      }

      long corretas = pergunta.alternativas()
          .stream()
          .filter(alternativa -> alternativa.correta())
          .count();

      if (corretas != 1) {
        throw new RegraNegocioException(
            "Cada pergunta precisa de uma única alternativa correta");
      }
    }
  }

  private boolean mesmaEstrutura(
      Quiz atual,
      Quiz recebido) {
    if (!Objects.equals(
        atual.cursoId(),
        recebido.cursoId())
        || !Objects.equals(
            atual.aulaId(),
            recebido.aulaId())
        || !Objects.equals(
            atual.pontos(),
            recebido.pontos())
        || atual.notaMinima() == null
        || recebido.notaMinima() == null
        || atual.notaMinima().compareTo(
            recebido.notaMinima()) != 0
        || atual.perguntas().size() != recebido.perguntas().size()) {
      return false;
    }

    for (int i = 0; i < atual.perguntas().size(); i++) {
      var primeira = atual.perguntas().get(i);
      var segunda = recebido.perguntas().get(i);

      if (!Objects.equals(
          primeira.enunciado(),
          segunda.enunciado())
          || primeira.alternativas().size() != segunda.alternativas().size()) {
        return false;
      }

      for (int j = 0; j < primeira.alternativas().size(); j++) {
        var alternativaAtual = primeira.alternativas().get(j);

        var alternativaRecebida = segunda.alternativas().get(j);

        if (!Objects.equals(
            alternativaAtual.texto(),
            alternativaRecebida.texto())
            || alternativaAtual.correta() != alternativaRecebida.correta()) {
          return false;
        }
      }
    }

    return true;
  }
}