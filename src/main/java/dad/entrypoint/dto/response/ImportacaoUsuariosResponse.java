package dad.entrypoint.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImportacaoUsuariosResponse(
    @Schema(description = "Quantidade persistida na transação.", example = "10") int importados) {}
