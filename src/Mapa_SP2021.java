import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale.Category;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

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
			data = nacitaniDat(args[0]);
			jmenoMapy = args[0];
		} 
		
		JFrame okno = new JFrame();
		okno.setTitle("Semestralni prace - Lukas Runt - A20B0226P");
		ImageIcon img = new ImageIcon("data\\lidl.png");
		okno.setIconImage(img.getImage());
		
		JMenuBar mb = new JMenuBar();
		JMenu export = new JMenu("Export");
		JMenuItem tisk = new JMenuItem("Tisk");
		tisk.addActionListener(e -> vytiskni(e));
		JMenuItem png = new JMenuItem("PNG");
		png.addActionListener(e -> exportPNG(e));
		export.add(tisk);
		export.add(png);
		
		JMenu grafy = new JMenu("Grafy");
		JMenuItem histogram = new JMenuItem("Histogram");
		histogram.addActionListener(e -> zobrazHistogram());
		JMenuItem tukeyBox = new JMenuItem("Tukey Box");
		tukeyBox.addActionListener(e -> zobrazTukeyBox());
		grafy.add(histogram);
		grafy.add(tukeyBox);
		
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
		
		/*Timer tm = new Timer();
		tm.schedule(new TimerTask() {
			@Override
			public void run() {
				panel.repaint();
			}
		}, 0, 20);*/
	}

	private static void exportPNG(ActionEvent e) {
		int velikost;
		JFrame zadaniVelikosti = new JFrame();
		double pomer = sirka/(double)vyska;
		String velikostStr = JOptionPane.showInputDialog(zadaniVelikosti, "Zadej sirku");
		try {
			velikost = Integer.parseInt(velikostStr);
			if (velikost * velikost/pomer > 400000) {
				JOptionPane errorDialog = new JOptionPane("Byl zadan moc veliky rozmer!", JOptionPane.ERROR_MESSAGE);
				JDialog dialog = errorDialog.createDialog("Error");
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
				exportPNG(e);
				return;
			}
		} catch (Exception ex) {
			System.out.println("Nebylo zadano cislo!" + ex.getMessage());
			JOptionPane errorDialog = new JOptionPane("Nebylo zadano cislo!", JOptionPane.ERROR_MESSAGE);
			JDialog dialog = errorDialog.createDialog("Error");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			return;
		}
		BufferedImage im = new BufferedImage(velikost, (int)(velikost/pomer), BufferedImage.TYPE_3BYTE_BGR);
		panel.drawPicture(im.createGraphics(), (int)velikost, (int)(velikost/pomer));
		try {
			ImageIO.write(im, "png", new File("Mapa.png"));
		} catch (IOException ex) {
			System.out.println("Doslo k chybe pri exportovani PNG obrazku" + ex.getMessage());
		}
	}

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

	@SuppressWarnings("unchecked")
	private static JFreeChart createTukeyBox(int[] poleDat) {
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		//List<int[]> values = Arrays.asList(poleDat);
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
		
		int minimum = getMinimum(poleDat);
		ValueAxis axis = plot.getRangeAxis();
		axis.setLowerBound(minimum - 100);
		axis.setUpperBound(kontrast + 100);
		
		return chart;
	}

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
	
	private static int getMinimum(int[] poleDat) {
		int minimum = Integer.MAX_VALUE;
		for(int i = 0; i < poleDat.length; i++) {
			if(poleDat[i] < minimum) {
				minimum = poleDat[i];
			}
		}
		return minimum;
	}

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
}
