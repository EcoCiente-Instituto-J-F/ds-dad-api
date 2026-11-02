package dad.core.exception;

public class UsuarioNaoAutenticadoException extends RuntimeException {
  public UsuarioNaoAutenticadoException() {
    super("Usuário autenticado válido é obrigatório para criar um agendamento");
  }
}
