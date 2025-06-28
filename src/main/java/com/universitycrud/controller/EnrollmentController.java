package com.universitycrud.controller;

import com.lowagie.text.DocumentException;
import com.universitycrud.model.Course;
import com.universitycrud.model.Enrollment;
import com.universitycrud.model.Student;
import com.universitycrud.repository.CourseRepository;
import com.universitycrud.repository.EnrollmentRepository;
import com.universitycrud.repository.StudentRepository;

import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.*;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @GetMapping
    public String listEnrollments(Model model) {
        model.addAttribute("enrollments", enrollmentRepository.findAll());
        return "enrollment/list";
    }

    @GetMapping("/add")
    public String addEnrollmentForm(Model model) {
        model.addAttribute("enrollment", new Enrollment());
        return "enrollment/add";
    }

    @PostMapping("/add")
    public String saveEnrollment(@ModelAttribute Enrollment enrollment) {
        enrollmentRepository.save(enrollment);
        return "redirect:/enrollments";
    }

    @GetMapping("/edit/{id}")
    public String editEnrollmentForm(@PathVariable Long id, Model model) {
        Enrollment enrollment = enrollmentRepository.findById(id).orElse(null);
        if (enrollment != null) {
            model.addAttribute("enrollment", enrollment);
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("courses", courseRepository.findAll());
            return "enrollment/edit";
        }
        return "redirect:/enrollments";
    }

    @PostMapping("/update")
    public String updateEnrollment(@ModelAttribute Enrollment enrollment) {
        enrollmentRepository.save(enrollment);
        return "redirect:/enrollments";
    }

    @GetMapping("/delete/{id}")
    public String deleteEnrollment(@PathVariable Long id) {
        enrollmentRepository.deleteById(id);
        return "redirect:/enrollments";
    }
    
    
    @GetMapping("/export/pdf")
    public void exportEnrollmentsToPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Enrollment> enrollments = enrollmentRepository.findAll();

        Context context = new Context();
        context.setVariable("enrollments", enrollments);

        String htmlContent = templateEngine.process("enrollment/pdf-template", context);

        // Set response properties
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=enrollments.pdf");

        // Render PDF
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(response.getOutputStream());
    }

    
    @PostMapping("/upload/csv")
    public String uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "redirect:/enrollments?error=File+is+empty";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 3) continue;

                Long studentId = Long.parseLong(data[0].trim());
                Long courseId = Long.parseLong(data[1].trim());
                String semester = data[2].trim();

                Student student = studentRepository.findById(studentId).orElse(null);
                Course course = courseRepository.findById(courseId).orElse(null);

                if (student != null && course != null) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setStudent(student);
                    enrollment.setCourse(course);
                    enrollment.setSemester(semester);
                    enrollmentRepository.save(enrollment);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/enrollments?error=Upload+failed";
        }

        return "redirect:/enrollments?success=CSV+uploaded";
    }
    
    
}
