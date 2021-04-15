import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JFrame;

/**
 * Semestralni prace UPG 2021
 * Pasivni vizualizace 
 * @author Lukas Runt
 * @version 1.1 (26-02-2020)
 */
public class Mapa_SP2021 {
	/** sirka mapy */
	public static int sirka;
	/** vyska mapy */
	public static int vyska;
	/** maximalni hodnota kontrastu */
	public static int kontrast;
	/** data nadmorskych vysek */
	public static int[] data;
	
	/**
	 * Metoda nacita data pgm souboru do pole
	 * @param jmenoSouboru jmeno souboru, ktery se ma vykreslit
	 * @return data nadmorskych vysek v poli
	 */
	public static int[] nacitaniDat(String jmenoSouboru) {
		String radka = "";
		int index = 0;
		try (Scanner sc = new Scanner(Paths.get(jmenoSouboru))){
			while(radka.matches(".*[a-z].*") || radka.isBlank() ||  radka.matches(".*[A-Z].*")) {
				radka = sc.nextLine();
			}
			String[] podretezec = radka.split(" ");
			sirka = Integer.parseInt(podretezec[0]);
			vyska = Integer.parseInt(podretezec[1]);
			int[] data = new int[sirka * vyska];
			kontrast = Integer.parseInt(sc.nextLine());
			while(sc.hasNextInt() && index < (sirka * vyska)) {
				data[index] = sc.nextInt();
				index++;
			}
			return data;
		}
		catch (Exception ex) {
			System.out.println("Doslo k chybe pri cteni souboru: " + jmenoSouboru + "(" + ex.getMessage() + ")");
		}
		return null;
	}
	
	/**
	 * Vstupni bod programu
	 * @param args jmeno souboru ktery se bude zobrazovat
	 */
	public static void main(String[] args) {
		if(args.length <= 0) {
			//pri nezadani argumentu se vykresli defaultni obrazek
			System.out.println("Nebyl zadan zadny argument.\nVykreslil se defaultni obrazek.");
			int[] pole = {0, 0, 0, 0, 0, 0, 0, 0, 0}; 
			data = pole;
			kontrast = 255;
			sirka = 3;
			vyska = 3;
		}
		else {
			data = nacitaniDat(args[0]);
		} 
		
		JFrame okno = new JFrame();
		okno.setTitle("Semestralni prace - Lukas Runt - A20B0226P");
		
		okno.add(new DrawingPanel());//pridani komponenty
		okno.pack(); //udela resize okna dle komponent
		
		okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//skonceni po zavreni okna
		okno.setLocationRelativeTo(null);//vycentrovat na obrazovce
		okno.setVisible(true);
	}
}
