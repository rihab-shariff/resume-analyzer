package com.example.resumeanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class to bootstrap the Spring Boot Resume Analyzer application.
 */
@SpringBootApplication
public class ResumeAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeAnalyzerApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  AI Resume Analyzer Backend Started Successfully! ");
        System.out.println("  Running on: http://localhost:8080               ");
        System.out.println("=================================================");
    }
}
