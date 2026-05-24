// ==========================================================================
// AI Resume Analyzer - Frontend Interaction Logic
// Features: Fetch API, Circular Progress Animation, Report Exporter, Form Controls
// ==========================================================================

document.addEventListener("DOMContentLoaded", () => {
    // 1. DOM Element Selectors
    const resumeInput = document.getElementById("resumeInput");
    const charCount = document.getElementById("charCount");
    const wordCount = document.getElementById("wordCount");
    const analyzeBtn = document.getElementById("analyzeBtn");
    const clearBtn = document.getElementById("clearBtn");
    const downloadBtn = document.getElementById("downloadBtn");

    const loadingSection = document.getElementById("loadingSection");
    const resultSection = document.getElementById("resultSection");

    const scoreNumber = document.getElementById("scoreNumber");
    const strengthBadge = document.getElementById("strengthBadge");
    const progressCircle = document.querySelector(".progress-ring__circle");

    const emailText = document.getElementById("emailText");
    const emailStatusBadge = document.getElementById("emailStatusBadge");
    const emailValIcon = document.getElementById("emailValIcon");

    const phoneText = document.getElementById("phoneText");
    const phoneStatusBadge = document.getElementById("phoneStatusBadge");
    const phoneValIcon = document.getElementById("phoneValIcon");

    const educationText = document.getElementById("educationText");
    const educationStatusBadge = document.getElementById("educationStatusBadge");
    const educationValIcon = document.getElementById("educationValIcon");

    const skillsFoundList = document.getElementById("skillsFoundList");
    const missingSkillsList = document.getElementById("missingSkillsList");
    const suggestionsList = document.getElementById("suggestionsList");

    // Circular Progress Settings
    const radius = 58;
    const circumference = 2 * Math.PI * radius; // Approx 364.42
    
    // Initialize circular progress offset
    progressCircle.style.strokeDasharray = `${circumference} ${circumference}`;
    progressCircle.style.strokeDashoffset = circumference;

    // Cache variable for downloading reports
    let currentAnalysisData = null;

    // Initialize Lucide icons on page load
    lucide.createIcons();

    // 2. Character & Word Count Handler
    resumeInput.addEventListener("input", () => {
        const text = resumeInput.value;
        charCount.textContent = `${text.length} characters`;
        
        const words = text.trim() === "" ? 0 : text.trim().split(/\s+/).length;
        wordCount.textContent = `${words} words`;
    });

    // 3. Clear Button Handler
    clearBtn.addEventListener("click", () => {
        resumeInput.value = "";
        charCount.textContent = "0 characters";
        wordCount.textContent = "0 words";
        resultSection.classList.add("hidden");
        loadingSection.classList.add("hidden");
        currentAnalysisData = null;
        resumeInput.focus();
    });

    // 4. API Request Handler
    analyzeBtn.addEventListener("click", async () => {
        const text = resumeInput.value.trim();

        if (text === "") {
            alert("Please paste your resume text before analyzing!");
            resumeInput.focus();
            return;
        }

        // Show loading screen and hide previous results
        loadingSection.classList.remove("hidden");
        resultSection.classList.add("hidden");
        analyzeBtn.disabled = true;

        try {
            // REST API Call
            const response = await fetch("http://localhost:8080/api/analyze", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ text: text })
            });

            if (!response.ok) {
                throw new Error("Server returned an error. Make sure the Spring Boot backend is running.");
            }

            const data = await response.json();
            currentAnalysisData = data;

            // Mock delay to show loading animation (adds premium feel)
            setTimeout(() => {
                renderResults(data);
                loadingSection.classList.add("hidden");
                resultSection.classList.remove("hidden");
                analyzeBtn.disabled = false;
                
                // Scroll smoothly to results card
                resultSection.scrollIntoView({ behavior: "smooth" });
            }, 800);

        } catch (error) {
            console.error("Analysis failed:", error);
            alert("Unable to reach the backend server. Make sure your Java Spring Boot application is started on http://localhost:8080!\n\nError details: " + error.message);
            loadingSection.classList.add("hidden");
            analyzeBtn.disabled = false;
        }
    });

    // 5. Dynamic Content Renderer
    function renderResults(data) {
        // Animate circular progress and counter
        animateScore(data.score);

        // Update Strength Badge
        strengthBadge.textContent = data.strengthLevel;
        strengthBadge.className = "badge"; // Reset classes
        if (data.strengthLevel === "Beginner") {
            strengthBadge.classList.add("badge-warning");
        } else if (data.strengthLevel === "Intermediate") {
            strengthBadge.classList.add("badge-primary");
        } else {
            strengthBadge.classList.add("badge-success");
        }

        // Render Email Check
        emailText.textContent = data.email;
        updateValidationItem(
            emailStatusBadge,
            emailValIcon,
            data.email !== "Not Found",
            data.emailValid,
            data.emailValid ? "Valid" : (data.email === "Not Found" ? "Missing" : "Invalid")
        );

        // Render Phone Check
        phoneText.textContent = data.phone;
        updateValidationItem(
            phoneStatusBadge,
            phoneValIcon,
            data.phone !== "Not Found",
            data.phoneValid,
            data.phoneValid ? "Valid" : (data.phone === "Not Found" ? "Missing" : "Invalid")
        );

        // Render Education Check
        const hasEducation = data.score >= (data.skillsFound.length * 8) + 
                            (data.emailValid ? 12 : 0) + 
                            (data.phoneValid ? 12 : 0) + 12; // checks if education is counted
        const educationFound = data.suggestions.every(s => !s.includes("educational background"));
        
        educationText.textContent = educationFound ? "Academic Details Found" : "Not Detected";
        updateValidationItem(
            educationStatusBadge,
            educationValIcon,
            educationFound,
            educationFound,
            educationFound ? "Detected" : "Missing"
        );

        // Render Skills Found
        skillsFoundList.innerHTML = "";
        if (data.skillsFound.length === 0) {
            skillsFoundList.innerHTML = `<span class="text-dark" style="font-size:0.9rem">No skills identified.</span>`;
        } else {
            data.skillsFound.forEach(skill => {
                const tag = document.createElement("span");
                tag.className = "skill-tag skill-tag-found";
                tag.innerHTML = `<i data-lucide="check" class="tag-icon"></i> ${skill}`;
                skillsFoundList.appendChild(tag);
            });
        }

        // Render Missing Skills
        missingSkillsList.innerHTML = "";
        if (data.missingSkills.length === 0) {
            missingSkillsList.innerHTML = `<span class="text-dark" style="font-size:0.9rem">Great! All core skills detected.</span>`;
        } else {
            data.missingSkills.forEach(skill => {
                const tag = document.createElement("span");
                tag.className = "skill-tag skill-tag-missing";
                tag.innerHTML = `<i data-lucide="plus" class="tag-icon"></i> ${skill}`;
                missingSkillsList.appendChild(tag);
            });
        }

        // Render Suggestions
        suggestionsList.innerHTML = "";
        data.suggestions.forEach(suggestion => {
            const li = document.createElement("li");
            li.textContent = suggestion;
            suggestionsList.appendChild(li);
        });

        // Re-run Lucide parser to process newly injected elements
        lucide.createIcons();
    }

    // Helper: Update Contact Check Items style & status badge
    function updateValidationItem(badgeElement, iconContainer, isPresent, isValid, labelText) {
        // Reset classes
        badgeElement.className = "badge";
        iconContainer.className = "val-icon-wrapper";

        if (isPresent && isValid) {
            badgeElement.classList.add("badge-success");
            iconContainer.classList.add("val-success");
            badgeElement.textContent = labelText;
        } else if (isPresent && !isValid) {
            badgeElement.classList.add("badge-warning");
            iconContainer.classList.add("val-danger");
            badgeElement.textContent = labelText;
        } else {
            badgeElement.classList.add("badge-danger");
            iconContainer.classList.add("val-danger");
            badgeElement.textContent = labelText;
        }
    }

    // Visual Score Counter and Arc Animation
    function animateScore(targetScore) {
        // 1. Animate Progress Circle Arc
        const offset = circumference - (targetScore / 100) * circumference;
        progressCircle.style.strokeDashoffset = offset;

        // 2. Visual Increment Counter for Score Number
        let currentCount = 0;
        scoreNumber.textContent = "0";

        if (targetScore === 0) return;

        const duration = 1000; // 1 second animation duration
        const steps = targetScore;
        const intervalTime = Math.max(duration / steps, 10); // Calculate interval step gap

        const counterInterval = setInterval(() => {
            currentCount++;
            scoreNumber.textContent = currentCount;

            if (currentCount >= targetScore) {
                clearInterval(counterInterval);
                scoreNumber.textContent = targetScore; // Safety lock
            }
        }, intervalTime);
    }

    // 6. Report Exporter Function
    downloadBtn.addEventListener("click", () => {
        if (!currentAnalysisData) {
            alert("No report data available to download!");
            return;
        }

        const data = currentAnalysisData;
        const today = new Date().toLocaleDateString("en-US", {
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });

        // Structure a professional ASCII analysis report
        const reportContent = `==================================================
           AI RESUME ANALYZER EVALUATION REPORT
==================================================
Date generated : ${today}
Resume Score   : ${data.score} / 100
Strength Level : ${data.strengthLevel.toUpperCase()}
==================================================

CONTACT INFORMATION CHECKS:
---------------------------
- Email Address  : ${data.email}
  Status         : ${data.email === "Not Found" ? "Missing" : (data.emailValid ? "Valid Format" : "Invalid Format")}
  
- Phone Number   : ${data.phone}
  Status         : ${data.phone === "Not Found" ? "Missing" : (data.phoneValid ? "Valid Format" : "Invalid Format")}

- Education Check: ${data.suggestions.every(s => !s.includes("educational background")) ? "Keywords Detected" : "Missing / Not Found"}

SKILLS SCAN SUMMARY:
-------------------
[+] Skills Detected (${data.skillsFound.length}):
    ${data.skillsFound.length > 0 ? data.skillsFound.join(", ") : "None Detected"}

[-] Missing Skills (${data.missingSkills.length}):
    ${data.missingSkills.length > 0 ? data.missingSkills.join(", ") : "None. Excellent work!"}

RECOMMENDED ACTIONS FOR IMPROVEMENT:
-----------------------------------
${data.suggestions.map((s, index) => `${index + 1}. ${s}`).join("\n")}

==================================================
        Mini Project submission for evaluation.
       Built with Spring Boot REST API & HTML/CSS/JS.
==================================================`;

        // Create Blob and trigger file download
        const blob = new Blob([reportContent], { type: "text/plain;charset=utf-8" });
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        
        link.href = url;
        link.download = `Resume_Analysis_Report_${new Date().toISOString().slice(0, 10)}.txt`;
        document.body.appendChild(link);
        link.click();
        
        // Clean up
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
    });
});
