package com.microvolunteer.dto.request;

import com.microvolunteer.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchRequest {

    private String query;
    private Long categoryId;
    private TaskStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 12;
    
    // Додаємо getter методи з валідацією
    public Integer getPage() {
        return page != null && page >= 0 ? page : 0;
    }
    
    public Integer getSize() {
        return size != null && size > 0 ? size : 12;
    }
}