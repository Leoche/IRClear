//==================================================
//CODE SOURCE:
//	Nom: IRClear
//	Fichier: Boutton.java
//	Description: JButton personnalisé
//	Auteur: Léo
//	Site: http://leoche.org
//==================================================

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

public class Boutton extends JButton implements MouseListener {

	private static final long serialVersionUID = -5178722747767990129L;
	private int W;
	private int H;
	private int X;
	private int Y;
	private String texte;
	private boolean hovered=false;
	private boolean actived=false;
	private boolean active=false;
	public Boutton(String txt, int px, int py, int width, int height){
		W = width;
		H = height;	
		X = px;
		Y = py;
		texte = txt;
		this.setBounds(X,Y,W,H);
		this.setBorder(null);
		this.setFocusPainted(false);
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(this);
	}
	
	//Évenements hover
	public void mouseEntered(MouseEvent e){hovered=true;} 
	public void mouseExited(MouseEvent e){hovered=false;}
	
	//Évenements click
	public void mousePressed(MouseEvent e){actived=true;} 
	public void mouseReleased(MouseEvent e){actived=false;}
	public void mouseClicked(MouseEvent e){}
	
	//Fonctions pour changer l'état du bouton
	public void setActive(){ active=true;}
	public void unsetActive(){ active=false;actived=false;}
	public boolean getActive(){ if(active) return true; return false;}
	
	//Fonctions pour changer le texte du bouton
	public void setText(String txt){ texte = txt; }
	
	public void paintComponent(Graphics g){
		if(active&&!actived) actived=active;
		g.clearRect(0, 0, W, H);
	    Graphics2D g2d = (Graphics2D)g;   
	    if(!actived){g2d.setColor(new Color(0,0,0,40));g2d.fillRoundRect(0, 0, W, H, 3, 3);}
	    GradientPaint gp;
	    if(hovered && !actived)
	    	gp = new GradientPaint(0, 0, new Color(255,255,255), 0, H, new Color(240,240,240), false);                
	    else if(this.actived&&this.active)
	    	gp = new GradientPaint(0, 0, new Color(220,220,220), 0, 10, new Color(220,220,220), false);               
	    else if(this.actived)
	    	gp = new GradientPaint(0, 0, new Color(200,200,200), 0, 10, new Color(220,220,220), false);                
	    else
	    	gp = new GradientPaint(0, 0, new Color(250,250,250), 0, H, new Color(220,220,220), false);                

	    g2d.setPaint(gp);
	    g2d.fillRoundRect(0, 0, W-1, H-2, 3, 3);
	    g2d.setColor(Color.GRAY);
	    g2d.drawRoundRect(0, 0, W-1, H-2, 3, 3);
	    FontMetrics fm = g2d.getFontMetrics();
	    if(!actived){
		    g2d.setColor(Color.WHITE);
		    g2d.drawString(texte, (W- fm.stringWidth(texte))/2, H/2+5);
		    g2d.setColor(new Color(100,100,100));
		    g2d.drawString(texte, (W- fm.stringWidth(texte))/2, H/2+4);
	    } 
	    else g2d.drawString(texte, (W- fm.stringWidth(texte))/2, H/2+5);
	}
}
