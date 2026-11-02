package dad.core.regra;

import dad.core.domain.CriarUsuario;
import dad.core.domain.ReferenciasCadastroUsuario;
import dad.core.exception.RegraNegocioException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class RegrasUsuario {
  private RegrasUsuario() {}

  public static void validarCriacao(CriarUsuario comando) {
    validarDados(comando.nome(), comando.email(), comando.dataNascimento());

    if (comando.endereco() == null) {
      throw new RegraNegocioException("Informe o endereço");
    }

    if (comando.tipoUsuarioId() == null || comando.tipoUsuarioId() <= 0) {
      throw new RegraNegocioException("Informe o tipo de usuário");
    }

    if (comando.senha() == null
        || !comando
            .senha()
            .matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])" + "(?=.*[^\\p{L}\\p{N}\\s])\\S{8,}$")) {
      throw new RegraNegocioException(
          "Senha precisa de 8 caracteres, "
              + "maiúscula, minúscula, número e especial, "
              + "sem espaços");
    }

    if (comando.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new RegraNegocioException("A senha excede o limite de 72 bytes");
    }

    if (comando.cpf() != null && !comando.cpf().matches("\\d{11}")) {
      throw new RegraNegocioException("Informe o CPF com 11 números");
    }
  }

  public static void validarDados(String nome, String email, LocalDate nascimento) {
    if (nome == null || nome.isBlank()) {
      throw new RegraNegocioException("Informe o nome do usuário");
    }

    if (email == null || email.isBlank()) {
      throw new RegraNegocioException("Informe o e-mail");
    }

    if (nascimento != null && !nascimento.isBefore(LocalDate.now())) {
      throw new RegraNegocioException("A data de nascimento deve estar no passado");
    }
  }

  public static String normalizarTipo(String tipo) {
    return Normalizer.normalize(tipo, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .trim()
        .toUpperCase(Locale.ROOT)
        .replaceAll("[\\s-]+", "_");
  }

  public static void validarEmail(boolean duplicado) {
    if (duplicado) throw new RegraNegocioException("E-mail já cadastrado");
  }

  public static void validarCpf(boolean duplicado) {
    if (duplicado) throw new RegraNegocioException("CPF já cadastrado");
  }

  public static void exigirCondominio(Integer id) {
    if (id == null) throw new RegraNegocioException("Informe o condomínio do morador");
  }

  public static void validarCondominioAtivo(boolean ativo) {
    if (!ativo) throw new RegraNegocioException("O condomínio está inativo");
  }

  public static void validarSemCondominio(Integer id) {
    if (id != null)
      throw new RegraNegocioException("Condomínio no cadastro é exclusivo do morador");
  }

  public static Map<String, String> errosImportacao(
      CriarUsuario u, ReferenciasCadastroUsuario fatos) {
    var erros = new LinkedHashMap<String, String>();
    if (fatos.emailExistente()) erros.put("email", "E-mail já cadastrado.");
    if (fatos.cpfExistente()) erros.put("cpf", "CPF já cadastrado.");
    if (fatos.tipoUsuario() == null) erros.put("tipoUsuarioId", "Tipo de usuário inválido.");
    else if (normalizarTipo(fatos.tipoUsuario()).equals("MORADOR")) {
      if (!Boolean.TRUE.equals(fatos.condominioAtivo()))
        erros.put("condominioId", "Informe um condomínio existente e ativo.");
      if (!fatos.nivelInicialConfigurado())
        erros.put("tipoUsuarioId", "Nível inicial de morador não configurado.");
    } else if (u.condominioId() != null)
      erros.put("condominioId", "Condomínio no cadastro é exclusivo do morador.");
    return erros;
  }
}
