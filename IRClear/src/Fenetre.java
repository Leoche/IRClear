//==================================================
//	CODE SOURCE:
//		Nom: IRClear
//		Fichier: Fenetre.java
//		Description: Classe principale du client IRC (Mise en oeuvre du GUI + main)
//		Auteur: Léo
//		Site: http://leoche.org
//==================================================

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URISyntaxException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class Fenetre extends JFrame implements WindowFocusListener{

//	VARIABLES GLOBALES
	
	// UID de la classe (Useless)
	private static final long serialVersionUID = 5494313552024023513L;
	
	//Variables de la Fenêtre
	private int W = 0;
	private int H = 0;
	private boolean focused = true;
	
	//Variables des composants de l'interface
	private JPanel pan;
	private JLabel logo_label;
	private Input serveur_input;
	private Input salon_input;
	private Input pseudo_input;
	private Boutton connexion_input;
	private JLabel statut_label;
	private JLabel loader_label;
	private JEditorPane console;
	private HTMLEditorKit kit;
	private HTMLDocument doc;
	private Input message_input;
	private Boutton envoyer_input;
	
	//Variables de données pour la connexion
	private String serveur;
	private String salon;
	private String pseudo;
	
	//Variables de connexions
	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;
	private boolean connected = false;
	
	//Variable du Thread secondaire
	private Thread connexionThread;
	
	public Fenetre(String titre, int width, int height) throws IOException, UnsupportedAudioFileException, LineUnavailableException, BadLocationException{

		
		//	CONFIGURATION DE LA FENETRE ET DU JPANEL CONTENEUR PRINCIPAL
		
		// Configuration de la Fenêtre principale
		W = width;
		H = height;
	    this.setTitle(titre);
	    this.setLocationRelativeTo(null);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setResizable(false);
	    this.setVisible(true);
	    this.addWindowFocusListener(this);
	    this.setIconImage(new ImageIcon(this.getClass().getResource("icone.png")).getImage());
	    
	    // Création & initialisation du JPanel principal
	    pan = new JPanel();
	    pan.setLayout(null);
	    pan.setPreferredSize(new Dimension(W,H));
	    pan.setBackground(new Color(240,240,240));
	    
	    
		//	MISE EN PLACE DE TOUTE L'INTERFACE
	    
	    // # Création et placement du logo IRClear
	    logo_label = new JLabel(new ImageIcon(Fenetre.class.getResource("logo.png")));
	    logo_label.setBounds(20, 20, 361, 51);
	    pan.add(logo_label);

	    
	    // # Création et placement du label et de l'input de l'url du serveur
	    JLabel serveur_label = new JLabel("Serveur");
	    serveur_label.setBounds(20, 100, 50, 20);
	    pan.add(serveur_label);
	    
	    serveur_input = new Input(70, 99, 205, 23,"irc.serveur.com");
	    pan.add(serveur_input);

	    
	    // # Création et placement du label et de l'input du salon
	    JLabel salon_label = new JLabel("#");
	    salon_label.setBounds(290, 100, 10, 20);
	    pan.add(salon_label);
	    
	    salon_input = new Input(300, 99, 80, 23,"salon");
	    pan.add(salon_input);

	    
	    // # Création et placement du label et de l'input du pseudo
	    JLabel pseudo_label = new JLabel("Pseudonyme");
	    pseudo_label.setBounds(20, 130, 80, 20);
	    pan.add(pseudo_label);
	    
	    pseudo_input = new Input(100, 129, 175, 23,"n00b_1337");
	    pan.add(pseudo_input);

	    // # Création et placement du bouton de Connexion/Déconnexion
	    connexion_input = new Boutton("Se Connecter", 285, 128, 95, 25);
	    connexion_input.addActionListener(new ActionListener(){ //Écouteur de cliques
	    	public void actionPerformed(ActionEvent event){
	    		if(connexion_input.getActive()) return; //On arrête tout si le bouton est déjà activé.
	    		connexion_input.setActive();
	    		if(!connected){ //Si pas connecté, On affiche le loader et on valide les données entrées.
	    			loader_label.setVisible(true);
		    		statut("Validation...");
					try {validateData(serveur_input.getText(),"#"+salon_input.getText(),pseudo_input.getText());} 
					catch (BadLocationException e1) {e1.printStackTrace();}
					catch (IOException e) {e.printStackTrace();} 
	    		}else{ //Si on est déjà connecté, On déconnecte.
	    			try{deconnexion();}
					catch (IOException e) {e.printStackTrace();} 
	    		}
	    	}
	    });
	    pan.add(connexion_input);

	    
	    // # Création et placement du label de status
	    JLabel statut_label_title = new JLabel("Statut:");
	    statut_label_title.setBounds(20, 158, 40, 25);
	    pan.add(statut_label_title);
	    
	    statut_label = new JLabel("Déconnecté");
	    statut_label.setForeground(new Color(120,120,120));
	    statut_label.setBounds(60, 158, 304, 25);
	    pan.add(statut_label);

	    
	    // # Création et placement de l'image du loader
	    loader_label = new JLabel(new ImageIcon(Fenetre.class.getResource("loader.gif")));
	    loader_label.setBounds(364, 164, 16, 14);
	    pan.add(loader_label);
	    loader_label.setVisible(false);

	    
	    // # Création, placement et configuration de la zone de tchat
	    console = new JEditorPane("text/html","");
	    console.setEditable(false);
		kit = new HTMLEditorKit();
		doc = new HTMLDocument();
		console.setEditorKit(kit);
		console.setDocument(doc);
		console.addHyperlinkListener(new HyperlinkListener() { //Écouteur de cliques sur les liens
		    public void hyperlinkUpdate(HyperlinkEvent e) {
		        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		        	if(Desktop.isDesktopSupported()) {
		        	    try { Desktop.getDesktop().browse(e.getURL().toURI());} //On essaye d'ouvrir le lien dans le navigateur par défaut
		        	    catch (IOException e1) {e1.printStackTrace();} 
		        	    catch (URISyntaxException e1) {e1.printStackTrace();}
		        	}
		        }
		    }
		});
		//On ajoute alors la zone dans un JScrollPane pour pouvoir scroller le contenu.
	    JScrollPane scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    scrollPane.setBounds(20, 180, 360, 260);
	    scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY)); //On rajoute une bordure.
	    scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, Integer.MAX_VALUE)); //On redimensionne la barre de Scroll
	    scrollPane.getVerticalScrollBar().setUI(new Scroll()); //On remplace l'UI pour redessiner la barre de scroll
	    pan.add(scrollPane);
	    

	    // # Création et placement de l'input d'envoi de messages
	    message_input = new Input(20, 449, 275, 33,"Message...");
	    message_input.setEnabled(false);
	    message_input.addActionListener(new ActionListener(){ //Écouteur de la touche ENTER
	    	public void actionPerformed(ActionEvent event){
	    		if(connected){ //Si connecté, on tente l'envoi
					try {send();} 
					catch (IOException e) {e.printStackTrace();} 
					catch (BadLocationException e) {e.printStackTrace();}
	    		}
	    	}
	    });
	    pan.add(message_input);
	    

	    // # Création et placement du bouton Envoi des messages
	    envoyer_input = new Boutton("Envoyer", 305, 448, 75, 35);
	    envoyer_input.setActive();
	    envoyer_input.addActionListener(new ActionListener(){ //Écouteur de cliques
	    	public void actionPerformed(ActionEvent event){
	    		if(envoyer_input.getActive()) return;
	    		if(connected){  //Si connecté, on tente l'envoi
					try {send();} 
					catch (IOException e) {e.printStackTrace();} 
					catch (BadLocationException e) {e.printStackTrace();}
	    		}
	    	}
	    });
	    pan.add(envoyer_input);
	    

	    // # Création et placement du footer
	    JLabel leoche = new JLabel("Leoche.org - "+titre);
	    leoche.setForeground(new Color(180,180,180));
	    leoche.setBounds(20, 490, 180, 20);
	    pan.add(leoche);


	    // # On Pack le tout
	    this.setContentPane(pan);
	    pack();
	    this.setLocationRelativeTo(null);
	}
	
	// # Actualise focused quand la fenêtre gagne le focus
	public void windowGainedFocus(WindowEvent e){focused = true;} 

	// # Actualise focused quand la fenêtre gagne le focus
	public void windowLostFocus(WindowEvent e){focused = false;} 
	
	public void validateData(String s, String c, String p) throws IOException, BadLocationException{
		
		//	VALIDATION DES DONNEES
		
		String pseudopattern = "[A-z0-9\\-_|]+";
		String salonpattern = "#[A-z0-9\\-_|]+";
		String serveurpattern = "[A-z0-9\\.\\-_]+";
		String error = "";
		if(s.equals("irc.serveur.com")) error = "Veuillez entrer un serveur.";
		if(error.equals("")&&!s.matches(serveurpattern)) error = "Votre serveur contient des charactères interdits.";
		if(error.equals("")&&c.equals("#salon")) error = "Veuillez entrer un salon.";
		if(error.equals("")&&!c.matches(salonpattern)) error = "Votre salon contient des charactères interdits.";
		if(error.equals("")&&p.equals("n00b_1337")) error = "Veuillez entrer un pseudonyme.";
		if(error.equals("")&&p.length()<3) error = "Votre pseudonyme est trop court.";
		if(error.equals("")&&p.length()>11) error = "Votre pseudonyme est trop long.";
		if(error.equals("")&&!p.matches(pseudopattern)) error = "Votre pseudonyme contient des charactères interdits.";
		if(error.equals("")){ //Si aucune erreur, on remplit les variables et on tente la connexion.
			serveur=s; salon=c; pseudo=p;
			connexion();
		}else{ //Sinon on enleve le loader et on affiche l'erreur.
		    loader_label.setVisible(false);
			statut("Déconnecté");
			connexion_input.unsetActive();
			connexion_input.repaint();
			JOptionPane.showMessageDialog(null, error, "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	// # Change le statut
	public void statut(String s){statut_label.setText(s);}
	
	// # Ajoute une ligne HTML
	public void log(String s) throws BadLocationException, IOException{
		kit.insertHTML(doc, doc.getLength(), s, 0, 0, null);
		console.setCaretPosition(console.getDocument().getLength()); 
	}

	// # Crée un nouveau thread de connexion
	public void connexion() throws IOException, BadLocationException{
		statut("Connexion au serveur");
		Thread connexionThread = new Thread(new ConnexionThread());
		connexionThread.start();
	}

	// # Arrête le thread de connexion et réactualise l'interface
	public void deconnexion() throws IOException{
		connected = false;
		try {
	        socket.close();
		}catch(IOException e){}
		try{connexionThread.interrupt();}catch(NullPointerException e){}
		connexion_input.unsetActive();
		statut("Déconnecté");
		connexion_input.setText("Se Connecter");
		connexion_input.repaint();
		enableConnectionform();
	    loader_label.setVisible(false);
	}

	// # Désactive tout le formulaire de connexion
	public void disableConnectionform(){
		serveur_input.setEnabled(false);
		salon_input.setEnabled(false);
		pseudo_input.setEnabled(false);
	}
	
	// # Active tout le formulaire de connexion et désactive le formulaire pour tchater
	public void enableConnectionform(){
		serveur_input.setEnabled(true);
		salon_input.setEnabled(true);
		pseudo_input.setEnabled(true);
		message_input.setEnabled(false);
		envoyer_input.setActive();
		envoyer_input.repaint();
	}
	
	// # Joue le son de notification
	public void bip() throws IOException, UnsupportedAudioFileException, LineUnavailableException{
		try{
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(Fenetre.class.getResource("bip.wav"));
			Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.start();
		}
		catch(IOException e){}
		catch(UnsupportedAudioFileException e){}
		catch(LineUnavailableException e){}
	}

	// # Tente d'envoyer le message si valide et l'ajoute à la zone de tchat
	public void send() throws IOException, BadLocationException{
		if(message_input.getText()=="Message...") return;
		String message = new String(message_input.getText().getBytes("UTF-8"),"ISO-8859-15");
		writer.write("PRIVMSG " + salon + " :"+message+"\r\n");
		writer.flush();
		log("<span style='font-weight:bold;color:#6B686F'>"+pseudo+"</span>: "+convertLinks(message_input.getText())+"\n");
		message_input.setText("");
		message_input.requestFocus();
	}

	// # Trouve les liens et les remplace par des liens formattés en html
	public String convertLinks(String s){
		return s.replaceAll("((?:(?:(?:http|https|ftp|ftps):\\/\\/)|(?:www.))[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(?:\\/\\S*)?)","<a style='text-decoration:underline;color:#7AB43C' href='$1'>$1</a>").replaceAll("href='www","href='http://www");
	}

	// # Thread secondaire
	class ConnexionThread implements Runnable{
	    public void run() {
			try {
				// # Tentative de connexion au serveur
				socket = new Socket(serveur, 6667);
				writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream( )));
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream( )));
				connected = true;
				connexion_input.unsetActive();
				connexion_input.setText("Déconnexion");
				connexion_input.repaint();
				disableConnectionform();


				// # Tentative d'authentification
				statut("Authentification");
		        writer.write("NICK " + pseudo + "\r\n");
		        writer.write("USER " + pseudo + " 8 * : IRCleaner Client\r\n");
		        writer.flush( );
		        String line = null;
		        // # Tant qu'on recoit des réponses serveur
		        while (!Thread.interrupted() && (line = reader.readLine( )) != null) {
		            System.out.println(line);
		        	if (line.indexOf("004") >= 0) { // Si la ligne contient 004, On arrête tout c'est ok, on est connecté.
		                break;
		            } else if (line.indexOf("433") >= 0) { // Si la ligne contient 433, ce pseudo est pris, on déconnecte tout.
		            	log("<span style='color:#CF3A3A'>Le pseudo "+pseudo+" est déjà pris\n</span>");
		            	deconnexion();
		            	return;
		            } else if (line.toLowerCase( ).startsWith("ping ")) { // Si on se fait pingé, on répond immédiatement.
		                writer.write("PONG " + line.substring(5) + "\r\n");
		                writer.flush( );
		                System.out.println("Got pinged: #"+line.substring(5));
		            }
		        }
		        
		        // # On est connecté et identifié, on tente d'accéder au salon.
				statut("Accès au salon "+salon);
		        writer.write("JOIN " + salon + "\r\n");
		        writer.flush();
		        statut("Connecté à "+salon);

		        // # On est dans le salon, on active les formulaires pour tchatter.
				message_input.setEnabled(true);
				envoyer_input.unsetActive();
				envoyer_input.repaint();
			    loader_label.setVisible(false);
			    
		        // # Tant qu'on recoit des réponses serveur
		        while (!Thread.interrupted() && (line = reader.readLine( )) != null) {
		            if (line.toLowerCase( ).startsWith("ping ")) { 
		            // Si on se fait pingé, on répond.
		                writer.write("PONG " + line.substring(5) + "\r\n");
		                writer.flush( );
		                System.out.println("Pinged: #"+line.substring(5));
		            }else{ 
		            // Sinon on analyse les réponses
		            	System.out.println(line);
		            	if(line.contains("PRIVMSG")&&line.matches("^:.*?!.*? PRIVMSG #.*? :.*$")){ //Si c'est une phrase de tchat basique
		            		
		            		//On converti la ligne en utf8 et on escape l'Html
		            		String utfline = new String ( line.getBytes(), "UTF-8" );
		            		String pseudo = utfline.replaceAll("^:(.*?)!.*?:.*$","$1");
		            		String message = convertLinks(utfline.replaceAll("^:.*?!.*?:(.*)$","$1").replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;"));
		            		
		            		log("<span style='font-weight:bold;color:#7AB43C'>"+pseudo+"</span>: "+message+"\n");
		            		if(!focused) // Si la fenêtre n'a plus le focus on joue le son de la notification.
		            			try {bip();}
		            			catch (UnsupportedAudioFileException e){e.printStackTrace();} 
		            			catch (LineUnavailableException e){e.printStackTrace();}
		            	}
		            	else if(line.contains("JOIN")){ //Si c'est l'arrivé de quelqu'un sur le tchat
		            		String message = new String ( line.getBytes(), "UTF-8" ).replaceAll("^:(.*?)!.*$", "<span style='color:#CCCCCC'>$1 a rejoin "+salon+"</span>\n");
		            		log(message);
		            	}
		            	else if(line.contains("PART") || line.contains("QUIT")){ //Si c'est le départ de quelqu'un sur le tchat
		            		String message = new String ( line.getBytes(), "UTF-8" ).replaceAll("^:(.*?)!.*$", "<span style='color:#CCCCCC'>$1 a quitté "+salon+"</span>\n");
		            		log(message);
		            	}
		            }
		        }
			}
			catch(IOException e){ 
				connexion_input.unsetActive(); connexion_input.repaint(); 
				if(!connected){statut("Déconnecté");}
				connected = false;
			} 
			catch (BadLocationException e1) {e1.printStackTrace();}
	    }          
	}

}
