package com.tr.autos.quote.repository;

import com.tr.autos.quote.service.updater.QuoteCacheUpsert;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuoteCacheUpsertRepositoryImpl implements QuoteCacheUpsertRepository {
    private final JdbcTemplate jdbc;

    @Override
    public void upsert(Long symbolId, QuoteCacheUpsert u) {
        jdbc.update("""
      INSERT INTO quote_cache
      (symbol_id, price, prev_diff, change_rate, change_sign,
       open_price, high_price, low_price, upper_limit, lower_limit, ref_price,
       volume, amount, volume_rate_vs_prev, foreign_net_buy_qty, program_net_buy_qty,
       shares_outstanding, market_cap, per, pbr, eps, bps, foreign_holding_ratio,
       high52w, high52w_date, high52w_diff_rate, low52w, low52w_date, low52w_diff_rate,
       item_status_code, credit_allowed, short_sell_allowed,
       margin_rate, market_warn_code, temp_halt, stac_month, updated_at)
      VALUES (?,?,?,?,?,
              ?,?,?,?,?,?,
              ?,?,?,?,?,?,
              ?,?,?,?,?,?,
              ?,?,?, ?,?,?,?,
              ?,?,?,?,?,?, NOW())
      ON DUPLICATE KEY UPDATE
        price=VALUES(price), prev_diff=VALUES(prev_diff),
        change_rate=VALUES(change_rate), change_sign=VALUES(change_sign),
        open_price=VALUES(open_price), high_price=VALUES(high_price), low_price=VALUES(low_price),
        upper_limit=VALUES(upper_limit), lower_limit=VALUES(lower_limit), ref_price=VALUES(ref_price),
        volume=VALUES(volume), amount=VALUES(amount), volume_rate_vs_prev=VALUES(volume_rate_vs_prev),
        foreign_net_buy_qty=VALUES(foreign_net_buy_qty), program_net_buy_qty=VALUES(program_net_buy_qty),
        shares_outstanding=VALUES(shares_outstanding), market_cap=VALUES(market_cap),
        per=VALUES(per), pbr=VALUES(pbr), eps=VALUES(eps), bps=VALUES(bps),
        foreign_holding_ratio=VALUES(foreign_holding_ratio),
        high52w=VALUES(high52w), high52w_date=VALUES(high52w_date), high52w_diff_rate=VALUES(high52w_diff_rate),
        low52w=VALUES(low52w),  low52w_date=VALUES(low52w_date),  low52w_diff_rate=VALUES(low52w_diff_rate),
        item_status_code=VALUES(item_status_code),
        credit_allowed=VALUES(credit_allowed), short_sell_allowed=VALUES(short_sell_allowed),
        margin_rate=VALUES(margin_rate), market_warn_code=VALUES(market_warn_code),
        temp_halt=VALUES(temp_halt), stac_month=VALUES(stac_month),
        updated_at=NOW()
      """,
                ps -> {
                    int i=1;
                    ps.setLong(i++, symbolId);
                    ps.setLong(i++, u.price()); ps.setLong(i++, u.prevDiff()); ps.setObject(i++, u.changeRate()); ps.setInt(i++, u.changeSign());
                    ps.setObject(i++, u.open()); ps.setObject(i++, u.high()); ps.setObject(i++, u.low());
                    ps.setObject(i++, u.upperLimit()); ps.setObject(i++, u.lowerLimit()); ps.setObject(i++, u.refPrice());
                    ps.setObject(i++, u.volume()); ps.setObject(i++, u.amount()); ps.setObject(i++, u.volumeRateVsPrev());
                    ps.setObject(i++, u.foreignNetBuyQty()); ps.setObject(i++, u.programNetBuyQty());
                    ps.setObject(i++, u.sharesOutstanding()); ps.setObject(i++, u.marketCap());
                    ps.setObject(i++, u.per()); ps.setObject(i++, u.pbr()); ps.setObject(i++, u.eps()); ps.setObject(i++, u.bps());
                    ps.setObject(i++, u.foreignHoldingRatio());
                    ps.setObject(i++, u.high52w()); ps.setObject(i++, u.high52wDate()); ps.setObject(i++, u.high52wDiffRate());
                    ps.setObject(i++, u.low52w()); ps.setObject(i++, u.low52wDate()); ps.setObject(i++, u.low52wDiffRate());
                    ps.setString(i++, u.itemStatusCode()); ps.setBoolean(i++, u.creditAllowed()); ps.setBoolean(i++, u.shortSellAllowed());
                    ps.setString(i++, u.marginRate()); ps.setString(i++, u.marketWarnCode()); ps.setBoolean(i++, u.tempHalt()); ps.setString(i++, u.stacMonth());
                }
        );
    }
}
