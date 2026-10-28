package dad.entrypoint.controller;

import java.net.URI;
import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dad.core.service.ColetaService;
import dad.entrypoint.dto.request.ColetaRequest;
import dad.entrypoint.dto.request.VisitaColetaRequest;
import dad.entrypoint.dto.response.ColetaResponse;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.mapper.ColetaMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/v1/coletas")
public class ColetaController {

  private final ColetaService service;
  private final ColetaMapper mapper;

  public ColetaController(
      ColetaService service,
      ColetaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
  public PaginaResponse<ColetaResponse> listar(
      @RequestParam(defaultValue = "") String busca,
      @RequestParam(required = false) Integer statusId,

      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,

      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim,

      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanho) {
    return mapper.toResponse(
        service.listar(
            busca,
            statusId,
            inicio,
            fim,
            pagina,
            tamanho));
  }

  @GetMapping("/{id}")
  public ColetaResponse buscar(
      @PathVariable Integer id) {
    return mapper.toResponse(
        service.buscar(id));
  }

  @PostMapping
  public ResponseEntity<ColetaResponse> criar(
      @Valid @RequestBody ColetaRequest request) {
    var coleta = service.salvar(
        null,
        mapper.toDomain(request));

    return ResponseEntity
        .created(URI.create(
            "/api/admin/v1/coletas/" + coleta.id()))
        .body(mapper.toResponse(coleta));
  }

  @PatchMapping("/{id}")
  public ColetaResponse atualizar(
      @PathVariable Integer id,
      @Valid @RequestBody ColetaRequest request) {
    return mapper.toResponse(
        service.salvar(
            id,
            mapper.toDomain(request)));
  }

  @PostMapping("/{id}/cancelamento")
  public ColetaResponse cancelar(
      @PathVariable Integer id) {
    return mapper.toResponse(
        service.cancelar(id));
  }

  @PostMapping("/{id}/visitas")
  public ColetaResponse registrarVisita(
      @PathVariable Integer id,
      @Valid @RequestBody VisitaColetaRequest request) {
    return mapper.toResponse(
        service.registrarVisita(
            id,
            mapper.toDomain(request)));
  }
}