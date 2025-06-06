package com.example.userservice.dto.request;

import jakarta.validation.constraints.Email;
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
public class AccountCreationRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(min = 4, max = 100, message = "Tên đăng nhập phải có từ {min} đến {max} ký tự.")
    String username;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Định dạng email không hợp lệ.")
    @Size(max = 255, message = "Email không được vượt quá {max} ký tự.")
    String email;

    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất {min} ký tự.")
    // Ví dụ về regex cho mật khẩu mạnh: ít nhất 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.")
    String password; // Mật khẩu dạng plaintext, sẽ được băm trong service

    @Size(max = 255, message = "Họ và tên không được vượt quá {max} ký tự.")
    String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá {max} ký tự.")
    // Ví dụ regex cho số điện thoại Việt Nam
    @Pattern(regexp = "^(0|\\+84)(\\d{9,10})$", message = "Số điện thoại không hợp lệ.")
    String phone;

    // roleId chỉ nên có nếu admin tạo user, không nên có khi user tự đăng ký
    // Nếu có, cần xử lý logic trong service để gán role_id mặc định cho user tự đăng ký
    // và cho phép admin truyền vào khi tạo user.
    Long roleId;
}