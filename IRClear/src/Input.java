//==================================================
//CODE SOURCE:
//	Nom: IRClear
//	Fichier: Input.java
//	Description: JTextField personnalisé
//	Auteur: Léo
//	Site: http://leoche.org
//==================================================

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class Input extends JTextField implements FocusListener{
	
	private static final long serialVersionUID = -4042389047198802041L;
	private String placeholder;
	
	public Input(int x, int y, int w, int h, String p){
		placeholder=p;
		this.setBounds(x,y,w,h);
		this.setText(placeholder);
		this.setForeground(Color.GRAY);
		this.addFocusListener(this);
		this.setDisabledTextColor(new Color(220,220,220));
		Border line = BorderFactory.createLineBorder(Color.GRAY);
		Border empty = new EmptyBorder(0, 5, 0, 5);
		CompoundBorder border = new CompoundBorder(line, empty);
		this.setBorder(border);

	}
	public void focusGained(FocusEvent e){
		if(this.getText().equals(placeholder)){
			this.setForeground(Color.BLACK);
			this.setText("");
		}
	}
	public void focusLost(FocusEvent e){
		if(this.getText().equals("")){
			this.setForeground(Color.GRAY);
			this.setText(placeholder);
		}
	}
}
