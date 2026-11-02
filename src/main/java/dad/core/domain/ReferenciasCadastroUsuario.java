package dad.core.domain;

public record ReferenciasCadastroUsuario(
    boolean emailExistente,
    boolean cpfExistente,
    String tipoUsuario,
    Boolean condominioAtivo,
    boolean nivelInicialConfigurado) {}
