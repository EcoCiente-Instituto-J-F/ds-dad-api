package dad.core.domain;

import java.util.List;

public record Condominio(
    Integer id,
    String nome,
    String cnpj,
    String codigoAcesso,
    boolean ativo,
    Integer tipoId,
    Integer sindicoId,
    Endereco endereco,
    List<Torre> torres) {
}