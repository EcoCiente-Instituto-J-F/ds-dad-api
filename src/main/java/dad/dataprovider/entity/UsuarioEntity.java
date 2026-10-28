package dad.dataprovider.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_usuarios")
public class UsuarioEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_usuario")
  public Integer id;

  @Column(name = "nome_usuario")
  public String nome;

  @Column(name = "email_usuario")
  public String email;

  @Column(name = "senha_hash")
  public String senhaHash;

  @Column(name = "data_nascimento")
  public LocalDate dataNascimento;

  @Column(name = "cpf")
  public String cpf;

  @Column(name = "url_avatar")
  public String avatar;

  @Column(name = "ativo")
  public boolean ativo;

  @Column(name = "registro_em")
  public OffsetDateTime registroEm;

  @Column(name = "tipo_usuario_id")
  public Integer tipoUsuarioId;

  @Column(name = "endereco_id")
  public Integer enderecoId;
}