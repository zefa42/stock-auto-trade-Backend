package com.tr.autos.domain.watchlist;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "watchlists",
        uniqueConstraints = @UniqueConstraint(name = "uq_watch", columnNames = {"user_id","symbol_id"}))
public class Watchlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    private String note;

    @Column(name = "sort_no", nullable = false)
    private int sortNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static Watchlist of(User user, Symbol symbol, String note, int sortNo) {
        return Watchlist.builder()
                .user(user)
                .symbol(symbol)
                .note(note)
                .sortNo(sortNo)
                .createdAt(LocalDateTime.now())
                .build();
    }
}