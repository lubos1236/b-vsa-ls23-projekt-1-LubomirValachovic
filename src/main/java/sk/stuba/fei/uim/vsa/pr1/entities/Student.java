package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@NamedQuery(name = "Student.findAll", query = "SELECT s FROM Student s")
//@NamedQuery(name = "Student.findByAIS", query = "SELECT s FROM Student s WHERE s.AIS = :AIS")
public class Student implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long AIS;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private Integer year;

    private Integer semester;

    private String program;

   @OneToOne(mappedBy = "student")
   private Assignment assignment;





}
