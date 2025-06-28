package com.universitycrud.controller;

import com.universitycrud.model.Student;
import com.universitycrud.repository.StudentRepository;
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

import java.io.IOException;
import java.util.List;


import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        return "student/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        return "student/add";
    }

    @PostMapping("/add")
    public String addStudent(@ModelAttribute Student student) {
        studentRepository.save(student);
        return "redirect:/students";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID: " + id));
        model.addAttribute("student", student);
        return "student/edit";
    }

    @PostMapping("/update")
    public String updateStudent(@ModelAttribute Student student) {
        studentRepository.save(student);
        return "redirect:/students";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id) {
        studentRepository.deleteById(id);
        return "redirect:/students";
    }
    
    
    
    @GetMapping("/export/pdf")
    public void exportStudentsToPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Student> students = studentRepository.findAll();

        Context context = new Context();
        context.setVariable("students", students);

        String htmlContent = templateEngine.process("student/pdf-template", context);

        // Set response properties
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=students.pdf");

        // Render PDF
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(response.getOutputStream());
    }

    
    
    @PostMapping("/upload")
    public String uploadCSVFile(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a CSV file to upload.");
            return "redirect:/students";
        }

        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue; // skip header row
                }

                String[] data = line.split(",");
                if (data.length >= 2) {
                    String name = data[0].trim();
                    String email = data[1].trim();

                    Student student = new Student(name, email);
                    studentRepository.save(student);
                }
            }

            model.addAttribute("message", "CSV file uploaded successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "An error occurred while uploading the file.");
        }

        return "redirect:/students";
    }
    
    
}
