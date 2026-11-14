package com.example.banking.common;

import org.springframework.data.domain.*;
import java.util.Set;

public final class Pages {
    private Pages() {
    }

    public static Pageable checked(Pageable page, Set<String> allowed) {
        if (page.getPageSize() > 100 || page.getSort().stream().anyMatch(o -> !allowed.contains(o.getProperty())))
            throw new IllegalArgumentException("Invalid pagination or sort");
        Sort sort = page.getSort().isUnsorted() ? Sort.by(Sort.Direction.DESC, "createdAt") : page.getSort();
        if (sort.getOrderFor("id") == null)
            sort = sort.and(Sort.by("id"));
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
    }
}
