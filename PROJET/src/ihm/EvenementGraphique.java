package ihm;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import modele.Case;

public class EvenementGraphique implements Runnable, Serializable {
	public enum FinPartie {VICTOIRE, DEFAITE, AUCUNE};
	private Case[][] terrain;
	private Point[] deplacement;
	private Point[] choixPrise;
	private ArrayList<Point> pionsManges, chemin;
	private int[] score;
	private static TerrainGraphique tg;
	public static boolean animationEnCours = false;
	private String bandeauSup,bandeauInf;
	private int joueurCourant = 0;
	private FinPartie finPartie = FinPartie.AUCUNE;
	
	public EvenementGraphique(Point[] d, Point[] c, ArrayList<Point> p, int[] s,String bS,String bI, ArrayList<Point> chemin) {
		deplacement = d;
		choixPrise = c;
		pionsManges = p;
		score = s;
		bandeauSup = bS;
		bandeauInf = bI;
		this.chemin = chemin;
	}
	public EvenementGraphique(Point[] d, Point[] c, ArrayList<Point> p, int[] s, ArrayList<Point> chemin) {
		deplacement = d;
		choixPrise = c;
		pionsManges = p;
		score = s;
		bandeauSup = null;
		bandeauInf = null;
		this.chemin = chemin;
		
	}
	public EvenementGraphique(){
		terrain=null;
		deplacement=null;
		choixPrise=null;
		pionsManges=null;
		chemin=null;
		score=null;
		tg=null;
		bandeauSup=null;
		bandeauInf=null;
	}
	
	public EvenementGraphique(Case[][] t){
		terrain = t;
		deplacement=null;
		choixPrise=null;
		pionsManges=null;
		chemin=null;
		score=null;
		tg=null;
		bandeauSup=null;
		bandeauInf=null;
		this.chemin=new ArrayList<Point>();
	}
	
	public EvenementGraphique(String bandeauSup , String bandeauInf , FinPartie b){
		deplacement=null;
		choixPrise=null;
		pionsManges=null;
		chemin=null;
		score=null;
		this.bandeauSup=bandeauSup;
		this.bandeauInf=bandeauInf;
		finPartie=b;
		this.chemin=new ArrayList<Point>();
	}
	
	public EvenementGraphique(String bandeauSup , String bandeauInf , int i){
		this.bandeauSup=bandeauSup;
		this.bandeauInf=bandeauInf;
		joueurCourant=i;
		
	}
	
	public EvenementGraphique(int i){
		joueurCourant=i;
	}
	
	
	public void lancer(){
		Thread t = new Thread(this);
		t.start();
	}
	public void run() {
	
		if(terrain != null){
			latence(50);
			tg.dessinerTerrain(terrain);
			
			
			latence(50);
		}
		
		
		if(deplacement != null){
			tg.deplacer(deplacement[0], deplacement[1]);
		}
		
		
		latence(TerrainGraphique.ANIM_DEPL/4);
		
		if(pionsManges != null) {
			tg.manger(pionsManges);
		}
		if(choixPrise != null){
			tg.afficherPrisesPossibles(choixPrise);
		} else {
			tg.cacherPrisesPossibles();
		}
		
		latence(TerrainGraphique.ANIM_DISP);
	
		if(chemin != null) {
			tg.trait = chemin;
			tg.repaint();
		}
		
		if(score != null){
			tg.ihm.bandeauInfos.setScore(1,score[0]);
			tg.ihm.bandeauInfos.setScore(2,score[1]);
		}
		
		if(bandeauSup != null){
		tg.ihm.bandeauInfos.setTexteSup(bandeauSup);}
		
		if(bandeauInf != null){
		tg.ihm.bandeauInfos.setTexteInf(bandeauInf);}
		
		if(joueurCourant != 0) {
			tg.ihm.bandeauInfos.setJoueurActif(joueurCourant);
		}
		
		if(tg.lCoups.size() != 0) {
			tg.lCoups.pollFirst().lancer();
		}
		else{
			EvenementGraphique.animationEnCours = false;
		}
		
		
	}
	
	public void latence(int tps){
		try {
			Thread.sleep(tps);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void afficherCoups(TerrainGraphique tg){
		EvenementGraphique.tg = tg;
		if(!EvenementGraphique.animationEnCours){
			EvenementGraphique.animationEnCours = true;	
			tg.lCoups.pollFirst().lancer();
		}
	}
}
