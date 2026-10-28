package dad.core.gateway;

import java.util.Optional;

import dad.core.domain.Pagina;
import dad.core.domain.Quiz;

public interface QuizGateway {

    Pagina<Quiz> listar(
        String busca,
        int pagina,
        int tamanho
    );

    Optional<Quiz> buscarPorId(Integer id);

    boolean aulaPertenceAoCurso(
        Integer aulaId,
        Integer cursoId
    );

    boolean existeOutroQuizNaAula(
        Integer aulaId,
        Integer quizIdIgnorado
    );

    boolean possuiTentativas(Integer quizId);

    Quiz criar(Quiz quiz);

    Quiz atualizar(Quiz quiz);

    void excluir(Integer quizId);
}