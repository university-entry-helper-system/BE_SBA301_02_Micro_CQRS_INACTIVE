package com.example.userservice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    Long id; // ID của Role là Long
    String name; // Tên vai trò (e.g., "ADMIN", "USER")
    // String description; // Mô tả vai trò, có thể không cần thiết trong response này
}
