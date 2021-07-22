package com.cd.bad.code;

import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class DocElement implements TocElementExtraction {

	@Override
	public String processElement(String xPathExpression, Element element) {
		return getProcessedDoc(xPathExpression, "", element);
	}
	
	private String getProcessedDoc(String xPathString, String jsonString, Element elem) {
		List<Attribute> list = elem.attributes();
		String titleAttrContent = elem.attributeValue("title");
		String fileAttrContent = elem.attributeValue("file");
		
		for (Attribute attribute : list) {
			jsonString = jsonString.concat("{");
			String attrName = attribute.getName();
			jsonString = jsonString.concat("'data':'").concat(titleAttrContent).concat("',");

			if (attrName.equals("key")) {
				String keyContent = elem.attributeValue("key");
				jsonString = jsonString.concat("'attr':{'id':'").concat(xPathString).concat("_dk:")
						.concat(keyContent).concat("','file':'").concat(fileAttrContent).concat("'}");
				break;
			} else if (attrName.equals("trnum")) {
				String trnumContent = elem.attributeValue("trnum");
				jsonString = jsonString.concat("'attr':{'id':'").concat(xPathString).concat("_dtrn:")
						.concat(trnumContent).concat("','file':'").concat(fileAttrContent).concat("'}");
				break;
			}
		}
		
		if (hasChildren(elem)) {
			jsonString = jsonString.concat(",'state':'closed'");
		}

		jsonString = jsonString.concat("},");
		return jsonString;
	}
	
	private boolean hasChildren(Element elem) {
		return elem.elements().size() > 0;
	}
}
