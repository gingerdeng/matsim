package playground.gregor.sim2d.simulation;

import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.MatsimXmlWriter;

public class StaticForceFieldWriter extends MatsimXmlWriter {
	
	private static final Logger log = Logger.getLogger(StaticForceFieldWriter.class);
	
	public static final String TAB = "\t";
	
	public static final String WHITESPACE = " ";
	
	public static final String OPEN_TAG_1 = "<";
	
	public static final String OPEN_TAG_2 = "</";
	
	public static final String CLOSE_TAG_1 = ">";
	
	public static final String CLOSE_TAG_2 = "/>";
	
	public static final String QUOTE = "\"";
	
	public static final String EQUALS = "=";
	
	public static final String DTD_LOCATION = "http://www.matsim.org/files/dtd";
	
	public static final String W3_URL = "http://www.w3.org/2001/XMLSchema-instance";
	
	public static final String XSD_LOCATION = "http://www.matsim.org/files/dtd/staticForceField.xsd";
	
	public static final String STATIC_FORCE_FIELD_TAG = "staticForceField";

	public static final String  STATIC_FORCE_TAG = "staticForce";

	public static final String X_COORD_TAG = "x";
	
	public static final String Y_COORD_TAG = "y";
	
	public static final String FORCE_X_TAG = "fx";
	
	public static final String FORCE_Y_TAG = "fy";
	

	
	public void write(String file, StaticForceField sff) {
		try {
			openFile(file);
			super.writeXmlHead();
			
			this.writer.write(OPEN_TAG_1);
			this.writer.write(STATIC_FORCE_FIELD_TAG);
			
			this.writer.write(WHITESPACE);
			this.writer.write("xmlns");
			this.writer.write(EQUALS);
			this.writer.write(QUOTE);
			this.writer.write(DTD_LOCATION);
			this.writer.write(QUOTE);
			
			this.writer.write(WHITESPACE);
			this.writer.write("xmlns:xsi");
			this.writer.write(EQUALS);
			this.writer.write(QUOTE);
			this.writer.write(W3_URL);
			this.writer.write(QUOTE);
			
			this.writer.write(WHITESPACE);
			this.writer.write("xsi:schemaLocation");
			this.writer.write(EQUALS);
			this.writer.write(QUOTE);
			this.writer.write(DTD_LOCATION);
			this.writer.write(WHITESPACE);
			this.writer.write(XSD_LOCATION);
			this.writer.write(QUOTE);
			this.writer.write(CLOSE_TAG_1);
			this.writer.write(NL);
			this.writer.write(NL);
		
			for (Force  force : sff.getForces()) {
				writeEvent(force);
				this.writer.write(NL);
				this.writer.write(NL);
			}
			
			this.writer.write(OPEN_TAG_2);
			this.writer.write(STATIC_FORCE_FIELD_TAG);
			this.writer.write(CLOSE_TAG_1);
			this.writer.write(NL);
			
			close();
		} catch (IOException e) {
			log.fatal("Error during writing static force field!", e);
		}
	}
	
	private void writeEvent(Force force) throws IOException {
		this.writer.write(TAB);
		this.writer.write(OPEN_TAG_1);
		this.writer.write(STATIC_FORCE_TAG);
		this.writer.write(WHITESPACE);
		this.writer.write(X_COORD_TAG);
		this.writer.write(EQUALS);
		this.writer.write(QUOTE);
		this.writer.write(Double.toString(force.getXCoord()));
		this.writer.write(QUOTE);
		this.writer.write(WHITESPACE);
		this.writer.write(Y_COORD_TAG);
		this.writer.write(EQUALS);
		this.writer.write(QUOTE);
		this.writer.write(Double.toString(force.getYCoord()));
		this.writer.write(QUOTE);
		this.writer.write(WHITESPACE);
		this.writer.write(FORCE_X_TAG);
		this.writer.write(EQUALS);
		this.writer.write(QUOTE);
		this.writer.write(Double.toString(force.getFx()));
		this.writer.write(QUOTE);
		this.writer.write(WHITESPACE);
		this.writer.write(FORCE_Y_TAG);
		this.writer.write(EQUALS);
		this.writer.write(QUOTE);
		this.writer.write(Double.toString(force.getFy()));
		this.writer.write(QUOTE);
		this.writer.write(CLOSE_TAG_2);
		this.writer.write(NL);

	}
	

}
