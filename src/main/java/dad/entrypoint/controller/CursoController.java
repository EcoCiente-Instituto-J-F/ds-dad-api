package dad.entrypoint.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dad.core.service.CursoService;
import dad.entrypoint.dto.request.CursoRequest;
import dad.entrypoint.dto.response.CursoResponse;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.mapper.CursoMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/v1/cursos")
public class CursoController {

  private final CursoService service;
  private final CursoMapper mapper;

  public CursoController(
      CursoService service,
      CursoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
  public PaginaResponse<CursoResponse> listar(
      @RequestParam(defaultValue = "") String busca,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanho) {
    return mapper.toResponse(
        service.listar(busca, pagina, tamanho));
  }

  @GetMapping("/{id}")
  public CursoResponse buscar(
      @PathVariable Integer id) {
    return mapper.toResponse(
        service.buscar(id));
  }

  @PostMapping
  public ResponseEntity<CursoResponse> criar(
      @Valid @RequestBody CursoRequest request) {
    var curso = service.criar(
        mapper.toDomain(null, request));

    return ResponseEntity
        .created(URI.create(
            "/api/admin/v1/cursos/" + curso.id()))
        .body(mapper.toResponse(curso));
  }

  @PatchMapping("/{id}")
  public CursoResponse atualizar(
      @PathVariable Integer id,
      @Valid @RequestBody CursoRequest request) {
    return mapper.toResponse(
        service.atualizar(
            mapper.toDomain(id, request)));
  }
}