import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Trida {@code DrawingPanel} kresli barevnou mapu s vrstevnicemi,
 * mapa reaguje na kliknuti leveho tlacitka mysi, v miste kliknuti zobtazi bod a jeho nadmorskou vysku
 * @author Lukas Runt 
 * @version 2.0 (10-05-2021)
 */
@SuppressWarnings("serial")
public class DrawingPanel extends JPanel implements Printable{

	/** pomer ktery by byl idealni*/
	private final double POZADOVANY_POMER = (double)4/3; //optimalni velikost 800*600 -> pomer 4:3
	/** index v poli s maximalni hodnotou*/
	private int maximum;
	/** index v poli s minimalni hodnotou*/
	private int minimum;
	/** index v poli s maximalnim stoupanim*/
	private int maxStoupani;
	/** scale mapy*/
	private double scale;
	/** x-ova souradnice zacatku obrazku*/
	private int startX;
	/** y-ova souradnice zacatku obrazku*/
	private int startY;
	/** jak bude sipka dlouha*/
	private final double DELKA_SIPKY = 100;
	/** jak bude dlouhy hrot*/
	private final double DELKA_HROTU = 0.2 * DELKA_SIPKY;
	/** jak byde veliky text**/
	private final int VELIKOST_TEXTU = 20;
	/** obrazek - mapa*/
	private BufferedImage obrazek = new BufferedImage(Mapa_SP2021.sirka, Mapa_SP2021.vyska, BufferedImage.TYPE_3BYTE_BGR);
	/** barevna paleta*/
	private int[] paleta;
	/** velikost bileho okraje vedle mapy*/
	private int velikostOkrajeX = 0;
	/** velikost bileho okraje nad/pod mapou*/
	private int velikostOkrajeY = 0;
	/** Aktualni sirka obrazku v okne*/
	private double aktualVelikostObrX = 0;
	/** Aktualni vyska obrazku v okne*/
	private double aktualVelikostObrY = 0;
	/** x-ova souradnice kliknuti*/
	private double klikX = -1;
	/** y-ova souradnice kliknuti*/
	private double klikY = -1;
	/** x-ova souradnice zacatku cesty*/
	private double bod1X = -1;
	/** y-ova souradnice zacatku cesty*/
	private double bod1Y = -1;
	/** x-ova souradnice konce cesty*/
	private double bod2X = -1;
	/** y-ova souradnice konce cesty*/
	private double bod2Y = -1;
	private int nadmorskaVyska;
	/** pravdivostni tabulka ukryva tajemstvi, jestli je dany index vyse nez nadmorska vyska*/ 
	private boolean[] pravdivostniTabulka;
	/** 2D obdoba pravdivostni tabulky*/
	private boolean[][] pravdivostniTabulka2D;
	/** ciselna hodnota minimalniho prevyseni v mape*/
	private int hodnotaMin;
	/** ciselna hodnota maximalniho prevyseni v mape*/
	private int hodnotaMax;
	/** pomer stran mapy*/
	private double pomerStran;
	/** 2D pole dat*/
	private int[][] pole2D;
	/** pole hodnot pro ktere se budou vykreslovat vrstevnice*/
	private int[] vrstevnice;
	/** krera vrstevnice je zvyraznena 0 znamena, ze zadna vrstevnice neni zvyraznena*/
	private int zviraznenaVrstevnice = 0;
	/** konstanta kterou se upravovala data*/
	private double konst;
	/** vyska DrawingPanelu*/
	private int vyskaPanelu;
	/** sirka DraweingPanelu*/
	private int sirkaPanelu;
	/** hodnota po kolika se budou vykreslovat vrstevnice*/
	private int poKolika;
	/** celkem zbytecna promena*/
	private boolean metoda;
	
	/**
	 * Urcuje pocetecni velikost okna, tak aby mela sirku alespon 800px a alespon vysku 600px,
	 * pritom zachovava pomer stran
	 */
	public DrawingPanel() {
		this.setFocusable(true);
		int width = 800;
		int height = 600;
		pomerStran = (double)Mapa_SP2021.sirka/Mapa_SP2021.vyska;
		//tato podminka zaruci otevreni okna v co nejlepsi velikosti
		if(pomerStran < POZADOVANY_POMER) {
			if(pomerStran > 1) {
				height = (int)(width / pomerStran);
			}else if (pomerStran < 0.5){
				width = (int)(height * pomerStran);
			}else {
				height = (int)(width * pomerStran);
			}
		} else if (pomerStran < 2){
			width = (int)(height * pomerStran);
		} else {
			height = (int)(width / pomerStran);
		}
		this.setPreferredSize(new Dimension(width, height));
		makePalette();
		maximum = getMax(Mapa_SP2021.data);
		minimum = getMin(Mapa_SP2021.data);
		maxStoupani = getMaxStoupani(Mapa_SP2021.data);
		pole2D = pole2D();
		zjistiKolikrat();
		vyskaPanelu = this.getHeight();
		sirkaPanelu = this.getWidth();
		if(Mapa_SP2021.sirka < Integer.MAX_VALUE && Mapa_SP2021.vyska < Integer.MAX_VALUE) {//Zbytecna cast kodu
			metoda = true;																	//Ale nechci aby se kresleni vrstevnic prvni verze citilo zbytecne 
		} else {
			metoda = false;
		}
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				//Reagovani jen na leve tlacitko mysi
				if(SwingUtilities.isLeftMouseButton(e)) {
					if(e.getX() > velikostOkrajeX && e.getX() < velikostOkrajeX + Mapa_SP2021.sirka * scale) {
						if(e.getY() > velikostOkrajeY && e.getY() < velikostOkrajeY + Mapa_SP2021.vyska * scale) {
							klikX = (e.getX() - velikostOkrajeX) / scale;
							klikY = (e.getY() - velikostOkrajeY) / scale;
							//int nadmorskaVyska = Mapa_SP2021.data[(int)((e.getX() - velikostOkrajeX) / scale  + ((((e.getY() - velikostOkrajeY)/ scale) * Mapa_SP2021.sirka)))];
							nadmorskaVyska = pole2D[(int)((e.getX() - velikostOkrajeX) / scale)][(int)((e.getY() - velikostOkrajeY)/ scale)];	
							zviraznenaVrstevnice = najdiNejblizsi(nadmorskaVyska);
							repaint();
						}
					}
				} else if(SwingUtilities.isRightMouseButton(e)) {
					if(e.getX() > velikostOkrajeX && e.getX() < velikostOkrajeX + Mapa_SP2021.sirka * scale) {
						if(e.getY() > velikostOkrajeY && e.getY() < velikostOkrajeY + Mapa_SP2021.vyska * scale) {
							if(bod1X == -1 && bod1Y == -1) {
								bod1X = (e.getX() - velikostOkrajeX) / scale;
								bod1Y = (e.getY() - velikostOkrajeY) / scale;
							} else if(bod2X == -1 && bod2Y == -1) {
								bod2X = (e.getX() - velikostOkrajeX) / scale;
								bod2Y = (e.getY() - velikostOkrajeY) / scale;
								showGraph();
							} else {
								bod1X = (e.getX() - velikostOkrajeX) / scale;
								bod1Y = (e.getY() - velikostOkrajeY) / scale;
								bod2X = -1;
								bod2Y = -1;
							}
							repaint();
						}
					}
						
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
				
			}
		});
		
		this.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				//RESET (Po zmacknuti R)
				if (e.getKeyChar() == 'r' || e.getKeyChar() == 'R') {
					zviraznenaVrstevnice = 0;
					klikX = -1;
					klikY = -1;
					bod1X = -1;
					bod1Y = -1;
					bod2X = -1;
					bod2Y = -1;
					repaint();
				}
			}
		});
	}
	
	/**
	 * Metoda zobrazuje graf prevyseni na ceste
	 */
	private void showGraph() {
		//Vypocet U
		double u_x = bod2X - bod1X;
		double u_y = bod2Y - bod1Y;
		double u_len1 = 1 / Math.sqrt(u_x * u_x + u_y * u_y);
		//jednotkove U
		u_x *= u_len1;
		u_y *= u_len1;
		
		JFrame graf = new JFrame();
		graf.setTitle("Cesta - Lukas Runt - A20B0226P");
		
		ImageIcon img = new ImageIcon("data\\prevyseni.png");
		graf.setIconImage(img.getImage());
		
		ChartPanel lineChartPanel = new ChartPanel(createLineChart(u_x, u_y));
		graf.add(lineChartPanel);
		graf.pack();
		graf.setSize(new Dimension(600, 400));
		graf.setLocationRelativeTo(null);
		graf.setVisible(true);
		//smazani bodu a primky po zavreni grafu
		graf.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
               bod1X = -1;
               bod1Y = -1;
               bod2X = -1;
               bod2Y = -1;
               repaint();
            }
        });
	}

	/**
	 * Medota vytvari line graf
	 * @param u_x element x smeroveho vektoru u
	 * @param u_y element y smeroveho vektoru y
	 * @return lineChart
	 */
	private JFreeChart createLineChart(double u_x, double u_y) {
		int vzdalenost = 0;
		double x = bod1X;
		double y = bod1Y;
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries s1 = new XYSeries("Prevyseni");
		while(Math.abs(x - bod2X) > 1 || Math.abs(y - bod2Y) > 1) {
			s1.add(vzdalenost,pole2D[(int)x][(int)y]);
			x += u_x;
			y += u_y;
			vzdalenost++;
		}
		s1.add(vzdalenost,pole2D[(int)x][(int)y]); //asi by bylo vhodnejsi pouzit do-while cyklus
		dataset.addSeries(s1);
		JFreeChart lineChart = ChartFactory.createXYLineChart("Prevyseni na ceste",  "Vzdalenost","Nadmorska vyska", dataset);
		
		XYPlot plot = lineChart.getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setOutlinePaint(null);
		
		ValueAxis axis = plot.getDomainAxis();
		axis.setUpperBound(vzdalenost);
		
		return lineChart;
	}

	/**
	 * Metoda meni paletu barev
	 */
	private void makePalette() {
		paleta = new int [265];
		int r;
		int g;
		int b;
		for(int i = 0; i < paleta.length;i++) {
			if(i <= 50) {
				b = 250 - (4 * i);
				r = i;
				g = 3 * i;
			} else if(i <= 100 && i > 50) {
				b = 50;
				r = 50 + (3 * (i - 50));
				g = 150 + (2* (i - 50));
			} else if(i > 100 && i <= 150) {
				b = 50;
				r = 200 + (i - 100);
				g = 250; 
			} else if(i > 150 && i <=200) {
				b = 50 - (i - 150);
				r = 250;
				g = 250 - (i - 150);
			} else if(i > 200 && i <= 250) {
				b = 0;
				r = 250 - (int)(3*(i - 200));
				g = 200 - (int)(3*(i - 200));
			} 
			else {
			b = 150;
			r = 150;
			g = 150;
			}
			paleta[i] = (r << 16) | (g << 8) | (b << 0);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		
		createPicture(Mapa_SP2021.data);
		drawPicture(g2, this.getWidth(), this.getHeight());
		getVelikostOkraje();
		g2.setColor(Color.WHITE);
		drawVsechnyVrstevnice(g2);
		drawZviraznenaVrstevnice(g2);
		drawBod(g2);
		drawPoint(g2);
		
		drawArrow(maximum % Mapa_SP2021.sirka, (int)(maximum / Mapa_SP2021.sirka), "Max. prevyseni", g2);
		drawArrow(minimum % Mapa_SP2021.sirka, (int)(minimum / Mapa_SP2021.sirka), "Min. prevyseni", g2);
		drawArrow(maxStoupani % Mapa_SP2021.sirka, (int)(maxStoupani / Mapa_SP2021.sirka), "Max. stoupani", g2);
		//pri meneni velikosti okna se bude mapa prakreslovat bez pouziti timeru
		if(this.getHeight() != vyskaPanelu || this.getWidth() != sirkaPanelu) {
			this.repaint();
			vyskaPanelu = this.getHeight();
			sirkaPanelu = this.getWidth();
		}
	}
	
	/**
	 * Vytvori pole vrstevnic, ktere se budou dale vykreslovat
	 */
	private void zjistiKolikrat() {
		poKolika = 50;
		if(hodnotaMax - hodnotaMin > 1000) {
			poKolika = 500;
		}
		if(hodnotaMax - hodnotaMin > 10000) {
			poKolika = 5000;
		}
		createPoleVrstevnic();
	}
	
	/**
	 * Vystari pole hodnot pro ktere se budou vykreslovati vrstevnice
	 */
	private void createPoleVrstevnic() {
		ArrayList<Integer> vrstevniceList = new ArrayList<Integer>();
		int a = hodnotaMin;
		while(a <= hodnotaMax) {
			if(a % (poKolika) == 0 && a != 0) {
				vrstevniceList.add(a);
			}
			a++;
		}
		vrstevnice = new int[vrstevniceList.size()];
		for(int j = 0; j < vrstevnice.length; j++) {
			vrstevnice[j] = vrstevniceList.get(j).intValue();
		}
	}
	
	/**
	 * Metoda nastavi hodnoty na jednotlivyvh pixelech obrazku
	 * @param data pole dat ze souboru PGM
	 */
	private void createPicture(int[] data) {
		if(Mapa_SP2021.kontrast > 255) {
			data = upraveniHodnot(data);
		}
		int gr;
		int[] barva = new int[data.length];
		int iW = obrazek.getWidth();
		int iH = obrazek.getHeight();
		for(int i = 0;  i < data.length; i++) {
			gr = data[i];
			barva[i] = paleta[gr];
		}
		obrazek.setRGB(0, 0, iW, iH, barva, 0, iW);
	}
	
	/**
	 * Metoda opravuje data k zobrazeni, je-li nejaka hodnota v poli vetsi nez 255 (aby se data dala vykreslit)
	 * @param data hodnoty nadmorskych vysek
	 * @return upravena data
	 */
	public int[] upraveniHodnot(int[] data) {
		int[] vysledek = new int[data.length];
		konst = 1;
		int kontrastPom = Mapa_SP2021.kontrast;
		while(kontrastPom > 255) {  
			konst += 0.8; //pricitam 0.8, aby to bylo hezke :-)
			kontrastPom = (int)(Mapa_SP2021.kontrast / konst);
		}
		for (int i = 0; i < data.length; i++) {
			vysledek[i] = (int)(data[i]/konst);
		}
		return vysledek;
	}
	
	/**
	 * Metoda nakresli obrazek se zachovanim pomeru stran a bilinearni interpolaci
	 * @param g2 grafika
	 * @param W sirka obrazku
	 * @param H vyska obrazku
	 */
	public void drawPicture(Graphics2D g2, int W, int H) {
		//Cerny obdelnik v pozadi
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, W, H);
		
		int iW = obrazek.getWidth();
		int iH = obrazek.getHeight();
		double scaleX = ((double)W) / iW; //Kolikrat se obrazek zvetsuje v ose X 
		double scaleY = ((double)H) / iH; //Kolikrat se obrazek zvetsuje v ose Y
		scale = Math.min(scaleX, scaleY);
		
		aktualVelikostObrX = obrazek.getWidth() * scale;
		aktualVelikostObrY = obrazek.getHeight() * scale;
		
		int niW = (int)(iW * scale); //nova sirka obrazku
		int niH = (int)(iH * scale); //nova vyska obrazku
		startX = (W - niW) / 2;//zacatek obrazku X
		startY = (H - niH) / 2;//zacatek obrazku Y
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); //nastaveni bilinearni interpolace
				
		g2.drawImage(obrazek, startX, startY, niW, niH, null);
	}
	
	/**
	 * Metoda ktesli bod s nadmorskou vyskou v miste kliknuti
	 * @param g2 Grafika 2D
	 */
	private void drawBod(Graphics2D g2) {
		int prumer = 5;
		int y;
		int x;
		if(klikX >= 0 && klikY >= 0) {
			Ellipse2D bod = new Ellipse2D.Double(klikX * scale + velikostOkrajeX - prumer/2, klikY * scale + velikostOkrajeY - prumer/2, prumer, prumer);
			g2.setColor(Color.BLACK);
			g2.fill(bod);
			g2.setFont(new Font("Calibri", Font.BOLD, VELIKOST_TEXTU));
			FontMetrics font = g2.getFontMetrics();
			if((int)(klikY * scale + velikostOkrajeY) > VELIKOST_TEXTU + 10) {
				y = (int)(klikY * scale + velikostOkrajeY) - 2;
			} else {
				y = (int)(klikY * scale + velikostOkrajeY + VELIKOST_TEXTU);
			}
			if((int)(klikX * scale + velikostOkrajeX) > obrazek.getWidth() - font.stringWidth(nadmorskaVyska + "")/2){
				x = (int)(klikX * scale + velikostOkrajeX - font.stringWidth(nadmorskaVyska + ""));
			} else if((int)(klikX * scale + velikostOkrajeX) < font.stringWidth(nadmorskaVyska + "")/2) {
				x = (int)(klikX * scale + velikostOkrajeX);
			} else {
				x = (int)(klikX * scale + velikostOkrajeX - font.stringWidth(nadmorskaVyska + "")/2);
			}
			g2.drawString(nadmorskaVyska + "",x , y);
		}
	}
	
	/**
	 * Metoda kresli body, ktere zjistuji prevyseni cesty vodorovnou carou
	 * @param g2 2Dgrafika
	 */
	private void drawPoint(Graphics2D g2) {
		int polomer = 10;
		g2.setColor(Color.RED);
		if(bod1X >= 0 && bod1Y >=0) {
			Ellipse2D bod1 = new Ellipse2D.Double(bod1X * scale + velikostOkrajeX, bod1Y * scale + velikostOkrajeY, polomer, polomer);
			g2.fill(bod1);
		}
		if(bod2X >= 0 && bod2Y >= 0) {
			Ellipse2D bod2 = new Ellipse2D.Double(bod2X * scale + velikostOkrajeX , bod2Y * scale + velikostOkrajeY, polomer, polomer);
			g2.fill(bod2);
			Line2D usecka = new Line2D.Double(bod1X * scale + velikostOkrajeX + polomer/2, bod1Y * scale + velikostOkrajeY + polomer/2, bod2X * scale + velikostOkrajeX + polomer/2, bod2Y * scale + velikostOkrajeY + polomer/2);
			g2.draw(usecka);
		}
	}

	/**
	 * Vraci index nejvyssiho bodu, kdyz vice stejne vysokych budu vraci posledni
	 * @param data pgm data
	 * @return index nevyssiho bodu
	 */
	private int getMax(int[] data) {
		int max = 0;
		for(int i = 0; i < data.length; i++) {
			if(data[i] > data[max]) {
				max = i;
			}
		}
		hodnotaMax = data[max];
		return max;
	}
	
	/**
	 * Vraci index nejnizsiho bodu, kdyz je vice stejne nizkych bodu vraci prvni
	 * @param data pgm data
	 * @return index nejnizsiho bodu
	 */
	private int getMin(int[] data) {
		int min = 0;
		for(int i = 0; i < data.length; i++) {
			if(data[i] <= data[min]) {
				min = i;
				hodnotaMin = data[i];
			}
		}
		return min;
	}
	
	/**
	 * Pocita maximalni soupani ve svou smerech (vertikalne a horizontalne)
	 * @param data pgm data
	 * @return index nejvetsiho stoupani
	 */
	private int getMaxStoupani(int[] data) {
		int max = 1;
		int stoupani = 0;
		//Maximalni stoupani vertikane
		for(int i = 1; i < Mapa_SP2021.vyska; i++) {	
			for(int j = 1; j < Mapa_SP2021.sirka; j++) {
				if ((i * j) % Mapa_SP2021.sirka == 0) {		}//aby se nepocitalo stoupani pres okraj
				else if(Math.abs(data[i * j] - data[i * j - 1]) > stoupani) {
					max = i * j;
					stoupani = Math.abs(data[i * j] - data[i * j - 1]);
				}
			}
		}
		//Maximalni stoupani horizontalne
		for(int i = 0; i < Mapa_SP2021.sirka; i++) {
			for(int j = 1; j < Mapa_SP2021.vyska; j++) {
				if(Math.abs(data[i + j * Mapa_SP2021.sirka] - data[i + (j-1) * Mapa_SP2021.sirka]) > stoupani){
					max = i + j * Mapa_SP2021.sirka;
					stoupani = Math.abs(data[i + j * Mapa_SP2021.sirka] - data[i + (j-1) * Mapa_SP2021.sirka]);
				}
			}
		}
		return max;
	}
	
	/**
	 * Metoda na kresleni sipky, sipka ani text sipky nesmi nikdy ven z obrazku 
	 * @param x1 bod X, kde se bude kreslit pocatek sipky
	 * @param y1 bod Y, kde se bude kreslit pocetek sipky 
	 * @param g2 grafika
	 */
	private void drawArrow(double x1, double y1, String text, Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
		g2.setFont(new Font("Calibri", Font.BOLD, VELIKOST_TEXTU));
		FontMetrics font = g2.getFontMetrics();
		int textX;
		int textY;
		
		x1 = (x1*scale) + startX;
		y1 = (y1*scale) + startY;
		
		//vypocet U
		double u_x = x1 - this.getWidth()/2;
		double u_y = y1 - this.getHeight()/2;
		double u_len1 = 1 / Math.sqrt(u_x * u_x + u_y * u_y);
		//jednotkove U
		u_x *= u_len1;
		u_y *= u_len1;
		//vypocet x2 a y2 o tak aby delka sipky byla 50px
		double x2 = x1 - u_x * DELKA_SIPKY;
		double y2 = y1 - u_y * DELKA_SIPKY;
		g2.draw(new Line2D.Double(x1, y1, x2, y2));
		//--------------hrot--------------------------------------------------
		//hrot
		double u_x2 = x1 - x2;
		double u_y2 = y1 - y2;
		//jednotkove U
		double u_len2 = 1 / DELKA_SIPKY;
		u_x2 *= u_len2;
		u_y2 *= u_len2;
		//smer kolmy (jednotkova delka)
		double v_x = u_y2;
		double v_y = -u_x2;
		//smer kolmy - delka o 1/2 sirky hrotu
		v_x *= 0.35 * DELKA_HROTU;
		v_y *= 0.35 * DELKA_HROTU;
		double c_x = x1 - u_x2 * DELKA_HROTU;
		double c_y = y1 - u_y2 * DELKA_HROTU;
		g2.draw(new Line2D.Double(c_x + v_x, c_y + v_y, x1, y1));
		g2.draw(new Line2D.Double(c_x - v_x, c_y - v_y, x1, y1));
		//-------------------text---------------------------------------------
		//A co se stane zlobivym hosankum, pane Filuto?
		if(u_y < 0) {
			if(textVObrazeY(u_y, y1) == true) {
				textY = (int)(y2 + 15);
			}
			else {
				textY = this.getHeight()/2 + (int)(aktualVelikostObrY)/2 - 2;
			}
		}
		else {
			if(textVObrazeY(u_y, y1) == true) {
				textY = (int)(y2 - 5);
			}
			else {
				textY = velikostOkrajeY + VELIKOST_TEXTU;
			}
		}
		//Strycek Arnie je usmazi zaziva vrazednym pohledem.
		if(u_x < 0) {
			if(textVObrazeX(u_x, x1, font.stringWidth(text)/2)) {
				textX = (int)(x2 - font.stringWidth(text)/2);
			}
			else {
				textX = (int)(this.getWidth() - velikostOkrajeX - font.stringWidth(text));
			}
		}
		else {
			if(textVObrazeX(u_x, x1, font.stringWidth(text)/2)) {
				textX = (int)(x2 - font.stringWidth(text)/2);
			}
			else {
				textX = velikostOkrajeX;
			}
		}
		//To je pravda, pane Filuto.
		g2.drawString(text,textX, textY);
	}
	
	/**
	 * Metoda zjistuje zda se text vejde do obrazu v ose y
	 * @param u_y vektor sipky y
	 * @param y pozice na ose y
	 * @return true - text se cely vejde do obrazu, false - sipka se nevejde do obrazu
	 */
	private boolean textVObrazeY(double u_y, double y) {
		if(u_y > 0) {
			if(aktualVelikostObrY > (u_y * DELKA_SIPKY + VELIKOST_TEXTU + (aktualVelikostObrY - y) + velikostOkrajeY)) {
				return true;
			}
		}
		else {
			if(aktualVelikostObrY > (Math.abs(u_y) * DELKA_SIPKY + VELIKOST_TEXTU - velikostOkrajeY + y)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Metoda zjistuje zda se text vejde do obrazu v ose x
	 * @param u_x vektor sipky x
	 * @param x pozice na ose x
	 * @param stringWidth delka textu v pixelech
	 * @return true - text se vejde, false text se nevejde
	 */
	private boolean textVObrazeX(double u_x, double x, double stringWidth) {
		if(u_x > 0) {
			if(0 < x - (u_x * DELKA_SIPKY) - stringWidth - velikostOkrajeX) {
				return true;
			}
		}
		else {
			if(this.getWidth() - velikostOkrajeX > x + (Math.abs(u_x) * DELKA_SIPKY) + stringWidth) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Metoda zjistuje, jestli je 
	 * @param nadmorskaVyska vyska se kterou se budou porovnavat ostatni vysky
	 * @return pravdivostni pole
	 */
	private boolean[] vetsiNez(int nadmorskaVyska){
		boolean[] pole = new boolean[Mapa_SP2021.data.length];
		for(int i = 0; i < Mapa_SP2021.data.length; i++) {
			if(Mapa_SP2021.data[i] < nadmorskaVyska) {
				pole[i] = false;
			}
			else {
				pole[i] = true;
			}
		}
		return pole;
	}
	
	/**
	 * Zjisteni velikosti okraje
	 */
	private void getVelikostOkraje() {
		velikostOkrajeY = (int)(this.getHeight() - aktualVelikostObrY)/2;
		velikostOkrajeX = (int)(this.getWidth()  - aktualVelikostObrX)/2;
	}
	
	/**
	 * Metoda zjistuje, jestli je bod v mape vys nez bod se kterym to porovnavame
	 * @param nadmorskaVyska nadmorska vyska porovnavaneho bodu
	 * @return (true - kdyz je vetsi, false - kdyz je mensi) -> dvourozmerne pravdivodtni pole
	 */
	private boolean[][] vetsiNez2D(int nadmorskaVyska){ //vytvari se dvoutrozmerne pole, uz me nebavilo pracovat s jednorozmernym
		boolean[][] pole = new boolean[Mapa_SP2021.sirka + 1][Mapa_SP2021.vyska + 1];
		for(int i = 0; i < Mapa_SP2021.sirka; i++) {
			for(int j = 0; j < Mapa_SP2021.vyska; j++) {
				if(Mapa_SP2021.data[i + j * Mapa_SP2021.sirka] < nadmorskaVyska) {
					pole[i][j] = false;
				} else {
					pole[i][j] = true;
				}
			}
		}
		for(int i = 0; i < Mapa_SP2021.sirka; i++) {
			if(Mapa_SP2021.data[i + (Mapa_SP2021.vyska - 1) * Mapa_SP2021.sirka] < nadmorskaVyska) {
				pole[i][Mapa_SP2021.vyska] = false;
			} else {
				pole[i][Mapa_SP2021.vyska] = true;
			}
		}
		for(int i = 0; i < Mapa_SP2021.vyska; i++) {
			if(Mapa_SP2021.data[Mapa_SP2021.sirka - 1 + i * Mapa_SP2021.sirka] < nadmorskaVyska) {
				pole[Mapa_SP2021.sirka][i] = false;
			} else {
				pole[Mapa_SP2021.sirka][i] = true;
			}
		}
		if(Mapa_SP2021.data[Mapa_SP2021.data.length - 1] > nadmorskaVyska) {
			pole[Mapa_SP2021.sirka][Mapa_SP2021.vyska] = true;
		} else {
			pole[Mapa_SP2021.sirka][Mapa_SP2021.vyska] = false;
		}
		return pole;
	}
	
	/**
	 * Metoda ktera nakresli vsechny vrstevnice
	 * @param g2 obycejna grafika
	 */
	private void drawVsechnyVrstevnice(Graphics2D g2) {
		int a;
		int i = 0;
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
		while(i < vrstevnice.length && vrstevnice.length >= 1) {
			a = vrstevnice[i];
			if(metoda) {
				pravdivostniTabulka2D = vetsiNez2D(a);
				drawVrstevniceV2(g2, a);
			} else {
				pravdivostniTabulka = vetsiNez(a);
				drawVrstevnice(g2, a);
			}
			i++;
		}	
	}
	
	/**
	 * Metoda nakresli 1 konkretni vrstevnici (stara metoda predek vrstevnice druhe generace, nechce se mi toho boomera zabijet, navic by byla vrstevnice verze dva smutna, ze ji umrel dedecek)
	 * @param g2 grafika
	 * @param porovnani madmorska vyska
	 */
	private void drawVrstevnice(Graphics2D g2, int porovnani) {
		//kreslesleni vrstevnic vodorovne
		for(int i = 0; i < Mapa_SP2021.vyska; i++) {
			for(int j = 0; j < Mapa_SP2021.sirka - 1; j++) {
				if(pravdivostniTabulka[i * Mapa_SP2021.sirka + j] != pravdivostniTabulka[(i * Mapa_SP2021.sirka + j) + 1]) {
					double y1 = ((i + 1) * scale) + velikostOkrajeY;
					double y2 = ((i) * scale) + velikostOkrajeY;
					double x1 = (j * scale) + velikostOkrajeX;
					double x2 = (j * scale) + velikostOkrajeX;
					Line2D line1 = new Line2D.Double(x1, y1, x2, y2);
					g2.draw(line1);
				}
			}
		}
		//kresleni vrstevnic svisle
		for(int i = 0; i < Mapa_SP2021.sirka; i++) {
			for(int j = 1; j < Mapa_SP2021.vyska; j++) {
				if(pravdivostniTabulka[i + j * Mapa_SP2021.sirka] != pravdivostniTabulka[i + (j-1) * Mapa_SP2021.sirka]){
					double x1 = ((i - 1) * scale) + velikostOkrajeX;
					double x2 = ((i) * scale) + velikostOkrajeX;
					double y1 = (j * scale) + velikostOkrajeY;
					double y2 = (j * scale) + velikostOkrajeY;
					Line2D line2 = new Line2D.Double(x1, y1, x2, y2);
					g2.draw(line2);
				}
			}
		}
	}
	
	/**
	 * Metoda ktera kresli vrstevnice druhe generace
	 * @param g2 grafika GEFORCE GTX
	 * @param nadmorskaVyska nadmorska vyska vrstevnice ktera se bude vykreslovat
	 */
	private void drawVrstevniceV2(Graphics2D g2, int nadmorskaVyska) {
		double x1, x2, y1, y2;
		byte binKomb;
		for(int i = 0; i < Mapa_SP2021.sirka; i++) {
			for(int j = 0; j < Mapa_SP2021.vyska; j++) {
				binKomb = 0;
				if(pravdivostniTabulka2D[i][j]) {
					binKomb += 1;
				}
				if(pravdivostniTabulka2D[i + 1][j]) {
					binKomb += 2;
				}
				if(pravdivostniTabulka2D[i + 1][j + 1]) {
					binKomb += 4;
				}
				if(pravdivostniTabulka2D[i][j + 1]) {
					binKomb += 8;
				}
				switch(binKomb) { //Dalsi pomerne oskliva cast kodu: hodne opakovani, zadna motivace s tim neco delat :-( 
					case 1:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 2:
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 3:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 4:
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale + 1 * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 5:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale + 1 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						
						break;
					case 6:
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale) + velikostOkrajeY;
						y2 = (j * scale + 1 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 7:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale + 1 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 8:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale + 1 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 9:
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale) + velikostOkrajeY;
						y2 = (j * scale + 1 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 10:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale + 1 * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 11:
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale + 1 * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 12:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 13:
						x1 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						x2 = (i * scale + 1 * scale) + velikostOkrajeX;
						y1 = (j * scale) + velikostOkrajeY;
						y2 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
					case 14:
						x1 = (i * scale) + velikostOkrajeX;
						x2 = (i * scale + 0.5 * scale) + velikostOkrajeX;
						y1 = (j * scale + 0.5 * scale) + velikostOkrajeY;
						y2 = (j * scale) + velikostOkrajeY;
						nakresliCaru(g2, x1, x2, y1, y2);
						break;
						
				}
			}
		}
	}
	
	/**
	 * Metoda kresli zvyraznenou vrstevnici ruzovou barvou a vetsi tlustkou cary
	 * zviraznena vrstevnice je takova, ktere je nejblize hodnotou bod v mape
	 * @param g2 grafika RTX
	 */
	private void drawZviraznenaVrstevnice(Graphics2D g2) {
		if (zviraznenaVrstevnice != 0) {
			g2.setColor(Color.MAGENTA);
			g2.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
			if(metoda) {
				pravdivostniTabulka2D = vetsiNez2D(zviraznenaVrstevnice);
				drawVrstevniceV2(g2, zviraznenaVrstevnice);
			}else {
				g2.translate(1 * scale, 0);
				pravdivostniTabulka = vetsiNez(zviraznenaVrstevnice);
				drawVrstevnice(g2, zviraznenaVrstevnice);
				g2.translate(-1 * scale, 0);
			}
			
		}
	}
	
	/**
	 * Kresli caru - vrstevici, uprtavenou scalem
	 * @param g2 grafarna
	 * @param x1 bod x1
	 * @param x2 bod x2
	 * @param y1 bod y1
	 * @param y2 bod y2
	 */
	private void nakresliCaru(Graphics2D g2, double x1, double x2, double y1, double y2) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		g2.draw(line);
	}
	
	
	/**
	 * Metoda vytvori dvourozmerne pole dat 
	 * @return dvourozmerne pole
	 */
	private int[][] pole2D(){
		int[][] pole2D = new int[Mapa_SP2021.sirka][Mapa_SP2021.vyska];
		for(int i = 0; i < Mapa_SP2021.sirka; i++) {
			for(int j = 0; j < Mapa_SP2021.vyska; j++) {
				pole2D[i][j] = Mapa_SP2021.data[i + j * Mapa_SP2021.sirka];
			}
		}
		return pole2D;
	}
	
	/**
	 * Metoda zjisti, ktera vrstevnice je hodnotove nejblize k zadane hodnote
	 * @param nadmorskaVyska nadmorska vyska kliknuti
	 * @return hodnotu nejblizsi vrstevnice
	 */
	private int najdiNejblizsi(int nadmorskaVyska) {
		if(vrstevnice.length < 1) {
			return 0;
		}
		if(vrstevnice[0] > nadmorskaVyska) {
			return vrstevnice[0];
		}
		for(int i = 0; i < vrstevnice.length; i++) {
			if(Math.abs(nadmorskaVyska - vrstevnice[i]) <= poKolika/2) {
				return vrstevnice[i];
			}
		}
		if(vrstevnice[vrstevnice.length-1] == 0) {
			return vrstevnice[vrstevnice.length-2];
		}
		return vrstevnice[vrstevnice.length-1];
	}

	/**
	 * Metoda na vytisknuti stranky
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex > 0) {
			return NO_SUCH_PAGE;
		}
		Graphics2D g2 = (Graphics2D)graphics;
		
		final double mmToPt = 72 / 25.4;
		
		if(Mapa_SP2021.sirka > Mapa_SP2021.vyska) {
			final double X_MM = 200;
			final double Y_MM = 200/pomerStran;
			double PosunutiY = (297 - Y_MM) * mmToPt; 
			
			g2.translate(10 * mmToPt, PosunutiY/2);
			
			drawPicture(g2, (int)(X_MM * mmToPt), (int)(Y_MM * mmToPt));
		} else {
			final double Y_MM = 250;
			final double X_MM = 200 * pomerStran;
			double PosunutiX = (210 - X_MM) * mmToPt;
			
			g2.translate(PosunutiX/2, 0);
			
			drawPicture(g2, (int)(X_MM * mmToPt), (int)(Y_MM * mmToPt));
			
		}
		drawVsechnyVrstevnice(g2);
		drawZviraznenaVrstevnice(g2);
		drawBod(g2);
		drawPoint(g2);
		drawArrow(maximum % Mapa_SP2021.sirka, (int)(maximum / Mapa_SP2021.sirka), "Max. prevyseni", g2);
		drawArrow(minimum % Mapa_SP2021.sirka, (int)(minimum / Mapa_SP2021.sirka), "Min. prevyseni", g2);
		drawArrow(maxStoupani % Mapa_SP2021.sirka, (int)(maxStoupani / Mapa_SP2021.sirka), "Max. stoupani", g2);
		return 0;
	}
	
	/**
	 * Metoda kresli vse v panelu, co je nakresleno vektorovou grafikou
	 * @param g2 grafika
	 */
	public void drawSVG(Graphics2D g2) {
		g2.setColor(Color.GRAY);
		drawVsechnyVrstevnice(g2);
		drawZviraznenaVrstevnice(g2);
		drawBod(g2);
		drawPoint(g2);
		drawArrow(maximum % Mapa_SP2021.sirka, (int)(maximum / Mapa_SP2021.sirka), "Max. prevyseni", g2);
		drawArrow(minimum % Mapa_SP2021.sirka, (int)(minimum / Mapa_SP2021.sirka), "Min. prevyseni", g2);
		drawArrow(maxStoupani % Mapa_SP2021.sirka, (int)(maxStoupani / Mapa_SP2021.sirka), "Max. stoupani", g2);
	}
	
	/**
	 * Matoda kresli PNG obrazek
	 * @param g2 grafarna
	 * @param sirka sirka exportovaneho obrazku
	 * @param vyska vyska exportovaneho obrazku
	 */
	@SuppressWarnings("deprecation")
	public void drawPNG(Graphics2D g2, int sirka, int vyska) { //metoda je takle oskliva protoze se mi vzdy po exportu deformovaly vrstevnice
		int velikostX = velikostOkrajeX;					   //musel jsem tedy vsechno si ulozit nadefinovat na konci metody
		int velikostY = velikostOkrajeY;					   
		double scaleR = scale;
		Dimension d = this.size();
		this.setSize(sirka, vyska);
		drawPicture(g2, sirka, vyska);
		getVelikostOkraje();
		drawVsechnyVrstevnice(g2);
		drawZviraznenaVrstevnice(g2);
		drawBod(g2);
		drawPoint(g2);
		drawArrow(maximum % Mapa_SP2021.sirka, (int)(maximum / Mapa_SP2021.sirka), "Max. prevyseni", g2);
		drawArrow(minimum % Mapa_SP2021.sirka, (int)(minimum / Mapa_SP2021.sirka), "Min. prevyseni", g2);
		drawArrow(maxStoupani % Mapa_SP2021.sirka, (int)(maxStoupani / Mapa_SP2021.sirka), "Max. stoupani", g2);
		this.setSize(d);
		scale = scaleR;
		velikostOkrajeX = velikostX;
		velikostOkrajeY = velikostY;
		repaint();
	}
	
	//----------------getry--------------------------------------------
	/**
	 * Vraci upravenou paletu barev
	 * @return paleta
	 */
	public int[] getPaleta() {
		return paleta;
	}
	
	/**
	 * Vraci maximalni nadmorskou vysku
	 * @return maximalni hodnota
	 */
	public int getMaximum() {
		return hodnotaMax;
	}
	
	/**
	 * Vraci minimalni nadmorskou vysku
	 * @return minimalni hodnota
	 */
	public int getMinimum() {
		return hodnotaMin;
	}
	
	/**
	 * Vraci obrazek "mapu"
	 * @return obrazek
	 */
	public BufferedImage getObrazek() {
		return obrazek;
	}
	
	/**
	 * Nastavuje po kolika metrech se budou vykreslovat vrstevnice
	 * @param metry po kolika metrech se budou vykreslovat vrstevnice
	 */
	public void setPoKolika(int metry) {
		this.poKolika = metry;
		zviraznenaVrstevnice = 0;
		klikX = -1;
		klikY = -1;
		createPoleVrstevnic();
		repaint();
	}
}
