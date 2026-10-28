package dad.entrypoint.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record UsuarioResponse(
    Integer id,
    String nome,
    String email,
    Integer tipoUsuarioId,
    String tipoUsuario,
    boolean ativo,
    LocalDate dataNascimento,
    String cpfMascarado,
    String avatar,
    OffsetDateTime registroEm,
    EnderecoResponse endereco,
    Integer condominioId) {
}