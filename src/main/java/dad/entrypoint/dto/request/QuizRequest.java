package dad.entrypoint.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record QuizRequest(

    @NotNull @Positive Integer cursoId,

    @NotNull @Positive Integer aulaId,

    @NotBlank @Size(max = 255) String titulo,

    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal notaMinima,

    @NotNull @Positive Integer pontos,

    @NotNull Boolean ativo,

    @NotNull @Size(min = 1) @Valid List<@NotNull PerguntaRequest> perguntas

) {

  public record PerguntaRequest(

      @NotBlank String enunciado,

      @NotNull @Size(min = 2) @Valid List<@NotNull AlternativaRequest> alternativas

  ) {
  }

  public record AlternativaRequest(

      @NotBlank @Size(max = 500) String texto,

      @NotNull Boolean correta

  ) {
  }
}