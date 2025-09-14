package com.tr.autos.domain.symbol;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="symbols",
        uniqueConstraints=@UniqueConstraint(name="uq_ticker_market", columnNames={"ticker","market"}))
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Symbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=20)
    private String ticker; // 티커(종목 번호)

    @Column(nullable=false, length=20)
    private String market; // 시장(한국, 미국)

    @Column(length = 100)
    private String name; // 이름(삼성전자, TESLA)
}
