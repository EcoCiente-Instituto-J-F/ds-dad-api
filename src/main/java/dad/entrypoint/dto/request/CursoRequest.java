package dad.entrypoint.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CursoRequest(

    @NotBlank @Size(max = 255) String titulo,

    @NotBlank String descricao,

    @NotNull Boolean ativo,

    @NotNull @Valid List<@NotNull AulaRequest> aulas

) {

  public record AulaRequest(

      @Positive Integer id,

      @NotBlank @Size(max = 255) String titulo,

      @NotBlank String conteudo

  ) {
  }
}