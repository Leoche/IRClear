//==================================================
//CODE SOURCE:
//	Nom: IRClear
//	Fichier: Scroll.java
//	Description: Barre de scroll personnalisé
//	Auteur: Léo
//	Site: http://leoche.org
//==================================================

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class Scroll extends BasicScrollBarUI {

    @Override //Repaint le fond du scroll
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    	g.setColor(Color.WHITE);
        g.fillRect(trackBounds.x,trackBounds.y,trackBounds.width,trackBounds.height);
    }

    @Override //Repaint la barre de scroll
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    	Graphics2D g2d = (Graphics2D)g;   
    	//Si la souris est dessus, on met la couleur plus foncé
    	if(this.isThumbRollover()) g2d.setColor(new Color(210,210,210));
    	else g2d.setColor(new Color(230,230,230));
    	//On active l'anti-aliasing pour un rendu net de la berre de scroll
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.fillRoundRect(thumbBounds.x+2, thumbBounds.y+2, thumbBounds.width-4, thumbBounds.height-4, 6, 6);
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createInvisibleButton(); // On enleve les fleches haut du scroll en créer un bouton invisible
    }
    
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createInvisibleButton(); // On enleve les fleches bas du scroll en créer un bouton invisible
    }
    
    protected JButton createInvisibleButton() {
        JButton button = new JButton("createInvisibleButton");
        Dimension zeroDim = new Dimension(0,0);// On met ses dimensions à zéro
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }
}