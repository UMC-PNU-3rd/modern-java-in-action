package mixedDSL;
import domain.*;
public class StockBuilder {
    private final TradeBuilder tradeBuilder;
    private final Trade trade;

    private Stock stock = new Stock();

    public StockBuilder(TradeBuilder tradeBuilder, Trade trade, String symbol) {
        this.tradeBuilder = tradeBuilder;
        this.trade = trade;
        stock.setSymbol(symbol);
    }

    public TradeBuilder on(String market) {
        stock.setMarket(market);
        trade.setStock(stock);
        return this.tradeBuilder;
    }
}
