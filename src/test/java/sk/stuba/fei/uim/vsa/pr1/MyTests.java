package sk.stuba.fei.uim.vsa.pr1;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sk.stuba.fei.uim.vsa.pr1.entities.Assignment;
import sk.stuba.fei.uim.vsa.pr1.entities.Student;
import sk.stuba.fei.uim.vsa.pr1.entities.Teacher;
import sk.stuba.fei.uim.vsa.pr1.enums.Status;
import sk.stuba.fei.uim.vsa.pr1.enums.Type;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MyTests {

    private EntityManagerFactory emf;
    private API api;
    private EntityManager em;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("vsa-project-1");
        api = new API();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Assignment ").executeUpdate();
        em.createQuery("DELETE FROM Student").executeUpdate();
        em.createQuery("DELETE FROM Teacher").executeUpdate();
        em.getTransaction().commit();
    }

    @AfterEach
    void tearDown() {
        em.close();
        emf.close();
    }

    @Test
    void testCreateStudent() {
        // Arrange
        Long aisId = 12345L;
        String name = "Johns Doe";
        String email = "johndoes@example.com";

        // Act
        Student student = api.createStudent(aisId, name, email);

        // Assert
        assertNotNull(student);
        assertEquals(aisId, student.getAIS());
        assertEquals(name, student.getName());
        assertEquals(email, student.getEmail());

        // Check that the student was actually persisted to the database
        em.clear();
        Student persistedStudent = em.find(Student.class, student.getAIS());
        assertNotNull(persistedStudent);
        assertEquals(aisId, persistedStudent.getAIS());
        assertEquals(name, persistedStudent.getName());
        assertEquals(email, persistedStudent.getEmail());
    }

    @Test
    void testUniqueStudentId() {
        // Arrange
        Long aisId = 1234L;
        String name = "John Doe";
        String email = "eeee@example.com";

        // Act
        Student student = api.createStudent(aisId, name, email);

        // Assert
        assertNotNull(student);
        Student student2 = api.createStudent(aisId, "Another Name", "anotheremail@example.com");
        assertNull(student2);
    }

    @Test
    void testUniqueStudentEmail() {
        // Arrange
        Long aisId = 12341L;
        String name = "John Doe";
        String email = "johndoe@example.com";

        // Act
        Student student = api.createStudent(aisId, name, email);

        // Assert
        assertNotNull(student);
        Student student2 = api.createStudent(5678L, "Another Name", email);
        assertNull(student2);
    }

    @Test
    void testGetStudent() {
        // Arrange
        Long aisId = 1234L;
        String name = "John Doe";
        String email = "johndoe@example.com";
        Student student = api.createStudent(aisId, name, email);
        // Act
        Student foundStudent = api.getStudent(student.getAIS());

        // Assert
        assertNotNull(foundStudent);
        assertEquals(aisId, foundStudent.getAIS());
        assertEquals(name, foundStudent.getName());
        assertEquals(email, foundStudent.getEmail());
    }

    @Test
    void testGetStudentNullId() {
        // Arrange & Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            api.getStudent(null);
        });

        // Assert
        assertNotNull(exception);
    }

    @Test
    void testGetStudentNotFound() {
        // Arrange
        Long aisId = 1234L;
        String name = "John Doe";
        String email = "johndoe@example.com";
        Student student = api.createStudent(aisId, name, email);
        // Act
        Student foundStudent = api.getStudent(student.getAIS() + 1);

        // Assert
        assertNull(foundStudent);
    }

    @Test
    public void testUpdateStudent() {
        // Create a new student
        Student student = new Student();
        student.setAIS(12345L);
        student.setName("John Doe");
        student.setEmail("john.doe@example.com");
        // Persist the student
        student = api.createStudent(student.getAIS(), student.getName(), student.getEmail());

        // Update the student's name and email
        student.setName("Jane Doe");
        student.setEmail("jane.doe@example.com");
        Student updatedStudent = api.updateStudent(student);

        // Retrieve the updated student
        Student retrievedStudent = api.getStudent(student.getAIS());

        // Verify that the retrieved student has the updated name and email
        assertEquals("Jane Doe", retrievedStudent.getName());
        assertEquals("jane.doe@example.com", retrievedStudent.getEmail());
    }

    @Test
    public void testUpdateStudentWithNullAisId() {
        // Create a new student
        Student student = new Student();
        student.setAIS(null);
        student.setName("John Doe");
        student.setEmail("john.doe@example.com");
        try {
            // Try to update the student with null aisId
            api.updateStudent(student);
            // Fail the test if no exception is thrown
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            // Pass the test if an IllegalArgumentException is thrown
            assertTrue(true);
        }
    }

    @Test
    public void testUpdateStudentWithNullStudent() {
        Student student = null;
        try {
            // Try to update a null student
            api.updateStudent(student);
            // Fail the test if no exception is thrown
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            // Pass the test if an IllegalArgumentException is thrown
            assertTrue(true);
        }
    }

    @Test
    public void testGetStudents() {
        // Create some test students
        Student student1 = new Student();
        student1.setAIS(1L);
        student1.setName("John Doe");
        student1.setEmail("john.doe@example.com");

        Student student2 = new Student();
        student2.setAIS(2L);
        student2.setName("Jane Doe");
        student2.setEmail("jane.doe@example.com");

        // Persist the test students
        api.createStudent(student1.getAIS(), student1.getName(), student1.getEmail());
        api.createStudent(student2.getAIS(), student2.getName(), student2.getEmail());

        // Call the getStudents() method and check if it returns the correct number of students
        List<Student> students = api.getStudents();
        assertEquals(2, students.size());

        // Check if the returned students have the correct properties
        assertEquals(1L, students.get(0).getAIS());
        assertEquals("John Doe", students.get(0).getName());
        assertEquals("john.doe@example.com", students.get(0).getEmail());

        assertEquals(2L, students.get(1).getAIS());
        assertEquals("Jane Doe", students.get(1).getName());
        assertEquals("jane.doe@example.com", students.get(1).getEmail());
    }

    @Test
    public void testDeleteStudent() {
        // Create a student
        Student student = api.createStudent(1234L, "John Doe", "johndoe@example.com");

        // Delete the student
        Student deletedStudent = api.deleteStudent(student.getAIS());

        // Check that the deleted student is the same as the original student
        assertNotNull(deletedStudent);
        assertEquals(student.getAIS(), deletedStudent.getAIS());
        assertEquals(student.getName(), deletedStudent.getName());
        assertEquals(student.getEmail(), deletedStudent.getEmail());

        // Check that the student is no longer in the database
        assertNull(api.getStudent(student.getAIS()));
    }

    @Test
    public void testDeleteStudentWithIDNull() {
        try {
            // Try to update a null student
            api.deleteStudent(null);
            // Fail the test if no exception is thrown
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            // Pass the test if an IllegalArgumentException is thrown
            assertTrue(true);
        }
    }

    //TEACHER

    @Test
    public void testCreateTeacher() {
        Long aisId = 12345L;
        String name = "John Smith";
        String email = "john.smith@example.com";
        String department = "Computer Science";
        Teacher teacher = api.createTeacher(aisId, name, email, department);
        assertNotNull(teacher);
        assertEquals(aisId, teacher.getAIS());
        assertEquals(name, teacher.getName());
        assertEquals(email, teacher.getEmail());
        assertEquals(department, teacher.getDepartment());
    }

    @Test
    public void testCreateTeacherWithUniqueValues() {
        // Create a new teacher with unique values
        Long aisId = 123456L;
        String name = "John Doe";
        String email = "john.doe@example.com";
        String department = "Mathematics";
        Teacher teacher = api.createTeacher(aisId, name, email, department);

        // Check that the created teacher is not null and has the expected values
        assertNotNull(teacher);
        assertEquals(aisId, teacher.getAIS());
        assertEquals(name, teacher.getName());
        assertEquals(email, teacher.getEmail());
        assertEquals(department, teacher.getDepartment());

        // Try to create a new teacher with the same AIS ID and email, and check that it returns null
        Teacher duplicateTeacher = api.createTeacher(aisId, "Jane Doe", "ss", "Physics");
        assertNull(duplicateTeacher);

        Teacher duplicateTeacher2 = api.createTeacher(1L, "Jane Doe", email, "Physics");
        assertNull(duplicateTeacher2);
    }

    @Test
    public void testGetTeacher() {
        // Create a new teacher
        Teacher teacher = api.createTeacher(1234L, "John Doe", "john.doe@university.edu", "Computer Science");

        // Call the getTeacher method
        Teacher result = api.getTeacher(1234L);

        // Verify that the returned teacher is not null and has the expected properties
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john.doe@university.edu", result.getEmail());
        assertEquals("Computer Science", result.getDepartment());
    }

    @Test
    public void testGetTeacherWithIDNull() {
        try {
            // Try to update a null student
            api.getTeacher(null);
            // Fail the test if no exception is thrown
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            // Pass the test if an IllegalArgumentException is thrown
            assertTrue(true);
        }
    }

    @Test
    public void testUpdateTeacher() {
        // Test valid teacher update
        Teacher teacher = api.createTeacher(1L, "John Smith", "john@example.com", "Computer Science");
        teacher.setName("Jane Doe");
        teacher.setEmail("jane@example.com");
        teacher.setDepartment("Information Technology");
        Teacher updatedTeacher = api.updateTeacher(teacher);
        assertEquals("Jane Doe", updatedTeacher.getName());
        assertEquals("jane@example.com", updatedTeacher.getEmail());
        assertEquals("Information Technology", updatedTeacher.getDepartment());

        // Test illegal argument exception when teacher is null
        try {
            api.updateTeacher(null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            // Test passed
        }

        // Test illegal argument exception when teacher's aisId is null
        teacher.setAIS(null);
        try {
            api.updateTeacher(teacher);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            // Test passed
        }
    }

    @Test
    public void testGetTeachers() {
        // Create some test students
        Teacher teacher1 = new Teacher();
        teacher1.setAIS(1L);
        teacher1.setName("John Doe");
        teacher1.setEmail("john.doe@example.com");
        teacher1.setDepartment("Computer Science");

        Teacher teacher2 = new Teacher();
        teacher2.setAIS(2L);
        teacher2.setName("Jane Doe");
        teacher2.setEmail("jane.doe@example.com");
        teacher2.setDepartment("Computer Science");
        // Persist the test teachers
        api.createTeacher(teacher1.getAIS(), teacher1.getName(), teacher1.getEmail(), teacher1.getDepartment());
        api.createTeacher(teacher2.getAIS(), teacher2.getName(), teacher2.getEmail(), teacher2.getDepartment());

        // Call the getTeachers() method and check if it returns the correct number of teachers
        List<Teacher> teachers = api.getTeachers();
        System.out.println(teachers);
        assertEquals(2, teachers.size());

        // Check if the returned teachers have the correct properties
        assertEquals(1L, teachers.get(0).getAIS());
        assertEquals("John Doe", teachers.get(0).getName());
        assertEquals("john.doe@example.com", teachers.get(0).getEmail());

        assertEquals(2L, teachers.get(1).getAIS());
        assertEquals("Jane Doe", teachers.get(1).getName());
        assertEquals("jane.doe@example.com", teachers.get(1).getEmail());
    }

    @Test
    public void testDeleteTeacher() {
        // Create a teacher
        Teacher teacher = api.createTeacher(1234L, "John Doe", "johndoe@example.com", "CS");

        // Delete the teacher
        Teacher deletedTeacher = api.deleteTeacher(teacher.getAIS());

        // Check that the deleted teacher is the same as the original teacher
        assertEquals(teacher.getAIS(), deletedTeacher.getAIS());
        assertEquals(teacher.getName(), deletedTeacher.getName());
        assertEquals(teacher.getEmail(), deletedTeacher.getEmail());

        // Check that the teacher is no longer in the database
        assertNull(api.getTeacher(teacher.getAIS()));
    }

    @Test
    public void testDeleteTeacherWithIDNull() {
        try {
            // Try to update a null teacher
            api.deleteTeacher(null);
            // Fail the test if no exception is thrown
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            // Pass the test if an IllegalArgumentException is thrown
            assertTrue(true);
        }
    }

    //THESIS

    @Test
    public void testMakeThesisAssignmentValidInputs() {
        // Arrange
        Long supervisor = 100L;
        String title = "Test Thesis";
        String type = "BACHELOR";
        String description = "This is a test thesis";
        // Act
        Teacher teacher = api.createTeacher(100L, "John Doe", "a@a.com", "Computer Science");
        Assignment thesis = api.makeThesisAssignment(supervisor, title, type, description);


        // Assert
        assertNotNull(thesis);
        assertNotNull(thesis.getID());
        assertEquals(title, thesis.getTitle());
        assertEquals(Type.BACHELOR, thesis.getType());
        assertEquals(description, thesis.getDescription());
        assertNotNull(thesis.getRegistrationNumber());
        assertNotNull(thesis.getPublicationDate());
        assertNotNull(thesis.getDeadline());
        assertEquals(api.getTeacher(supervisor).getInstitute(), thesis.getInstitute());
        assertEquals(api.getTeacher(supervisor).getAIS(), thesis.getTeacher().getAIS());
    }

    @Test
    public void testMakeThesisAssignmentInvalidSupervisor() {
        // Arrange
        Long supervisor = 1000L; // invalid supervisor id
        String title = "Test Thesis";
        String type = "BACHELOR";
        String description = "This is a test thesis";

        // Act
        Assignment thesis = api.makeThesisAssignment(supervisor, title, type, description);

        // Assert
        assertNull(thesis);
    }

    @Test
    public void testMakeThesisAssignmentNullSupervisor() {
        // Arrange
        Long supervisor = null;
        String title = "Test Thesis";
        String type = "BACHELOR";
        String description = "This is a test thesis";
        try {
            api.makeThesisAssignment(null, title, type, description);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

    }

    @Test
    public void testAssignThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "johndoe@example.com", "Computer Science");

        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "janesmith@example.com");

        // Create a thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");

        // Assign thesis to student
        Assignment assignedThesis = api.assignThesis(thesis.getID(), student.getAIS());

        // Check that the thesis was assigned to the student
        assertEquals(student.getAIS(), assignedThesis.getStudent().getAIS());
    }

    @Test
    public void testAssignThesisWithNullParams() {
        try {
            api.assignThesis(null, null);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testAssignSubmittedThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "johndoe@example.com", "Computer Science");

        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "janesmith@example.com");

        // Create a thesis and set status to submitted
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        Long id = thesis.getID();
        thesis = em.find(Assignment.class, thesis.getID());
        thesis.setStatus(Status.SUBMITTED);

        em.getTransaction().begin();
        em.merge(thesis);
        em.getTransaction().commit();

        // Try to assign thesis to student
        try {
            api.assignThesis(id, student.getAIS());
            fail("Expected an IllegalStateException to be thrown.");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testAssignTakenThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "johndoe@example.com", "Computer Science");

        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "janesmith@example.com");

        // Create a thesis and set status to submitted
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        thesis.setStatus(Status.WORKING);
        em.getTransaction().begin();
        em.merge(thesis);
        em.getTransaction().commit();

        // Try to assign thesis to student
        try {
            api.assignThesis(thesis.getID(), student.getAIS());
            fail("Expected an IllegalStateException to be thrown.");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testAssignOverdueThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "johndoe@example.com", "Computer Science");

        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "janesmith@example.com");

        // Create a thesis and set deadline to a date in the past
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        thesis.setDeadline(LocalDate.now().minusDays(1));
        em.getTransaction().begin();
        em.merge(thesis);
        em.getTransaction().commit();
        try {
            api.assignThesis(thesis.getID(), student.getAIS());
            fail("Expected an IllegalStateException to be thrown.");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSubmitThesis() {
        // Create a supervisor and a student
        Teacher supervisor = api.createTeacher(1L, "John Doe", "john.doe@example.com", "Computer Science");
        Student student = api.createStudent(2L, "Jane Smith", "jane.smith@example.com");

        // Create a thesis assignment and assign it to the student
        Assignment thesis = api.makeThesisAssignment(supervisor.getAIS(), "My Thesis", "BACHELOR", "This is my thesis");
        thesis = api.assignThesis(thesis.getID(), student.getAIS());

        // Verify that the initial status of the thesis is "IN_PROGRESS"
        assertEquals(Status.WORKING, thesis.getStatus());

        // Submit the thesis and verify that its status changes to "SUBMITTED"
        thesis = api.submitThesis(thesis.getID());
        assertEquals(Status.SUBMITTED, thesis.getStatus());
    }

    @Test
    public void testSubmitThesisWithNullId() {
        try {
            api.submitThesis(null);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testSubmitPastDeadlineThesis() {
        // Create a supervisor and a student
        Teacher supervisor = api.createTeacher(1L, "John Doe", "john.doe@example.com", "Computer Science");
        Student student = api.createStudent(2L, "Jane Smith", "jane.smith@example.com");

        // Create a thesis assignment and assign it to the student
        Assignment thesis = api.makeThesisAssignment(supervisor.getAIS(), "My Thesis", "BACHELOR", "This is my thesis");
        api.assignThesis(thesis.getID(), student.getAIS());

        // Set the thesis deadline to a past date
        thesis.setDeadline(LocalDate.now().minusDays(1));
        em.getTransaction().begin();
        em.merge(thesis);
        em.getTransaction().commit();
        try {
            api.submitThesis(thesis.getID());
            fail("Expected an IllegalStateException to be thrown.");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testDeleteThesis() {
        // Create a thesis to delete
        Teacher teacher = api.createTeacher(1L, "John Doe", "asdf", "Computer Science");
        Assignment thesis = api.makeThesisAssignment(1L, "Thesis Title", "BACHELOR", "Thesis Description");
        Long id = thesis.getID();

        Assignment thesis2 = api.deleteThesis(id);
//        assertEquals(thesis, thesis2);
        thesis = api.getThesis(id);
        assertNull(thesis);

    }

    @Test
    public void testDeleteThesisWithNullId() {
        try {
            api.deleteThesis(null);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testDeleteThesisWithNonexistentId() {
        // Try to delete a thesis with an ID that doesn't exist
        Assignment deletedThesis = api.deleteThesis(12345L);
        assertNull(deletedThesis);
    }

    @Test
    public void testGetThesesReturnsAllTheses() {
        Teacher teacher1 = api.createTeacher(1L, "John Doe", ":", "Computer Science");
        Teacher teacher2 = api.createTeacher(2L, "Jane Doe", "::", "Computer Science");

        // create some theses and persist them
        Assignment thesis1 = api.makeThesisAssignment(1L, "Thesis 1", "BACHELOR", "Thesis 1 description");
        Assignment thesis2 = api.makeThesisAssignment(2L, "Thesis 2", "BACHELOR", "Thesis 2 description");


        // call getTheses and verify that it returns both theses
        List<Assignment> result = api.getTheses();
        assertEquals(2, result.size());
        assertTrue(result.get(0).getID().equals(thesis1.getID()) || result.get(0).getID().equals(thesis2.getID()));
        assertTrue(result.get(1).getID().equals(thesis1.getID()) || result.get(1).getID().equals(thesis2.getID()));
    }

    @Test
    public void testGetThesesReturnsEmptyListWhenNoThesesExist() {
        // call getTheses when there are no theses and verify that it returns an empty list
        List<Assignment> theses = api.getTheses();
        assertEquals(0, theses.size());
    }

    @Test
    public void testGetThesesByTeacherWithValidInput() {
        Teacher teacher = api.createTeacher(1L, "John Doe", "johndoe@test.com", "School of Engineering");
        Assignment thesis1 = api.makeThesisAssignment(1L, "type1", "BACHELOR", "Thesis 1");
        Assignment thesis2 = api.makeThesisAssignment(1L, "Thesis 2", "BACHELOR", "description2");

        List<Assignment> result = api.getThesesByTeacher(teacher.getAIS());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getID().equals(thesis1.getID()) || result.get(0).getID().equals(thesis2.getID()));
        assertTrue(result.get(1).getID().equals(thesis1.getID()) || result.get(1).getID().equals(thesis2.getID()));

    }

    @Test
    public void testGetThesesByTeacherWithNoAssociatedTheses() {
        Teacher teacher1 = api.createTeacher(11L, "John Doe", "johndoe@test.com", "School of Engineering");
        Teacher teacher2 = api.createTeacher(12L, "Jane Doe", "janedoe@test.com", "School of Computer Science");

        List<Assignment> result = api.getThesesByTeacher(teacher2.getAIS());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetThesesByTeacherWithInvalidInput() {
        Long teacherId = 100L;

        List<Assignment> result = api.getThesesByTeacher(teacherId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    public void testGetThesesByStudentWithNullInput() {
        try {
            api.getThesisByStudent(null);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetThesisByStudentWithNonexistentStudent() {
        Long studentId = 100L; // assuming 100L is not a valid student ID
        Assignment thesis = api.getThesisByStudent(studentId);
        assertNull(thesis);
    }

    @Test
    public void testGetThesisByStudentWithExistingStudent() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "johndoe@example.com", "Computer Science");

        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "janesmith@example.com");

        // Create a thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");

        // Assign thesis to student
        api.assignThesis(thesis.getID(), 200L);

        Long studentId = 200L;
        Assignment thesisAssigned = api.getThesisByStudent(studentId);
        assertNotNull(thesisAssigned);
        assertEquals(studentId, thesisAssigned.getStudent().getAIS());
    }

    @Test
    public void testRemovedTeacherRemovesAllTheses() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");

        // Create a thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        Assignment thesis2 = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title 2", "BACHELOR", "Thesis Description 2");

        Teacher teacher1 = api.getTeacher(teacher.getAIS());
        List<Assignment> l = teacher1.getAssignments();

        // Remove the teacher
        api.deleteTeacher(teacher.getAIS());

        // Check that the teacher is gone
        Teacher teacher2 = api.getTeacher(teacher.getAIS());
        assertNull(teacher2);
        //check that the theses are gone
        Assignment thesis3 = api.getThesis(thesis.getID());
        Assignment thesis4 = api.getThesis(thesis2.getID());
        assertNull(thesis3);
        assertNull(thesis4);
    }


    @Test
    public void testDeleteThesisfromStudent() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");
        //create thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "a@a.com:123");
        //assign thesis to student
        api.assignThesis(thesis.getID(), student.getAIS());
        //delete thesis
        api.deleteThesis(thesis.getID());
        //check that the student is not assigned to the thesis
        Student student1 = api.getStudent(student.getAIS());
        assertNull(student1.getAssignment());
    }

    @Test
    public void testGetThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");
        //create thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        //get thesis
        Assignment thesis1 = api.getThesis(thesis.getID());
        //check that the thesis is the same
        assertEquals(thesis.getID(), thesis1.getID());
        assertEquals(thesis.getDescription(), thesis1.getDescription());
        assertEquals(thesis.getType(), thesis1.getType());
        assertEquals(thesis.getTeacher().getAIS(), thesis1.getTeacher().getAIS());
    }

    @Test
    public void testNullInputGetThesis() {
        try {
            api.getThesis(null);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testUpdateThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");
        //create thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");

        Assignment thesis1 = new Assignment();
        thesis1.setID(thesis.getID());
//        thesis1.setRegistrationNumber(thesis.getRegistrationNumber());
        thesis1.setDescription("new description");
        thesis1.setType(Type.MASTER);
        thesis1.setTeacher(teacher);
        thesis1.setTitle("new title");
        thesis1.setDescription("new description");

        // update thesis
        api.updateThesis(thesis1);

        // get thesis
        Assignment thesis2 = api.getThesis(thesis.getID());

        //check that the thesis is the same
        assertEquals(thesis1.getID(), thesis2.getID());
        assertEquals(thesis1.getDescription(), thesis2.getDescription());
        assertEquals(thesis1.getType(), thesis2.getType());
        assertEquals(thesis1.getTeacher().getAIS(), thesis2.getTeacher().getAIS());
    }

    @Test
    public void testNullInputUpdateThesis() {
        try {
            api.updateThesis(null);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testNullIdInputUpdateThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");
        //create thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");

        Assignment thesis1 = new Assignment();
        thesis1.setID(null);

        try {
            api.updateThesis(thesis1);
            fail("Expected an IllegalArgumentException to be thrown.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
    //todo test
    @Test
    public void testDeleteStudenSetThesisToNull() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");
        //create thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "aaa");
        //assign thesis to student
        api.assignThesis(thesis.getID(), student.getAIS());
        //delete student
        api.deleteStudent(student.getAIS());
        //check that the student is not assigned to the thesis
        Assignment thesis1 = api.getThesis(thesis.getID());
        assertNull(thesis1.getStudent());
    }

    @Test
    public void testTwoStudentsAttThesis() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");
        //create thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        // Create a student
        Student student = api.createStudent(200L, "Jane Smith", "aaa");
        Student student2 = api.createStudent(201L, "Jane Smith", "aaaa");
        //assign thesis to student
        api.assignThesis(thesis.getID(), student.getAIS());
        try {
            api.assignThesis(thesis.getID(), student2.getAIS());
            fail("Expected an IllegalStateException to be thrown.");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
        //check that the student is not assigned to the thesis
        Assignment thesis1 = api.getThesis(thesis.getID());
        assertEquals(thesis1.getStudent().getAIS(), student.getAIS());
        assertNull(student2.getAssignment());
    }

    @Test
    public void testDeleteThesisFromTeacher() {
        // Create a teacher
        Teacher teacher = api.createTeacher(100L, "John Doe", "asdf", "Computer Science");
        //create thesis
        Assignment thesis = api.makeThesisAssignment(teacher.getAIS(), "Thesis Title", "BACHELOR", "Thesis Description");
        //delete teacher
        api.deleteThesis(thesis.getID());
        //check that the teacher is not assigned to the thesis
        teacher = api.getTeacher(teacher.getAIS());
        assertEquals( 0,teacher.getAssignments().size());
    }
}








