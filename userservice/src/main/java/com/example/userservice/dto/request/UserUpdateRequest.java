package com.example.userservice.dto.request;

import jakarta.validation.constraints.Email;
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
public class UserUpdateRequest {
    @Size(max = 255, message = "Họ và tên không được vượt quá {max} ký tự.")
    String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá {max} ký tự.")
    @Pattern(regexp = "^(0|\\+84)(\\d{9,10})$", message = "Số điện thoại không hợp lệ.")
    String phone;

    @Email(message = "Định dạng email không hợp lệ.")
    @Size(max = 255, message = "Email không được vượt quá {max} ký tự.")
    String email; // User có thể cập nhật email, có thể cần quy trình xác minh lại
}
