package dad.dataprovider.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_condominios")
public class CondominioEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_condominio")
  public Integer id;

  @Column(name = "nome_condominio")
  public String nome;

  @Column(name = "cnpj")
  public String cnpj;

  @Column(name = "codigo_acesso")
  public String codigoAcesso;

  @Column(name = "ativo")
  public boolean ativo;

  @Column(name = "tipo_condominio_id")
  public Integer tipoId;

  @Column(name = "sindico_id")
  public Integer sindicoId;

  @Column(name = "endereco_id")
  public Integer enderecoId;
}