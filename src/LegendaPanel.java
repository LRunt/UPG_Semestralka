import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LegendaPanel extends JPanel{
	private final int VELIKOST_TEXTU = 20;
	private int pocetCtvercu;
	private int[] hodnoty;
	private double konst;
	int velikostCtverce;
	
	public LegendaPanel() {
		this.setPreferredSize(new Dimension(Mapa_SP2021.panel.getWidth(), 100));
		this.setFocusable(false);
		}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		//this.setSize(Mapa_SP2021.panel.getWidth(), 100);
		Graphics2D g2 = (Graphics2D)g;
		
			hodnoty = createHodnoty();
			pocetCtvercu = hodnoty.length;
			velikostCtverce = this.getWidth()/(2*(pocetCtvercu + 1) + pocetCtvercu);
			konst = zjistiKonst();
			
			//this.setSize(this.getWidth(), 40 + velikostCtverce + 30);
		
			drawLegenda(g2);
		
	}
	
	/**
	 * Metoda nakresli legendu ve spodni casti obrazovky
	 * @param g2 grafika
	 */
	private void drawLegenda(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for(int i = 0; i < hodnoty.length; i++) {
			drawCtverecLegendy(g2, i);
		}
	}
	
	/**
	 * Metoda kresli jeden ctverecek legendy
	 * @param g2 grafika
	 * @param i index nadmorske vysky v poli
	 */
	private void drawCtverecLegendy(Graphics2D g2, int i) {
		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Calibri", Font.BOLD, VELIKOST_TEXTU));
		FontMetrics font = g2.getFontMetrics();
		g2.drawString("Legenda:", 10, 20);
		g2.setColor(new Color (Mapa_SP2021.panel.getPaleta()[(int)(hodnoty[i] / konst)]));
		g2.fillRect(2*velikostCtverce + 2*i*velikostCtverce + i*velikostCtverce, 30, velikostCtverce, velikostCtverce);
		g2.setColor(Color.BLACK);
		String text = hodnoty[i] + "";
		g2.drawString(text, 2*velikostCtverce + 2*i*velikostCtverce + i*velikostCtverce + velikostCtverce/2 - font.stringWidth(text)/2, 30 + velikostCtverce + VELIKOST_TEXTU);
	}
	
	/**
	 * Metoda vytvori pole nadmorskych vysek po 50, ktere budou v legende
	 * @return pole nadmorskych vysek
	 */
	private int[] createHodnoty() {
		if (Mapa_SP2021.kontrast <= 255) {
			int[] hodnoty = {0, 50, 100, 150, 200, 250, 255};
			return hodnoty;
		}
		//pomerne osklivy usek kodu, ale funguje a co funguje to se nemeni!
		else {
			int pocet = 0;
			int a = Mapa_SP2021.panel.getMinimum();
			int b = Mapa_SP2021.panel.getMaximum();
			while(a <= b) {
				if(a % 50 == 0) {
					pocet++;
				}
				a++;
			}
			int[] hodnoty = new int[pocet];
			a = Mapa_SP2021.panel.getMinimum();
			pocet = 0;
			while(a <= b) {
				if(a % 50 == 0) {
					hodnoty[pocet] = a;
					pocet++;
				}
				a++;
			}
			return hodnoty;
		}
	}
	
	/**
	 * Metoda zjisti konstantu, kterou se upravovaly hodnoty v mape
	 * @return konstanta
	 */
	private double zjistiKonst() {
		if (hodnoty[hodnoty.length - 1] <= 255) {
			return 1;
		}
		else {
			double konst = 1;
			int kontrastPom = Mapa_SP2021.kontrast;
			while(kontrastPom > 255) {  
				konst += 0.8;
				kontrastPom = (int)(Mapa_SP2021.kontrast / konst);
			}
			return konst;
		}
	}
}
