package sk.stuba.fei.uim.vsa.pr1;

import sk.stuba.fei.uim.vsa.pr1.entities.Assignment;
import sk.stuba.fei.uim.vsa.pr1.entities.Student;
import sk.stuba.fei.uim.vsa.pr1.entities.Teacher;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;
import sk.stuba.fei.uim.vsa.pr1.enums.Type;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class API extends AbstractThesisService<Student, Teacher, Assignment>{
    Random rand = new Random();
    public API() {

        super();
    }
    @Override
    public Student createStudent(Long aisId, String name, String email) {
        EntityManager em= emf.createEntityManager();
        Student s=new Student();
        try {
            s.setAIS(aisId);
            s.setName(name);
            s.setEmail(email);
            em.getTransaction().begin();
            em.persist(s);
            em.getTransaction().commit();
            return s;

        }catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Student getStudent(Long id) {
        EntityManager em= emf.createEntityManager();
        if (id==null) throw new IllegalArgumentException();

        try {
            return em.find(Student.class, id);
        }catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }

    }

    @Override
    public Student updateStudent(Student student) {
        if (student==null || student.getAIS()==null) throw new IllegalArgumentException();

        EntityManager em= emf.createEntityManager();
        try {
            Student s=em.find(Student.class, student.getAIS());
            s.setName(student.getName());
            s.setEmail(student.getEmail());
            s.setYear(student.getYear());
            s.setSemester(student.getSemester());
            s.setProgram(student.getProgram());
            s.setAssignment(student.getAssignment());

            em.getTransaction().begin();
            em.merge(s);
            em.getTransaction().commit();
            return s;
        }
        catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public List<Student> getStudents() {
        EntityManager em= emf.createEntityManager();
        try {
            return em.createNamedQuery("Student.findAll", Student.class).getResultList();
        } catch (Exception e){
            return new ArrayList<>();
        }
        finally {
            em.close();
        }
    }

    @Override
    public Student deleteStudent(Long id) {
        if (id==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            Student s=em.find(Student.class, id);
            em.getTransaction().begin();
            if (s.getAssignment()!=null){
                s.getAssignment().setStudent(null);
                s.getAssignment().setStatus(Status.Free);
                em.merge(s.getAssignment());
            }
            em.remove(s);
            em.getTransaction().commit();
            return s;
        } catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }
    @Override
    public Teacher createTeacher(Long aisId, String name, String email, String department) {
        EntityManager em= emf.createEntityManager();
        Teacher t=new Teacher();
        try {
            t.setAIS(aisId);
            t.setName(name);
            t.setEmail(email);
            t.setInstitute(department);
            t.setDepartment(department);
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
            return t;
        }catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Teacher getTeacher(Long id) {
        if (id==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            return em.find(Teacher.class, id);
        }catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Teacher updateTeacher(Teacher teacher) {
        if (teacher==null || teacher.getAIS()==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            Teacher t=em.find(Teacher.class, teacher.getAIS());
            t.setName(teacher.getName());
            t.setEmail(teacher.getEmail());
            t.setInstitute(teacher.getInstitute());
            t.setDepartment(teacher.getDepartment());
            t.setAssignments(teacher.getAssignments());

            em.getTransaction().begin();
            em.merge(t);
            em.getTransaction().commit();
            return t;
        }
        catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public List<Teacher> getTeachers() {
        EntityManager em= emf.createEntityManager();
        try {
            return em.createNamedQuery("Teacher.findAll", Teacher.class).getResultList();
        } catch (Exception e){
            return new ArrayList<>();
        }
        finally {
            em.close();
        }
    }

    @Override
    public Teacher deleteTeacher(Long id) {
        if (id==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            for (Assignment a: getThesesByTeacher(id))
                deleteThesis(a.getID());
            em.getTransaction().begin();
            Teacher t=em.find(Teacher.class, id);
            em.remove(t);
            em.getTransaction().commit();
            return t;
        } catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Assignment makeThesisAssignment(Long supervisor, String title, String type, String description) {
        if (supervisor==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            Assignment a=new Assignment();
            Teacher t=getTeacher(supervisor);
            t.getAssignments().add(a);
            a.setTeacher(t);
            a.setInstitute(a.getTeacher().getInstitute());
            a.setTitle(title);
            a.setType(Type.valueOf(type));
            a.setDescription(description);
            a.setStatus(Status.Free);
            a.setPublicationDate(LocalDate.now());
            a.setDeadline(LocalDate.now().plusMonths(3));
            a.setRegistrationNumber("FEI-"+Math.abs(rand.nextInt()));

            em.getTransaction().begin();
            em.persist(a);
            em.merge(t);
            em.getTransaction().commit();
            return a;
        }
        catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }
    @Override
    public Assignment assignThesis(Long thesisId, Long studentId) {

        if (thesisId==null || studentId==null) throw new IllegalArgumentException();

        EntityManager em= emf.createEntityManager();
        Assignment a=getThesis(thesisId);
        if (a.getStatus()!=Status.Free || a.getDeadline().isBefore(LocalDate.now())) throw new IllegalStateException();
        try {
            Student s=em.find(Student.class, studentId);
            a.setStatus(Status.WORKING);
            a.setStudent(s);
            s.setAssignment(a);

            em.getTransaction().begin();
            em.merge(a);
            em.merge(s);
            em.getTransaction().commit();
            return a;
        }
        catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Assignment submitThesis(Long thesisId) {
        if (thesisId==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        Assignment a=em.find(Assignment.class, thesisId);
        if (a.getStatus()==Status.SUBMITTED || a.getStudent()==null || a.getDeadline().isBefore(LocalDate.now())) throw new IllegalStateException();
        try {
            a.setStatus(Status.SUBMITTED);
            return a;
        }
        catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Assignment deleteThesis(Long id) {
        if (id==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            Assignment a=em.find(Assignment.class, id);
            em.getTransaction().begin();
            a.getTeacher().getAssignments().remove(a);
            if (a.getStudent()!=null)
            {
                a.getStudent().setAssignment(null);
                em.merge(a.getStudent());
            }

            em.merge(a.getTeacher());
            em.remove(a);
            em.getTransaction().commit();
            return a;
        } catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public List<Assignment> getTheses() {
        EntityManager em= emf.createEntityManager();
        try {
            return em.createNamedQuery("Assignment.findAll", Assignment.class).getResultList();
        } catch (Exception e){
            return new ArrayList<>();
        }
        finally {
            em.close();
        }
    }

    @Override
    public List<Assignment> getThesesByTeacher(Long teacherId) {
        if (teacherId==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            return getTeacher(teacherId).getAssignments();
        } catch (Exception e){
            return new ArrayList<>();
        }
        finally {
            em.close();
        }

    }

    @Override
    public Assignment getThesisByStudent(Long studentId) {
        if (studentId==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            return getStudent(studentId).getAssignment();
        } catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Assignment getThesis(Long id) {
        if (id==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            return em.find(Assignment.class, id);
        } catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }

    @Override
    public Assignment updateThesis(Assignment thesis) {
        if (thesis==null || thesis.getID()==null) throw new IllegalArgumentException();
        EntityManager em= emf.createEntityManager();
        try {
            Assignment a=em.find(Assignment.class, thesis.getID());
            if (thesis.getTitle()!=null) a.setTitle(thesis.getTitle());
            if (thesis.getType()!=null) a.setType(thesis.getType());
            if (thesis.getDescription()!=null) a.setDescription(thesis.getDescription());
            if (thesis.getDeadline()!=null) a.setDeadline(thesis.getDeadline());
            if (thesis.getPublicationDate()!=null) a.setPublicationDate(thesis.getPublicationDate());
            if (thesis.getRegistrationNumber()!=null) a.setRegistrationNumber(thesis.getRegistrationNumber());
            if (thesis.getStatus()!=null) a.setStatus(thesis.getStatus());
            if (thesis.getStudent()!=null) a.setStudent(thesis.getStudent());
            if (thesis.getTeacher()!=null) a.setTeacher(thesis.getTeacher());

            em.getTransaction().begin();
            em.merge(a);
            em.getTransaction().commit();
            return a;
        }
        catch (Exception e){
            return null;
        }
        finally {
            em.close();
        }
    }
}
