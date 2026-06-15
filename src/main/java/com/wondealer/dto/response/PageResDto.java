package com.wondealer.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class PageResDto<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    public static <T> PageResDto<T> from(Page<T> page) {
        return PageResDto.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }
}
