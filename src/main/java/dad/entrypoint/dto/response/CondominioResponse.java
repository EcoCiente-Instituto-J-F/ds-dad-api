package dad.entrypoint.dto.response;

import java.util.List;

public record CondominioResponse(
    Integer id,
    String nome,
    String cnpj,
    String codigoAcesso,
    boolean ativo,
    Integer tipoId,
    Integer sindicoId,
    EnderecoResponse endereco,
    List<TorreResponse> torres) {

  public record TorreResponse(
      Integer id,
      String nome) {
  }
}