package dad.entrypoint.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dad.core.service.UsuarioService;
import dad.entrypoint.dto.request.StatusRequest;
import dad.entrypoint.dto.request.UsuarioCriarRequest;
import dad.entrypoint.dto.request.UsuarioEditarRequest;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.dto.response.UsuarioResponse;
import dad.entrypoint.mapper.UsuarioMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/v1/usuarios")
public class UsuarioController {

  private final UsuarioService service;
  private final UsuarioMapper mapper;

  public UsuarioController(
      UsuarioService service,
      UsuarioMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
  public PaginaResponse<UsuarioResponse> listar(
      @RequestParam(defaultValue = "") String busca,
      @RequestParam(required = false) Boolean ativo,
      @RequestParam(required = false) Integer tipoUsuarioId,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "20") int tamanho) {
    return mapper.toResponse(
        service.listar(
            busca,
            ativo,
            tipoUsuarioId,
            pagina,
            tamanho));
  }

  @GetMapping("/{id}")
  public UsuarioResponse buscar(
      @PathVariable Integer id) {
    return mapper.toResponse(
        service.buscar(id));
  }

  @PostMapping
  public ResponseEntity<UsuarioResponse> criar(
      @Valid @RequestBody UsuarioCriarRequest request) {
    var usuario = service.criar(
        mapper.toDomain(request));

    return ResponseEntity
        .created(URI.create(
            "/api/admin/v1/usuarios/" + usuario.id()))
        .body(mapper.toResponse(usuario));
  }

  @PatchMapping("/{id}")
  public UsuarioResponse atualizar(
      @PathVariable Integer id,
      @Valid @RequestBody UsuarioEditarRequest request) {
    return mapper.toResponse(
        service.atualizar(
            id,
            mapper.toDomain(request)));
  }

  @PatchMapping("/{id}/status")
  public UsuarioResponse alterarStatus(
      @PathVariable Integer id,
      @Valid @RequestBody StatusRequest request) {
    return mapper.toResponse(
        service.alterarStatus(
            id,
            request.ativo()));
  }
}