package com.carrerai.model;

import java.util.List;

public record ResumeAnalysis(
    int atsScore,
    int contentScore,
    int impactScore,
    List<String> strengths,
    List<String> fixes,
    List<String> rewriteSuggestions
) {}
