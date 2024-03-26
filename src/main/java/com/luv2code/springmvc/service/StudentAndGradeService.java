package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.Grade;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.models.HistoryGrade;
import com.luv2code.springmvc.models.MathGrade;
import com.luv2code.springmvc.models.ScienceGrade;
import com.luv2code.springmvc.models.StudentGrades;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class StudentAndGradeService {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    @Qualifier("mathGrades")
    private MathGrade mathGrade;

    @Autowired
    @Qualifier("scienceGrades")
    private ScienceGrade scienceGrade;

    @Autowired
    @Qualifier("historyGrades")
    private HistoryGrade historyGrade;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Autowired
    private ScienceGradesDao scienceGradeDao;

    @Autowired
    private HistoryGradesDao historyGradeDao;

    @Autowired
    private StudentGrades studentGrades;

    public void createStudent(String firstName, String lastName, String emailAddress) {
        CollegeStudent student = new CollegeStudent(firstName, lastName, emailAddress);
        student.setId(0);
        studentDao.save(student);
    }

    public boolean checkIfStudentIsNull(int id) {
        Optional<CollegeStudent> student = studentDao.findById(id);
        return student.isPresent();
    }

    public void deleteStudent(int id) {
        if(checkIfStudentIsNull(id)) {
            studentDao.deleteById(id);
            mathGradeDao.deleteByStudentId(id);
            historyGradeDao.deleteByStudentId(id);
            scienceGradeDao.deleteByStudentId(id);
        }
    }

    public Iterable<CollegeStudent> getGradebook() {
        return studentDao.findAll();
    }

    public boolean createGrade(double grade, int studentId, String gradeType) {
        if(!checkIfStudentIsNull(studentId)) {
            return false;
        }
        if(grade >= 0 && grade <= 100) {
            if("math".equals(gradeType)) {
                mathGrade.setId(0);
                mathGrade.setGrade(grade);
                mathGrade.setStudentId(studentId);
                mathGradeDao.save(mathGrade);
                return true;
            } else if("science".equals(gradeType)) {
                scienceGrade.setId(0);
                scienceGrade.setGrade(grade);
                scienceGrade.setStudentId(studentId);
                scienceGradeDao.save(scienceGrade);
                return true;
            } else if("history".equals(gradeType)) {
                historyGrade.setId(0);
                historyGrade.setGrade(grade);
                historyGrade.setStudentId(studentId);
                historyGradeDao.save(historyGrade);
                return true;
            }
        }
        return false;
    }

    public int deleteGrade(int id, String type) {

        int studentId = 0;
        CrudRepository gradeDao = null;

        if("math".equals(type)) {
            gradeDao = mathGradeDao;
        } else if("science".equals(type)) {
            gradeDao = scienceGradeDao;
        } else if("history".equals(type)) {
            gradeDao = historyGradeDao;
        } else {
            return studentId;
        }


        Optional<Grade> oGrade = gradeDao.findById(id);
        if (oGrade.isPresent()) {
            studentId = oGrade.get().getStudentId();
            gradeDao.delete(oGrade.get());
        }

        return studentId;
    }

    public GradebookCollegeStudent studentInformation(int id) {
        Optional<CollegeStudent> student = studentDao.findById(id);
        if(student.isEmpty()) {
            return null;
        }
        Iterable<MathGrade> mathGrades = mathGradeDao.findGradeByStudentId(id);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(id);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(id);

        List<Grade> mathGradeList = StreamSupport.stream(mathGrades.spliterator(), false).collect(Collectors.toList());
        List<Grade> historyGradeList = StreamSupport.stream(historyGrades.spliterator(), false).collect(Collectors.toList());
        List<Grade> scienceGradeList = StreamSupport.stream(scienceGrades.spliterator(), false).collect(Collectors.toList());

        studentGrades.setMathGradeResults(mathGradeList);
        studentGrades.setHistoryGradeResults(historyGradeList);
        studentGrades.setScienceGradeResults(scienceGradeList);

        return new GradebookCollegeStudent(
              student.get().getId(),
              student.get().getFirstname(),
                student.get().getLastname(),
                student.get().getEmailAddress(),
                studentGrades
        );
    }

    public void configureStudentInformationModel(int studentId, Model m) {
        GradebookCollegeStudent studentEntity = this.studentInformation(studentId);
        m.addAttribute("student", studentEntity);

        if(!studentEntity.getStudentGrades().getMathGradeResults().isEmpty()) {
            m.addAttribute("mathAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getMathGradeResults()
            ));
        } else {
            m.addAttribute("mathAverage", "N/A");
        }

        if(!studentEntity.getStudentGrades().getScienceGradeResults().isEmpty()) {
            m.addAttribute("scienceAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getScienceGradeResults()
            ));
        } else {
            m.addAttribute("scienceAverage", "N/A");
        }

        if(!studentEntity.getStudentGrades().getHistoryGradeResults().isEmpty()) {
            m.addAttribute("historyAverage", studentEntity.getStudentGrades().findGradePointAverage(
                    studentEntity.getStudentGrades().getHistoryGradeResults()
            ));
        } else {
            m.addAttribute("historyAverage", "N/A");
        }
    }
}
