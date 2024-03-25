package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource("/application.properties")
@SpringBootTest
class StudentAndGradeServiceTest {

    final static Logger LOGGER = LoggerFactory.logger(StudentAndGradeService.class);

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Autowired
    private ScienceGradesDao scienceGradeDao;

    @Autowired
    private HistoryGradesDao historyGradeDao;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @Test
    void createStudentService() {
        studentService.createStudent("Chad", "Darby", "chad.darby@luv2code_school.com");

       CollegeStudent student = studentDao.findByEmailAddress("chad.darby@luv2code_school.com");

       assertEquals("chad.darby@luv2code_school.com", student.getEmailAddress(), "find by mail");
    }

    @Test
    void isStudentNullCheck() {
        assertTrue(studentService.checkIfStudentIsNull(1));
        assertFalse(studentService.checkIfStudentIsNull(0));
    }

    @Test
    void deleteStudentService() {
        Optional<CollegeStudent> deletedCollegeStudent = studentDao.findById(1);
        Iterable<MathGrade> iterableMathGrade = mathGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> iterableHistoryGrade = historyGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> iterableScienceGrade = scienceGradeDao.findGradeByStudentId(1);

        iterableMathGrade.forEach(grade-> {
            Optional<MathGrade> oMathGrade = mathGradeDao.findById(grade.getId());
            assertTrue(oMathGrade.isPresent());
        });
        iterableHistoryGrade.forEach(grade-> {
            Optional<HistoryGrade> oHistoryGrade = historyGradeDao.findById(grade.getId());
            assertTrue(oHistoryGrade.isPresent());
        });
        iterableScienceGrade.forEach(grade-> {
            Optional<ScienceGrade> oScienceGrade = scienceGradeDao.findById(grade.getId());
            assertTrue(oScienceGrade.isPresent());
        });

        assertTrue(deletedCollegeStudent.isPresent(), "Return true");

        studentService.deleteStudent(1);

        deletedCollegeStudent = studentDao.findById(1);
        iterableMathGrade = mathGradeDao.findGradeByStudentId(1);
        iterableHistoryGrade = historyGradeDao.findGradeByStudentId(1);
        iterableScienceGrade = scienceGradeDao.findGradeByStudentId(1);
        assertFalse(deletedCollegeStudent.isPresent(), "Return false");
        assertFalse(iterableMathGrade.iterator().hasNext(), "Shouldn't have next");
        assertFalse(iterableHistoryGrade.iterator().hasNext(), "Shouldn't have next");
        assertFalse(iterableScienceGrade.iterator().hasNext(), "Shouldn't have next");
    }

    @Sql("/insertData.sql")
    @Test
    void getGradebookService() {
        Iterable<CollegeStudent> iterableCollegeStudents = studentService.getGradebook();

        List<CollegeStudent> collegeStudents = new ArrayList<>();

        for(CollegeStudent collegeStudent : iterableCollegeStudents) {
            collegeStudents.add(collegeStudent);
        }

        assertEquals(5, collegeStudents.size());
    }

    @Test
    void createGradeService() {
        // Create all grades
        assertTrue(studentService.createGrade(80.50, 1, "math"));
        assertTrue(studentService.createGrade(90.50, 1, "science"));
        assertTrue(studentService.createGrade(70.50, 1, "history"));

        // Get all grades with student id
        Iterable<MathGrade> mathgrades = mathGradeDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> sciencegrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historygrades = historyGradeDao.findGradeByStudentId(1);

        // Verify there is grades
        assertEquals(2, ((Collection<MathGrade>) mathgrades).size(),  "Student has math grades");
        assertEquals(2, ((Collection<ScienceGrade>) sciencegrades).size(),  "Student has science grades");
        assertEquals(2, ((Collection<HistoryGrade>) historygrades).size(),  "Student has history grades");

    }

    @Test
    void createGradeServiceReturnFalse() {
        assertFalse(studentService.createGrade(105, 1, "math"));
        assertFalse(studentService.createGrade(-5, 1,"math"));
        assertFalse(studentService.createGrade(80.50, 2, "math"));
        assertFalse(studentService.createGrade(80.50, 1, "literature"));
    }

    @Test
    void deleteGradeService() {
        assertEquals(1, studentService.deleteGrade(1, "math"), "Should return student id after delete");
        assertEquals(1, studentService.deleteGrade(1, "science"), "Should return student id after delete");
        assertEquals(1, studentService.deleteGrade(1, "history"), "Should return student id after delete");

    }

    @Test
    void deleteGradeServiceReturnStudentIdOfZero() {
        assertEquals(0, studentService.deleteGrade(5, "history"), "Should return 0 due to invalid input data");
        assertEquals(0, studentService.deleteGrade(1, "literature"), "Should return 0 due to invalid input data");
        assertEquals(0, studentService.deleteGrade(1, null), "Should return 0 due to invalid input data");
    }

    @Test
    void studentInformation() {
        GradebookCollegeStudent gradeBookCollegeStudent = studentService.studentInformation(1);

        assertNotNull(gradeBookCollegeStudent);
        assertEquals(1, gradeBookCollegeStudent.getId());
        assertEquals("Eric", gradeBookCollegeStudent.getFirstname());
        assertEquals("Roby", gradeBookCollegeStudent.getLastname());
        assertEquals("eric.roby@luv2code_school.com", gradeBookCollegeStudent.getEmailAddress());
        assertEquals(1, gradeBookCollegeStudent.getStudentGrades().getMathGradeResults().size());
        assertEquals(1, gradeBookCollegeStudent.getStudentGrades().getHistoryGradeResults().size());
        assertEquals(1, gradeBookCollegeStudent.getStudentGrades().getScienceGradeResults().size());
    }

    @Test
    void studentInformationServiceReturnNull() {
        GradebookCollegeStudent gradebookCollegeStudent = studentService.studentInformation(0);
        assertNull(gradebookCollegeStudent);
    }

    @AfterEach
    void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }
}
