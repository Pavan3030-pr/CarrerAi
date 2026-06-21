package com.carrerai.model;

import java.util.List;

public record RoadmapWeek(int week, String focus, List<String> actions, String proofOfWork) {}
