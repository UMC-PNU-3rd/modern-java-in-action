package mixedDSL;
import domain.*;

public class TradeBuilder {
    private Trade trade = new Trade();

    public Trade getTrade() {
        return trade;
    }

    public TradeBuilder quantity(int quantity) {
        trade.setQuantity(quantity);
        return this;
    }

    public TradeBuilder at(double price) {
        trade.setPrice(price);
        return this;
    }

    public StockBuilder stock(String symbol) {
        return new StockBuilder(this, trade, symbol);
    }

}
