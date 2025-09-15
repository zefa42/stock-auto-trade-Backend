package com.tr.autos.domain.quote;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long symbolId;       // FK 심볼 id
    private Double price;        // 현재가
    private Double changeAmt;    // 전일 대비 절대값
    private Double changeRate;   // 전일 대비 %
    private LocalDateTime asOf;  // 시세 기준 시각
}
