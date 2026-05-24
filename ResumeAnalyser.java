import java.util.*;
import java.io.*;

public class ResumeAnalyser {

    // ─── Colour helpers (ANSI) ───────────────────────────────────────────────
    static final String RESET  = "\u001B[0m";
    static final String GREEN  = "\u001B[32m";
    static final String RED    = "\u001B[31m";
    static final String YELLOW = "\u001B[33m";
    static final String CYAN   = "\u001B[36m";
    static final String BOLD   = "\u001B[1m";

    // ─── Common section headers a resume might contain ───────────────────────
    static final List<String> SECTIONS = Arrays.asList(
        "objective", "summary", "education", "experience",
        "skills", "projects", "certifications", "achievements",
        "hobbies", "references", "contact"
    );

    // ─── Regex patterns ──────────────────────────────────────────────────────
    static final String EMAIL_REGEX = "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}";
    static final String PHONE_REGEX = "(\\+91[\\-\\s]?)?[6-9]\\d{9}|\\d{3}[\\-\\s]\\d{3}[\\-\\s]\\d{4}";

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        printBanner();

        System.out.println(CYAN + "Choose input method:" + RESET);
        System.out.println("  1. Paste resume text manually");
        System.out.println("  2. Load resume from a .txt file");
        System.out.print(BOLD + "Enter choice (1/2): " + RESET);

        String choice = sc.nextLine().trim();
        String resumeText = "";

        if (choice.equals("2")) {
            System.out.print("Enter file path (e.g. resume.txt): ");
            String path = sc.nextLine().trim();
            resumeText = readFile(path);
            if (resumeText == null) {
                System.out.println(RED + "File not found! Switching to manual input." + RESET);
                resumeText = readManual(sc);
            }
        } else {
            resumeText = readManual(sc);
        }

        System.out.print(BOLD + "\nEnter the Job Description (type END on a new line when done):\n" + RESET);
        StringBuilder jd = new StringBuilder();
        String line;
        while (!(line = sc.nextLine()).equalsIgnoreCase("END")) {
            jd.append(line).append(" ");
        }
        String jobDescription = jd.toString();

        // ── Run all analyses ─────────────────────────────────────────────────
        System.out.println("\n" + BOLD + CYAN + "═══════════════════════════════════════════" + RESET);
        System.out.println(BOLD + CYAN + "          RESUME ANALYSIS REPORT           " + RESET);
        System.out.println(BOLD + CYAN + "═══════════════════════════════════════════" + RESET);

        extractContactInfo(resumeText);
        checkSections(resumeText);
        int completeness = completenessScore(resumeText);
        int matchScore   = keywordMatch(resumeText, jobDescription);
        int lengthScore  = lengthScore(resumeText);

        // ── Final Score ──────────────────────────────────────────────────────
        int total = (completeness + matchScore + lengthScore) / 3;
        printFinalScore(total);

        sc.close();
    }

    // ─── Banner ──────────────────────────────────────────────────────────────
    static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("  ____  _____ ____  _   _ __  __ _____");
        System.out.println(" |  _ \\| ____/ ___|| | | |  \\/  | ____|");
        System.out.println(" | |_) |  _| \\___ \\| | | | |\\/| |  _|");
        System.out.println(" |  _ <| |___ ___) | |_| | |  | | |___");
        System.out.println(" |_| \\_\\_____|____/ \\___/|_|  |_|_____|");
        System.out.println("          A N A L Y S E R  v1.0");
        System.out.println(RESET);
    }

    // ─── Read file ───────────────────────────────────────────────────────────
    static String readFile(String path) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(path));
            String l;
            while ((l = br.readLine()) != null) sb.append(l).append("\n");
            br.close();
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    // ─── Read manual paste ───────────────────────────────────────────────────
    static String readManual(Scanner sc) {
        System.out.println(BOLD + "Paste your resume text below (type END on a new line when done):" + RESET);
        StringBuilder sb = new StringBuilder();
        String line;
        while (!(line = sc.nextLine()).equalsIgnoreCase("END")) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    // ─── 1. Extract contact info ─────────────────────────────────────────────
    static void extractContactInfo(String text) {
        System.out.println("\n" + BOLD + "📋 CONTACT INFORMATION" + RESET);
        System.out.println("─────────────────────────────────────────");

        // Email
        java.util.regex.Pattern ep = java.util.regex.Pattern.compile(EMAIL_REGEX);
        java.util.regex.Matcher em = ep.matcher(text);
        if (em.find()) {
            System.out.println(GREEN + "  ✔ Email   : " + em.group() + RESET);
        } else {
            System.out.println(RED + "  ✘ Email   : Not found" + RESET);
        }

        // Phone
        java.util.regex.Pattern pp = java.util.regex.Pattern.compile(PHONE_REGEX);
        java.util.regex.Matcher pm = pp.matcher(text);
        if (pm.find()) {
            System.out.println(GREEN + "  ✔ Phone   : " + pm.group() + RESET);
        } else {
            System.out.println(RED + "  ✘ Phone   : Not found" + RESET);
        }

        // LinkedIn (simple check)
        if (text.toLowerCase().contains("linkedin")) {
            System.out.println(GREEN + "  ✔ LinkedIn: Mentioned" + RESET);
        } else {
            System.out.println(YELLOW + "  ~ LinkedIn: Not mentioned (recommended)" + RESET);
        }

        // GitHub
        if (text.toLowerCase().contains("github")) {
            System.out.println(GREEN + "  ✔ GitHub  : Mentioned" + RESET);
        } else {
            System.out.println(YELLOW + "  ~ GitHub  : Not mentioned (optional)" + RESET);
        }
    }

    // ─── 2. Check which sections are present ─────────────────────────────────
    static void checkSections(String text) {
        System.out.println("\n" + BOLD + "📂 RESUME SECTIONS DETECTED" + RESET);
        System.out.println("─────────────────────────────────────────");
        String lower = text.toLowerCase();
        int found = 0;
        for (String section : SECTIONS) {
            if (lower.contains(section)) {
                System.out.println(GREEN + "  ✔ " + capitalize(section) + RESET);
                found++;
            } else {
                System.out.println(RED + "  ✘ " + capitalize(section) + " (missing)" + RESET);
            }
        }
        System.out.println(CYAN + "  Sections found: " + found + " / " + SECTIONS.size() + RESET);
    }

    // ─── 3. Completeness score ───────────────────────────────────────────────
    static int completenessScore(String text) {
        String lower = text.toLowerCase();
        int score = 0;
        // Required sections contribute more
        String[] required = {"education", "experience", "skills", "contact", "objective"};
        for (String r : required) {
            if (lower.contains(r)) score += 14;   // 5 × 14 = 70
        }
        // Bonus sections
        String[] bonus = {"projects", "certifications", "achievements"};
        for (String b : bonus) {
            if (lower.contains(b)) score += 10;   // up to 30
        }
        score = Math.min(score, 100);

        System.out.println("\n" + BOLD + "✅ COMPLETENESS SCORE" + RESET);
        System.out.println("─────────────────────────────────────────");
        printBar(score);
        return score;
    }

    // ─── 4. Keyword match with job description ───────────────────────────────
    static int keywordMatch(String resume, String jd) {
        System.out.println("\n" + BOLD + "🔍 JOB DESCRIPTION KEYWORD MATCH" + RESET);
        System.out.println("─────────────────────────────────────────");

        if (jd.trim().isEmpty()) {
            System.out.println(YELLOW + "  No job description provided — skipping." + RESET);
            return 50;
        }

        // Tokenise JD → unique meaningful words (length > 3)
        Set<String> jdKeywords = new LinkedHashSet<>();
        for (String word : jd.toLowerCase().split("[^a-zA-Z0-9#+.]+")) {
            if (word.length() > 3) jdKeywords.add(word);
        }

        String lowerResume = resume.toLowerCase();
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String kw : jdKeywords) {
            if (lowerResume.contains(kw)) matched.add(kw);
            else missing.add(kw);
        }

        int score = jdKeywords.isEmpty() ? 50 : (int)((matched.size() * 100.0) / jdKeywords.size());

        System.out.println(GREEN + "  Matched keywords (" + matched.size() + "): " + RESET + String.join(", ", matched));
        if (!missing.isEmpty()) {
            System.out.println(RED + "  Missing keywords (" + missing.size() + "): " + RESET + String.join(", ", missing));
        }
        printBar(score);
        return score;
    }

    // ─── 5. Length / word-count score ────────────────────────────────────────
    static int lengthScore(String text) {
        System.out.println("\n" + BOLD + "📏 RESUME LENGTH ANALYSIS" + RESET);
        System.out.println("─────────────────────────────────────────");

        String[] words = text.trim().split("\\s+");
        int wc = words.length;
        int score;
        String remark;

        if (wc < 100) {
            score = 30; remark = "Too short — add more detail.";
        } else if (wc < 300) {
            score = 60; remark = "A bit brief — consider expanding.";
        } else if (wc <= 700) {
            score = 100; remark = "Ideal length!";
        } else if (wc <= 1000) {
            score = 80; remark = "Slightly long — try to trim.";
        } else {
            score = 50; remark = "Too long — keep it to 1–2 pages.";
        }

        System.out.println("  Word count : " + wc);
        System.out.println("  Remark     : " + remark);
        printBar(score);
        return score;
    }

    // ─── Final overall score ─────────────────────────────────────────────────
    static void printFinalScore(int total) {
        System.out.println("\n" + BOLD + CYAN + "═══════════════════════════════════════════" + RESET);
        System.out.println(BOLD + CYAN + "           OVERALL RESUME SCORE            " + RESET);
        System.out.println(BOLD + CYAN + "═══════════════════════════════════════════" + RESET);
        printBar(total);

        String grade;
        if      (total >= 85) grade = GREEN  + BOLD + "Excellent! Your resume is strong. 🚀"   + RESET;
        else if (total >= 65) grade = YELLOW + BOLD + "Good, but a few improvements needed. 👍" + RESET;
        else if (total >= 45) grade = YELLOW + BOLD + "Average — work on missing sections. ✏️"  + RESET;
        else                  grade = RED    + BOLD + "Needs major improvement. ⚠️"             + RESET;

        System.out.println("  " + grade);
        System.out.println(BOLD + CYAN + "═══════════════════════════════════════════\n" + RESET);
    }

    // ─── ASCII progress bar ───────────────────────────────────────────────────
    static void printBar(int score) {
        int filled = score / 5;           // 20-block bar
        StringBuilder bar = new StringBuilder("  [");
        for (int i = 0; i < 20; i++) bar.append(i < filled ? "█" : "░");
        bar.append("] ").append(score).append("%");

        String color = score >= 75 ? GREEN : score >= 50 ? YELLOW : RED;
        System.out.println(color + bar + RESET);
    }

    // ─── Utility ─────────────────────────────────────────────────────────────
    static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
