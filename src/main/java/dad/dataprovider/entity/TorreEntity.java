package dad.dataprovider.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_torres")
public class TorreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_torre")
    public Integer id;

    @Column(name = "nome_torre")
    public String nome;

    @Column(name = "condominio_id")
    public Integer condominioId;
}