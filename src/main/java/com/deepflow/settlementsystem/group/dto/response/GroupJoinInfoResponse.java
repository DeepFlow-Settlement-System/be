package com.deepflow.settlementsystem.group.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GroupJoinInfoResponse {
    private Long groupId;
    private String groupName;
    private String groupDescription;
    private String imageUrl;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String inviteCode;
}
