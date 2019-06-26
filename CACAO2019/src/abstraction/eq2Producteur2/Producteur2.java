package abstraction.eq2Producteur2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import abstraction.eq1Producteur1.ventesCacaoAleatoires.SuperviseurVentesCacaoAleatoires;
import abstraction.eq7Romu.produits.Chocolat;
import abstraction.eq7Romu.produits.Feve;
import abstraction.eq7Romu.ventesContratCadre.ContratCadre;
import abstraction.eq7Romu.ventesContratCadre.Echeancier;
import abstraction.eq7Romu.ventesContratCadre.IVendeurContratCadre;
import abstraction.eq7Romu.ventesContratCadre.StockEnVente;
import abstraction.fourni.Entree;
import abstraction.fourni.Historique;
import abstraction.fourni.IActeur;
import abstraction.fourni.Indicateur;
import abstraction.fourni.Journal;
import abstraction.fourni.Monde;

public class Producteur2 implements IActeur, IVendeurContratCadre<Feve> {

	private static int NB_PROD = 2;
	private static final double PRIX_INIT = 1.500;
	private static final double PRIX_MIN = 0.800;
	private static final double PRIX_MAX = 2.500;

	private Indicateur soldeBancaire;
	private Journal journal;
	private double meteo = 0;
	private double maladie_predateurs = 0;

	private int numero;
	private List<ContratCadre<Feve>> contratsEnCours;
	private int numStep;
	private GestionnaireFeve gestionnaireFeve;
	private Arbre arbres;
	private double beneficesDuMois = 0;
	private int contratsConclus = 0;

	private double salaire = 1;
	private double salaireDemande = 0.5;
	private boolean enGreve = false;


	public Producteur2() {
		this.gestionnaireFeve = new GestionnaireFeve(this);

		List<Feve> feves = this.gestionnaireFeve.getFeves();
		for (Feve f : feves) {
			Monde.LE_MONDE.ajouterIndicateur(gestionnaireFeve.get(f).getStockIndicateur());
		}

		this.soldeBancaire = new Indicateur(this.getNom() + " Solde", this, 10000000);

		Monde.LE_MONDE.ajouterIndicateur(this.soldeBancaire);
		this.contratsEnCours = new ArrayList<ContratCadre<Feve>>();
		this.journal = new Journal("Journal " + this.getNom());
		Monde.LE_MONDE.ajouterJournal(this.journal);
		System.out.println("ajout journal equipe 2");
		this.numStep = 1;

		arbres = new Arbre();
		arbres.initialise();
		this.gestionnaireFeve.setProduction(this, Feve.FORASTERO_MG_NEQ, 6750000);
		this.gestionnaireFeve.setStock(this, Feve.FORASTERO_MG_NEQ, 200000000);
		this.gestionnaireFeve.setProduction(this, Feve.FORASTERO_MG_NEQ, 67500000);
		this.gestionnaireFeve.setStock(this, Feve.FORASTERO_MG_NEQ, 20000000);
		this.gestionnaireFeve.setPrix(this, Feve.FORASTERO_MG_NEQ, 1.5);

		this.gestionnaireFeve.setProduction(this, Feve.FORASTERO_MG_EQ, 750000); // TODO rectifier les productions des
																					// autres feves
		this.gestionnaireFeve.setStock(this, Feve.FORASTERO_MG_EQ, 20000000);
		this.gestionnaireFeve.setPrix(this, Feve.FORASTERO_MG_EQ, 1.7);

		this.gestionnaireFeve.setProduction(this, Feve.MERCEDES_MG_NEQ, 750000);
		this.gestionnaireFeve.setStock(this, Feve.MERCEDES_MG_NEQ, 20000000);
		this.gestionnaireFeve.setPrix(this, Feve.MERCEDES_MG_NEQ, 1.3);

		this.gestionnaireFeve.setProduction(this, Feve.MERCEDES_MG_EQ, 75000);
		this.gestionnaireFeve.setStock(this, Feve.MERCEDES_MG_EQ, 2000000);
		this.gestionnaireFeve.setPrix(this, Feve.MERCEDES_MG_EQ, 1.4);

	}

	public String getNom() {
		return "EQ2";
	}

	public void initialiser() {

	}

// Begin Clément M 

	public void next() {
		retireVieuxContrats();
		payerCoutsProd();
		if (this.salaire < this.salaireDemande) {
			this.enGreve = true;
			this.journal.ajouter("on est en grève");

		} else {
			this.journal.ajouter("pas de grève");
			this.enGreve = false;
		}
		for (Feve f : gestionnaireFeve.getFeves()) {
			if (!this.enGreve) {
				this.recolte(f);
				if (this.balanceDuStep() > 0)
					this.salaireDemande *= 1.05;
				else
					this.salaire *= 0.95;
			} else {
				this.salaireDemande *= 0.95;
				this.salaire *= 1.05;
			}
			this.journal.ajouter(
					"Step " + Monde.LE_MONDE.getStep() + " : prix de vente = " + this.gestionnaireFeve.getPrixVente(f));
			if ((beneficesDuMois <200000)
					&& this.gestionnaireFeve.getPrixVente(f) * 0.95 > PRIX_MIN) {
				this.gestionnaireFeve.setPrix(this, f, this.gestionnaireFeve.getPrixVente(f) * 0.95);

			}
		}
		this.journal.ajouter("balance du step : " + this.balanceDuStep());
		this.journal.ajouter("benefices du mois : " + beneficesDuMois);
		this.journal.ajouter("contrats conclus : " + contratsConclus);
 
		contratsConclus = 0;
		beneficesDuMois = 0;

		if (this.numStep == 24) {
			this.numStep = 1;
			this.actualisationProduction();
			arbres.actualise();
			for (Feve f: this.gestionnaireFeve.getFeves()) {
				this.gestionSurfaceCultivee(f);
			}	
			this.actualisationProduction();
		} else {

			this.numStep++;

		}}

//		juste pour les prints : 
//		for (Feve f : this.gestionnaireFeve.getFeves()) {
//			System.out.println(getCoutProduction(f));
//		}
	

// End Clément M

	public void actualisationProduction() {
		for (Feve feve : this.gestionnaireFeve.keySet()) {
			if (feve == Feve.FORASTERO_MG_EQ || feve == Feve.FORASTERO_MG_NEQ) {
				this.gestionnaireFeve.setProduction(this, feve, this.arbres.getNbArbres(feve) * 1000 * 2);
			} // *1000 pour passer en kilo, et *2 pour le rapport tonne par hectare
			if (feve == Feve.MERCEDES_MG_EQ || feve == Feve.MERCEDES_MG_NEQ) {
				this.gestionnaireFeve.setProduction(this, feve, this.arbres.getNbArbres(feve) * 1000 * 6);
			}
		}
	}

	public void recolte(Feve f) {
		if (this.numStep <= 6 || this.numStep >= 21 || (this.numStep >= 9 && this.numStep <= 14)) {
			Random rand = new Random();
			double aleaMaladie = rand.nextDouble();
			double aleaMeteo = rand.nextDouble();
			this.maladie_predateurs = -aleaMaladie / 5;
			this.meteo = aleaMeteo / 5 - 0.1;
			double qualiteProduction = maladie_predateurs + meteo;
//			this.journal.ajouter("qualité de la production : " + qualiteProduction);
			// double qualiteProduction = (Math.random() - 0.5) / 2.5 + 1; // entre 0.8 et
			// 1.2
			double nouveauStock = this.gestionnaireFeve.getStock(f)
					+ this.gestionnaireFeve.getProductionParStep(f) * (1 + qualiteProduction);
			this.gestionnaireFeve.setStock(this, f, nouveauStock);
		}
	}

	public double getCoutProdTotal() {
		double couts = 0.0;
		for (Feve f : gestionnaireFeve.getFeves()) {
			couts = +getCoutProduction(f) * gestionnaireFeve.getProductionParStep(f);
		}
		return couts;
	}

	public double balanceDuStep() {
		return beneficesDuMois - this.getCoutProdTotal();

	}

	public void payerCoutsProd() {
		soldeBancaire.setValeur(this, soldeBancaire.getValeur() - this.getCoutProdTotal());
	}

	public void retireVieuxContrats() {
		List<ContratCadre<Feve>> aEnlever = new ArrayList<ContratCadre<Feve>>();
		for (ContratCadre<Feve> c : this.contratsEnCours) {
			if (c.getQuantiteRestantALivrer() <= 0.0 && c.getMontantRestantARegler() <= 0.0) {
				aEnlever.add(c);
			}
		}
		for (ContratCadre<Feve> c : aEnlever) {
			this.contratsEnCours.remove(c);
		}
	}

	@Override
	public StockEnVente<Feve> getStockEnVente() {
		StockEnVente<Feve> res = new StockEnVente<Feve>();
		List<Feve> feves = gestionnaireFeve.getFeves();
		for (Feve feve : feves) {
			double stockRestant = this.gestionnaireFeve.getStock(feve);

			for (ContratCadre<Feve> cc : this.contratsEnCours) {
				if (Monde.LE_MONDE != null) {
					if (cc.getProduit() == feve) {
						stockRestant = stockRestant - cc.getQuantiteRestantALivrer();
					}
				}
			}
			// System.out.println(stockRestant);
			res.ajouter(feve, Math.max(0.0, stockRestant));

		}
		return res;
	}

// Begin Elsa

	/**
	 * Propose un nouvel echeancier au producteur
	 */
	public void proposerEcheancierVendeur(ContratCadre<Feve> cc) {

		if (!contratsEnCours.contains(cc)) {
			contratsEnCours.add(cc);
		}
		Echeancier e = cc.getEcheancier();
		if (e.getQuantiteTotale() > this.getStockEnVente().get(cc.getProduit())) { // On s assure que la quantité
																					// demandée est en stock
			Feve feveDuContrat = cc.getProduit();
			double production = this.gestionnaireFeve.getProductionParStep(feveDuContrat);
			int echSuppl = (int) ((e.getQuantiteTotale() - this.getStockEnVente().get(cc.getProduit())) / production);
			cc.ajouterEcheancier(new Echeancier(e.getStepDebut(), e.getNbEcheances() + echSuppl,
					cc.getQuantite() / (cc.getEcheancier().getNbEcheances() + echSuppl)));
			;

		} else {
			cc.ajouterEcheancier(new Echeancier(e)); // on accepte la proposition de l'acheteur car on a la quantite
														// en stock
		}
	}

	@Override
	public void proposerPrixVendeur(ContratCadre<Feve> cc) {
		// Négociation avec l'acheteur
		if (cc.getListePrixAuKilo().size() == 0) { // On vérifie qu'on a un prix à proposer
			cc.ajouterPrixAuKilo(getPrix(cc.getProduit(), cc.getQuantite()));
		} else {
		//On définit prixVendeur et prixAcheteur pour cette étape de négociation
		double prixVendeur = cc.getListePrixAuKilo().get(cc.getListePrixAuKilo().size() - 2); //On récupère le dernier prix proposé
		double prixAcheteur = cc.getListePrixAuKilo().get(cc.getListePrixAuKilo().size() - 1);
		
		if (prixAcheteur>=prixVendeur) {
			cc.ajouterPrixAuKilo(prixAcheteur);
		} else {
			if (prixVendeur == getCoutProduction(cc.getProduit()) * 1.01) { 
				//On pose une marge minimale de 1% du cout de production
				cc.ajouterPrixAuKilo(prixVendeur);
			} else {
				if (prixAcheteur < getCoutProduction(cc.getProduit())*1.01) {
					//On s'assure de conserver la marge minimale 
					prixVendeur = getCoutProduction(cc.getProduit()) * 1.01;
					cc.ajouterPrixAuKilo(prixVendeur);
				} else {
					if ((prixVendeur - prixAcheteur) < 0.05 * prixVendeur) {
							// On arrête la négociation si la différence de prix est suffisamment faible (5%
							// du prixVendeur)
						cc.ajouterPrixAuKilo(prixAcheteur);
					} else { 
						if (prixAcheteur >= 0.75 * prixVendeur && prixAcheteur * 1.1 >= getCoutProduction(cc.getProduit())) { 
							// on ne fait une proposition que si l'acheteur ne demande pas un prix trop bas, tout en respectant la marge minimale
							prixVendeur = prixAcheteur * 1.1; // on augmente le prix proposé par l'acheteur de 10%
							cc.ajouterPrixAuKilo(prixVendeur);
						} else {
							if (prixVendeur * 0.90 < getCoutProduction(cc.getProduit())) {

								//On s'assure de conserver notre marge minimale
								prixVendeur = getCoutProduction(cc.getProduit()) * 1.01;

								cc.getListePrixAuKilo().add(prixVendeur);
								} else {
									prixVendeur *= 0.90; // On diminue le prix proposé de 10%
									cc.ajouterPrixAuKilo(prixVendeur);
								}
							}
						}
					}
				}
			}
		}
	}
	
	// A modifier après détermination des couts de production
	// prix au kg
	public double getCoutProduction(Feve f) {
//		System.out.println("pour la feve "+ f.toString());
		// System.out.println("pour la feve "+ f.toString());
		double salaire = getSalaire(f);
//		System.out.println("le salaire total vaut : "+salaire);
		// System.out.println("le salaire total vaut : "+salaire);
		double coutsarbres = arbres.getPrixParStep(f);

//		System.out.println("le cout d'entretien des arbres est : "+coutsarbres);
//		System.out.println("la production par step est "+this.gestionnaireFeve.getProductionParStep(f));
//		System.out.println("en tout on paye : "+ (salaire + coutsarbres)/gestionnaireFeve.getProductionParStep(f));
		double coupProduction = (salaire + coutsarbres) / gestionnaireFeve.getProductionParStep(f);
		return coupProduction;

	}

	public double getSalaire(Feve f) {
		double cout = 0;
		if (f.isEquitable()) {
			cout += this.gestionnaireFeve.getProductionParStep(f) * 2 * salaire;
		} else {
			cout += this.gestionnaireFeve.getProductionParStep(f) * salaire;
		}
		return cout; // 750 francs CFA=1.29 US dollar
	}

	public boolean greve() {
		return true;
	}

	public double getProduction() {
		double production = 0;
		for (Feve f : this.gestionnaireFeve.getFeves()) {
			production += this.gestionnaireFeve.getProductionParStep(f);

		}
		return production;
	}

// End Elsa

// Begin Clément M	

	@Override
	public void notifierVendeur(ContratCadre<Feve> cc) {
		this.contratsEnCours.add(cc);

		contratsConclus = contratsConclus + 1;
		double prixKilo = this.gestionnaireFeve.getPrixVente(cc.getProduit());
		double quantite = cc.getQuantite();
		beneficesDuMois = beneficesDuMois + prixKilo * quantite;

	}

	@Override
	public void encaisser(double montant, ContratCadre<Feve> cc) {
		if (montant < 0.0) {
			throw new IllegalArgumentException("Appel de la methode encaisser de Producteur2 avec un montant negatif");
		}
		this.soldeBancaire.ajouter(this, montant);
	}

	public double getPrix(Feve produit, Double quantite) {
		double prixAPayer = 0;

		if (produit == null || quantite <= 0.0 || this.getStockEnVente().get(produit) < quantite) {
			return Double.NaN;
		}

		else {
			if (quantite > 10000000 && quantite < 20000000) {
				prixAPayer = this.gestionnaireFeve.getPrixVente(produit) * 0.95; // on réduit le prix de 5% si l'on
																					// commande plus de 10 000 T
			} else if (quantite > 20000000) {
				prixAPayer = this.gestionnaireFeve.getPrixVente(produit) * 0.9; // on réduit le prix de 10% si l'on
																				// commande plus de 20 000 T
			}

			else {
				prixAPayer = this.gestionnaireFeve.getPrixVente(produit);
			}

			if (this.contratsEnCours.size() >= 1) {
				ContratCadre<Feve> cc = this.contratsEnCours.get(this.contratsEnCours.size() - 1);
				double dernierPrix = cc.getPrixAuKilo(); // on recherche le prix auquel on a vendu la dernière fois
				if (dernierPrix > this.gestionnaireFeve.getPrixVente(produit) * 0.9
						&& this.gestionnaireFeve.getPrixVente(produit) * 1.05 < PRIX_MAX) {
					this.gestionnaireFeve.get(produit).setPrix(this,
							this.gestionnaireFeve.getPrixVente(produit) * 1.05);
				} // si l'on a vendu à plus de 90% du prix maximal, on augmente le prix initial de
					// 5%
				else if (dernierPrix < this.gestionnaireFeve.getPrixVente(produit) * 0.8
						&& this.gestionnaireFeve.getPrixVente(produit) * 0.95 > PRIX_MIN) {
					this.gestionnaireFeve.get(produit).setPrix(this,
							this.gestionnaireFeve.getPrixVente(produit) * 0.95);
				} // si l'on a vendu à moins de 80% du prix maximal, on diminue le prix initial de
					// 5%
			}
			return prixAPayer;
		}
	}

	@Override
	public double livrer(Feve produit, double quantite, ContratCadre<Feve> cc) {
		if (produit == null || !produit.equals(produit)) {
			throw new IllegalArgumentException(
					"Appel de la methode livrer de Producteur2 avec un produit ne correspondant pas aux feves produites");
		}

		double livraison = Math.min(quantite, this.gestionnaireFeve.getStock(produit));
		this.gestionnaireFeve.get(produit).getStockIndicateur().retirer(this, livraison);

		return livraison;
	}
	
	public void gestionSurfaceCultivee(Feve f) {
		if(Monde.LE_MONDE.getStep()>=20) {
			Historique productionList=this.gestionnaireFeve.get(f).getProductionIndicateur().getHistorique();
			
			int currentStep=Monde.LE_MONDE.getStep();
			int stepVise= currentStep-24; //TODO a modifier
			System.out.println(productionList.getTaille());
			
			int step=0;
			while (productionList.get(step).getEtape()<stepVise) {
				step+=1;
			}
			double stockCurrentStep = productionList.get(step).getValeur();
			
			step=0;
			while (productionList.get(step).getEtape()<currentStep) {
				step+=1;
			}
			double stockStepAnneeDerniere = productionList.get(step).getValeur();

			if (f==Feve.FORASTERO_MG_EQ) {
				if  (stockCurrentStep>stockStepAnneeDerniere)
					Arbre.surface_F_EQ*=0.75;
				else
					Arbre.surface_F_EQ*=1.1;
			}
			else if (f==Feve.FORASTERO_MG_NEQ) {
				if  (stockCurrentStep>stockStepAnneeDerniere)
					Arbre.surface_F_NEQ*=0.75;
				else
					Arbre.surface_F_NEQ*=1.1;
			}
			else if (f==Feve.MERCEDES_MG_EQ) {
				if  (stockCurrentStep>stockStepAnneeDerniere)
					Arbre.surface_M_EQ*=0.75;
				else
					Arbre.surface_M_EQ*=1.1;
			}
			else {
				if  (stockCurrentStep>stockStepAnneeDerniere)
					Arbre.surface_M_NEQ*=0.75;
				else
					Arbre.surface_M_NEQ*=1.1;
			}
		}
	}
}

// End Clément M
