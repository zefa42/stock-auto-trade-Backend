package com.tr.autos.domain.quote;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteCache {

    @Id
    @Column(name = "symbol_id")
    private Long symbolId;       // FK 심볼 id (Primary Key)

    @Column(name = "price", precision = 18, scale = 4, nullable = false)
    private BigDecimal price;        // 현재가

    @Column(name = "change_amt", precision = 18, scale = 4, nullable = false)
    private BigDecimal changeAmt;    // 전일 대비 절대값

    @Column(name = "change_rate", precision = 9, scale = 4, nullable = false)
    private BigDecimal changeRate;   // 전일 대비 %

    @Column(name = "prev_close", precision = 18, scale = 4, nullable = false)
    private BigDecimal prevClose;    // 전일종가

    @Column(name = "as_of", nullable = false)
    private LocalDateTime asOf;      // 시세 기준 시각

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 업데이트 시각

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
