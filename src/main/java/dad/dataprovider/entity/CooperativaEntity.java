package dad.dataprovider.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_cooperativas")
public class CooperativaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_cooperativa")
  public Integer id;

  @Column(name = "nome_cooperativa")
  public String nome;

  @Column(name = "cnpj_cooperativa")
  public String cnpj;

  @Column(name = "email_cooperativa")
  public String email;

  @Column(name = "telefone_cooperativa")
  public String telefone;

  @Column(name = "usuario_id")
  public Integer usuarioId;

  @Column(name = "endereco_id")
  public Integer enderecoId;

  @Column(name = "data_cadastro")
  public OffsetDateTime criadoEm;
}