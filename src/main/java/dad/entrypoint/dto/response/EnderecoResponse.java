package dad.entrypoint.dto.response;

public record EnderecoResponse(
    String cep,
    String uf,
    String cidade,
    String logradouro,
    String numero,
    String complemento) {
}