package dad.entrypoint.controller;

import dad.core.service.CalendarioColetaService;
import dad.entrypoint.dto.response.OcorrenciaColetaResponse;
import dad.entrypoint.dto.response.PaginaResponse;
import dad.entrypoint.mapper.ColetaMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/coletas/calendario")
@Tag(name = "Calendário de coletas")
public class CalendarioColetaController {
  private final CalendarioColetaService service;
  private final ColetaMapper mapper;

  public CalendarioColetaController(CalendarioColetaService service, ColetaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @GetMapping
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
                        implementation = dad.entrypoint.dto.response.ErroResponse.class)))
  })
  @Operation(
      summary = "Lista ocorrências de coletas",
      description =
          "Exige administrador. Início inclusivo e fim exclusivo, ambos com fuso. "
              + "Recorrências usam America/Sao_Paulo e respeitam o instante final inclusivo da programação. "
              + "Filtros precedem a paginação; ordenação por ocorrência e ID. IDs são do agendamento.")
  public PaginaResponse<OcorrenciaColetaResponse> listar(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim,
      @RequestParam(required = false) Integer condominioId,
      @RequestParam(required = false) Integer cooperativaId,
      @RequestParam(required = false) Integer statusId,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "50") int tamanho) {
    return mapper.toCalendarioResponse(
        service.listar(inicio, fim, condominioId, cooperativaId, statusId, pagina, tamanho));
  }
}
