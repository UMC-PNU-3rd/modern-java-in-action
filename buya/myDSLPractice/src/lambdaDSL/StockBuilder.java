package lambdaDSL;

import domain.*;

public class StockBuilder {
    private Stock stock = new Stock();

    public void symbol(String symbol) {
        stock.setSymbol(symbol);
    }

    public void market(String market) {
        stock.setMarket(market);
    }

    public Stock getStock() {
        return stock;
    }
}
