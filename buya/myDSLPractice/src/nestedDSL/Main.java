package nestedDSL;
import domain.*;
import static nestedDSL.NestedFunctionOrderBuilder.*;

public class Main {
    public void doNested() {
        Order order = order("BigBank",
                            buy(80,
                                    stock("IBM", on("NYSE")),
                                    at(125.00)
                            ),
                            sell(50,
                                    stock("GOOGLE", on("NASDAQ")),
                                    at(375.00)
                            ));
    }
}
