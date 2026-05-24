package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.model.ResumeResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class containing business logic for analyzing resumes.
 * It detects skills, validates contact details, checks education keywords,
 * computes score, and generates customized suggestions.
 */
@Service
public class ResumeService {

    // List of core skills to analyze
    private final List<String> targetSkills = Arrays.asList(
            "Java", "Python", "SQL", "HTML", "CSS", "JavaScript", "Communication", "Teamwork"
    );

    // List of education keywords
    private final List<String> educationKeywords = Arrays.asList(
            "Bachelor", "Master", "PhD", "Degree", "B.Tech", "M.Tech", "B.E.", "MBA", 
            "B.Sc", "M.Sc", "University", "College", "School", "Education", "Graduate"
    );

    // Regular Expressions for Contact Information
    // Broad regex to detect any email-like token
    private static final Pattern BROAD_EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    // Strict regex to validate proper email format
    private static final Pattern STRICT_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    // Broad regex to detect potential phone number structures
    private static final Pattern BROAD_PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,4}[- .]?)?(\\(?\\d{3}\\)?[- .]?)?\\d{3}[- .]?\\d{4}");
    // Strict regex to validate standard phone numbers (10 digits, optional country code)
    private static final Pattern STRICT_PHONE_PATTERN = Pattern.compile("^(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}$");

    /**
     * Performs full resume analysis on raw input text.
     * @param text Raw resume content.
     * @return ResumeResponse containing structured analysis results.
     */
    public ResumeResponse analyzeResume(String text) {
        if (text == null || text.trim().isEmpty()) {
            return createEmptyResponse();
        }

        List<String> skillsFound = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        // 1. Skill Detection
        for (String skill : targetSkills) {
            // Regex to match skill as a whole word case-insensitively
            // Handles JS mapping to JavaScript or matching exact words
            String regexPattern = "\\b" + Pattern.quote(skill) + "\\b";
            if (skill.equalsIgnoreCase("JavaScript")) {
                regexPattern = "\\b(JavaScript|JS)\\b";
            }
            Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                skillsFound.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        // 2. Email Extraction and Validation
        String detectedEmail = "Not Found";
        boolean isEmailValid = false;
        Matcher emailMatcher = BROAD_EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            detectedEmail = emailMatcher.group().trim();
            isEmailValid = STRICT_EMAIL_PATTERN.matcher(detectedEmail).matches();
        }

        // 3. Phone Extraction and Validation
        String detectedPhone = "Not Found";
        boolean isPhoneValid = false;
        Matcher phoneMatcher = BROAD_PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            detectedPhone = phoneMatcher.group().trim();
            isPhoneValid = STRICT_PHONE_PATTERN.matcher(detectedPhone).matches();
        }

        // 4. Education Keyword Check
        boolean hasEducation = false;
        for (String keyword : educationKeywords) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                hasEducation = true;
                break;
            }
        }

        // 5. Compute Resume Score
        int score = calculateScore(skillsFound.size(), isEmailValid, isPhoneValid, hasEducation);

        // 6. Determine Strength Level
        String strengthLevel;
        if (score < 50) {
            strengthLevel = "Beginner";
        } else if (score < 80) {
            strengthLevel = "Intermediate";
        } else {
            strengthLevel = "Strong";
        }

        // 7. Generate Suggestions
        List<String> suggestions = generateSuggestions(skillsFound, missingSkills, detectedEmail, isEmailValid, 
                                                       detectedPhone, isPhoneValid, hasEducation, strengthLevel);

        return new ResumeResponse(
                score, skillsFound, missingSkills, detectedEmail, detectedPhone, 
                isEmailValid, isPhoneValid, strengthLevel, suggestions
        );
    }

    /**
     * Helper to compute the resume score.
     */
    private int calculateScore(int skillsCount, boolean isEmailValid, boolean isPhoneValid, boolean hasEducation) {
        int score = 0;
        
        // 8 skills * 8 points = Max 64 points
        score += (skillsCount * 8);

        // Valid Email presence = 12 points
        if (isEmailValid) {
            score += 12;
        }

        // Valid Phone presence = 12 points
        if (isPhoneValid) {
            score += 12;
        }

        // Education presence = 12 points
        if (hasEducation) {
            score += 12;
        }

        // Clip to maximum 100 points
        return Math.min(score, 100);
    }

    /**
     * Helper to generate smart recommendations.
     */
    private List<String> generateSuggestions(List<String> skillsFound, List<String> missingSkills, 
                                             String email, boolean isEmailValid, 
                                             String phone, boolean isPhoneValid, 
                                             boolean hasEducation, String strength) {
        List<String> suggestions = new ArrayList<>();

        // Contact Suggestions
        if (email.equals("Not Found")) {
            suggestions.add("Add your email address in the contact section so recruiters can reach out.");
        } else if (!isEmailValid) {
            suggestions.add("Ensure your email address format is valid (e.g., example@domain.com). Current detected: \"" + email + "\".");
        }

        if (phone.equals("Not Found")) {
            suggestions.add("Include a phone number in your resume contact header.");
        } else if (!isPhoneValid) {
            suggestions.add("Format your phone number using a standard template (e.g., +1-123-456-7890 or 10 digits). Current detected: \"" + phone + "\".");
        }

        // Education Suggestions
        if (!hasEducation) {
            suggestions.add("Mention your educational background, degrees, or certifications (e.g., Bachelor, B.Tech, Master).");
        }

        // Skill Suggestions
        if (!missingSkills.isEmpty()) {
            if (missingSkills.size() > 4) {
                suggestions.add("Add more core technical skills like " + String.join(", ", missingSkills.subList(0, 3)) + " to stand out.");
            } else {
                suggestions.add("Consider learning and adding: " + String.join(", ", missingSkills) + ".");
            }
        }

        // Strength Level General Suggestions
        if (strength.equals("Beginner")) {
            suggestions.add("Strengthen your resume by building projects using Java, Python, or Web Tech, and detailing your contributions.");
        } else if (strength.equals("Intermediate")) {
            suggestions.add("Excellent start! Highlight your professional achievements, soft skills (like teamwork), and quantify your project impacts.");
        } else {
            suggestions.add("Great resume layout and skills. Keep it updated and customize the summary specifically for the roles you apply to.");
        }

        return suggestions;
    }

    /**
     * Helper to generate an empty/null-safe response.
     */
    private ResumeResponse createEmptyResponse() {
        return new ResumeResponse(
                0, new ArrayList<>(), targetSkills, "Not Found", "Not Found", 
                false, false, "Beginner", 
                Arrays.asList("Please paste some resume text in the box and try analyzing again.")
        );
    }
}
