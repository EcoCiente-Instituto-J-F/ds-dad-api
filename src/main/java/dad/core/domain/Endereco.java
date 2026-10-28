package dad.core.domain;

public record Endereco(
    String cep,
    String uf,
    String cidade,
    String logradouro,
    String numero,
    String complemento) {
}