package dad.dataprovider.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

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
