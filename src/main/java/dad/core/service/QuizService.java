package dad.core.service;

import dad.core.domain.Pagina;
import dad.core.domain.Quiz;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.gateway.QuizGateway;
import dad.core.regra.RegrasPaginacao;
import dad.core.regra.RegrasQuiz;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuizService {

  private final QuizGateway gateway;

  public QuizService(QuizGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Quiz> listar(String busca, int pagina, int tamanho) {
    RegrasPaginacao.validar(pagina, tamanho);

    return gateway.listar(busca, pagina, tamanho);
  }

  @Transactional(readOnly = true)
  public Quiz buscar(Integer id) {
    return gateway
        .buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Quiz não encontrado"));
  }

  public Quiz criar(Quiz quiz) {
    RegrasQuiz.validar(quiz);

    return gateway.criar(quiz);
  }

  public Quiz atualizar(Quiz quiz) {
    RegrasQuiz.validar(quiz);

    return gateway.atualizar(quiz);
  }

  public void excluir(Integer id) {
    gateway.excluir(id);
  }
}
