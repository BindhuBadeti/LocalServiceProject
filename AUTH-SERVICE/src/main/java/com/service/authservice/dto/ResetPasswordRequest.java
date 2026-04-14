package com.service.authservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

   private String email;
    private String newPassword;
    private String confirmPassword;
}