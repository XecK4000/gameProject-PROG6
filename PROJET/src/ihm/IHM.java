package ihm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import modele.Parametres;
import modele.Parametres.NiveauJoueur;
import reseau.Communication;
import reseau.Echange;

@SuppressWarnings("serial")
/**
 * Classe globale de la fenêtre.
 */
public class IHM extends JFrame implements ComponentListener {

	public Communication com;
	/**
	 * Mini-fenêtre à afficher au lancement du jeu.
	 */
	FenetreChargement fenetreChargement;
	/**
	 * Theme de la fenêtre.
	 */
	Theme theme;
	/**
	 * Couche de base.
	 */
	JPanel coucheJeu;
	/**
	 * Couche supérieure bloquante.
	 */
	PopupBloquant popupB;
	/**
	 * Couche supérieure pour le menu.
	 */
	PopupMenu popupM;
	/**
	 * 
	 */
	PopupOptions popupO;
	PopupRegles popupR;
	PopupReseau popupReseau;
	PopupVictoire popupV;
	/**
	 * Plateau de jeu.
	 */
	TerrainGraphique tg;
	public BandeauInfos bandeauInfos;
	/**
	 * Widget de chargement de la fenêtre principale.
	 */
	Chargement chargement;
	/**
	 * Widget de chargement de la mini-fenêtre.
	 */
	Chargement chargement2;

	Bouton boutonAnnuler;
	Bouton boutonRefaire;
	Bouton boutonValidation;
	Bouton boutonAide;

	
	/**
	 * Constructeur de l'IHM.
	 */
	public IHM() {

		// Initialisation de la fenêtre
		super("Fanorona");
		fenetreChargement = new FenetreChargement();
		try {
			setIconImage(ImageIO.read(getClass().getResource("/images/icone.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addComponentListener(this);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				action(Ecouteur.Bouton.QUITTER);
			}
		});

		theme = new Theme(this);

		coucheJeu = new JPanel(new BorderLayout()) {
			@Override
			public void paintComponent(Graphics g) {
				for(int i=0 ; i<getWidth() ; i += theme.imgFond.getWidth(this)) {
					for(int j=0 ; j<getHeight() ; j += theme.imgFond.getHeight(this)) {
						g.drawImage(theme.imgFond, i, j, theme.imgFond.getWidth(this), theme.imgFond.getHeight(this), null);
					}
				}
			}
		};
		coucheJeu.setBounds(0, 0, getSize().width, getSize().height);
		add(coucheJeu);

		// ZONE NORD
		JPanel voletNord = new JPanel(new BorderLayout());
		coucheJeu.add(voletNord, BorderLayout.NORTH);
		voletNord.setOpaque(false);

		// Boutons
		JPanel panneauMenu = new JPanel();
		voletNord.add(panneauMenu, BorderLayout.NORTH);
		panneauMenu.setOpaque(false);

		Bouton boutonMenu = new Bouton("Menu");
		boutonMenu.addActionListener(new Ecouteur(Ecouteur.Bouton.MENU, this));
		panneauMenu.add(boutonMenu);
		chargement = new Chargement();
		panneauMenu.add(chargement);
		Bouton boutonParam = new Bouton("Paramètres");
		boutonParam.addActionListener(new Ecouteur(Ecouteur.Bouton.PARAMETRES, this));
		panneauMenu.add(boutonParam);

		tg = new TerrainGraphique(this);

		// Infos partie en cours
		bandeauInfos = new BandeauInfos(tg);
		voletNord.add(bandeauInfos);

		// ZONE CENTRE

		coucheJeu.add(tg, BorderLayout.CENTER);

		// ZONE SUD
		JPanel voletSud = new JPanel(new BorderLayout());
		coucheJeu.add(voletSud, BorderLayout.SOUTH);
		voletSud.setOpaque(false);

		JPanel voletSudOuest = new JPanel();
		voletSudOuest.setOpaque(false);
		voletSud.add(voletSudOuest, BorderLayout.WEST);
		JPanel voletSudCentre = new JPanel();
		voletSudCentre.setOpaque(false);
		voletSud.add(voletSudCentre, BorderLayout.CENTER);
		JPanel voletSudEst = new JPanel();
		voletSudEst.setOpaque(false);
		voletSud.add(voletSudEst, BorderLayout.EAST);

		boutonAide = new Bouton("Aide");
		boutonAide.addActionListener(new Ecouteur(Ecouteur.Bouton.AIDE, this));
		voletSudOuest.add(boutonAide);

		boutonAnnuler = new Bouton("Annuler");
		boutonAnnuler.addActionListener(new Ecouteur(Ecouteur.Bouton.ANNULER, this));
		boutonAnnuler.setEnabled(false);
		voletSudCentre.add(boutonAnnuler);

		boutonRefaire = new Bouton("Refaire");
		boutonRefaire.addActionListener(new Ecouteur(Ecouteur.Bouton.REFAIRE, this));
		boutonRefaire.setEnabled(false);
		voletSudCentre.add(boutonRefaire);

		boutonValidation = new Bouton("Terminer");
		boutonValidation.addActionListener(new Ecouteur(Ecouteur.Bouton.TERMINER, this));
		boutonValidation.setEnabled(false);
		voletSudEst.add(boutonValidation);

		JLayeredPane gestionCouche = getLayeredPane();
		popupB = new PopupBloquant(new Color(0, 0, 0, 128));
		gestionCouche.add(popupB, new Integer(1));
		popupB.setVisible(false);
		popupM = new PopupMenu(this);
		gestionCouche.add(popupM, new Integer(3));
		popupM.setVisible(false);
		popupO = new PopupOptions(this);
		gestionCouche.add(popupO, new Integer(4));
		popupO.setVisible(false);
		popupR = new PopupRegles(this);
		gestionCouche.add(popupR, new Integer(4));
		popupR.setVisible(false);

		popupReseau = new PopupReseau(this);
		gestionCouche.add(popupReseau, new Integer(4));
		popupReseau.setVisible(false);

		popupV = new PopupVictoire();
		gestionCouche.add(popupV, new Integer(2));

		theme.setTheme(Theme.Type.BOIS);

		setModeReseau(false);

		setMinimumSize(new Dimension(800, 600));
		setSize(Math.max(800, (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.75)), Math.max(600, (int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.75)));
		Toolkit screen = Toolkit.getDefaultToolkit();
		Dimension dFen = screen.getScreenSize();
		setLocation(dFen.width / 2 - getSize().width / 2, dFen.height / 2 - getSize().height / 2);

		setSize(new Dimension(800, 600));
		fenetreChargement.setVisible(false);
		setVisible(true);

	}

	public Parametres getParametres() {

		Parametres params = new Parametres();
		params.j1_identifiant = popupO.identifiantJoueur1.getText();
		params.j2_identifiant = popupO.identifiantJoueur2.getText();
		params.j1_type = NiveauJoueur.getFromIndex(popupO.selectJoueur1.getSelectedIndex());
		params.j2_type = NiveauJoueur.getFromIndex(popupO.selectJoueur2.getSelectedIndex());

		return params;

	}
	
	

	public void nouvellePartie() {
		
		
		
		Parametres params = getParametres();
		if(params.j1_type != NiveauJoueur.HUMAIN && params.j2_type != NiveauJoueur.HUMAIN ){
			popupO.selectJoueur1.setSelectedIndex(0);
			popupO.identifiantJoueur1.setText("Joueur 1");
		}
		
		
		EvenementGraphique.stopper();
		chargement.cacher();
		Echange e = new Echange();
		//e.ajouter("nouvellePartie", true);
		e.ajouter("nouvellePartie", getParametres());
		//e.ajouter("parametres", );
		e.ajouter("terrain", true);
		com.envoyer(e);	
		
		
		
		
		
	}
	
	public void sauverPartie(){
		JFileChooser fcSauver = new JFileChooser();
		fcSauver.addChoosableFileFilter(new FileNameExtensionFilter(".fa", "fanorona"));
		File fileToBeSaved=null;
		if (fcSauver.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			if(!fcSauver.getSelectedFile().getAbsolutePath().endsWith(".fa")){
			    fileToBeSaved = new File(fcSauver.getSelectedFile() + ".fa");
			}
			Echange e = new Echange();
			e.ajouter("sauvegarder", fileToBeSaved);
			com.envoyer(e);
		}
	}
	
	public void chargerPartie(){
		JFileChooser fcCharger = new JFileChooser();
		fcCharger.addChoosableFileFilter(new FileNameExtensionFilter(".fa", "fanorona"));
		if (fcCharger.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			Echange e = new Echange();
			e.ajouter("charger", fcCharger.getSelectedFile());
			com.envoyer(e);
		}
	}
	
	public void quitter(boolean confirmation){
		if ((Communication.enReseau())) {
			String[] choix = { "Oui", "Non" };
			int retour;
			if(confirmation)
				retour = JOptionPane.showOptionDialog(this, "Vous êtes actuellement sur une partie en réseau. Voulez-vous vraiment quitter ?", "Attention", 1, 1, null, choix, choix[0]);
			else retour = 0;
			if (retour == 0) {
				Communication.quitterReseau();
				System.exit(0);
			}
		} else {
			System.exit(0);
			/*
			String[] choix = { "Oui", "Non" };
			int retour;
			if(confirmation)
				retour = JOptionPane.showOptionDialog(this, "Voulez-vous sauvegarder la partie avant de quitter ?", "Attention", 1, 1, null, choix, choix[1]);
			else retour = 1;
			if (retour == 1) {
				System.exit(0);
			} else if (retour == 0) {
				action(Ecouteur.Bouton.SAUVEGARDER);
				System.exit(0);
			}
			*/
		}
	}
	
	public void reseau_heberger(){		
		
		int port;
		try{
			port = Integer.valueOf( popupReseau.champHebergerPort.getText() );			
		}
		catch(Exception e){
			port=0;
		}
		port = Communication.reseauHeberger(port);
		
		// erreur
		if(port == 0){
			popupReseau.message.setText("Impossible d'ouvrir une partie sur le port specifié");
		}
		else{
			
			Parametres param = new Parametres();
			param.j1_type = Parametres.NiveauJoueur.HUMAIN;
			param.j2_type = Parametres.NiveauJoueur.HUMAIN;			
			Echange ec = new Echange("nouvellePartie",param);					
			com.envoyer(ec);
			
			
			// On lance un client
			reseau_rejoindre("127.0.0.1",port);
			
			
		
		}
	}
	
	public void reseau_rejoindre(String host, int port){
		
		
		if(host == null){
			host =popupReseau.champRejoindreIp.getText();
		}
		if(port == 0){
			port = Integer.valueOf( popupReseau.champRejoindrePort.getText() );
		}
		String identifiant = popupReseau.champId.getText();
		String retour = Communication.reseauRejoindre(host, port, identifiant);
		if(retour != null){
			popupReseau.message.setText(retour);
		}
		else{
			popupReseau.setVisible(false);
			popupB.setVisible(false);
			setModeReseau(true);
			popupReseau.message.setText("");
		}
	}

	/**
	 * Gestion de toutes les entrées de l'IHM.
	 * @param id Identifiant du bouton cliqué.
	 */
	public void action(Ecouteur.Bouton id) {

		switch (id) {
		case REPRENDRE:
			popupB.setVisible(false);
			popupM.setVisible(false);
			break;
		case SAUVEGARDER:
			sauverPartie();
			break;
		case CHARGER:
			chargerPartie();
			break;
		case MODE:
			if (Communication.enReseau()) {
				String choix[] = { "Confirmer", "Annuler" };
				int retour = JOptionPane.showOptionDialog(this, "Revenir au jeu local quittera la partie réseau.", "Attention", 1, JOptionPane.INFORMATION_MESSAGE, null, choix, choix[1]);

				if (retour == 0) {
					Communication.quitterReseau();
					setModeReseau(false);
					nouvellePartie();
				}
				popupB.setVisible(false);
			} else {
				//popupReseau.message.setText("");
				popupReseau.setVisible(true);
			}
			popupM.setVisible(false);

			break;
		case REGLES:
			popupM.setVisible(false);
			popupR.setVisible(true);
			break;
		case RECOMMENCER:
			nouvellePartie();
			popupM.setVisible(false);
			popupB.setVisible(false);
			break;
		case QUITTER:
			quitter(true);
			break;
		case MENU:
			popupB.setVisible(true);
			popupM.setVisible(true);
			break;
		case PARAMETRES:
			popupB.setVisible(true);
			popupO.setVisible(true);
			break;
		case ANNULER:
			if (!EvenementGraphique.animationEnCours) {
				Echange e1 = new Echange();
				e1.ajouter("annuler", true);
				com.envoyer(e1);
			}
			break;
		case REFAIRE:
			if (!EvenementGraphique.animationEnCours) {
				Echange e2 = new Echange();
				e2.ajouter("refaire", true);
				com.envoyer(e2);
			}
			break;
		case TERMINER:
			Echange e3 = new Echange();
			e3.ajouter("finTour", true);
			com.envoyer(e3);
			break;
		case AIDE:
			Echange e4 = new Echange();
			e4.ajouter("aide", true);
			com.envoyer(e4);
			break;
		case OPTION_ANNULER:
			popupO.setVisible(false);
			popupB.setVisible(false);
			break;

		case OPTION_VALIDER:

			Parametres params = getParametres();
			if (popupO.theme.getSelectedItem() == "Standard")
				theme.setTheme(Theme.Type.STANDARD);
			else if (popupO.theme.getSelectedItem() == "Boisé")
				theme.setTheme(Theme.Type.BOIS);
			else if (popupO.theme.getSelectedItem() == "Marbre")
				theme.setTheme(Theme.Type.MARBRE);
			else if (popupO.theme.getSelectedItem() == "Sombre")
				theme.setTheme(Theme.Type.SOMBRE);
			else if (popupO.theme.getSelectedItem() == "Cochonou")
				theme.setTheme(Theme.Type.COCHON);

			if (!Communication.enReseau()) {
				Echange e = new Echange();
				e.ajouter("parametres", params);
				com.envoyer(e);
			}

			popupO.setVisible(false);
			popupB.setVisible(false);
			break;
		case REGLES_PLUS:
			try{
			ouvrirPDF("/documents/regles.pdf");
			}
			catch(Exception e){}
			break;
		case REGLES_RETOUR:
			popupR.setVisible(false);
			popupM.setVisible(true);
			break;

		case RESEAU_RETOUR:
			popupReseau.setVisible(false);
			popupM.setVisible(true);
			break;
		case RESEAU_HEBERGER:
			reseau_heberger();
			break;
		case RESEAU_REJOINDRE:
			reseau_rejoindre(null,0);
			break;
		}
	}
	
	
    
    public void ouvrirPDF(String src) throws IOException
    {
    	InputStream in = IHM.class.getClass().getResourceAsStream(src);
        File f = File.createTempFile("JAR_", ".pdf");
        FileOutputStream out = new FileOutputStream(f);
        
        byte[] buf = new byte[1024];
        for (int n; (n=in.read(buf))!=-1; out.write(buf, 0, n));
        
        out.close();
        in.close();
        
        Desktop.getDesktop().open(f);
    }

	/**
	 * Change l'agencement des boutons si la partie est en réseau.
	 * @param r Vrai si on est en réseau, faux sinon.
	 */
	public void setModeReseau(boolean r) {
		if (!r) {
			popupM.boutonMenuReseau.setVisible(true);
			popupM.boutonMenuLocal.setVisible(false);
			popupM.boutonRecommencer.setEnabled(true);
		}
		else{	
			popupM.boutonMenuReseau.setVisible(false);
			popupM.boutonMenuLocal.setVisible(true);
			if(!Communication.estServeur())				
				popupM.boutonRecommencer.setEnabled(false);			
		}

		popupO.selectJoueur1Etiq.setVisible(!r);
		popupO.selectJoueur2Etiq.setVisible(!r);
		popupO.identifiantJoueur1.setVisible(!r);
		popupO.identifiantJoueur2.setVisible(!r);
		popupO.selectJoueur1.setVisible(!r);
		popupO.selectJoueur2.setVisible(!r);
		popupM.boutonMenuSauvegarder.setEnabled(!r);
		popupM.boutonMenuCharger.setEnabled(!r);
		
		popupM.bloquerSauverCharger(r);

		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	/**
	 * Redimentionnement de la fenêtre.
	 */
	public void componentResized(ComponentEvent e) {
		coucheJeu.setBounds(0, 0, getWidth(), getHeight());
		popupB.setBounds(0, 0, getWidth(), getHeight());
		popupM.setBounds(getWidth() / 2 - 175, getHeight() / 2 - 275, 350, 550);
		popupO.setBounds(getWidth() / 2 - 320, getHeight() / 2 - 200, 640, 400);
		popupR.setBounds(getWidth() / 2 - 400, getHeight() / 2 - 250, 800, 500);

		popupReseau.setBounds(getWidth() / 2 - 320, getHeight() / 2 - 200, 640, 400);
		popupV.setBounds(0, 0, getWidth(), getHeight());

	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	/**
	 * Récéption des paquets du Moteur.
	 * @param e Paquet reçu.
	 */
	public void notifier(Echange e) {
	
		
		Object dataValue;
		
		if ((dataValue = e.get("terrain")) != null) {
			tg.dessinerTerrain((modele.Case[][]) dataValue); 
		}
		
		if ((dataValue = e.get("coup")) != null) {
			tg.lCoups.addLast((EvenementGraphique) dataValue);
			EvenementGraphique.lancer(tg);
		}

		/* Gardez cet ordre */
		if ((dataValue = e.get("pionDeselectionne")) != null) {
			tg.deselectionner();
		}

		if ((dataValue = e.get("pionSelectionne")) != null) {
			tg.selectionner((Point) dataValue);

		}
		/*
		 * if((dataValue = e.get("coups")) != null){
		 * LinkedList<EvenementGraphique> cg =
		 * (LinkedList<EvenementGraphique>)dataValue;
		 * java.util.Iterator<EvenementGraphique> it = cg.iterator();
		 * while(it.hasNext()){ tg.lCoups.addLast(it.next()); }
		 * 
		 * EvenementGraphique.afficherCoups(tg); }
		 */

		/*
		 * if ((dataValue = e.get("deplacement")) != null) { Point[] pts =
		 * (Point[]) dataValue; tg.deplacer(pts[0], pts[1]); tpsAnimation +=
		 * TerrainGraphique.ANIM_DEPL; } if ((dataValue = e.get("pionsManges"))
		 * != null) { new ExecuterDans(this, "pionsManges", dataValue,
		 * tpsAnimation); } if ((dataValue = e.get("choixPrise")) != null) { new
		 * ExecuterDans(this, "choixPrise", dataValue, tpsAnimation); } if
		 * ((dataValue = e.get("joueurs")) != null) { new ExecuterDans(this,
		 * "joueurs", dataValue, tpsAnimation); }
		 */

		if ((dataValue = e.get("bandeauSup")) != null) {
			bandeauInfos.setTexteSup((String) dataValue);
		}

		if ((dataValue = e.get("bandeauInf")) != null) {
			bandeauInfos.setTexteInf((String) dataValue);
		}
		if ((dataValue = e.get("annuler")) != null) {
			boutonAnnuler.setEnabled((boolean) dataValue);
		}
		if ((dataValue = e.get("refaire")) != null) {
			boutonRefaire.setEnabled((boolean) dataValue);
		}
		if ((dataValue = e.get("aide")) != null) {
			boutonAide.setEnabled((boolean) dataValue);
		}
		if ((dataValue = e.get("finTour")) != null) {
			boutonValidation.setEnabled((boolean) dataValue);
		}
		if ((dataValue = e.get("score")) != null) {
			int[] score = (int[]) dataValue;
			bandeauInfos.setScore(1, score[0]);
			bandeauInfos.setScore(2, score[1]);
		}
		if ((dataValue = e.get("parametres")) != null) {
			Parametres params = (Parametres) dataValue;
			if (params.j1_identifiant != null){
				bandeauInfos.setIdentifiant(1, params.j1_identifiant);
				if(!Communication.enReseau())
					popupO.identifiantJoueur1.setText(params.j1_identifiant);
			}
			if (params.j2_identifiant != null){
				bandeauInfos.setIdentifiant(2, params.j2_identifiant);
				if(!Communication.enReseau()){
					popupO.identifiantJoueur2.setText(params.j2_identifiant);
				}
			}
			
			if (params.j1_type != null){
				popupO.selectJoueur1.setSelectedIndex(  Parametres.NiveauJoueur.getToIndex(params.j1_type)  );
			}
			if (params.j2_type != null){
				popupO.selectJoueur2.setSelectedIndex(  Parametres.NiveauJoueur.getToIndex(params.j2_type)  );
			}
		}
		
		if((dataValue = e.get("chargement")) != null){
			
			if((boolean) dataValue == true){
				
				chargement.afficher();
			}
			else
				chargement.cacher();
		}
		

	}
	
	/**
	 * Affiche une popup d'information au joueur.
	 * @param info 
	 */
	public void notifier(String info){		
		
		if(info.equals("/INTER_SERVEUR") || info.equals("/ABANDON")){
			String message="";
			if(info.equals("/INTER_SERVEUR")){
				message = "Le seveur a mis fin à la partie en réseau en cours.";
			}
			else{
				message = "L'adversaire a mis fin à la partie en réseau.";
			}
			
			message +="\n Voulez-vous lancer une nouvelle partie en local ?";
			int reponse = JOptionPane.showConfirmDialog(this, message,
				      "Fin de partie réseau",
				      JOptionPane.YES_NO_OPTION);	
			setModeReseau(false);
			
			if (reponse == JOptionPane.YES_OPTION) nouvellePartie();
			else quitter(false);
			
		}

	}
}
