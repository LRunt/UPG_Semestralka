import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class DrawingPanel extends JPanel {

	private final double POZADOVANY_POMER = (double)4/3; //optimalni velikost 800*600 -> pomer 4:3
	private int maximum;
	private int minimum;
	private int maxStoupani;
	private double scale;
	private int startX;
	private int startY;
	private final double DELKA_SIPKY = 100;
	private final double DELKA_HROTU = 0.2 * DELKA_SIPKY;
	private final int VELIKOST_TEXTU = 20;
	private BufferedImage obrazek = new BufferedImage(Mapa_SP2021.sirka, Mapa_SP2021.vyska, BufferedImage.TYPE_3BYTE_BGR);
	private int[] paleta;
	
	/**
	 * Urcuje pocetecni velikost okna, tak aby mela sirku alespon 800px a alespon vysku 600px,
	 * pritom zachovava pomer stran
	 */
	public DrawingPanel() {
		int Width = 800;
		int Height = 600;
		double pomerStran = (double)Mapa_SP2021.sirka/Mapa_SP2021.vyska;
		//System.out.println("Pomer Stran: " + pomerStran);
		//tato podminka zaruci otevreni okna v co nejlepsi velikosti
		if(pomerStran < POZADOVANY_POMER) {
			if(pomerStran > 1) {
				Height = (int)(Width / pomerStran);
			}else if (pomerStran < 0.5){
				Width = (int)(Height * pomerStran);
			}else {
				Height = (int)(Width * pomerStran);
			}
		} else if (pomerStran < 2){
			Width = (int)(Height * pomerStran);
		} else {
			Height = (int)(Width / pomerStran);
		}
		this.setPreferredSize(new Dimension(Width, Height));
		makePalette(10, 220);
	}
	
	/**
	 * Metoda meni paletu barev
	 * @param min
	 * @param max
	 */
	private void makePalette(int min, int max) {
		paleta = new int [265];
		int r;
		int g;
		int b;
		for(int i = 0; i < paleta.length;i++) {
			if (i <= min) {
				r = 0;
				b = 255 - i;
				g = 255 - i/3;
			}
			else if (i >= max) {
				r = 200-i/4;
				b = 0;
				g = 200-(int)(i/2.5);
			}
			else {
				r =(int)(i/1.5);
				b = Math.max(255 - i*2, 0);
				g = 255 -(int)(i/1.5);
			}
			paleta[i] = (r << 16) | (g << 8) | (b << 0);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		
		maximum = getMax(Mapa_SP2021.data);
		minimum = getMin(Mapa_SP2021.data);
		maxStoupani = getMaxStoupani(Mapa_SP2021.data);
		
		createPicture(Mapa_SP2021.data);
		drawPicture(g2, this.getWidth(), this.getHeight());
		
		//System.out.println("Sirka: " + this.getWidth());
		//System.out.println("Vyska: " + this.getHeight());
		
		drawArrow(maximum % Mapa_SP2021.sirka, (int)(maximum / Mapa_SP2021.sirka), "Max. prevyseni", g2);
		drawArrow(minimum % Mapa_SP2021.sirka, (int)(minimum / Mapa_SP2021.sirka), "Min. prevyseni", g2);
		drawArrow(maxStoupani % Mapa_SP2021.sirka, (int)(maxStoupani / Mapa_SP2021.sirka), "Max. stoupani", g2);
		
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
		double konst = 1;
		int kontrastPom = Mapa_SP2021.kontrast;
		while(kontrastPom > 255) {  
			konst += 0.1; //pricitam 0.1, kvuli co nejmensim krokum 
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
	private void drawPicture(Graphics2D g2, int W, int H) {
		//Cerny obdelnik v pozadi
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, W, H);
		
		
		int iW = obrazek.getWidth();
		int iH = obrazek.getHeight();
		double scaleX = ((double)W) / iW; //Kolikrat se obrazek zvetsuje v ose X 
		double scaleY = ((double)H) / iH; //Kolikrat se obrazek zvetsuje v ose Y
		scale = Math.min(scaleX, scaleY);
		
		
		
		int niW = (int)(iW * scale); //nova sirka obrazku
		int niH = (int)(iH * scale); //nova vyska obrazku
		startX = (W - niW) / 2;//zacatek obrazku X
		startY = (H - niH) / 2;//zacatek obrazku Y
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); //nastaveni bilinearni interpolace
				
		g2.drawImage(obrazek, startX, startY, niW, niH, null);
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
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
		g2.setFont(new Font("Calibri", Font.BOLD, VELIKOST_TEXTU));
		FontMetrics font = g2.getFontMetrics();
		
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
		if(u_y < 0) {
			if(textVObraze(u_y, y1) == true) {
				g2.drawString(text,(int)(x2 - font.stringWidth(text)/2), (int)(y2 + 15));
			}
			else {
				g2.drawString(text,(int)(x2 - font.stringWidth(text)/2), this.getHeight() - 2);
			}
		} else {
			if(textVObraze(u_y, y1) == true) {
				g2.drawString(text,(int)(x2 - font.stringWidth(text)/2), (int)(y2 - 5));
			}
			else {
				g2.drawString(text,(int)(x2 - font.stringWidth(text)/2), VELIKOST_TEXTU);
			}
		} 
	}
	
	/**
	 * Metoda zjistuje zda se text vejde do obrazu v ose y
	 * @param u_y vektor y
	 * @param y pozice na ose y
	 * @return true - text se cely vejde do obrazu, false - sipka se nevejde do obrazu
	 */
	private boolean textVObraze(double u_y, double y) {
		if(u_y > 0) {
			if(this.getHeight() > (u_y * DELKA_SIPKY + VELIKOST_TEXTU + (this.getHeight() - y))) {
				return true;
			}
		}
		else {
			if(this.getHeight() > (Math.abs(u_y) * DELKA_SIPKY + VELIKOST_TEXTU + y)){
				return true;
			}
		}
		return false;
	}
}
