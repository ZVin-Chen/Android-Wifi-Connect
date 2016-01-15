package com.zvin.wificonnect.migration;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
/**
 * xml 转换工具
 * @author lixiujuan
 *
 */
public class XmlUtil
{

	private static String DEFAULT_ENCODE = "UTF-8";
	
    public XmlUtil() {
    }

    private String docToString(NodeList list)
    {
        String returnValue = "";
//        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < list.getLength(); i++)
        {
            if(list.item(i).getNodeType() == 3 || list.item(i).getNodeType() == 4)
            {
            	if (list.item(i).getNodeType() == 4) {
    				returnValue = (new StringBuilder(String.valueOf(returnValue)))
    						.append("<![CDATA[").append(list.item(i).getNodeValue())
    						.append("]]>").toString();
            	} else {
            		returnValue = (new StringBuilder(String.valueOf(returnValue))).append(list.item(i).getNodeValue()).toString();
            	}
            } else
            {
//            	returnValue = (new StringBuilder(String.valueOf(returnValue))).append("\n<").append(list.item(i).getNodeName()).toString();
                returnValue = (new StringBuilder(String.valueOf(returnValue))).append("<").append(list.item(i).getNodeName()).toString();
//                sb.append("<").append(list.item(i).getNodeName());
                for(int j = 0; j < list.item(i).getAttributes().getLength(); j++)
                	returnValue = (new StringBuilder(String.valueOf(returnValue))).append(" ").append(list.item(i).getAttributes().item(j).getNodeName()).append("=\"").append(list.item(i).getAttributes().item(j).getNodeValue()).append("\"").toString();
//                    sb.append(" ").append(list.item(i).getAttributes().item(j).getNodeName()).append("=\"").append(list.item(i).getAttributes().item(j).getNodeValue()).append("\"");

                returnValue = (new StringBuilder(String.valueOf(returnValue))).append(">").toString();
//                sb.append(">");
            }
            if(list.item(i).getChildNodes().getLength() > 0)
            	returnValue = (new StringBuilder(String.valueOf(returnValue))).append(docToString(list.item(i).getChildNodes())).toString();
//                sb.append(docToString(list.item(i).getChildNodes()));
            if(list.item(i).getNodeType() != 3 && list.item(i).getNodeType() != 4)
            {
//                if(sb.toString().endsWith(">"))
                if(returnValue.endsWith(">")) {
//                	returnValue = (new StringBuilder(String.valueOf(returnValue))).append("\n").toString();
//                	returnValue = (new StringBuilder(String.valueOf(returnValue))).toString();
                }
                	
                returnValue = (new StringBuilder(String.valueOf(returnValue))).append("</").append(list.item(i).getNodeName()).append(">").append("\n").toString();
//                sb.append("</").append(list.item(i).getNodeName()).append(">").toString();
            }
        }
        return returnValue;
    }

    public static final String documentToString(Document document) {
        String returnValue = "";
        XmlUtil util = new XmlUtil();
        if (document != null) {
	        returnValue = util.docToString(document.getChildNodes()).trim();
	        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	        return header + returnValue;
        }
        return "";
    }
    
    public static String generateString(InputStream stream, String encoding) {
		InputStreamReader reader = null;
		BufferedReader br = null;
		try {
			if (encoding == null) {
				encoding = DEFAULT_ENCODE;
			}
			reader = new InputStreamReader(stream, encoding);
			
		} catch (UnsupportedEncodingException e1) {
			reader = new InputStreamReader(stream);
		}
		br = new BufferedReader(reader);
		StringBuffer sb = new StringBuffer();
//		int BUFFER_SIZE = 1024;  
		try {
//			char[] cbuf = new char[BUFFER_SIZE];
//			int number = 0;
//			while ((number = reader.read(cbuf, 0, BUFFER_SIZE)) != -1) {
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\r\n");
//				sb.append(String.valueOf(cbuf, 0, number));
			}
		} catch (IOException e) {
			e.printStackTrace();
//			Utils.log("IOException: " + e.getMessage());
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
    
    public static String documentToString2(Document doc) {
		/*try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty("encoding","GB2312");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(doc), new StreamResult(bos));
			return bos.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}*/
		return "";
	}
}
