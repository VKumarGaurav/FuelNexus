package com.fuel.nexus.exception.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ApiErrorResponse {

    private String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    private String message;
    private String api;          // API name or endpoint
    private int statusCode;
    private String className;    // Exception class
    private String lineNumber;   // Where error happened
}


//{
//        "path": "/api/customer/accounts",
//        "time": "2025-09-14 12:20:45",
//        "message": "Username already exists: johndoe",
//        "api": "POST",
//        "statusCode": 409,
//        "className": "com.fuel.nexus.service.impl.CustomerAccountServiceImpl",
//        "lineNumber": "42"
//}
