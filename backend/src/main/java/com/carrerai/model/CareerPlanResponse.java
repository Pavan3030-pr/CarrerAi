package com.carrerai.model;

import java.util.List;

public record CareerPlanResponse(
    int readinessScore,
    List<CareerScore> careerMatches,
    List<GapScore> skillGaps,
    List<RoadmapWeek> roadmap,
    List<String> nextActions
) {}
