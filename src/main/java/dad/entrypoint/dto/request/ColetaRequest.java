package dad.entrypoint.dto.request;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.*;

public record ColetaRequest(

    @NotNull @Positive Integer condominioId,

    @NotNull @Positive Integer cooperativaId,

    @NotNull @Positive Integer statusId,

    @NotNull OffsetDateTime dataInicio,

    OffsetDateTime dataFim,

    @NotNull Boolean recorrente,

    @NotNull @Size(max = 7) List<@NotNull @Positive Integer> diasSemanaIds

) {
}