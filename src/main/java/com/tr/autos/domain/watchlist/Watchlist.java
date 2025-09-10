package com.tr.autos.domain.watchlist;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="watchlists",
        uniqueConstraints=@UniqueConstraint(name="uq_watch", columnNames={"user_id","symbol_id"}))
@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Watchlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="symbol_id", nullable=false)
    private Symbol symbol;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @PrePersist void onCreate(){ if(createdAt==null) createdAt = LocalDateTime.now(); }
}