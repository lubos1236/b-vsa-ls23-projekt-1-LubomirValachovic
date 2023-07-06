package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.persistence.annotations.CascadeOnDelete;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@NamedQuery(name = "Teacher.findAll", query = "SELECT t FROM Teacher t")
@NoArgsConstructor
public class Teacher implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long AIS;

    @Column(nullable = false)
    private String name;

    @Column(unique = true,nullable = false)
    private String email;

    private String institute;

    private String department;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @CascadeOnDelete
    private List<Assignment> assignments;

}
