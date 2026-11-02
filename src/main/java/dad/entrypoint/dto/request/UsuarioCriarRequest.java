package dad.entrypoint.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UsuarioCriarRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])" + "(?=.*[^\\p{L}\\p{N}\\s])\\S{8,}$",
            message = "Use maiúscula, minúscula, número, " + "especial e não utilize espaços")
        String senha,
    @NotNull @Positive Integer tipoUsuarioId,
    @Past LocalDate dataNascimento,
    @Pattern(regexp = "\\d{11}", message = "Informe o CPF com 11 números") String cpf,
    @Size(max = 500) String avatar,
    @NotNull @Valid EnderecoRequest endereco,
    @Positive Integer condominioId) {}
