package dad.entrypoint.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UsuarioEditarRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Email @Size(max = 255) String email,
    @Past LocalDate dataNascimento,
    @Size(max = 500) String avatar,
    @NotNull @Valid EnderecoRequest endereco) {}
