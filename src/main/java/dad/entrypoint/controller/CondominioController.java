package dad.entrypoint.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dad.core.service.CondominioService;
import dad.entrypoint.dto.request.CondominioRequest;
import dad.entrypoint.dto.request.StatusRequest;
import dad.entrypoint.dto.response.CondominioResponse;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.mapper.CondominioMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/v1/condominios")
public class CondominioController {

  private final CondominioService service;
  private final CondominioMapper mapper;

  public CondominioController(
      CondominioService service,
      CondominioMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
  public PaginaResponse<CondominioResponse> listar(
      @RequestParam(defaultValue = "") String busca,
      @RequestParam(required = false) Boolean ativo,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanho) {
    return mapper.toResponse(
        service.listar(
            busca,
            ativo,
            pagina,
            tamanho));
  }

  @GetMapping("/{id}")
  public CondominioResponse buscar(
      @PathVariable Integer id) {
    return mapper.toResponse(
        service.buscar(id));
  }

  @PostMapping
  public ResponseEntity<CondominioResponse> criar(
      @Valid @RequestBody CondominioRequest request) {
    var condominio = service.criar(
        mapper.toDomain(null, request));

    return ResponseEntity
        .created(URI.create(
            "/api/admin/v1/condominios/"
                + condominio.id()))
        .body(mapper.toResponse(condominio));
  }

  @PatchMapping("/{id}")
  public CondominioResponse atualizar(
      @PathVariable Integer id,
      @Valid @RequestBody CondominioRequest request) {
    return mapper.toResponse(
        service.atualizar(
            mapper.toDomain(id, request)));
  }

  @PatchMapping("/{id}/status")
  public CondominioResponse alterarStatus(
      @PathVariable Integer id,
      @Valid @RequestBody StatusRequest request) {
    return mapper.toResponse(
        service.alterarStatus(
            id,
            request.ativo()));
  }
}