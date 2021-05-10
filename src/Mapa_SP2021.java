import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.svg.SVGGraphics2D;

/**
 * Semestralni prace UPG 2021
 * Pasivni vizualizace 
 * @author Lukas Runt
 * @version 2.0 (26-02-2020)
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
	private static int[] stoupani;
	private static int[] prevyseni;
	public static DrawingPanel panel;
	private static String jmenoMapy;
	
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
	
	public static int[] vypocetStoupani() {
		int[] vodorovne = new int[vyska * (sirka - 1)];
		int[] svisle = new int[sirka * (vyska - 1)];
		int[] stoupani = new int[data.length];
		int soucet, n, prumer;
			for(int i = 1; i < vyska; i++) {	
				for(int j = 1; j < sirka; j++) {
					if ((i * j) % sirka == 0) {		}//aby se nepocitalo stoupani pres okraj
					else {
						vodorovne[i * j] = Math.abs(data[i * j] - data[i * j - 1]);
					}
				}
			}
			for(int i = 0; i < sirka; i++) {
				for(int j = 1; j < vyska; j++) {
					svisle[i + (j - 1) * sirka] = Math.abs(data[i + j * sirka] - data[i + (j-1) * sirka]);
				}
			}
			for(int i = 0; i < sirka; i++) {
				for(int j = 0; j < vyska; j++) {
					soucet = 0;
					n = 0;
					if(i != sirka - 1) {
						n++;
						soucet += vodorovne[i + j * (sirka - 1)];
					}
					if(i != 0) {
						n++;
						soucet += vodorovne[(i + j * (sirka - 1)) - 1];
					}
					if(j != vyska - 1) {
						n++;
						soucet += svisle[i + j * sirka];
					}
					if(j != 0) {
						n++;
						soucet += svisle[i + j * sirka - sirka];
					}
					prumer = (int)(soucet/n);
					stoupani[i + j * sirka] = prumer;
				}
			}
		return stoupani;
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
			jmenoMapy = "Default";
		}
		else {
			prevyseni = nacitaniDat(args[0]);
			jmenoMapy = args[0];
			data = prevyseni;
			stoupani = vypocetStoupani();
		} 
		
		JFrame okno = new JFrame();
		okno.setTitle("Semestralni prace - Lukas Runt - A20B0226P");
		ImageIcon img = new ImageIcon("data\\lidl.png");
		okno.setIconImage(img.getImage());
		
		JMenuBar mb = new JMenuBar();
		JMenu export = new JMenu("Export");
		export.setSize(300, 25);
		//export.setMnemonic(KeyEvent.VK_E);
		JMenuItem tisk = new JMenuItem("Tisk");
		tisk.addActionListener(e -> vytiskni(e));
		JMenuItem png = new JMenuItem("PNG");
		png.addActionListener(e -> exportPNG(e));
		JMenuItem svg = new JMenuItem("SVG");
		svg.addActionListener(e -> exportSVG(e));
		JMenuItem ascii = new JMenuItem("ASCII");
		ascii.addActionListener(e -> exportASCII(e));
		export.add(tisk);
		export.add(png);
		export.add(svg);
		export.add(ascii);
		
		JMenu grafy = new JMenu("Grafy");
		grafy.setSize(300, 25);
		JMenuItem histogram = new JMenuItem("Histogram");
		histogram.addActionListener(e -> zobrazHistogram());
		JMenuItem tukeyBox = new JMenuItem("Tukey Box");
		tukeyBox.addActionListener(e -> zobrazTukeyBox());
		grafy.add(histogram);
		grafy.add(tukeyBox);
	
		JMenu mode = new JMenu("Nastaveni");
		mode.setSize(300, 25);
		JMenuItem vrstevnice = new JMenuItem("Vrstevnice");
		vrstevnice.addActionListener(e -> setVrstevnice());
		mode.add(vrstevnice);
		
		JRadioButtonMenuItem prevyseni = new JRadioButtonMenuItem("Prevyseni", true);
		prevyseni.addActionListener(e -> setPrevyseni(e));
		JRadioButtonMenuItem stoupani = new JRadioButtonMenuItem("Stoupani");
		stoupani.addActionListener(e -> setStoupani(e));
		ButtonGroup directionGroup = new ButtonGroup();
		directionGroup.add(prevyseni);
		directionGroup.add(stoupani);
		
		mode.add(prevyseni);
		mode.add(stoupani);
		
		mb.add(export);
		mb.add(grafy);
		mb.add(mode);
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
		
		/*Timer tm = new Timer();
		tm.schedule(new TimerTask() {
			@Override
			public void run() {
				panel.repaint();
			}
		}, 0, 20);*/
	}

	private static void setVrstevnice() {
		String[] nadmorskeVysky = {"50", "500", "5000"};
		Frame frame = new Frame();
		String s = (String)JOptionPane.showInputDialog(frame, "Po kolika metrech se budou zobrazovat vrstevnice:", "Nastaveni vrstevnic", JOptionPane.PLAIN_MESSAGE,null, nadmorskeVysky, "50");
		if(s.equals("50")) {
			panel.setKolikrat(1);
		} else if(s.equals("500")) {
			panel.setKolikrat(10);
		} else if(s.equals("5000")) {
			panel.setKolikrat(100);
		}
	}

	private static void setStoupani(ActionEvent e) {
		data = stoupani;
		panel.repaint();
	}

	private static void setPrevyseni(ActionEvent e) {
		data = prevyseni;
		panel.repaint();
	}

	/**
	 * Metoda prevadi mapu do ascii artu
	 * @param e klikknuti na tlacitko exportu
	 */
	private static void exportASCII(ActionEvent e) {
		try {
			int[] pole = panel.upraveniHodnot(data);
			PrintWriter pw = new PrintWriter(
							new BufferedWriter(
							new FileWriter("asciiArt.txt")));
			for(int i = 0; i < vyska; i++) {
				for(int j = 0; j < sirka; j++) {
					int znak = (int)(pole[j + i * sirka]/50);
					switch(znak) {
					case 0:
						pw.print('■');
						pw.print('■');
						break;
					case 1:
						pw.print('#');
						pw.print('#');
						break;
					case 2:
						pw.print('|');
						pw.print('|');
						break;
					case 3:
						pw.print(':');
						pw.print(':');
						break;
					case 4:
						pw.print('.');
						pw.print('.');
						break;
					case 5:
						pw.print(' ');
						pw.print(' ');
						break;
					}
					if(j == sirka - 1) {
						pw.println();
					}
				}
			}
			pw.close();
		} catch (Exception ex) {
			System.out.println("Doslo k chybe pri vytvareni ascii artu: " + ex.getMessage());
		}
	}

	private static void exportPNG(ActionEvent e) {
		int sirka, vyska;
		Frame frame = new Frame();
		JTextField sirkaTF = new JTextField();
		JTextField vyskaTF = new JTextField();
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Sirka: "),
				sirkaTF,
				new JLabel("Vyska: "),
				vyskaTF
		};
		int result = JOptionPane.showConfirmDialog(null, inputs, "Zadejte sirku a vysku obrazku", JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			if(sirkaTF.getText().matches(".*[a-z].*") ||  sirkaTF.getText().matches(".*[A-Z].*") || sirkaTF.getText().matches(".*\\p{Punct}.*") || sirkaTF.getText().equals("")) {
				JOptionPane.showMessageDialog(frame ,"V policku pro sirku nebyla cislice", "Error", JOptionPane.ERROR_MESSAGE);
				exportPNG(e);
			} else if(vyskaTF.getText().matches(".*[a-z].*") ||  vyskaTF.getText().matches(".*[A-Z].*") || vyskaTF.getText().matches(".*\\p{Punct}.*") || vyskaTF.getText().equals("")) {
				JOptionPane.showMessageDialog(frame ,"V policku pro vysku nebyla cislice", "Error", JOptionPane.ERROR_MESSAGE);
				exportPNG(e);
			} else {
				sirka = Integer.parseInt(sirkaTF.getText());
				vyska = Integer.parseInt(vyskaTF.getText());
				BufferedImage im = new BufferedImage(sirka, vyska, BufferedImage.TYPE_3BYTE_BGR);
				panel.drawPicture(im.createGraphics(), sirka, vyska);
				try {
					ImageIO.write(im, "png", new File("Mapa.png"));
				} catch (IOException ex) {
					System.out.println("Doslo k chybe pri exportovani PNG obrazku" + ex.getMessage());
				}
			}
		} else {
			System.out.println("export zrusen");
		}
	}

	/**
	 * Metoda zobrazuje v novem okne tukeyBox
	 */
	private static void zobrazTukeyBox() {
		JFrame tukeyBox = new JFrame();
		tukeyBox.setTitle("TukeyBox - Lukas Runt - A20B0226P");
		
		ChartPanel tukeyBoxPanel = new ChartPanel(createTukeyBox(data));
		
		tukeyBox.add(tukeyBoxPanel);
		tukeyBox.pack();
		tukeyBox.setLocationRelativeTo(null);
		tukeyBox.setSize(new Dimension(600, 400));
		tukeyBox.setVisible(true);
	}

	/**
	 * Metoda vytvari tukeyBox z dat nadmorskych vysek
	 * @param poleDat Nadmorske vysky
	 * @return tukeyBox
	 */
	@SuppressWarnings("unchecked")
	private static JFreeChart createTukeyBox(int[] poleDat) {
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		//List<int[]> values = Arrays.asList(poleDat);
		//if Martin Cervenka see this vi von zulul
		ArrayList<Integer> values = new ArrayList<>();
		for(int vyska : poleDat) {
			values.add(vyska);
		}
		String pomocny = jmenoMapy.replace("\\", "!");
		String[] cesta = pomocny.split("!");
		dataset.add(values, cesta[cesta.length - 1], cesta[cesta.length - 1]);
		
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart("Pocet prevyseni", "Mapa", "Nadmorska vyska", (BoxAndWhiskerCategoryDataset)dataset, false);
		
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setOutlineVisible(true);
	
		BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer)plot.getRenderer();
		renderer.setMedianVisible(true);
		renderer.setMeanVisible(false);
		renderer.setFillBox(true);
		renderer.setMaxOutlierVisible(true);
		renderer.setMinOutlierVisible(true);
		renderer.setUseOutlinePaintForWhiskers(true);
		renderer.setSeriesOutlinePaint(0, Color.BLACK);
		
		int minimum = panel.getMinimum();
		ValueAxis axis = plot.getRangeAxis();
		axis.setLowerBound(minimum - 100);
		axis.setUpperBound(kontrast + 100);
		
		return chart;
	}

	/**
	 * Metoda ktera zobrazuje histogram v novem okne
	 */
	private static void zobrazHistogram() {
		JFrame histogram = new JFrame();
		histogram.setTitle("Histogram - Lukas Runt - A20B0226P");
		ImageIcon img = new ImageIcon("data\\histogram.png");
		histogram.setIconImage(img.getImage());
		
		ChartPanel histogramPanel = new ChartPanel(createBarChart(data));
		
		histogram.add(histogramPanel);
		histogram.pack();
		histogram.setLocationRelativeTo(null);
		histogram.setSize(new Dimension(600, 400));
		histogram.setVisible(true);
	}

	/**
	 * Metoda ktera vytvari histogram prevyseni
	 * @param poleDat data za kterych se tvori histogram
	 * @return histogram
	 */
	private static JFreeChart createBarChart(int[] poleDat) {
		HistogramDataset dataset = new HistogramDataset();
		double[] data = new double[poleDat.length];
		int minimum = Integer.MAX_VALUE;
		for(int i = 0; i < poleDat.length; i++) {
			data[i] = poleDat[i];
			if(poleDat[i] < minimum) {
				minimum = poleDat[i];
			}
		}
		dataset.setType(HistogramType.FREQUENCY);
		int pomocna = (kontrast - minimum)%5;
		int pocetBinu = (kontrast - minimum + pomocna)/5;
		if (pocetBinu == 0) {
			pocetBinu = 1;
		}
		dataset.addSeries("", data, pocetBinu);
		
		JFreeChart chart = ChartFactory.createHistogram("Histogram prevyseni", "Nadmorska vyska", "Pocet vyskytu", dataset);
		
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setOutlinePaint(null);
		chart.clearSubtitles();
		ValueAxis axis = plot.getDomainAxis();
		if (pocetBinu == 1) {
			axis.setLowerBound(minimum - 1);
			axis.setUpperBound(kontrast + 1);
		} else {
			axis.setLowerBound(minimum);
			axis.setUpperBound(kontrast);
		}
		
		XYBarRenderer renderer = (XYBarRenderer)plot.getRenderer();
		renderer.setMargin(0.05);
		renderer.setBarPainter(new StandardXYBarPainter());
		
		return chart;
	}

	/**
	 * Metoda provede vytisknuti mapy
	 * @param e akce - kliknuti na menu item
	 */
	private static void vytiskni(ActionEvent e) {
		PrinterJob job = PrinterJob.getPrinterJob();
		if (job.printDialog()) {
			job.setPrintable(panel);
			try {
				job.print();
			} catch (PrinterException e1) {
				System.out.println("Doslo k chybe pri tisku obrazku: (" + e1.getMessage() + ")");
			}
		}
	}
	
	/**
	 * Metoda provede export do souboru svg
	 * @param e akce - kliknuti na menu item
	 */
	private static void exportSVG(ActionEvent e) {
		SVGGraphics2D svg = new SVGGraphics2D(1920, 1080);
		panel.drawSVG(svg);
		try {
			PrintWriter pw = new PrintWriter(
							new BufferedWriter(
							new FileWriter("svgExport.svg")));
			pw.print(svg.getSVGElement());
			pw.close();
			Frame frame = new Frame();
			JOptionPane.showMessageDialog(frame ,"Soubor byl uspesne vytvoren.");
		} catch(Exception ex) {
			System.out.println("Doslo k chybe pri exportu souboru: " + ex.getMessage());
		}
	}
}
