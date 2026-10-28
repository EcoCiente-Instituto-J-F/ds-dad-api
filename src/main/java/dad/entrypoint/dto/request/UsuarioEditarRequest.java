package dad.entrypoint.dto.request;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UsuarioEditarRequest(

    @NotBlank
    @Size(max = 100)
    String nome,

    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    @Past
    LocalDate dataNascimento,

    @Size(max = 500)
    String avatar,

    @NotNull
    @Valid
    EnderecoRequest endereco

) {
}