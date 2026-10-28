package dad.config.security;

public record UsuarioAutenticado(
    Integer id,
    String nome,
    String email,
    boolean administrador) {
}