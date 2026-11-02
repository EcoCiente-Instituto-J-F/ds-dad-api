package dad.entrypoint.dto.request;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public record ColetaRequest(
    @NotNull @Positive Integer condominioId,
    @NotNull @Positive Integer cooperativaId,
    @NotNull @Positive Integer statusId,
    @NotNull OffsetDateTime dataInicio,
    OffsetDateTime dataFim,
    @NotNull Boolean recorrente,
    @NotNull @Size(max = 7) List<@NotNull @Positive Integer> diasSemanaIds) {
  @JsonAnySetter
  public void rejeitarCampoDesconhecido(String campo, Object valor) {
    throw new IllegalArgumentException("Campo não permitido: " + campo);
  }
}
