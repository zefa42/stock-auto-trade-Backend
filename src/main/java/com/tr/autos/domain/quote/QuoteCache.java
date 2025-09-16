package com.tr.autos.domain.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name="quote_cache") @Getter
@Setter
public class QuoteCache {
    @Id
    @Column(name="symbol_id") private Long symbolId;
    private Long price; @Column(name="prev_diff") private Long prevDiff;
    @Column(name="change_rate") private Double changeRate;
    @Column(name="change_sign") private Integer changeSign;
    @Column(name="open_price") private Long openPrice;
    @Column(name="high_price") private Long highPrice;
    @Column(name="low_price")  private Long lowPrice;
    @Column(name="upper_limit") private Long upperLimit;
    @Column(name="lower_limit") private Long lowerLimit;
    @Column(name="ref_price")   private Long refPrice;
    private Long volume; private Long amount;
    @Column(name="volume_rate_vs_prev") private Double volumeRateVsPrev;
    @Column(name="foreign_net_buy_qty") private Long foreignNetBuyQty;
    @Column(name="program_net_buy_qty") private Long programNetBuyQty;
    @Column(name="shares_outstanding") private Long sharesOutstanding;
    @Column(name="market_cap") private Long marketCap;
    private Double per; private Double pbr; private Double eps; private Double bps;
    @Column(name="foreign_holding_ratio") private Double foreignHoldingRatio;
    @Column(name="high52w") private Long high52w;  @Column(name="high52w_date") private Date high52wDate;
    @Column(name="high52w_diff_rate") private Double high52wDiffRate;
    @Column(name="low52w") private Long low52w;    @Column(name="low52w_date") private Date low52wDate;
    @Column(name="low52w_diff_rate") private Double low52wDiffRate;
    @Column(name="item_status_code") private String itemStatusCode;
    @Column(name="credit_allowed") private Boolean creditAllowed;
    @Column(name="short_sell_allowed") private Boolean shortSellAllowed;
    @Column(name="margin_rate") private String marginRate;
    @Column(name="market_warn_code") private String marketWarnCode;
    @Column(name="temp_halt") private Boolean tempHalt;
    @Column(name = "stac_month", columnDefinition = "char(2)", length = 2) private String stacMonth;
    @Column(name="updated_at") private Timestamp updatedAt;
}
