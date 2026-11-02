package dad.entrypoint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Lote JSON com os mesmos campos do cadastro individual. Não recebe CSV.")
public record ImportacaoUsuariosRequest(
    @NotNull
        @Size(min = 1, max = 100)
        @Schema(description = "De 1 a 100 usuários; se um falhar, nenhum é persistido.")
        List<@NotNull @Valid UsuarioCriarRequest> usuarios) {}
