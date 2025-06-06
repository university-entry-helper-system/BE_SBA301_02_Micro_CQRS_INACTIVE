package com.example.userservice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    List<T> content; // Danh sách các phần tử trên trang hiện tại
    int pageNo; // Số trang hiện tại (bắt đầu từ 0)
    int pageSize; // Kích thước trang
    long totalElements; // Tổng số phần tử
    int totalPages; // Tổng số trang
    boolean last; // Có phải là trang cuối cùng không
    boolean first; // Có phải là trang đầu tiên không (bổ sung)
    boolean empty; // Có trống không (bổ sung)
}