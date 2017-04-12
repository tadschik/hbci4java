package org.kapott.hbci.examples;

import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassportPinTanNoFile;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Properties;

/**
 * Created by matthiasfressdorf on 14.11.16.
 */
public class Configuration {
    private HBCIPassportPinTanNoFile passport;

    public Configuration() {
        try {
            File file = new File("src/org/kapott/hbci/examples/credentials.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            String bankId = document.getElementsByTagName("bankId").item(0).getTextContent();
            String accountId = document.getElementsByTagName("accountId").item(0).getTextContent();
            String pin = document.getElementsByTagName("pin").item(0).getTextContent();
            String countryId = document.getElementsByTagName("countryId").item(0).getTextContent();

            Properties properties = new Properties();
            properties.put("kernel.rewriter", "InvalidSegment,WrongStatusSegOrder,WrongSequenceNumbers,MissingMsgRef,HBCIVersion,SigIdLeadingZero,InvalidSuppHBCIVersion,SecTypeTAN,KUmsDelimiters,KUmsEmptyBDateSets");
            properties.put("client.passport.default", "PinTanNoFile");
            properties.put("log.loglevel.default", "2");
            properties.put("default.hbciversion", "FinTS3");
            properties.put("client.passport.PinTan.checkcert", "1");
            properties.put("client.passport.PinTan.init", "1");

            // Initialize Bank Data
            properties.put("client.passport.country", countryId);
            properties.put("client.passport.blz", bankId);
            properties.put("client.passport.customerId", accountId);

            HBCIUtils.refreshBLZList(ClassLoader.getSystemResource("blz.properties").openStream());

            // Initialize User Passport
            this.passport = (HBCIPassportPinTanNoFile) AbstractHBCIPassport
                    .getInstance(new HBCICallbackConsole(), properties);

            this.passport.setPIN(pin);

        } catch (Exception e){
            System.out.println("Error reading configuration file:");
            System.out.println(e.getMessage());
        }
    }

    public HBCIPassportPinTanNoFile getPassport() {
        return passport;
    }

}
