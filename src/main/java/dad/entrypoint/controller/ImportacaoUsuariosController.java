package dad.entrypoint.controller;

import dad.core.service.ImportacaoUsuariosService;
import dad.entrypoint.dto.request.ImportacaoUsuariosRequest;
import dad.entrypoint.dto.response.ImportacaoUsuariosResponse;
import dad.entrypoint.mapper.UsuarioMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/usuarios/importacao")
@Tag(name = "Importação de usuários")
public class ImportacaoUsuariosController {
  private final ImportacaoUsuariosService service;
  private final UsuarioMapper mapper;

  public ImportacaoUsuariosController(ImportacaoUsuariosService service, UsuarioMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Operação concluída."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Entradas inválidas.",
        content =
            @io.swagger.v3.oas.annotations.media.Content(
                schema =
                    @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = dad.entrypoint.dto.response.ErroResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "Token ausente ou inválido.",
        content =
            @io.swagger.v3.oas.annotations.media.Content(
                schema =
                    @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = dad.entrypoint.dto.response.ErroResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Usuário sem autorização administrativa.",
        content =
            @io.swagger.v3.oas.annotations.media.Content(
                schema =
                    @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = dad.entrypoint.dto.response.ErroResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "409",
        description = "Conflito durante a gravação; o lote inteiro é revertido.",
        content =
            @io.swagger.v3.oas.annotations.media.Content(
                schema =
                    @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = dad.entrypoint.dto.response.ErroResponse.class)))
  })
  @Operation(
      summary = "Importa de 1 a 100 usuários",
      description =
          "Exige administrador. Recebe JSON já convertido pelo front. "
              + "Reutiliza o cadastro individual e sua criptografia. A operação é atômica. "
              + "Erros de importação usam mensagem e campos indexados a partir de zero.")
  public ImportacaoUsuariosResponse importar(
      @Valid @RequestBody ImportacaoUsuariosRequest request) {
    return new ImportacaoUsuariosResponse(
        service.importar(request.usuarios().stream().map(mapper::toDomain).toList()));
  }
}
