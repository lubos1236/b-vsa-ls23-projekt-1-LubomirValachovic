package sk.stuba.fei.uim.vsa.pr1.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;
import sk.stuba.fei.uim.vsa.pr1.enums.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@NamedQuery(name = "Assignment.findAll", query = "SELECT a FROM Assignment a")
public class Assignment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ID;
    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @Column(nullable = false)
    private String title;

    private String description;

    private String institute;

    @ManyToOne()
    @JoinColumn(name= "teacher_id", nullable = false)
    private Teacher teacher;

    @OneToOne()
    @JoinColumn(name= "student_id")
    private Student student;

    @Column(nullable = false)
    private LocalDate publicationDate;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
}
