package com.example.week8.dto.alert;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatAlertDto {
    private String message;
    private String title;
    private String eventId;
}
