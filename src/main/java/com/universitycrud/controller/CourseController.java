package com.universitycrud.controller;

import com.universitycrud.model.Course;
import com.universitycrud.model.Enrollment;
import com.universitycrud.model.Faculty;
import com.universitycrud.model.Student;
import com.universitycrud.repository.CourseRepository;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.universitycrud.model.Faculty;
import com.universitycrud.repository.FacultyRepository;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private SpringTemplateEngine templateEngine;

    @GetMapping
    public String listCourses(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        return "course/list";
    }

    @GetMapping("/add")
    public String addCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "course/add";
    }

    @PostMapping("/add")
    public String saveCourse(@ModelAttribute Course course) {
        courseRepository.save(course);
        return "redirect:/courses";
    }

    @GetMapping("/edit/{id}")
    public String editCourseForm(@PathVariable Long id, Model model) {
        Course course = courseRepository.findById(id).orElse(null);
        if (course != null && course.getFaculty() == null) {
            course.setFaculty(new Faculty()); // avoid null pointer on form
        }
        model.addAttribute("course", course);
        return "course/edit";
    }


    @PostMapping("/update")
    public String updateCourse(@ModelAttribute Course course) {
        courseRepository.save(course);
        return "redirect:/courses";
    }

    @GetMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseRepository.deleteById(id);
        return "redirect:/courses";
    }
    
    
    
    @GetMapping("/export/pdf")
    public void exportStudentsToPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Course> courses = courseRepository.findAll();

        Context context = new Context();
        context.setVariable("courses", courses);

        String htmlContent = templateEngine.process("course/pdf-template", context);

        // Set response properties
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=courses.pdf");

        // Render PDF
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(response.getOutputStream());
    }
    
    
    
    
    @PostMapping("/upload/csv")
    public String uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "redirect:/courses?error=File+is+empty";
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
                if (data.length < 4) continue;

                String name = data[0].trim();
                String code = data[1].trim();
                String description = data[2].trim();
                Long facultyId = Long.parseLong(data[3].trim());


                Faculty faculty = facultyRepository.findById(facultyId).orElse(null);

                if (faculty != null) {
                    Course course = new Course();
                    course.setFaculty(faculty);
                    course.setName(name);
                    course.setCode(code);
                    course.setDescription(description);
                    
                    courseRepository.save(course);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/courses?error=Upload+failed";
        }

        return "redirect:/courses?success=CSV+uploaded";
    }
}