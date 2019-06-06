package abstraction.eq5Distributeur1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.Set;

import abstraction.eq7Romu.produits.Chocolat;
import abstraction.fourni.IActeur;

public class Stock {
	private HashMap<Chocolat, Double> stock;

	/** @author Estelle BONNET */
	public Stock() {
		this.stock = new HashMap<Chocolat, Double>();
	}

	/**
	 * Si produit figure deja dans le stock en vente, actualise la quantite du produit a stock.
	 * Sinon, ajoute qu'il y a une quantite stock de produit dans le stock en vente
	 * @param produit
	 * @param quantite mise en vente du produit
	 */
	/** @authors Erine DUPONT & Estelle BONNET 
	 * V2 @author Estelle Bonnet
	 */
	
	public void ajouter(Chocolat produit, Double quantite, IActeur acteur) {
		if (quantite<0.0) {
			throw new IllegalArgumentException("Appel de ajouter(produit, quantite) de Stock avec quantite<0.0 (=="+quantite+")");
		} else {
			this.stock.put(produit, this.get(produit) + quantite);
		}
	}
	
	/** @authors Erine DUPONT & Estelle BONNET 
	 * V2 @author Estelle Bonnet
	 */
	public void enlever(Chocolat produit, Double quantite, IActeur acteur) {
		if (quantite<0.0) {
			throw new IllegalArgumentException("Appel de enlever(produit, quantite) de Stock "
					+ "avec quantite<0.0 (=="+quantite+")");
		} else if (this.get(produit) < quantite) {
			throw new IllegalArgumentException("Appel de enlever(produit, quantite) de Stock "
					+ "avec stock (=="+ this.get(produit) + ") < quantite (==" + quantite + ")");
		} else {
			this.stock.put(produit, this.get(produit)- quantite);
		}
	}
	
	/** @author Estelle BONNET */
	public List<Chocolat> getProduitsEnVente() {
		ArrayList<Chocolat> produits=new ArrayList<Chocolat>();
		produits.addAll(this.stock.keySet());
		return produits;
	}

	/** @author Estelle BONNET */
	public Double get(Chocolat produit) {
		return (this.stock.containsKey(produit)? this.stock.get(produit) : 0.0) ;
	}

	/** @author Estelle BONNET */
	public String toString() {
		return this.stock.toString();
	}
	
	/** @author Estelle BONNET */
	public String toHtml() {
		String res = "";
		for (Chocolat produit : this.stock.keySet()) {
			res+=produit+":"+String.format("%.3f",this.stock.get(produit))+"<br>";
		}
		return res;
	}

	/*
	public static void main(String[] args) {
		StockEnVente<Chocolat> s = new StockEnVente<Chocolat>();
		s.ajouter(Chocolat.MG_E_SHP, 120.0);
		s.ajouter(Chocolat.MG_NE_SHP, 200.0);
		System.out.println(s);
		s.ajouter(Chocolat.MG_E_SHP, 300.0);
		System.out.println(s);
	}
	*/
}