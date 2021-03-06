package io.github.luzzu.annotations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.RDF;

import io.github.luzzu.operations.properties.PropertyManager;
import io.github.luzzu.qualityproblems.ProblemCollection;
import io.github.luzzu.semantics.commons.ResourceCommons;
import io.github.luzzu.semantics.vocabularies.QPRO;

/**
 * @author Jeremy Debattista
 * 
 * The ProblemReport Class provides a number of methods
 * to enable the representation of problematic triples 
 * found during the assessment of Linked Datasets.
 * This class describes these problematic triples in
 * terms of either Refied RDF or a Sequence of Resources.
 * The Quality Report description can be found in
 * @see src/main/resource/vocabularies/QPRO/QPRO.trig
 * 
 */
public class ProblemReportTDB {
	
	protected String TDB_DIRECTORY = PropertyManager.getInstance().getProperties("luzzu.properties").getProperty("TDB_TEMP_BASE_DIR")+"tdb_"+UUID.randomUUID().toString()+"/";
	protected Dataset dataset = TDBFactory.createDataset(TDB_DIRECTORY);
	
	
	private Model m = dataset.getDefaultModel();
	private Resource reportURI;

	public ProblemReportTDB(Resource computedOn){
		TDB.sync(dataset);
		dataset.begin(ReadWrite.WRITE);
		
		
		reportURI = ResourceCommons.generateURI();
		m.add(new StatementImpl(reportURI, RDF.type, QPRO.QualityReport));
		m.add(new StatementImpl(reportURI, QPRO.computedOn, computedOn));
	}
	
	
	/**
	 * Create instance triples corresponding towards a Quality Report
	 * 
	 * @param computedOn - The resource URI of the dataset computed on
	 * @param problemCollection - A list of quality problem collections
	 * 
	 */
	public void addToQualityProblemReport(ProblemCollection<?> problemCollection){
		this.m.add(new StatementImpl(this.reportURI, QPRO.hasProblem, problemCollection.getProblemURI()));
		this.m.add(problemCollection.getDataset().getNamedModel(problemCollection.getNamedGraph()));

		problemCollection.cleanup();
	}
	

	public void serialiseToFile(File file) throws IOException {
		file.createNewFile();
		OutputStream out = new FileOutputStream(file, false);
		RDFDataMgr.write(out, m, RDFFormat.TURTLE_PRETTY);
	}
	
	public void flush(){
		dataset.close();
	}
	
	public void cleanup() {
		File f = new File(TDB_DIRECTORY);
		f.delete();
	}
}
