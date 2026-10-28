package dad.core.service;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dad.core.domain.AlternativaQuiz;
import dad.core.domain.PerguntaQuiz;
import dad.core.domain.Quiz;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.QuizGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

  @Mock
  private QuizGateway gateway;

  @InjectMocks
  private QuizService service;

  @ParameterizedTest
  @ValueSource(ints = { 0, -1 })
  void deveBloquearPontosInvalidos(int pontos) {
    var quiz = quiz(
        pontos,
        new BigDecimal("70"),
        perguntas(1));

    assertInvalido(quiz);
  }

  @ParameterizedTest
  @ValueSource(strings = { "-0.01", "100.01" })
  void deveBloquearNotaForaDoIntervalo(String nota) {
    var quiz = quiz(
        20,
        new BigDecimal(nota),
        perguntas(1));

    assertInvalido(quiz);
  }

  @Test
  void deveBloquearQuizSemPerguntas() {
    assertInvalido(
        quiz(
            20,
            new BigDecimal("70"),
            List.of()));
  }

  @Test
  void deveBloquearPerguntaSemAlternativaCorreta() {
    assertInvalido(
        quiz(
            20,
            new BigDecimal("70"),
            perguntas(0)));
  }

  @Test
  void deveBloquearPerguntaComDuasCorretas() {
    assertInvalido(
        quiz(
            20,
            new BigDecimal("70"),
            perguntas(2)));
  }

  @Test
  void deveBloquearAlternativaSemTexto() {
    var pergunta = new PerguntaQuiz(
        null,
        null,
        "Pergunta",
        1,
        List.of(
            new AlternativaQuiz(
                null,
                null,
                " ",
                true),
            new AlternativaQuiz(
                null,
                null,
                "Outra",
                false)));

    assertInvalido(
        quiz(
            20,
            new BigDecimal("70"),
            List.of(pergunta)));
  }

  @ParameterizedTest
  @ValueSource(strings = { "0", "100" })
  void deveAceitarLimitesDaNota(String nota) {
    var quiz = quiz(
        20,
        new BigDecimal(nota),
        perguntas(1));

    when(gateway.criar(quiz)).thenReturn(quiz);

    assertEquals(
        quiz,
        service.criar(quiz));

    verify(gateway).criar(quiz);
  }

  private void assertInvalido(Quiz quiz) {
    assertThrows(
        RegraNegocioException.class,
        () -> service.criar(quiz));

    verifyNoInteractions(gateway);
  }

  private Quiz quiz(
      Integer pontos,
      BigDecimal nota,
      List<PerguntaQuiz> perguntas) {
    return new Quiz(
        null,
        1,
        2,
        "Quiz",
        nota,
        pontos,
        true,
        null,
        perguntas);
  }

  private List<PerguntaQuiz> perguntas(
      int quantidadeCorretas) {
    return List.of(
        new PerguntaQuiz(
            null,
            null,
            "Qual material pode ser reciclado?",
            1,
            List.of(
                new AlternativaQuiz(
                    null,
                    null,
                    "Primeira alternativa",
                    quantidadeCorretas >= 1),
                new AlternativaQuiz(
                    null,
                    null,
                    "Segunda alternativa",
                    quantidadeCorretas >= 2))));
  }
}