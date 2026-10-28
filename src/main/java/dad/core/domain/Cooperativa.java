package dad.core.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record Cooperativa(
    Integer id,
    String nome,
    String cnpj,
    String email,
    String telefone,
    Integer usuarioId,
    OffsetDateTime criadoEm,
    Endereco endereco,
    List<Integer> categoriaIds,
    List<PontoColeta> pontos) {
}