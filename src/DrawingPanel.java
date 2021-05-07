import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;

import javax.swing.JPanel;

/**
 * Trida {@code DrawingPanel} kresli barevnou mapu s vrstevnicemi,
 * mapa reaguje na kliknuti leveho tlacitka mysi, v miste kliknuti zobtazi bod a jeho nadmorskou vysku
 * @author Lukas Runt 
 * @version 2.0 (2021-04-27)
 */
@SuppressWarnings("serial")
public class DrawingPanel extends JPanel implements Printable{

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
	private int velikostOkrajeX = 0;
	private int velikostOkrajeY = 0;
	private double aktualVelikostObrX = 0;
	private double aktualVelikostObrY = 0;
	private double klikX = -1;
	private double klikY = -1;
	private int nadmorskaVyska;
	private boolean[] pravdivostniTabulka;
	private boolean[][] pravdivostniTabulka2D;
	private int hodnotaMin;
	private int hodnotaMax;
	private double pomerStran;
	private int[][] pole2D;
	private int[] vrstevnice;
	private int zviraznenaVrstevnice = 0;
	private double konst;
	private int vyskaPanelu;
	private int sirkaPanelu;
	private int kolikrat;
	private boolean metoda;
	
	/**
	 * Urcuje pocetecni velikost okna, tak aby mela sirku alespon 800px a alespon vysku 600px,
	 * pritom zachovava pomer stran
	 */
	public DrawingPanel() {
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
		createPoleVrstevnic();
		vyskaPanelu = this.getHeight();
		sirkaPanelu = this.getWidth();
		if(Mapa_SP2021.sirka < 10000 && Mapa_SP2021.vyska < 10000) {
			metoda = true;
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
				if(e.getX() > velikostOkrajeX && e.getX() < velikostOkrajeX + Mapa_SP2021.sirka * scale) {
					if(e.getY() > velikostOkrajeY && e.getY() < velikostOkrajeY + Mapa_SP2021.vyska * scale) {
						klikX = (e.getX() - velikostOkrajeX) / scale;
						klikY = (e.getY() - velikostOkrajeY)/ scale;
						//int nadmorskaVyska = Mapa_SP2021.data[(int)((e.getX() - velikostOkrajeX) / scale  + ((((e.getY() - velikostOkrajeY)/ scale) * Mapa_SP2021.sirka)))];
						nadmorskaVyska = pole2D[(int)((e.getX() - velikostOkrajeX) / scale)][(int)((e.getY() - velikostOkrajeY)/ scale)];	
						zviraznenaVrstevnice = najdiNejblizsi(nadmorskaVyska);
						repaint();
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
		g2.setColor(Color.WHITE);
		drawVsechnyVrstevnice(g2);
		drawZviraznenaVrstevnice(g2);
		drawBod(g2);
		
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
	private void createPoleVrstevnic() {
		ArrayList<Integer> vrstevniceList = new ArrayList<Integer>();
		kolikrat = 1;
		if(hodnotaMax - hodnotaMin > 1000) {
			kolikrat = 10;
		}
		if(hodnotaMax - hodnotaMin > 10000) {
			kolikrat = 100;
		}
		int a = hodnotaMin;
		while(a <= hodnotaMax) {
			if(a % (50 * kolikrat) == 0 && a != 0) {
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
	
	private void drawBod(Graphics2D g2) {
		if(klikX >= 0 && klikY >= 0) {
			Ellipse2D bod = new Ellipse2D.Double(klikX * scale + velikostOkrajeX, klikY * scale + velikostOkrajeY, 5, 5);
			/*g2.setColor(Color.RED);
			g2.fill(bod);*/
			g2.setColor(Color.BLACK);
			g2.fill(bod);
			g2.setFont(new Font("Calibri", Font.BOLD, VELIKOST_TEXTU));
			FontMetrics font = g2.getFontMetrics();
			if((int)(klikY * scale + velikostOkrajeY) > VELIKOST_TEXTU + 10) {
				g2.drawString(nadmorskaVyska + "",(int)(klikX * scale + velikostOkrajeX - font.stringWidth(nadmorskaVyska + "")/2), (int)(klikY * scale + velikostOkrajeY));
			} else {
				g2.drawString(nadmorskaVyska + "",(int)(klikX * scale + velikostOkrajeX - font.stringWidth(nadmorskaVyska + "")/2), (int)(klikY * scale + velikostOkrajeY + VELIKOST_TEXTU));
			}
			/*g2.setColor(Color.RED);
			g2.setFont(new Font("Calibri", Font.PLAIN, VELIKOST_TEXTU));
			//FontMetrics font = g2.getFontMetrics();
			if((int)(klikY * scale + velikostOkrajeY) > VELIKOST_TEXTU + 10) {
				g2.drawString(nadmorskaVyska + "",(int)(klikX * scale + velikostOkrajeX - font.stringWidth(nadmorskaVyska + "")/2), (int)(klikY * scale + velikostOkrajeY));
			} else {
				g2.drawString(nadmorskaVyska + "",(int)(klikX * scale + velikostOkrajeX - font.stringWidth(nadmorskaVyska + "")/2), (int)(klikY * scale + velikostOkrajeY + VELIKOST_TEXTU));
			}*/
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
		velikostOkrajeY = (int)(this.getHeight()-aktualVelikostObrY)/2;
		velikostOkrajeX = (int)(this.getWidth()-aktualVelikostObrX)/2;
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
	 * 
	 * @param nadmorskaVyska vyska se kterou se budou porovnavat ostatni vysky
	 * @return dvojrozmerne pravdivostni pole
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
	
	private boolean[][] vetsiNez2D(int nadmorskaVyska){
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
	 * Metoda nakresli vrstevnice
	 * @param g2 grafika
	 * @param porovnani madmorska vyska
	 */
	private void drawVrstevnice(Graphics2D g2, int porovnani) {
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
			if(Math.abs(nadmorskaVyska - vrstevnice[i]) <= 25 * kolikrat) {
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
		
		final double X_MM = 200;
		final double Y_MM = 200/pomerStran;
		final double mmToPt = 72 / 25.4;
		double PosunutiY = (297 - Y_MM) * mmToPt; 
		g2.translate(10 * mmToPt, PosunutiY/2);
		
		drawPicture(g2, (int)(X_MM * mmToPt), (int)(Y_MM * mmToPt));
		return 0;
	}
	
	public void drawSVG(Graphics2D g2) {
		g2.setColor(Color.GRAY);
		drawVsechnyVrstevnice(g2);
		drawZviraznenaVrstevnice(g2);
		drawBod(g2);
		drawArrow(maximum % Mapa_SP2021.sirka, (int)(maximum / Mapa_SP2021.sirka), "Max. prevyseni", g2);
		drawArrow(minimum % Mapa_SP2021.sirka, (int)(minimum / Mapa_SP2021.sirka), "Min. prevyseni", g2);
		drawArrow(maxStoupani % Mapa_SP2021.sirka, (int)(maxStoupani / Mapa_SP2021.sirka), "Max. stoupani", g2);
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
	
	public BufferedImage getObrazek() {
		return obrazek;
	}
}
