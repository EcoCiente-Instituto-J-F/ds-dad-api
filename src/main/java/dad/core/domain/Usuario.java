package dad.core.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record Usuario(
    Integer id,
    String nome,
    String email,
    Integer tipoUsuarioId,
    String tipoUsuario,
    boolean ativo,
    LocalDate dataNascimento,
    String cpf,
    String avatar,
    OffsetDateTime registroEm,
    Endereco endereco,
    Integer condominioId) {
}