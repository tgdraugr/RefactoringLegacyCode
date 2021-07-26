package com.cd.bad.code;

import org.approvaltests.Approvals;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.net.URL;

public class XMLToJsonTest {
	@Test
	public void shouldTranslateEmptyXMLToJson() throws Exception {
		XMLToJson translate = new XMLToJson();

		URL url = new URL("file:./src/test/resources/toc.xml");
		String xPathString = "fk:AMM24_fk:AMM24-00-00_fk:AMM24-00-00-02";

		Approvals.verify(translate.getJson(url, xPathString));
	}
	
	@Test
	public void shouldParseEmptyFolder() throws Exception {
		final XMLToJson translate = new XMLToJson();
		final Document doc = docFor("<folder></folder>");
		assertEquals("[]", translate.getJsonFromDocument("/", doc));
	}
	
	@Test
	public void shouldParseEmptyDoc() throws Exception {
		final XMLToJson translate = new XMLToJson();
		final Document doc = docFor("<folder><doc/></folder>");
		assertEquals("[{}]", translate.getJsonFromDocument("/", doc));
	}
	
	@Test
	public void shouldParseSimpleDoc() throws Exception {
		final XMLToJson translate = new XMLToJson();
		final Document doc = docFor("<folder><doc title=\"test_title\"/></folder>");
		assertEquals("[{'data':'test_title'}]", translate.getJsonFromDocument("/", doc));
	}
	
	@Test
	public void shouldMapTocPatternsIntoXPathPatterns() throws Exception {
		XPathProvider provider = new TocQueryExpressions();
		assertEquals("//folder[@key='AMM24']/folder[@key='AMM24-00-00']/folder[@key='AMM24-00-00-02']", 
				provider.xPathFrom("fk:AMM24_fk:AMM24-00-00_fk:AMM24-00-00-02"));
	}

	private Document docFor(String xml) throws DocumentException {
		var reader = new SAXReader();
		return reader.read(new ByteArrayInputStream(xml.getBytes()));
	}
}
