package com.example.week8.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class FriendInfoResponseDto {
    private final Long id;
    private final String nickname;
    private final String profileImgUrl;
    private final double creditScore;//int로 정의하는게 맞지 않나?
}