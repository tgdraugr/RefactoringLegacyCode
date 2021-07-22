package com.cd.bad.code;

import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class FolderElement implements TocElementExtraction {

	@Override
	public String processElement(String xPathExpression, Element element) {
		return getProcessedFolder(xPathExpression, "", element);
	}
	
	private String getProcessedFolder(String xPathString, String jsonString, Element elem) {
		jsonString = jsonString.concat("{");

		List<Attribute> list = elem.attributes();
		String titleAttrContent = elem.attributeValue("title");
		String fileAttrContent = elem.attributeValue("file");

		for (Attribute attribute : list) {
			String attrName = attribute.getName();
			jsonString = jsonString.concat("'data':'").concat(titleAttrContent).concat("',");

			if (attrName.equals("key")) {
				String keyContent = elem.attributeValue("key");
				jsonString = jsonString.concat("'attr':{'id':'").concat(xPathString).concat("_fk:")
						.concat(keyContent).concat("'}");
				if (fileAttrContent != null) {
					jsonString = jsonString.concat("','file':'").concat(fileAttrContent).concat("'}");
				}
				break;
			} else if (attrName.equals("type")) {
				String typeContent = elem.attributeValue("type");

				if (typeContent == "history") {
					jsonString = jsonString.concat("'attr':{'id':'").concat(xPathString).concat("_fth,");
				}
				break;
			}
		}
		jsonString = jsonString.concat("},");
		return jsonString;
	}
}
