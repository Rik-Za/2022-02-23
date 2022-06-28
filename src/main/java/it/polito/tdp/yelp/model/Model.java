package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	private Graph<Review, DefaultWeightedEdge> grafo;
	private YelpDao dao;
	private Map<String,Review> vertici;
	private List<Coppia> archi;
	private int max;
	
	private List<Review> migliore;
	private double pesoOttimo;
	
	public Model() {
		this.dao= new YelpDao();
		this.vertici= new HashMap<>();
		this.archi= new ArrayList<>();
	}
	
	public String creaGrafo(Business b) {
		this.grafo= new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		//aggiungo vertici
		this.vertici= this.dao.getVertici(b);
		Graphs.addAllVertices(this.grafo, this.vertici.values());
		//aggiungo archi
		this.archi= this.dao.getArchi(b, vertici);
		for(Coppia c: archi)
			if(c.getPeso()!=0)
				Graphs.addEdge(this.grafo, c.getR1(), c.getR2(), c.getPeso());
		String s="GRAFO CREATO!\n";
		s+="#VERTICI: "+this.grafo.vertexSet().size()+"\n";
		s+="#ARCHI: "+this.grafo.edgeSet().size()+"\n";
		return s;
		
	}
	
	public List<Review> getMassimo(){
		List<Review> ris = new ArrayList<Review>();
		this.max=0;
		int num=0;
		for(Review r: this.grafo.vertexSet()) {
			num = this.grafo.outgoingEdgesOf(r).size();
			if(num>max)
				max=num;
		}
		for(Review r: this.grafo.vertexSet()) {
			if(this.grafo.outgoingEdgesOf(r).size()==max)
				ris.add(r);
		}
		return ris;	
	}
	
	public List<Review> calcolaPercorso(){
		this.migliore= new ArrayList<>();
		this.pesoOttimo=0;
		List<Review> parziale = new ArrayList<>();
		for(Review r: this.grafo.vertexSet())
		{
			parziale.add(r);
			cerca(parziale);
			parziale.remove(parziale.size()-1);
		}
		
		return migliore;
	}
	
	private void cerca(List<Review> parziale) {
		//Terminazione non presente, controllo ogni volta
		if(parziale.size()>migliore.size()) {
			migliore = new ArrayList<>(parziale);
			pesoOttimo = calcolaPeso(migliore);
		}
		//Continuo la ricorsione
		Review ultimo = parziale.get(parziale.size()-1);
		for(DefaultWeightedEdge e: this.grafo.outgoingEdgesOf(ultimo)) {
			Review r = this.grafo.getEdgeTarget(e);
			if(r.getStars()>=ultimo.getStars() && !parziale.contains(r)) {
				parziale.add(r);
				cerca(parziale);
				parziale.remove(parziale.size()-1);
			}
		}
	}

	private double calcolaPeso(List<Review> parziale) {
		double peso=0;
		for(int i=0; i<parziale.size()-1;i++) {
			Review da = parziale.get(i);
			Review a = parziale.get(i+1);
			DefaultWeightedEdge e = this.grafo.getEdge(da, a);
			peso+=this.grafo.getEdgeWeight(e);
		}
		return peso;
	}
	
	public double getPesoOttimo() {
		return this.pesoOttimo;
	}

	public int getMax() {
		return this.max;
	}
	
	public List<String> getCitta(){
		return this.dao.getCitta();
	}
	
	public List<Business> getLocaliCitta(String citta){
		List<Business> l = new ArrayList<>(this.dao.getBusinessCitta(citta));
		Collections.sort(l);
		return l;
	}
}
