package mixedDSL;
import domain.*;
import static mixedDSL.MixedBuilder.*;

public class Main {
    public static void doMixed() {
        Order order = forCustomer("BigBank",
                            buy(t -> t.quantity(80)
                                    .stock("IBM")
                                    .on("NYSE")
                                    .at(125.00)),
                            sell(t -> t.quantity(50)
                                    .stock("GOOGLE")
                                    .on("NASDAQ")
                                    .at(375.00))
        );
    }
}
