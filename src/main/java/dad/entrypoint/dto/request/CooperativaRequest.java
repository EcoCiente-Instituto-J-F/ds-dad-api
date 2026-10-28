package dad.entrypoint.dto.request;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CooperativaRequest(

    @NotBlank @Size(max = 255) String nome,

    @NotBlank @Size(max = 18) String cnpj,

    @Email @Size(max = 255) String email,

    @Size(max = 20) String telefone,

    @Positive Integer usuarioId,

    @Valid EnderecoRequest endereco,

    @NotNull List<@NotNull @Positive Integer> categoriaIds,

    @NotNull @Valid List<@NotNull PontoRequest> pontos

) {

  public record PontoRequest(

      @Positive Integer id,

      @NotBlank @Size(max = 255) String nome,

      LocalTime abertura,

      LocalTime fechamento,

      @NotNull Boolean ativo,

      @Valid EnderecoRequest endereco,

      @NotNull List<@NotNull @Positive Integer> categoriaIds

  ) {
  }
}