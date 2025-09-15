package com.tr.autos.watchlist.service;

import com.tr.autos.domain.symbol.Symbol;
import com.tr.autos.domain.symbol.repository.SymbolRepository;
import com.tr.autos.domain.user.User;
import com.tr.autos.domain.user.repository.UserRepository;
import com.tr.autos.domain.watchlist.Watchlist;
import com.tr.autos.quote.service.QuoteService;
import com.tr.autos.quote.service.QuoteTargetRegistry;
import com.tr.autos.watchlist.dto.WatchItemDto;
import com.tr.autos.domain.watchlist.repository.WatchlistRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final UserRepository userRepository;
    private final SymbolRepository symbolRepository;
    private final WatchlistRepository watchlistRepository;
    private final QuoteService quoteService;
    private final QuoteTargetRegistry targetRegistry;

    private final EntityManager em;

    private User currentUser(Principal principal){
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    /** ✅ 조회 전용은 readOnly = true */
    @Transactional(readOnly = true)
    public List<WatchItemDto> myList(Principal principal) {
        var user = currentUser(principal);
        return watchlistRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(w -> {
                    var s = w.getSymbol();
                    return new WatchItemDto(s.getId(), s.getTicker(), s.getMarket(), s.getName());
                })
                .toList();
    }

    /** ✅ 현재 로그인 사용자가 관심목록에 추가 */
    @Transactional
    public void add(Principal principal, Long symbolId) {
        var user = currentUser(principal);
        if (watchlistRepository.existsByUserIdAndSymbolId(user.getId(), symbolId)) {
            throw new IllegalArgumentException("이미 관심목록에 있습니다.");
        }

        var symbol = symbolRepository.findById(symbolId).orElseThrow();

        // new Watchlist() 대신 팩토리/빌더 사용 (note/sortNo 유지)
        var item = Watchlist.of(user, symbol, null, 0);
        watchlistRepository.save(item);

        // 즉시 1회 시세 웜업 + 스케줄 대상 등록(관리자는 기본 포함)
        quoteService.refreshSymbolAsync(symbolId);
        if (!user.isAdmin()) {
            targetRegistry.registerExtraTarget(symbolId);
        }
    }



    /** ✅ 현재 로그인 사용자가 관심목록에서 제거 */
    @Transactional
    public void remove(Principal principal, Long symbolId) {
        var user = currentUser(principal);
        watchlistRepository.deleteByUserIdAndSymbolId(user.getId(), symbolId);

        // orphan 판단 후 extraTargets에서 제거
        boolean anyAdminHasIt = watchlistRepository.existsBySymbolIdAndAdmin(symbolId);
        long totalWatchCount  = watchlistRepository.countBySymbolId(symbolId);
        targetRegistry.unregisterExtraTargetIfOrphan(symbolId, anyAdminHasIt, totalWatchCount);
    }

    /** ✅ (배치/관리자 등) 식별자만으로 직접 추가하는 오버로드 */
    @Transactional
    public void addToWatchlist(Long userId, Long symbolId, boolean isAdmin) {
        if (watchlistRepository.existsByUserIdAndSymbolId(userId, symbolId)) return;

        // 쿼리 절약: 프록시 참조
        User userRef  = em.getReference(User.class, userId);
        Symbol symRef = em.getReference(Symbol.class, symbolId);

        var item = Watchlist.of(userRef, symRef, null, 0);
        watchlistRepository.save(item);

        quoteService.refreshSymbolAsync(symbolId);
        if (!isAdmin) {
            targetRegistry.registerExtraTarget(symbolId);
        }
    }

    /** ✅ (배치/관리자 등) 식별자만으로 제거하는 오버로드 */
    @Transactional
    public void removeFromWatchlist(Long userId, Long symbolId) {
        watchlistRepository.deleteByUserIdAndSymbolId(userId, symbolId);

        boolean anyAdminHasIt = watchlistRepository.existsBySymbolIdAndAdmin(symbolId);
        long totalWatchCount  = watchlistRepository.countBySymbolId(symbolId);
        targetRegistry.unregisterExtraTargetIfOrphan(symbolId, anyAdminHasIt, totalWatchCount);
    }
}