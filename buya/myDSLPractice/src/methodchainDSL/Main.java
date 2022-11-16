package methodchainDSL;

import domain.*;

import static methodchainDSL.MethodChainingOrderBuilder.forCustomer;

public class Main {
    public void doMethodChain() {
        Order order = forCustomer("BigBank")
                .buy(80)
                .stock("IBM")
                .on("NYSE")
                .at(125.00)
                .sell(50)
                .stock("GOOGLE")
                .on("NASDAQ")
                .at(375.00)
                .end();
    }
}
