package com.example.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePasswordRequest {
    @NotBlank(message = "Mật khẩu cũ không được để trống.")
    String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất {min} ký tự.")
    // Regex cho mật khẩu mạnh: ít nhất 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
            message = "Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.")
    String newPassword;
}