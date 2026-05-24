package com.example.resumeanalyzer.model;

import java.util.List;

/**
 * Data Transfer Object (DTO) for the resume analysis response.
 * Contains score, detected skills, missing skills, contact details, strength level, and recommendations.
 */
public class ResumeResponse {
    
    private int score;
    private List<String> skillsFound;
    private List<String> missingSkills;
    private String email;
    private String phone;
    private boolean emailValid;
    private boolean phoneValid;
    private String strengthLevel;
    private List<String> suggestions;

    // Default constructor
    public ResumeResponse() {
    }

    public ResumeResponse(int score, List<String> skillsFound, List<String> missingSkills, 
                          String email, String phone, boolean emailValid, boolean phoneValid, 
                          String strengthLevel, List<String> suggestions) {
        this.score = score;
        this.skillsFound = skillsFound;
        this.missingSkills = missingSkills;
        this.email = email;
        this.phone = phone;
        this.emailValid = emailValid;
        this.phoneValid = phoneValid;
        this.strengthLevel = strengthLevel;
        this.suggestions = suggestions;
    }

    // Getters and Setters
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getSkillsFound() {
        return skillsFound;
    }

    public void setSkillsFound(List<String> skillsFound) {
        this.skillsFound = skillsFound;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEmailValid() {
        return emailValid;
    }

    public void setEmailValid(boolean emailValid) {
        this.emailValid = emailValid;
    }

    public boolean isPhoneValid() {
        return phoneValid;
    }

    public void setPhoneValid(boolean phoneValid) {
        this.phoneValid = phoneValid;
    }

    public String getStrengthLevel() {
        return strengthLevel;
    }

    public void setStrengthLevel(String strengthLevel) {
        this.strengthLevel = strengthLevel;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
