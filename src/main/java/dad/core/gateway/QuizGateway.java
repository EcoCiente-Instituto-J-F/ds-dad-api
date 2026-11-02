package dad.core.gateway;

import dad.core.domain.Pagina;
import dad.core.domain.Quiz;
import java.util.Optional;

public interface QuizGateway {

  Pagina<Quiz> listar(String busca, int pagina, int tamanho);

  Optional<Quiz> buscarPorId(Integer id);

  Quiz criar(Quiz quiz);

  Quiz atualizar(Quiz quiz);

  void excluir(Integer quizId);
}
