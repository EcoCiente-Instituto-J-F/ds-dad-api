package dad.dataprovider.entity;

import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_pontos_coletas")
public class PontoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ponto_coleta")
    public Integer id;

    @Column(name = "nome_ponto")
    public String nome;

    @Column(name = "endereco_id")
    public Integer enderecoId;

    @Column(name = "cooperativa_id")
    public Integer cooperativaId;

    @Column(name = "horario_abertura")
    public LocalTime abertura;

    @Column(name = "horario_fechamento")
    public LocalTime fechamento;

    @Column(name = "ativo")
    public boolean ativo;
}