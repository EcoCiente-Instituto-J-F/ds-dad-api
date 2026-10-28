package dad.core.domain;

import java.time.LocalDate;

public record CriarUsuario(
    String nome,
    String email,
    String senha,
    Integer tipoUsuarioId,
    LocalDate dataNascimento,
    String cpf,
    String avatar,
    Endereco endereco,
    Integer condominioId) {
}