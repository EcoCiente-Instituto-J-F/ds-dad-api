package dad.core.regra;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dad.core.domain.AlternativaQuiz;
import dad.core.domain.Aula;
import dad.core.domain.PerguntaQuiz;
import dad.core.domain.Quiz;
import dad.core.domain.Torre;
import dad.core.exception.RegraNegocioException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegrasHistoricoTest {
  private Quiz quiz(String titulo, int pontos, boolean ativo, String alternativa) {
    return new Quiz(
        1,
        2,
        3,
        titulo,
        new BigDecimal("70"),
        pontos,
        ativo,
        null,
        List.of(
            new PerguntaQuiz(
                4,
                1,
                "Pergunta",
                1,
                List.of(
                    new AlternativaQuiz(5, 4, alternativa, true),
                    new AlternativaQuiz(6, 4, "Errada", false)))));
  }

  @Test
  void permiteTituloESituacaoDoQuizComHistorico() {
    assertDoesNotThrow(
        () ->
            RegrasQuiz.validarEdicaoComTentativas(
                quiz("Original", 20, true, "Correta"), quiz("Novo título", 20, false, "Correta")));
  }

  @Test
  void bloqueiaPontosEAlternativasMesmoComTituloValido() {
    var atual = quiz("Original", 20, true, "Correta");
    assertThrows(
        RegraNegocioException.class,
        () -> RegrasQuiz.validarEdicaoComTentativas(atual, quiz("Novo", 30, true, "Correta")));
    assertThrows(
        RegraNegocioException.class,
        () -> RegrasQuiz.validarEdicaoComTentativas(atual, quiz("Novo", 20, true, "Alterada")));
  }

  @Test
  void bloqueiaExclusaoComQualquerTentativa() {
    assertThrows(RegraNegocioException.class, () -> RegrasQuiz.validarExclusao(true));
    assertDoesNotThrow(() -> RegrasQuiz.validarExclusao(false));
  }

  @Test
  void bloqueiaAulaDeOutroCursoEOcupacaoDaMesmaAula() {
    assertThrows(RegraNegocioException.class, () -> RegrasQuiz.validarAula(false));
    assertThrows(RegraNegocioException.class, () -> RegrasQuiz.validarUnicidade(true, false));
  }

  @Test
  void naoRemoveAulaComQuizOuProgresso() {
    assertThrows(RegraNegocioException.class, () -> RegrasCurso.validarRemocaoComQuiz(1));
    assertThrows(RegraNegocioException.class, () -> RegrasCurso.validarRemocaoComProgresso(1));
    assertDoesNotThrow(() -> RegrasCurso.validarRemocaoComProgresso(0));
  }

  @Test
  void bloqueiaIdsDeAulasEstrangeirasOuRepetidas() {
    var a = new Aula(1, 2, "Título", "Conteúdo", 1);
    assertThrows(RegraNegocioException.class, () -> RegrasCurso.validarIds(List.of(a), Set.of(2)));
    assertThrows(
        RegraNegocioException.class, () -> RegrasCurso.validarIds(List.of(a, a), Set.of(1)));
  }

  @Test
  void bloqueiaRemocaoDeTorreComHistorico() {
    assertThrows(RegraNegocioException.class, () -> RegrasCondominio.validarRemocaoTorre(1));
    assertDoesNotThrow(() -> RegrasCondominio.validarRemocaoTorre(0));
  }

  @Test
  void torrePrecisaPertencerAoCondominioESerUnicaNoPedido() {
    var torre = new Torre(1, "Torre A");
    assertThrows(
        RegraNegocioException.class, () -> RegrasCondominio.validarIds(List.of(torre), Set.of(2)));
    assertThrows(
        RegraNegocioException.class,
        () -> RegrasCondominio.validarIds(List.of(torre, torre), Set.of(1)));
  }

  @Test
  void cooperativaExigeCategoriaEUsuarioAdequados() {
    assertThrows(RegraNegocioException.class, () -> RegrasCooperativa.validarUsuario(false));
    assertThrows(RegraNegocioException.class, () -> RegrasCooperativa.validarCategoria(99, false));
    assertThrows(RegraNegocioException.class, () -> RegrasCooperativa.validarVinculoUsuario(true));
  }
}
