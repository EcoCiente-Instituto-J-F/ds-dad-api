package dad.dataprovider.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_enderecos")
public class EnderecoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    public Integer id;

    @Column(name = "cep")
    public String cep;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(name = "estado", length = 2)
    public String uf;

    @Column(name = "cidade")
    public String cidade;

    @Column(name = "logradouro")
    public String logradouro;

    @Column(name = "numero")
    public String numero;

    @Column(name = "complemento")
    public String complemento;
}