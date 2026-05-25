package com.example.resumeanalyzer.model;

/**
 * Data Transfer Object (DTO) for the resume analysis request payload.
 */
public class ResumeRequest {
    
    private String text;

    // Default constructor (needed for JSON deserialization)
    public ResumeRequest() {
    }

    public ResumeRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
