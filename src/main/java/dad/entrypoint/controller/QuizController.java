package dad.entrypoint.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dad.core.service.QuizService;
import dad.entrypoint.dto.request.QuizRequest;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.dto.response.QuizResponse;
import dad.entrypoint.mapper.QuizMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/v1/quizzes")
public class QuizController {

  private final QuizService service;
  private final QuizMapper mapper;

  public QuizController(
      QuizService service,
      QuizMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
  public PaginaResponse<QuizResponse> listar(
      @RequestParam(defaultValue = "") String busca,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanho) {
    return mapper.toResponse(
        service.listar(busca, pagina, tamanho));
  }

  @GetMapping("/{id}")
  public QuizResponse buscar(
      @PathVariable Integer id) {
    return mapper.toResponse(
        service.buscar(id));
  }

  @PostMapping
  public ResponseEntity<QuizResponse> criar(
      @Valid @RequestBody QuizRequest request) {
    var quiz = service.criar(
        mapper.toDomain(null, request));

    return ResponseEntity
        .created(URI.create(
            "/api/admin/v1/quizzes/" + quiz.id()))
        .body(mapper.toResponse(quiz));
  }

  @PutMapping("/{id}")
  public QuizResponse atualizar(
      @PathVariable Integer id,
      @Valid @RequestBody QuizRequest request) {
    return mapper.toResponse(
        service.atualizar(
            mapper.toDomain(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(
      @PathVariable Integer id) {
    service.excluir(id);

    return ResponseEntity.noContent().build();
  }
}