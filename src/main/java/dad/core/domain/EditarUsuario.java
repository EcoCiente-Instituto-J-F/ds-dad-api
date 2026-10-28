package dad.core.domain;

import java.time.LocalDate;

public record EditarUsuario(
    String nome,
    String email,
    LocalDate dataNascimento,
    String avatar,
    Endereco endereco) {
}