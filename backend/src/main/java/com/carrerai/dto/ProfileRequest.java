package com.carrerai.dto;

import java.util.List;

public record ProfileRequest(
    String name,
    String degree,
    String targetRole,
    String experienceLevel,
    List<String> skills,
    List<String> interests
) {}
