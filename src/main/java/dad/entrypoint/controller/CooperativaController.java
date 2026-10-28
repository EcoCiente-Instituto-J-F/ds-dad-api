package dad.entrypoint.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dad.core.service.CooperativaService;
import dad.entrypoint.dto.request.CooperativaRequest;
import dad.entrypoint.dto.response.CooperativaResponse;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.mapper.CooperativaMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/v1/cooperativas")
public class CooperativaController {

  private final CooperativaService service;
  private final CooperativaMapper mapper;

  public CooperativaController(
      CooperativaService service,
      CooperativaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
  public PaginaResponse<CooperativaResponse> listar(
      @RequestParam(defaultValue = "") String busca,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanho) {
    return mapper.toResponse(
        service.listar(busca, pagina, tamanho));
  }

  @GetMapping("/{id}")
  public CooperativaResponse buscar(
      @PathVariable Integer id) {
    return mapper.toResponse(
        service.buscar(id));
  }

  @PostMapping
  public ResponseEntity<CooperativaResponse> criar(
      @Valid @RequestBody CooperativaRequest request) {
    var cooperativa = service.criar(
        mapper.toDomain(null, request));

    return ResponseEntity
        .created(URI.create(
            "/api/admin/v1/cooperativas/"
                + cooperativa.id()))
        .body(mapper.toResponse(cooperativa));
  }

  @PatchMapping("/{id}")
  public CooperativaResponse atualizar(
      @PathVariable Integer id,
      @Valid @RequestBody CooperativaRequest request) {
    return mapper.toResponse(
        service.atualizar(
            mapper.toDomain(id, request)));
  }
}