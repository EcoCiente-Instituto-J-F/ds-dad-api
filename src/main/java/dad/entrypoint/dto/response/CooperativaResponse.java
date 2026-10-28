package dad.entrypoint.dto.response;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

public record CooperativaResponse(
    Integer id,
    String nome,
    String cnpj,
    String email,
    String telefone,
    Integer usuarioId,
    OffsetDateTime criadoEm,
    EnderecoResponse endereco,
    List<Integer> categoriaIds,
    List<PontoResponse> pontos) {

  public record PontoResponse(
      Integer id,
      String nome,
      LocalTime abertura,
      LocalTime fechamento,
      boolean ativo,
      EnderecoResponse endereco,
      List<Integer> categoriaIds) {
  }
}