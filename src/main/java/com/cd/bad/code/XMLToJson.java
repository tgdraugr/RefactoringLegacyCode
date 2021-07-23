package com.cd.bad.code;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.*;

/**
 * fk--folder key,
 * dk--doc key --> value is key string 
 * dt--doc type,
 * ft--folder
 * type --> both have 19 key options: bom tr amend history help info loeid cust
 * cust0-cust9 custhist folder type only seen as "history" in toc -> why ?? do we use other ones?
 * 
 * -- it only has one child doc type only seen two "tr" and "history" in toc??
 * -- it only has one child node with "folder type="history"" is the only child of it parent
 * either "<doc type="tr" key="xxx" trnum="xxx"....." or "<doc type="tr" trnum="xxx"" <folder type="history"
 * could has more then one doc children .e.g. title="History of AMM31-32-00-720-807" in 700/amm
 *
 *
 * folder element has the following to identify itself: 
 * - 1, key 2, type="history",
 * in this case, folder is the only child of doc element with type ="tr"?
 *
 * doc element has the following to identify itself:
 * - 1, key 2, type="tr" trnum="xxxxx" 3, type="history"
 * Is doc the only child of a folder element?
 *
 *
 * the return json format likes following: [ { "data" : "A node", "children" ,
 * "state" : "open" }, { "data" : "Only child", "state" : "closed" }, "Ajax
 * node" ]
 */

public class XMLToJson {
	private static final String EMPTY_STRING = "";
	private static final Map<String, TocElement> ELEMENTS;
	
	static {
		ELEMENTS = new HashMap<>();
		ELEMENTS.put("folder", new FolderElement());
		ELEMENTS.put("doc", new DocElement());
	}

	public String getJson(URL url, String xPathString) throws Exception {
		Document TOCDoc = getDocument(url);
		return getJsonFromDocument(xPathString, TOCDoc);
	}
	
	public String getJsonFromDocument(String xPathString, Document TOCDoc) throws Exception {
		String jsonString = "[";

		Element node = getNode(xPathString, TOCDoc);

		for (Iterator<Element> i = node.elementIterator(); i.hasNext();) {
			jsonString = jsonString.concat(getProcessedElement(xPathString, i.next()));
		}
		
		return getClosedJson(jsonString);
	}
	
	private Document getDocument(URL url) throws DocumentException {
		SAXReader reader = new SAXReader();
		return reader.read(url);	
	}

	private String getClosedJson(String jsonString) {
		if (jsonString.length() > 1) {
			jsonString = jsonString.substring(0, jsonString.length() - 1);			
		}
		jsonString = jsonString.concat("]");
		return jsonString;
	}
	
	private String getProcessedElement(String xPathExpression, Element element) {
		String elementName = element.getName();
		if (ELEMENTS.containsKey(elementName)) {
			return ELEMENTS.get(elementName).processedElement(xPathExpression, element);
		}
		return EMPTY_STRING;	
	}
	
	private Element getNode(String xPathString, Document TOCDoc) throws Exception {
		if (xPathString.equals("/")) {
			return TOCDoc.getRootElement();
		} else {
			String realXPathString = XPathPatterns.xPathFrom(xPathString);
			return (Element) TOCDoc.selectSingleNode(realXPathString);
		}
	}
	
	public static class XPathPatterns {
		private static final String FOLDER_KEY = "fk";
		private static final String FOLDER_TYPE = "ft";
		private static final String FOLDER_TYPE_AS_HISTORY = "fth";
		private static final String DOC_KEY = "dk";
		private static final String DOC_TYPE = "dt";
		private static final String DOC_TYPE_AS_HISTORY = "dth";
		private static final String DOC_TRNUM = "dtrn";
		private static final Map<String, String> XPATHEXPRESSIONS_BY_INTERNALEXPRESSION;
		
		static {
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION = new HashMap<String, String>();
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.put(FOLDER_KEY, "folder[@key");
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.put(FOLDER_TYPE, "folder[@type");
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.put(FOLDER_TYPE_AS_HISTORY, "folder[@type='history'");
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.put(DOC_KEY, "doc[@key");
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.put(DOC_TYPE, "doc[@type");
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.put(DOC_TYPE_AS_HISTORY, "doc[@type='history'");
			XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.put(DOC_TRNUM, "doc[@trnum");
		}

		/**
		 * post string looks like :
		 * 
		 * "fk:LOETR_dtrn:TR12-118_fth_dth" it represents the inner doc element:
		 * <folder key="LOETR" type="loetr"
		 * title="List of Effective TRs" file="loetr.html"> <doc type="tr"
		 * trnum="TR12-118" trdate="May 07/2012"
		 * title="[TR12-118] TASK AMM12-31-00-660-806 - Inspection and Removal of De-Hydrated Anti-Icing Fluid inside the Flight Control Surfaces"
		 * file="TR12-118.pdf" refloc="AMM12-31-00-660-806"> <folder type="history"
		 * title="History of AMM12-31-00-660-806"> <doc
		 * title="TASK 12-31-00-660-806 - Inspection and Removal of De-Hydrated Anti-Icing Fluid inside the Flight Control Surfaces"
		 * file="AMM12-31-00-660-806.pdf" type="history" refloc="AMM12-31-00-660-806"/>
		 * </folder> 
		 * 
		 * the xpath string should be:
		 * 
		 * folder[@key="LOETR"]/doc[@trnum="TR12-118"]/folder[@type="history"]/doc[@type
		 * ="history"]
		 *
		 *
		 * the String : "fk:AMM24_fk:AMM24-FM_dk:CTOC-24" it represents the inner doc with attribute file="CTOC-24.pdf" 
		 * 
		 * the string : "fk:AMM24_fk:AMM24-00-00_fk:AMM24-00-00-02_dk:AMM24-00-00-700-801" represents
		 * <folder key="AMM24" title="CH 24 - Electrical Power"> <folder key="AMM24-FM"
		 * title="Front Matter"> <doc key="CTOC-24" title="Table of Contents"
		 * file="CTOC-24.pdf"/> </folder> <folder key="AMM24-00-00"
		 * title="24-00-00 - General"> <folder key="AMM24-00-00-02"
		 * title="General - Maintenance Practices"> <doc key="AMM24-00-00-700-801"
		 * title="TASK 24-00-00-700-801 - AC Power, DC Power and Battery Maintenance Practice Recommendations"
		 * file="AMM24-00-00-700-801.pdf"/>
		 *
		 * it can be even optimised as: "fk:AMM24_fk:00-00_fk:02_dk:AMM24-00-00-700-801"
		 * 
		 * if the inner key fully includes the previous key, omit it. Otherwise use full string the xpath string should be:
		 * folder[@key="AMM24"]/folder[@key="AMM24-00-00"]/folder[@key="AMM24-00-00-02"]/doc[@key="AMM24-00-00-700-801"]
		 *
		 * if shortXPath is ?? which means the query based on the root of the document
		 */
		public static String xPathFrom(String shortXPath) throws Exception {
			String targetString = null;
			if (shortXPath.equals("")) {
				targetString = "//toc";
			} else {
				targetString = "//";
			}

			int start = 0;
			int underscorePosition = shortXPath.indexOf("_", start);
			
			while (underscorePosition > -1) {
				String substring = shortXPath.substring(start, underscorePosition);
				start = underscorePosition + 1;
				targetString = withUnfinishedFilledValue(targetString, substring);
				underscorePosition = shortXPath.indexOf("_", start);
			}
			
			return withFinishedFilledValue(targetString, shortXPath.substring(start));
		}
		
		private static String withFinishedFilledValue(String target, String substring) {
			return withFilledValue(target, substring, "']");
		}
		
		private static String withUnfinishedFilledValue(String target, String substring) {
			return withFilledValue(target, substring, "']/");
		}

		private static String withFilledValue(String target, String substring, String closure) {
			if (substring.indexOf(":") > 0) {
				int keyValueSepPos = substring.indexOf(":");
				String keyString = substring.substring(0, keyValueSepPos);
				String valueString = substring.substring(keyValueSepPos + 1);
				target = target.concat(XPATHEXPRESSIONS_BY_INTERNALEXPRESSION.get(keyString));
				target = target.concat("='").concat(valueString).concat(closure);
			}
			return target;
		}
	}
}