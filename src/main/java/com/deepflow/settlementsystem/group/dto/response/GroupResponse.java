package com.deepflow.settlementsystem.group.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String inviteCode;
    private String inviteLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
