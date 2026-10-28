package dad.entrypoint.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CondominioRequest(

    @NotBlank @Size(max = 255) String nome,

    @Pattern(regexp = "(?:\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})") String cnpj,

    @Size(max = 20) String codigoAcesso,

    @NotNull Boolean ativo,

    @NotNull @Positive Integer tipoId,

    @Positive Integer sindicoId,

    @Valid EnderecoRequest endereco,

    @NotNull @Valid List<@NotNull TorreRequest> torres

) {

  public record TorreRequest(

      @Positive Integer id,

      @NotBlank @Size(max = 100) String nome

  ) {
  }
}