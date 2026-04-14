package com.service.authservice.dto;



import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String phone;
}
