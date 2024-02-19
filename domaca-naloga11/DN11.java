import java.io.File;
import java.util.Set;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.DataInputStream;

public class DN11 {
    public static void main(String[] args) {
        try {
            int metodaZaIzvedbo = Integer.parseInt(args[0]);
            String datotekaKraji = args[1];
            String datotekaPovezave = args[2];
            int l = args.length;

            EuroRail test = new EuroRail();
            test.preberiKraje(datotekaKraji);

            switch (metodaZaIzvedbo) {
                case 1:
                    test.preberiPovezave(datotekaPovezave);
                    test.izpisiKraje();
                    test.izpisiPovezave();
                    break;
                case 2:
                    test.preberiPovezave(datotekaPovezave);
                    test.izpisVlakovIzKrajevOdhoda();
                    break;
                case 3:
                    test.preberiPovezave(datotekaPovezave);
                    Collections.sort(test.getZbirkaVsehKrajev());
                    Collections.sort(test.getZbirkaVsehVlakov());
                    test.izpisVlakovIzKrajevOdhoda();
                    break;
                case 4:
                    test.preberiPovezave(datotekaPovezave);
                    StringBuilder imeKrajaVArgumentih = new StringBuilder(args[4]);
                    if (l > 5) {
                        for (int i = 5; i < l; i++) {
                            imeKrajaVArgumentih.append(" ").append(args[i]);
                        }
                    }
                    int stPrestopov = Integer.parseInt(args[3]) + 1;
                    Kraj iskaniKraj = test.najdiKraj(test.getZbirkaVsehKrajev(), imeKrajaVArgumentih.toString());

                    if (iskaniKraj == null) {
                        System.out.printf("NAPAKA: podanega kraja (%s) ni na seznamu krajev.%n", imeKrajaVArgumentih);
                        return;
                    }

                    Set<Kraj> dosegljiviKraji = iskaniKraj.destinacije(stPrestopov);
                    if (dosegljiviKraji.size() <= 1) {
                        System.out.printf("Iz kraja %s ni nobenih povezav.", iskaniKraj);

                    } else {
                        System.out.printf("Iz kraja %s lahko z max %d prestopanji pridemo do naslednjih krajev:%n",
                                iskaniKraj, stPrestopov - 1);
                    }

                    if (dosegljiviKraji.isEmpty()) {
                        System.out.println("   (noben)");
                    } else {

                        for (Kraj k : dosegljiviKraji) {
                            if (k.getIme().equals(imeKrajaVArgumentih.toString())) {
                                dosegljiviKraji.remove(k);
                                break;
                            }
                        }
                        for (Kraj kraj : dosegljiviKraji) {
                            System.out.println(kraj);
                        }
                    }
                    break;
                case 5:
                    test.preberiPovezaveBinarno(datotekaPovezave);
                    test.izpisiPovezave();
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace());
            System.out.println(e.getMessage());
        }
    }
}

class Kraj implements Comparable<Kraj> {
    private String ime;
    private String oznaka;
    private static int naslednjiID = 1;
    private int id;
    List<Vlak> odhodi; // seznam vseh vlakov, ki vozijo iz tega kraja v sosednje kraje

    public Kraj(String ime, String oznaka) {
        this.ime = ime;
        setOznaka(oznaka);
        this.odhodi = new ArrayList<>();
        this.id = naslednjiID;
        naslednjiID++;
    }

    public String toString() {
        return String.format("%s (%s)", ime, oznaka);
    }

    public List<Vlak> getOdhodi() {
        return odhodi;
    }

    public void setOznaka(String oznaka) {
        if (oznaka.length() > 2) {
            throw new IllegalArgumentException("Vnost kratice ni pravilen!!!");
        }
        this.oznaka = oznaka.toUpperCase();
    }

    boolean dodajOdhod(Vlak vlak) {
        if (!odhodi.contains(vlak)) {
            odhodi.add(vlak);
            return true;
        }
        return false;
    }

    Set<Kraj> destinacije(int k) {
        // uporabil bi lahko BFS algoritem z preiskovanjem do dolžine k
        Set<Kraj> dosegljiviKraji = new TreeSet<>();
        Queue<Kraj> kandidati = new LinkedList<>();
        kandidati.add(this);
        dosegljiviKraji.add(this);

        for (int i = 1; i <= k; i++) {
            int prestejKandidate = kandidati.size();

            for (int j = 0; j < prestejKandidate; j++) {
                Kraj trenutniKraj = kandidati.poll();

                assert trenutniKraj != null;
                for (Vlak vlak : trenutniKraj.getOdhodi()) {
                    Kraj naslednjiKraj = vlak.getKoncniKraj();

                    if (!dosegljiviKraji.contains(naslednjiKraj)) {
                        dosegljiviKraji.add(naslednjiKraj);
                        kandidati.offer(naslednjiKraj);
                    }
                }
            }
        }
        return dosegljiviKraji;
    }

    public String getIme() {
        return ime;
    }

    public int getId() {
        return id;
    }
    // ureja se po abecedi kratice države

    @Override
    public int compareTo(Kraj o) {
        int rez = this.oznaka.compareTo(o.oznaka);
        if (rez == 0) {
            rez = this.ime.compareTo(o.ime);
        }
        return rez;
    }
}

abstract class Vlak implements Comparable<Vlak> {
    private String identifikacijskaOznaka;
    private Kraj zacetniKraj;
    private Kraj koncniKraj;
    public int trajanje;

    public Vlak(String identifikacijskaOznaka, Kraj zacetniKraj, Kraj koncniKraj, int trajanje) {
        this.identifikacijskaOznaka = identifikacijskaOznaka;
        this.zacetniKraj = zacetniKraj;
        this.koncniKraj = koncniKraj;
        this.trajanje = trajanje;
    }

    public Kraj getKoncniKraj() {
        return koncniKraj;
    }

    public abstract String opis();

    public abstract double cenaVoznje();

    private String pretvorbaCasaVNiz(int casVMinutah) {
        if (casVMinutah >= 60) {
            int ure = casVMinutah / 60;
            int minute = casVMinutah % 60;
            return String.format("%d.%02dh", ure, minute);
        } else {
            return String.format("%d min", casVMinutah);
        }
    }

    public String toString() {
        return String.format("Vlak %s (%s) %s -- %s (%s, %.2f EUR)", identifikacijskaOznaka, opis(), zacetniKraj,
                koncniKraj, pretvorbaCasaVNiz(trajanje), cenaVoznje());
    }
}

class RegionalniVlak extends Vlak {
    private int povprecnaHitrost = 50;
    private double cenaVozovniceNaKilometer = 0.068;

    public RegionalniVlak(String identifikacijskaOznaka, Kraj zacetniKraj, Kraj koncniKraj, int trajanje) {
        super(identifikacijskaOznaka, zacetniKraj, koncniKraj, trajanje);
    }

    @Override
    public String opis() {
        return "regionalni";
    }

    @Override
    public double cenaVoznje() {
        double s = this.povprecnaHitrost * ((double) this.trajanje / 60);
        return s * this.cenaVozovniceNaKilometer;
    }

    @Override
    public int compareTo(Vlak o) {
        return Double.compare(o.cenaVoznje(), this.cenaVoznje());
    }
}

class EkspresniVlak extends Vlak {
    private int povprecnaHitrost = 110;
    private double cenaVozovniceNaKilometer = 0.154;
    private double doplacilo;

    public EkspresniVlak(String identifikacijskaOznaka, Kraj zacetniKraj, Kraj koncniKraj, int trajanje,
            double doplacilo) {
        super(identifikacijskaOznaka, zacetniKraj, koncniKraj, trajanje);
        this.doplacilo = doplacilo;
    }

    @Override
    public String opis() {
        return "ekspresni";
    }

    @Override
    public double cenaVoznje() {
        double s = this.povprecnaHitrost * ((double) this.trajanje / 60);
        return (s * this.cenaVozovniceNaKilometer) + this.doplacilo;
    }

    @Override
    public int compareTo(Vlak o) {
        return Double.compare(o.cenaVoznje(), this.cenaVoznje());
    }
}

class EuroRail {
    private List<Kraj> zbirkaVsehKrajev;
    private List<Vlak> zbirkaVsehVlakov;

    public EuroRail() {
        zbirkaVsehKrajev = new ArrayList<>();
        zbirkaVsehVlakov = new ArrayList<>();
    }

    boolean preberiKraje(String imeDatoteke) {
        try {
            File f = new File(imeDatoteke);
            Scanner sc = new Scanner(f);

            while (sc.hasNextLine()) {
                String[] temp = sc.nextLine().split(";");
                if (temp.length != 2 || obstajaVZbirkiKrajev(temp[0])) {
                    continue;
                } else {
                    zbirkaVsehKrajev.add(new Kraj(temp[0], temp[1]));
                }
            }

            return true;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean preberiPovezaveBinarno(String imeDatoteke) {
        try {
            FileInputStream fis = new FileInputStream(imeDatoteke);
            DataInputStream dis = new DataInputStream(fis);

            while (dis.available() > 0) {
                byte[] bitiZaOznakoVlaka = new byte[6]; // prvih 6 bajtov določa oznako vlaka
                dis.readFully(bitiZaOznakoVlaka);
                String oznakaVlaka = new String(bitiZaOznakoVlaka).trim();
                int indexZacetnegaKraja = dis.readByte() & 0xFF;

                int indexKoncnegaKraja = dis.readByte() & 0xFF;

                int casVoznje = dis.readUnsignedShort();
                int doplacilo = dis.readUnsignedShort();
                double doplaciloVEvrih = doplacilo / 100.0;

                Kraj odTemp = najdiKrajPoID(zbirkaVsehKrajev, indexZacetnegaKraja);
                Kraj doTemp = najdiKrajPoID(zbirkaVsehKrajev, indexKoncnegaKraja);

                if (odTemp == null || doTemp == null || odTemp.getIme().equals(doTemp.getIme())) {
                    continue;
                }

                Vlak dodajVlakTemp;

                if (doplacilo == 0) {
                    dodajVlakTemp = new RegionalniVlak(oznakaVlaka, odTemp, doTemp, casVoznje);
                } else {
                    dodajVlakTemp = new EkspresniVlak(oznakaVlaka, odTemp, doTemp, casVoznje, doplaciloVEvrih);
                }

                zbirkaVsehVlakov.add(dodajVlakTemp);
                odTemp.dodajOdhod(dodajVlakTemp);
            }
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean preberiPovezave(String imeDatoteke) {
        try {
            File f = new File(imeDatoteke);
            Scanner sc = new Scanner(f);

            while (sc.hasNextLine()) {
                String[] temp = sc.nextLine().split(";");
                int l = temp.length;

                Kraj odTemp = najdiKraj(zbirkaVsehKrajev, temp[1]);
                Kraj doTemp = najdiKraj(zbirkaVsehKrajev, temp[2]);
                Vlak dodajVlakTemp;

                if (l == 5) {

                    if (odTemp == null || doTemp == null || odTemp.getIme().equals(doTemp.getIme())) {
                        continue;
                    }
                    dodajVlakTemp = new EkspresniVlak(temp[0], odTemp, doTemp, ureVMinute(temp[3]),
                            Double.parseDouble(temp[4]));
                } else {

                    if (odTemp == null || doTemp == null || odTemp.getIme().equals(doTemp.getIme())) {
                        continue;
                    }
                    dodajVlakTemp = new RegionalniVlak(temp[0], odTemp, doTemp, ureVMinute(temp[3]));
                }
                zbirkaVsehVlakov.add(dodajVlakTemp);
                odTemp.dodajOdhod(dodajVlakTemp);
            }
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Kraj> getZbirkaVsehKrajev() {
        return zbirkaVsehKrajev;
    }

    public List<Vlak> getZbirkaVsehVlakov() {
        return zbirkaVsehVlakov;
    }

    public Kraj najdiKraj(List<Kraj> kraji, String ime) {
        for (Kraj k : kraji) {
            if (k.getIme().equals(ime)) {
                return k;
            }
        }
        return null;
    }

    public Kraj najdiKrajPoID(List<Kraj> kraji, int iskaniId) {
        for (Kraj k : kraji) {
            if (k.getId() == iskaniId) {
                return k;
            }
        }
        return null;
    }

    private int ureVMinute(String testniPodatek) {
        int temp;
        if (testniPodatek.contains(".")) {
            String[] temp2 = testniPodatek.split("\\.");
            temp = Integer.parseInt(temp2[0]) * 60 + Integer.parseInt(temp2[1]);
        } else {
            temp = Integer.parseInt(testniPodatek);
        }
        return temp;
    }

    public void izpisiKraje() {
        System.out.println("Kraji, povezani z vlaki:");
        for (Kraj k : zbirkaVsehKrajev) {
            System.out.println(k);
        }
    }

    public void izpisiPovezave() {
        System.out.println("\nVlaki, ki povezujejo kraje:");
        for (Vlak v : zbirkaVsehVlakov) {
            System.out.println(v);
        }
    }

    public void izpisVlakovIzKrajevOdhoda() {
        System.out.println("Kraji in odhodi vlakov:");

        for (Kraj k : zbirkaVsehKrajev) {
            System.out.println(k);
            List<Vlak> tempSeznamVlakov = k.getOdhodi(); // za optimizacijo, da se koda ne ponavlja
            System.out.printf("odhodi vlakov (%d):\n", tempSeznamVlakov.size());

            for (Vlak v : tempSeznamVlakov) {
                System.out.printf(" - %s\n", v);
            }
            System.out.println();
        }
    }

    public boolean obstajaVZbirkiKrajev(String imeKraja) {
        boolean obstaja = false;
        for (Kraj k : zbirkaVsehKrajev) {
            if (k.getIme().equals(imeKraja)) {
                obstaja = true;
                break;
            }
        }
        return obstaja;
    }

}
