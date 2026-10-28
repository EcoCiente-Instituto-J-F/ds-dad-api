package dad.integration;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;

public class ConcorrenciaIntegrationTest
    extends AbstractIntegrationTest {

  @Test
  void naoDeveCadastrarMesmoEmailDuasVezes()
      throws Exception {

    var corpo = usuario();

    corpo.put("cpf", null);

    var resultados = executarEmParalelo(
        () -> codigo(
            HttpMethod.POST,
            "/api/admin/v1/usuarios",
            corpo),
        () -> codigo(
            HttpMethod.POST,
            "/api/admin/v1/usuarios",
            corpo));

    assertEquals(
        List.of(201, 409),
        resultados);

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_usuarios
            WHERE LOWER(email_usuario) = ?
            """,
            "pessoa@teste.com"));
  }

  @Test
  void naoDeveCadastrarDoisQuizzesNaMesmaAula()
      throws Exception {

    var curso = criarCurso();

    int cursoId = curso.get("id").asInt();
    int aulaId = curso.get("aulas")
        .get(0).get("id").asInt();

    var corpo = quiz(cursoId, aulaId);

    var resultados = executarEmParalelo(
        () -> codigo(
            HttpMethod.POST,
            "/api/admin/v1/quizzes",
            corpo),
        () -> codigo(
            HttpMethod.POST,
            "/api/admin/v1/quizzes",
            corpo));

    assertEquals(
        List.of(201, 409),
        resultados);

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_quizzes
            WHERE aula_id = ?
            """,
            aulaId));

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_perguntas_quiz p
            JOIN tb_quizzes q
                ON q.id_quiz = p.quiz_id
            WHERE q.aula_id = ?
            """,
            aulaId));

    assertEquals(
        2,
        contar("""
            SELECT COUNT(*)
            FROM tb_alternativas_quiz a
            JOIN tb_perguntas_quiz p
                ON p.id_pergunta = a.pergunta_id
            JOIN tb_quizzes q
                ON q.id_quiz = p.quiz_id
            WHERE q.aula_id = ?
            """,
            aulaId));
  }

  @Test
  void naoDeveRegistrarMesmaVisitaDuasVezes()
      throws Exception {

    var criada = criar(
        "/api/admin/v1/coletas",
        coleta());

    int coletaId = criada.get("id").asInt();

    var corpo = dados(
        "data", dataVisita(),
        "realizada", true,
        "confirmada", false,
        "observacao", "Coleta realizada");

    String caminho = "/api/admin/v1/coletas/"
        + coletaId + "/visitas";

    var resultados = executarEmParalelo(
        () -> codigo(
            HttpMethod.POST,
            caminho,
            corpo),
        () -> codigo(
            HttpMethod.POST,
            caminho,
            corpo));

    assertEquals(
        List.of(200, 409),
        resultados);

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_visitas_coletas
            WHERE agendamento_coleta_id = ?
            """,
            coletaId));
  }

  @Test
  void naoDeveCadastrarMesmoCodigoDeCondominioDuasVezes()
      throws Exception {

    var primeiro = condominio();
    var segundo = condominio();

    segundo.put("cnpj", "12345678000195");
    segundo.put("nome", "Segundo condomínio");

    var resultados = executarEmParalelo(
        () -> codigo(
            HttpMethod.POST,
            "/api/admin/v1/condominios",
            primeiro),
        () -> codigo(
            HttpMethod.POST,
            "/api/admin/v1/condominios",
            segundo));

    assertEquals(
        List.of(201, 409),
        resultados);

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_condominios
            WHERE codigo_acesso = 'NOVO-01'
            """));
  }

  private int codigo(
      HttpMethod metodo,
      String caminho,
      Object corpo) throws Exception {
    return chamar(
        metodo,
        caminho,
        corpo)
        .andReturn()
        .getResponse()
        .getStatus();
  }

  private List<Integer> executarEmParalelo(
      Callable<Integer> primeira,
      Callable<Integer> segunda) throws Exception {

    ExecutorService executor = Executors.newFixedThreadPool(2);

    CountDownLatch prontas = new CountDownLatch(2);
    CountDownLatch largada = new CountDownLatch(1);

    try {
      Future<Integer> resultado1 = executor.submit(
          prepararTarefa(
              primeira,
              prontas,
              largada));

      Future<Integer> resultado2 = executor.submit(
          prepararTarefa(
              segunda,
              prontas,
              largada));

      assertTrue(
          prontas.await(10, TimeUnit.SECONDS),
          "As duas tarefas precisam estar prontas");

      largada.countDown();

      int codigo1 = resultado1.get(
          30,
          TimeUnit.SECONDS);

      int codigo2 = resultado2.get(
          30,
          TimeUnit.SECONDS);

      return List.of(codigo1, codigo2)
          .stream()
          .sorted()
          .toList();

    } finally {
      largada.countDown();
      executor.shutdownNow();

      if (!executor.awaitTermination(
          10,
          TimeUnit.SECONDS)) {
        throw new IllegalStateException(
            "As tarefas concorrentes não terminaram");
      }
    }
  }

  private Callable<Integer> prepararTarefa(
      Callable<Integer> tarefa,
      CountDownLatch prontas,
      CountDownLatch largada) {
    return () -> {
      prontas.countDown();

      if (!largada.await(
          15,
          TimeUnit.SECONDS)) {
        throw new IllegalStateException(
            "Tempo excedido aguardando a largada");
      }

      return tarefa.call();
    };
  }
}