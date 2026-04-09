
import java.util.ArrayList;
import java.util.List;

public class DonneesTarifs {

    public static List<Tarif> chargerTarifs() {
        List<Tarif> tarifs = new ArrayList<>();

        tarifs.add(new Tarif("EXT", 18000, Double.MAX_VALUE, 5.98, 17.95, 11.97, 5.98, 2.98));
        tarifs.add(new Tarif("A",   18000, Double.MAX_VALUE, 5.54, 16.63, 11.08, 5.54, 2.79));
        tarifs.add(new Tarif("B",   15000, 17999.99,         4.89, 14.73,  9.78, 4.89, 2.43));
        tarifs.add(new Tarif("B2",  13000, 14999.99,         4.32, 12.46,  8.34, 4.17, 2.11));
        tarifs.add(new Tarif("C",   11000, 12999.99,         4.17, 12.46,  8.34, 4.17, 2.11));
        tarifs.add(new Tarif("D",    9000, 10999.99,         3.51, 10.55,  7.02, 3.51, 1.76));
        tarifs.add(new Tarif("E",    7000,  8999.99,         2.92,  8.82,  5.83, 2.92, 1.48));
        tarifs.add(new Tarif("F",    5000,  6999.99,         2.16,  6.50,  4.32, 2.16, 1.08));
        tarifs.add(new Tarif("F2",   3000,  4999.99,         1.57,  6.50,  4.32, 2.16, 1.08));
        tarifs.add(new Tarif("G",       0,  2999.99,         1.43,  4.35,  2.85, 1.43, 0.70));

        return tarifs;
    }
}