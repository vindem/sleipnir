/**
 * 
 */
package at.ac.tuwien.ec.model.infrastructure.planning.mobile.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
	void testPreParse() throws ParserConfigurationException, SAXException, IOException {
		File testFile = new File("./traces/simmering.coords");
		ArrayList<String> ids = new ArrayList<String>();
		ids.add("0.0");
		SumoTraceParser.preParse(testFile, ids);
		//TODO: make it a real unit test...
	}

}
