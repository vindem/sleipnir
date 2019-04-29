/**
 * 
 */
package at.ac.tuwien.ec.model.infrastructure.planning.mobile.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;

/**
 * @author vincenzo
 *
 */
class SumoTraceParserTest {

	/**
	 * Test method for {@link at.ac.tuwien.ec.model.infrastructure.planning.mobile.utils.SumoTraceParser#parse(java.io.File, java.lang.String)}.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	@Test
	void testParse() throws ParserConfigurationException, SAXException, IOException {
		File testFile = new File("../traces/eichstatt.coords");
		String id="0.0";
		SumoTraceMobility trace = SumoTraceParser.parse(testFile, id);
		//TODO: make it a real unit test...
	}

}
