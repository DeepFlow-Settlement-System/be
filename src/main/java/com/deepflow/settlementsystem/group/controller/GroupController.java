package com.deepflow.settlementsystem.group.controller;

import com.deepflow.settlementsystem.group.dto.request.GroupCreateRequest;
import com.deepflow.settlementsystem.group.dto.request.RoomJoinRequest;
import com.deepflow.settlementsystem.group.dto.response.GroupDetailResponse;
import com.deepflow.settlementsystem.group.dto.response.GroupResponse;
import com.deepflow.settlementsystem.group.dto.response.RoomJoinResponse;
import com.deepflow.settlementsystem.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupCreateRequest request,
            @AuthenticationPrincipal Long userId) {
        GroupResponse response = groupService.createGroup(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            @AuthenticationPrincipal Long userId) {
        List<GroupResponse> responses = groupService.getMyGroups(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId) {
        GroupDetailResponse response = groupService.getGroupDetail(groupId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Long userId) {
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    public ResponseEntity<RoomJoinResponse> joinRoom(
            @Valid @RequestBody RoomJoinRequest request,
            @AuthenticationPrincipal Long userId) {
        RoomJoinResponse response = groupService.joinRoom(request, userId);
        return ResponseEntity.ok(response);
    }
}