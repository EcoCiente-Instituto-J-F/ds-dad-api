package dad.entrypoint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(

    @NotBlank @Pattern(regexp = "\\d{8}") String cep,

    @NotBlank @Pattern(regexp = "[A-Z]{2}") String uf,

    @NotBlank @Size(max = 100) String cidade,

    @NotBlank @Size(max = 150) String logradouro,

    @NotBlank @Size(max = 20) String numero,

    @Size(max = 100) String complemento

) {
}