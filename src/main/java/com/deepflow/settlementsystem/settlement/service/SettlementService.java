package com.deepflow.settlementsystem.settlement.service;

import com.deepflow.settlementsystem.auth.config.KakaoApiUrl;
import com.deepflow.settlementsystem.auth.service.KakaoTokenService;
import com.deepflow.settlementsystem.common.code.ErrorCode;
import com.deepflow.settlementsystem.common.exception.CustomException;
import com.deepflow.settlementsystem.expense.entity.Expense;
import com.deepflow.settlementsystem.expense.entity.ExpenseAllocation;
import com.deepflow.settlementsystem.expense.entity.ExpenseItem;
import com.deepflow.settlementsystem.expense.entity.SettlementStatus;
import com.deepflow.settlementsystem.expense.entity.SettlementType;
import com.deepflow.settlementsystem.expense.repository.ExpenseItemAllocationRepository;
import com.deepflow.settlementsystem.settlement.dto.SettlementItem;
import com.deepflow.settlementsystem.settlement.dto.request.KakaoMessageRequest;
import com.deepflow.settlementsystem.settlement.dto.response.KakaoFriendsResponse;
import com.deepflow.settlementsystem.settlement.dto.response.KakaoSendMessageResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementListResponse;
import com.deepflow.settlementsystem.settlement.dto.response.SettlementResponse;
import com.deepflow.settlementsystem.user.entity.User;
import com.deepflow.settlementsystem.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SettlementService {
    
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final KakaoTokenService kakaoTokenService;
    private final ObjectMapper objectMapper;
    private final ExpenseItemAllocationRepository expenseAllocationRepository;
    
    /**
     * 정산 요청 메시지 전송
     * 돈을 받는 사람(receiver)이 돈을 보낼 사람(sender)에게 카카오톡 메시지를 전송합니다.
     */
    @Transactional
    public void sendSettlementMessage(Long allocationId, Long receiverUserId) {
        // 입력값 검증
        if (allocationId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (receiverUserId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // ExpenseAllocation 조회
        ExpenseAllocation allocation = expenseAllocationRepository.findByIdWithRelations(allocationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_SETTLEMENT));
        
        // 돈을 받는 사람(receiver)만 요청 가능
        if (!allocation.getReceiver().getId().equals(receiverUserId)) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }
        
        // 이미 REQUESTED 상태인지 확인
        if (allocation.getStatus() == SettlementStatus.REQUESTED) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // 돈을 받는 사람 (메시지를 보내는 사람)
        User receiver = allocation.getReceiver();
        // 돈을 보낼 사람 (메시지를 받는 사람)
        User sender = allocation.getSender();
        Long amount = allocation.getShareAmount().longValue();
        
        // receiver의 카카오페이 링크가 필요 (receiver가 돈을 받아야 하므로)
        if (receiver.getKakaoPaySuffix() == null || receiver.getKakaoPaySuffix().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // Access token 조회 (receiver의 토큰 사용 - receiver가 메시지를 보냄)
        String accessToken = kakaoTokenService.getKakaoAccessToken(receiverUserId);
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // 돈을 보낼 사람(sender)의 UUID 찾기
        String senderUuid = findUserUuidByUserId(accessToken, sender.getId());
        
        // 송금 링크 생성 (receiver의 카카오페이 링크 - receiver에게 돈을 보내는 링크)
        String paymentLink = generatePaymentLink(receiver.getKakaoPaySuffix(), amount);
        
        // 그룹명과 지출 내역 가져오기
        String groupName = allocation.getGroup().getName();
        List<SettlementItem> items = getSettlementItems(allocation);
        
        KakaoMessageRequest message = createSettlementMessage(
                paymentLink,
                groupName,
                items,
                amount
        );
        
        // 카카오 메시지 전송 API 호출 (receiver가 sender에게 전송)
        sendKakaoMessage(accessToken, senderUuid, message);
        
        // 상태를 REQUESTED로 변경
        allocation.setStatus(SettlementStatus.REQUESTED);
        expenseAllocationRepository.save(allocation);
    }
    
    /**
     * 카카오 친구 목록에서 특정 사용자의 UUID를 찾습니다.
     * @param accessToken 카카오 Access Token
     * @param targetUserId 찾을 사용자의 ID
     * @return 사용자의 UUID
     */
    private String findUserUuidByUserId(String accessToken, Long targetUserId) {
        // 친구 목록에서 찾기 (페이지네이션 포함)
        String currentAfterUrl = null;
        int maxPages = 1000;
        int pageCount = 0;
        
        do {
            KakaoFriendsResponse friendsResponse = getKakaoFriends(accessToken, currentAfterUrl);
            
            String userUuid = findUserUuidInFriends(friendsResponse, targetUserId);
            if (userUuid != null) {
                return userUuid;
            }
            
            currentAfterUrl = friendsResponse.getAfterUrl();
            pageCount++;
            
        } while (currentAfterUrl != null && pageCount < maxPages);
        
        log.warn("카카오 친구 목록에서 사용자를 찾지 못했습니다. targetUserId: {}", targetUserId);
        
        throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }
    
    private KakaoFriendsResponse getKakaoFriends(String accessToken, String afterUrl) {
        String url = afterUrl != null 
            ? afterUrl 
            : KakaoApiUrl.FRIENDS.getUrl();
        
        KakaoFriendsResponse response = restClient.get()
                .uri(UriComponentsBuilder.fromUriString(url).build().toUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                    log.error("카카오 친구 목록 API 호출 실패: {}", httpResponse.getStatusCode());
                    throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
                })
                .body(KakaoFriendsResponse.class);
        
        return Objects.requireNonNull(response);
    }
    
    /**
     * 카카오 친구 목록 응답에서 특정 사용자 ID의 UUID를 찾습니다.
     * @param response 카카오 친구 목록 응답
     * @param targetUserId 찾을 사용자의 ID
     * @return 사용자의 UUID, 없으면 null
     */
    private String findUserUuidInFriends(KakaoFriendsResponse response, Long targetUserId) {
        if (response.getElements() == null || response.getElements().isEmpty()) {
            return null;
        }
        
        return response.getElements().stream()
                .filter(friend -> friend.getId() != null && friend.getId().equals(targetUserId))
                .map(KakaoFriendsResponse.Friend::getUuid)
                .filter(uuid -> uuid != null && !uuid.isEmpty())
                .findFirst()
                .orElse(null);
    }
    
    private String generatePaymentLink(String kakaoPaySuffix, Long amount) {
        // 입력값 검증
        if (kakaoPaySuffix == null || kakaoPaySuffix.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        long multipliedAmount = amount * 8;
        String hexAmount = Long.toHexString(multipliedAmount).toUpperCase();
        
        // 랜덤 숫자 4자리 생성
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 10000);
        String randomSuffix = String.format("%04d", randomNumber);
        
        // 링크
        return "https://qr.kakaopay.com/" + kakaoPaySuffix + hexAmount + randomSuffix;
    }
    
    private KakaoMessageRequest createSettlementMessage(
            String paymentLink,
            String groupName,
            List<SettlementItem> items,
            Long totalAmount) {
        
        // items 생성
        List<KakaoMessageRequest.Item> messageItems = new ArrayList<>();

        // 그룹 이름
        messageItems.add(KakaoMessageRequest.Item.builder()
                .item("그룹")
                .itemOp(groupName)
                .build());
        
        // 지출 내역
        for (SettlementItem item : items) {
            messageItems.add(KakaoMessageRequest.Item.builder()
                    .item(item.getDescription())
                    .itemOp(formatAmount(item.getAmount()))
                    .build());
        }
        
        // 링크 생성
        KakaoMessageRequest.Link link = KakaoMessageRequest.Link.builder()
                .webUrl(paymentLink)
                .mobileWebUrl(paymentLink)
                .build();
        
        // 메시지 생성
        return KakaoMessageRequest.builder()
                .objectType("feed")
                .content(KakaoMessageRequest.Content.builder()
                        .title("💸 송금 부탁드립니다.")
                        .imageUrl("https://plus.unsplash.com/premium_photo-1679830513869-cd3648acb1db?q=80&w=927&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
                        .imageWidth(640)
                        .imageHeight(640)
                        .link(link)
                        .build())
                .itemContent(KakaoMessageRequest.ItemContent.builder()
                        .profileText("정산 요청")
                        .items(messageItems)
                        .sum("총 송금 금액")
                        .sumOp(formatAmount(totalAmount))
                        .build())
                .buttons(List.of(KakaoMessageRequest.Button.builder()
                        .title("카카오페이로 송금하기")
                        .link(link)
                        .build()))
                .build();
    }
    
    private String formatAmount(Long amount) {
        return String.format("%,d원", amount);
    }
    
    private void sendKakaoMessage(String accessToken, String receiverUuid, KakaoMessageRequest message) {
        // 입력값 검증
        if (accessToken == null || accessToken.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        if (receiverUuid == null || receiverUuid.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        try {
            // receiver_uuids를 JSON 배열 문자열로 변환: ["uuid"]
            String receiverUuidsJson = objectMapper.writeValueAsString(List.of(receiverUuid));
            
            // template_object를 JSON 문자열로 변환
            String templateObjectJson = objectMapper.writeValueAsString(message);
            
            // form-urlencoded 형식으로 요청 본문 생성
            String requestBody = "receiver_uuids=" + URLEncoder.encode(receiverUuidsJson, StandardCharsets.UTF_8)
                    + "&template_object=" + URLEncoder.encode(templateObjectJson, StandardCharsets.UTF_8);
            
            KakaoSendMessageResponse response = restClient.post()
                    .uri(KakaoApiUrl.SEND_MESSAGE.getUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, httpResponse) -> {
                        log.error("카카오 메시지 전송 API 호출 실패: {}", httpResponse.getStatusCode());
                        throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
                    })
                    .body(KakaoSendMessageResponse.class);
            
            // successful_receiver_uuids에 receiverUuid가 포함되어 있는지 확인
            if (response.getSuccessfulReceiverUuids() == null || 
                response.getSuccessfulReceiverUuids().isEmpty() ||
                !response.getSuccessfulReceiverUuids().contains(receiverUuid)) {
                log.error("카카오 메시지 전송 실패 - receiverUuid가 성공 목록에 없습니다. receiverUuid: {}", receiverUuid);
                throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
            }
            
            log.info("카카오 메시지 전송 성공. receiverUuid: {}", receiverUuid);
            
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON 직렬화 중 오류 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("카카오 메시지 전송 중 오류 발생", e);
            throw new CustomException(ErrorCode.EXTERNAL_SERVER_ERROR);
        }
    }
    
    // 정산 상태 조회
    public SettlementResponse getSettlementStatus(Long allocationId, Long userId) {
        ExpenseAllocation allocation = expenseAllocationRepository.findByIdWithRelations(allocationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_SETTLEMENT));
        
        // 사용자가 sender 또는 receiver인지 확인
        if (!allocation.getSender().getId().equals(userId) && !allocation.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }
        
        return toSettlementResponse(allocation);
    }
    
    /**
     * 정산 완료 처리
     * 돈을 받는 사람(receiver)이 송금 수령 확인 후 완료 처리합니다.
     */
    @Transactional
    public void completeSettlement(Long allocationId, Long userId) {
        ExpenseAllocation allocation = expenseAllocationRepository.findByIdWithRelations(allocationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_SETTLEMENT));
        
        // 돈을 받는 사람(receiver)만 완료 처리 가능
        if (!allocation.getReceiver().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_ACCESS_PERMISSION);
        }
        
        // REQUESTED 상태인지 확인
        if (allocation.getStatus() != SettlementStatus.REQUESTED) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // 상태를 COMPLETED로 변경
        allocation.setStatus(SettlementStatus.COMPLETED);
        expenseAllocationRepository.save(allocation);
    }
    
    // 정산 목록 조회
    public SettlementListResponse getSettlementList(Long userId) {
        List<ExpenseAllocation> allocations = expenseAllocationRepository.findByUserId(userId);
        
        List<SettlementResponse> settlements = allocations.stream()
                .map(this::toSettlementResponse)
                .collect(Collectors.toList());
        
        return SettlementListResponse.builder()
                .settlements(settlements)
                .totalCount((long) settlements.size())
                .build();
    }
    
    // ExpenseAllocation에서 SettlementItem 리스트 생성
    private List<SettlementItem> getSettlementItems(ExpenseAllocation allocation) {
        List<SettlementItem> items = new ArrayList<>();
        Expense expense = allocation.getExpense();
        
        if (expense == null) {
            return items;
        }
        
        // N빵인 경우
        if (expense.getSettlementType() == SettlementType.N_BBANG) {
            items.add(new SettlementItem(expense.getTitle(), expense.getTotalAmount().longValue()));
        } 
        // 품목별인 경우
        else if (expense.getSettlementType() == SettlementType.ITEMIZED && allocation.getItem() != null) {
            ExpenseItem item = allocation.getItem();
            items.add(new SettlementItem(item.getItemName(), item.getLineAmount().longValue()));
        }
        
        return items;
    }
    
    // ExpenseAllocation을 SettlementResponse로 변환
    private SettlementResponse toSettlementResponse(ExpenseAllocation allocation) {
        return SettlementResponse.builder()
                .allocationId(allocation.getAllocationId())
                .groupId(allocation.getGroup().getId())
                .groupName(allocation.getGroup().getName())
                .expenseId(allocation.getExpense() != null ? allocation.getExpense().getExpenseId() : null)
                .expenseTitle(allocation.getExpense() != null ? allocation.getExpense().getTitle() : null)
                .senderId(allocation.getSender().getId())
                .senderNickname(allocation.getSender().getNickname())
                .receiverId(allocation.getReceiver().getId())
                .receiverNickname(allocation.getReceiver().getNickname())
                .amount(allocation.getShareAmount().longValue())
                .status(allocation.getStatus())
                .createdAt(allocation.getCreatedAt())
                .build();
    }
    
}
