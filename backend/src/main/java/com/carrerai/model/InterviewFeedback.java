package com.carrerai.model;

import java.util.List;

public record InterviewFeedback(
    int score,
    String question,
    String verdict,
    List<String> coachingTips,
    String improvedAnswer
) {}
