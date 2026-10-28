package dad.entrypoint.dto.request;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(
    @NotNull Boolean ativo) {
}