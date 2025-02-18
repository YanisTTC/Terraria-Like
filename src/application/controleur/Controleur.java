package application.controleur;

import java.net.URL;
import java.util.ResourceBundle;

import application.controleur.inventaire.ObservateurObjet;
import application.controleur.inventaire.ObservateurResources;

import application.controleur.inventaire.ObservateurObjet;
import application.controleur.inventaire.ObservateurResources;
import application.controleur.map.ObservateurMap;
import application.modele.Environnement;
import application.modele.Joueur;
import application.modele.Personnage;
import application.modele.SoundEffect;
import application.modele.objet.armes.Epee;
import application.modele.SoundEffect;
import application.vue.CarteVue;
import application.vue.DeplacementAnimation;
import application.vue.DroneSentinelleVue;
import application.vue.DroneSentinelleVue;
import application.vue.JoueurVue;
import application.vue.RobotFantassinVue;
import application.vue.VieVue;
import application.vue.inventaire.InventaireVue;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.util.Duration;

public class Controleur implements Initializable {

	private Environnement env;

	private Timeline gameLoop;
	private int temps;
	@FXML
	private BorderPane root;
	@FXML
	private TilePane panneauJeu;
	@FXML
	private Pane paneCentral;
	private ObservableVie obsVie;
	private JoueurVue joueurVue;
	private VieVue vieVue;
	private CarteVue carteVue;
	private Personnage robotFantassin;
	private RobotFantassinVue robotFantassinVue;
	private Personnage droneSentinelle;
	private DroneSentinelleVue droneSentinelleVue;
	private Joueur joueur;
	private SoundEffect die = new SoundEffect("application/ressources/sounds/boss_die.wav");
	private BooleanProperty mine;
	private int tempInit;
	private InventaireVue inventaireVue;
	boolean isGameOverAdded = false;
	private SoundEffect bgSound = new SoundEffect("application/ressources/sounds/bgSound.wav");

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Image fond = new Image("application/ressources/map/bgorange.png");

		BackgroundImage imageFond = new BackgroundImage(fond, BackgroundRepeat.SPACE,
				BackgroundRepeat.SPACE,
				BackgroundPosition.DEFAULT,
				BackgroundSize.DEFAULT);

		Background background = new Background(imageFond);


		bgSound.playSound();

		root.setBackground(background);

		initialiserVariables();
		faireBindEtListener();
		insererImagesPaneCentral();
		initGameLoop();
		getInventaireVue().affichageInventaire();

		gameLoop.play();
		root.addEventHandler(KeyEvent.KEY_PRESSED, new KeyPressed(joueur, joueurVue, this));
		root.addEventHandler(KeyEvent.KEY_RELEASED, new KeyReleased(joueur, joueurVue));
		joueur.getInventaire().ajouterObjet(0, 1);
		joueur.getInventaire().ajouterObjet(1, 1);
		joueur.getInventaire().ajouterObjet(2, 93);
		joueur.getInventaire().ajouterObjet(3, 7);
		joueur.getInventaire().ajouterObjet(4, 2);
		//(Joueur)joueur.getInventaire().supprimerObjet(2, 5);
		//joueur.getInventaire().supprimerObjet(0, 3);
		//joueur.getInventaire().ajouterObjet(2, 93);
	}

	private void initGameLoop() {
		gameLoop = new Timeline();
		temps = 0;
		gameLoop.setCycleCount(Timeline.INDEFINITE);

		KeyFrame kf = new KeyFrame(Duration.seconds(0.002), // 1 frame = 0.002 seconde
				(ev -> {

					if (temps % 5 == 0) {
						faireTour();
						afficherInfosEnConsole();
					}
					temps++;
				}));

		gameLoop.getKeyFrames().add(kf);
	}

	private void faireTour() {
		this.env.update();
		joueur.gravite();
		robotFantassin.gravite();

		if (robotFantassin.estVivant() && joueur.estVivant()) {
			if (joueur.getX() > robotFantassin.getX() && (joueur.getX() - robotFantassin.getX()) > 50) {
				robotFantassin.setX(robotFantassin.getX() + 1);
				robotFantassin.setDirection(1);

			} else if (joueur.getX() < robotFantassin.getX() && (robotFantassin.getX() - joueur.getX()) > 50) {
				robotFantassin.setX(robotFantassin.getX() - 1);
				robotFantassin.setDirection(-1);

			} else {
				if (Math.abs(joueur.getY() - robotFantassin.getY()) < 50 && temps % 700 == 0) {
					robotFantassin.attaque(joueur);
					robotFantassinVue.setOnMouseClicked(event -> {
						if (Math.abs(joueur.getY() - robotFantassin.getY()) < 50 && joueur.getEnMain() instanceof Epee)
							robotFantassin.perdreVie(2);
					});
				}

			}
		}

		if (droneSentinelle.estVivant() && joueur.estVivant()) {
			if (joueur.getX() > droneSentinelle.getX() && (joueur.getX() - droneSentinelle.getX()) > 50) {
				droneSentinelle.setX(droneSentinelle.getX() + 2);
				droneSentinelle.setDirection(1);

			} else if (joueur.getX() < droneSentinelle.getX() && (droneSentinelle.getX() - joueur.getX()) > 50) {
				droneSentinelle.setX(droneSentinelle.getX() - 2);
				droneSentinelle.setDirection(-1);

			} else {
				if (Math.abs(joueur.getY() - droneSentinelle.getY()) < 50 && temps % 700 == 0) {
					droneSentinelle.attaque(joueur);

					droneSentinelleVue.setOnMouseClicked(event -> {
						if (Math.abs(joueur.getY() - droneSentinelle.getY()) < 50)
							droneSentinelle.perdreVie(2);
					});
				}
			}
		}

		// Collision joueur

		if (env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())) == 1 ||
				env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())) == 2 ||
				env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())) == 3 ||
				env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())) == 4 ||
				env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())) == 5 ||
				env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())) == 6) {


			joueur.setY(joueur.getY() - 5);
			joueur.setSaute(false);
		}

		if (env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())) == 7) {
			this.joueur.meurt();
			joueurVue.setVisible(false);
			joueur.setY(joueur.getY() - 5);
		}

		if (env.mapProperty().get(env.getTileBasDroite(joueur.getX(), joueur.getY())) == 1 ||
				env.mapProperty().get(env.getTileBasDroite(joueur.getX(), joueur.getY())) == 3) {
			System.out.println("TOUCHE BAS DROITE");
			joueur.setY(joueur.getY() - 5);
		}

		if (env.mapProperty().get(env.getTileBasGauche(joueur.getX(), joueur.getY())) == 1 ||
				env.mapProperty().get(env.getTileBasDroite(joueur.getX(), joueur.getY())) == 3) {
			System.out.println("TOUCHE BAS GAUCHE");
			joueur.setY(joueur.getY() - 5);
		}

		if (env.mapProperty().get(env.getTileHautGauche(joueur.getX(), joueur.getY())) == 2) {
			System.out.println("TOUCHE HAUT GAUCHE");
			joueur.setX(joueur.getX() + 5);
		}

		if (env.mapProperty().get(env.getTileHautDroite(joueur.getX(), joueur.getY())) == 2) {
			System.out.println("TOUCHE HAUT DROITE");
			joueur.setX(joueur.getX() - 5);
		}


		// Collision robot

		if (env.mapProperty().get(env.getTileBas(robotFantassin.getX(), robotFantassin.getY())) == 1 ||
				env.mapProperty().get(env.getTileBas(robotFantassin.getX(), robotFantassin.getY())) == 2 ||
				env.mapProperty().get(env.getTileBas(robotFantassin.getX(), robotFantassin.getY())) == 3 ||
				env.mapProperty().get(env.getTileBas(robotFantassin.getX(), robotFantassin.getY())) == 4 ||
				env.mapProperty().get(env.getTileBas(robotFantassin.getX(), robotFantassin.getY())) == 5 ||
				env.mapProperty().get(env.getTileBas(robotFantassin.getX(), robotFantassin.getY())) == 6) {

			robotFantassin.setY(robotFantassin.getY() - 5);

		}
		if (env.mapProperty().get(env.getTileBas(robotFantassin.getX(), robotFantassin.getY())) == 7) {
			this.robotFantassin.meurt();
			robotFantassin.setY(robotFantassin.getY() - 5);
		}


		if (env.mapProperty().get(env.getTileBasDroite(robotFantassin.getX(), robotFantassin.getY())) == 1 ||
				env.mapProperty().get(env.getTileBasDroite(robotFantassin.getX(), robotFantassin.getY())) == 3) {
			robotFantassin.setY(robotFantassin.getY() - 5);
		}

		if (env.mapProperty().get(env.getTileBasGauche(robotFantassin.getX(), robotFantassin.getY())) == 1 ||
				env.mapProperty().get(env.getTileBasDroite(robotFantassin.getX(), robotFantassin.getY())) == 3) {
			robotFantassin.setY(robotFantassin.getY() - 5);
		}

		if (env.mapProperty().get(env.getTileHautGauche(robotFantassin.getX(), robotFantassin.getY())) == 2) {
			robotFantassin.setX(robotFantassin.getX() + 5);
		}

		if (env.mapProperty().get(env.getTileHautDroite(robotFantassin.getX(), robotFantassin.getY())) == 2) {
			robotFantassin.setX(robotFantassin.getX() - 5);
		}

		if (!joueur.estVivant()) {
			//paneCentral.getChildren().remove(joueurVue);
			Image joueurMort = new Image("application/ressources/sprites/joueur/9.png");
			joueurVue.setImage(joueurMort);
			joueurVue.setLayoutY(joueurVue.getY() + 20);

			ImageView gameover = new ImageView("application/ressources/menu/gameover.png");
			gameover.setFitWidth(300);
			gameover.setFitHeight(200);
			gameover.setLayoutX((panneauJeu.getMaxWidth() / 3) + 100);
			gameover.setLayoutY(30);

			if (!isGameOverAdded) {
				paneCentral.getChildren().add(gameover);
				TranslateTransition gameover_down = new TranslateTransition(Duration.millis(250), gameover);
				gameover_down.setByY(+40);
				gameover_down.play();
				die.playSound();
				isGameOverAdded = true;
			}

			if (temps % 1000 == 0) {
				gameLoop.stop();
			}

		}

		if (!robotFantassin.estVivant()) {
			paneCentral.getChildren().remove(robotFantassinVue);
		}

		if (!droneSentinelle.estVivant()) {
			paneCentral.getChildren().remove(droneSentinelleVue);
		}


		joueurVue.setScaleX(joueur.getDirection());
		robotFantassinVue.setScaleX(robotFantassin.getDirection());
		droneSentinelleVue.setScaleX(droneSentinelle.getDirection());

	}

	private void initialiserVariables() {
		// création de l'environnement
		this.env = new Environnement();

		// création de la vue de la map
		this.carteVue = new CarteVue(env, panneauJeu, 95, 68);

		// Création du joueur et de la vue du joueur
		this.joueur = this.env.getJoueur();
		this.joueurVue = new JoueurVue();

		// Création de la vue de la vie avec l'observable
		this.vieVue = new VieVue(carteVue);
		this.obsVie = new ObservableVie(vieVue);
		this.joueur.vieProperty().addListener(obsVie);

		//Création de l'inventaire
		this.inventaireVue = new InventaireVue();

		// Création d'un fantassin et de sa vue
		this.robotFantassin = this.env.getRobotFantassin();
		this.robotFantassinVue = new RobotFantassinVue();

		// Création d'une sentinelle et de sa vue
		this.droneSentinelle = this.env.getDroneSentinelle();
		this.droneSentinelleVue = new DroneSentinelleVue();
	}

	private void faireBindEtListener() {
		// Bind Joueur
		this.joueurVue.translateXProperty().bindBidirectional(this.joueur.xProperty());
		this.joueurVue.translateYProperty().bindBidirectional(this.joueur.yProperty());

		// Bind RobotFantassin
		this.robotFantassinVue.translateXProperty().bindBidirectional(this.robotFantassin.xProperty());
		this.robotFantassinVue.translateYProperty().bindBidirectional(this.robotFantassin.yProperty());

		// Bind DroneSentinelle
		this.droneSentinelleVue.translateXProperty().bindBidirectional(this.droneSentinelle.xProperty());
		this.droneSentinelleVue.translateYProperty().bindBidirectional(this.droneSentinelle.yProperty());

		//Creation des Listeners pour InventaireVue
		((Joueur)joueur).getInventaire().getObjet(0).objetProperty().addListener(new ObservateurObjet(joueur.getInventaire().getObjet(0), inventaireVue, joueur));
		joueur.getInventaire().getObjet(1).objetProperty().addListener(new ObservateurObjet(joueur.getInventaire().getObjet(1), inventaireVue, joueur));
		joueur.getInventaire().getObjet(2).objetProperty().addListener(new ObservateurResources(joueur.getInventaire().getObjet(2), inventaireVue, joueur));
		joueur.getInventaire().getObjet(3).objetProperty().addListener(new ObservateurResources(joueur.getInventaire().getObjet(3), inventaireVue, joueur));
		joueur.getInventaire().getObjet(4).objetProperty().addListener(new ObservateurResources(joueur.getInventaire().getObjet(4), inventaireVue, joueur));


		//Creation Observateur Map
		env.mapProperty().addListener(new ObservateurMap(carteVue, env.mapProperty()));
	}

	private void insererImagesPaneCentral() {
		// Ajout import javafx.beans.property.SimpleIntegerProperty;des vue au panneau central
		this.paneCentral.getChildren().add(joueurVue);
		this.paneCentral.getChildren().add(vieVue);
		this.paneCentral.getChildren().add(robotFantassinVue);
		this.paneCentral.getChildren().add(droneSentinelleVue);
		this.paneCentral.getChildren().add(inventaireVue);
	}

	private void afficherInfosEnConsole() {
//		System.out.println("Direction joueur:" + joueur.getDirection());
		System.out.println("Joueur x:" + joueur.getX());
		System.out.println("Joueur y:" + joueur.getY());
		System.out.println("Robot x:" + robotFantassin.getX());
		System.out.println("Robot y:" + robotFantassin.getY());
//		System.out.println("Direction robot:" + robotFantassin.getDirection());
		System.out.println("Vie du joueur: " + joueur.vieProperty().getValue());
		System.out.println(env.mapProperty().get(env.getTileBas(joueur.getX(), joueur.getY())));
		System.out.println("Vie du robot fantassin: " + robotFantassin.vieProperty().getValue());
		System.out.println("Vie du dronde sentinelle: " + droneSentinelle.vieProperty().getValue());
		System.out.println(this.env.getListePersonnages());
	}

	public InventaireVue getInventaireVue() {
		return inventaireVue;
	}
}