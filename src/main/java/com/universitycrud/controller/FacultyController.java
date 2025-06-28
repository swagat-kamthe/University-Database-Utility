package com.universitycrud.controller;

import com.universitycrud.model.Faculty;
import com.universitycrud.model.Student;
import com.universitycrud.repository.FacultyRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.*;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/faculties")
public class FacultyController {

    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private SpringTemplateEngine templateEngine;

    @GetMapping
    public String listFaculties(Model model) {
        model.addAttribute("faculties", facultyRepository.findAll());
        return "faculty/list";
    }

    @GetMapping("/add")
    public String addFacultyForm(Model model) {
        model.addAttribute("faculty", new Faculty());
        return "faculty/add";
    }

    @PostMapping("/add")
    public String saveFaculty(@ModelAttribute Faculty faculty) {
    	facultyRepository.save(faculty);
        return "redirect:/faculties";
    }

    @GetMapping("/edit/{id}")
    public String editFacultyForm(@PathVariable Long id, Model model) {
        model.addAttribute("faculty", facultyRepository.findById(id).orElse(null));
        return "faculty/edit";
    }

    @PostMapping("/update")
    public String updateFaculty(@ModelAttribute Faculty faculty) {
    	facultyRepository.save(faculty);
        return "redirect:/faculties";
    }

   
    
    @GetMapping("/delete/{id}")
    public String deleteFaculty(@PathVariable Long id) {
    	facultyRepository.deleteById(id);
        return "redirect:/faculties";
    }
    
    @GetMapping("/export/pdf")
    public void exportFacultiessToPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Faculty> faculties = facultyRepository.findAll();

        Context context = new Context();
        context.setVariable("faculties", faculties);

        String htmlContent = templateEngine.process("faculty/pdf-template", context);

        // Set response properties
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=faculties.pdf");

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
            return "redirect:/faculties";
        }

        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length >= 2) {
                    String name = data[0].trim();
                    String department = data[1].trim();

                    Faculty faculty = new Faculty();
                    faculty.setName(name);
                    faculty.setDepartment(department);

                    facultyRepository.save(faculty);
                }
            }

            model.addAttribute("message", "CSV file uploaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "An error occurred while uploading the file.");
        }

        return "redirect:/faculties";
    }

    
    
}