package com.example.resumeanalyzer.controller;

import com.example.resumeanalyzer.model.ResumeRequest;
import com.example.resumeanalyzer.model.ResumeResponse;
import com.example.resumeanalyzer.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Resume Analysis.
 * Exposes endpoint to receive resume texts and return evaluation details.
 * CrossOrigin is enabled to allow frontend web requests.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows local HTML files to access the backend server
public class ResumeController {

    private final ResumeService resumeService;

    @Autowired
    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Endpoint to analyze resume text.
     * URL: POST http://localhost:8080/api/analyze
     *
     * @param request Payload containing raw resume text.
     * @return ResponseEntity with the structured ResumeResponse analysis output.
     */
    @PostMapping("/analyze")
    public ResponseEntity<ResumeResponse> analyzeResume(@RequestBody ResumeRequest request) {
        if (request == null || request.getText() == null) {
            return ResponseEntity.badRequest().body(new ResumeResponse());
        }
        
        ResumeResponse response = resumeService.analyzeResume(request.getText());
        return ResponseEntity.ok(response);
    }
}
