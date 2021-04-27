import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
	public static DrawingPanel panel;
	
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
			int[] pole = {0, 50, 100, 0, 50, 100, 0, 50, 100}; 
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
		ImageIcon img = new ImageIcon("data\\lidl.png");
		okno.setIconImage(img.getImage());
		
		JMenuBar mb = new JMenuBar();
		JMenu export = new JMenu("Export");
		JMenuItem tisk = new JMenuItem("Tisk");
		tisk.addActionListener(e -> vytiskni(e));
		export.add(tisk);
		
		//JMenuItem  = new JMenuItem("Grafy");
		//menu.add(graf);
		JMenu grafy = new JMenu("Grafy");
		
		mb.add(export);
		mb.add(grafy);
		okno.setJMenuBar(mb);
		
		panel = new DrawingPanel();
		okno.add(panel, BorderLayout.CENTER);//pridani komponenty
		//okno.pack(); //udela resize okna dle komponent
		
		LegendaPanel legenda = new LegendaPanel();
		okno.add(legenda, BorderLayout.SOUTH);
		okno.pack();
		
		okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//skonceni po zavreni okna
		okno.setLocationRelativeTo(null);//vycentrovat na obrazovce
		okno.setVisible(true);
		
		Timer tm = new Timer();
		tm.schedule(new TimerTask() {
			@Override
			public void run() {
				panel.repaint();
			}
		}, 0, 20);
	}

	private static void vytiskni(ActionEvent e) {
		PrinterJob job = PrinterJob.getPrinterJob();
		if (job.printDialog()) {
			job.setPrintable(panel);
			try {
				job.print();
			} catch (PrinterException e1) {
				e1.printStackTrace();
			}
		}
	}
}
