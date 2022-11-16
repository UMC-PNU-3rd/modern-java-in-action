package beforeMethodRefDSL;

import domain.Order;

import static mixedDSL.MixedBuilder.*;

public class Main {
    public static void main(String[] args) {
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

        double value = new TaxCalculator().withTaxGeneral()
                .withTaxRegional()
                .withTaxSurcharge()
                .calculate(order);

        System.out.println(value);
    }
}
