package org.kapott.hbci.GV.parsers;

import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.sepa.jaxb.pain_001_001_02.*;

import javax.xml.bind.JAXB;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;


/**
 * Parser-Implementierung fuer Pain 001.001.02.
 */
public class ParsePain00100102 extends AbstractSepaParser<List<HashMap<String, String>>> {
    /**
     * @see org.kapott.hbci.GV.parsers.ISEPAParser#parse(InputStream, Object)
     */
    public void parse(InputStream xml, List<HashMap<String, String>> sepaResults) {
        Document doc = JAXB.unmarshal(xml, Document.class);
        Pain00100102 pain = doc.getPain00100102();

        if (pain == null)
            return;

        PaymentInstructionInformation4 pmtInf = pain.getPmtInf();

        //Payment Information - Credit Transfer Transaction Information
        List<CreditTransferTransactionInformation2> txList = pmtInf.getCdtTrfTxInf();

        for (CreditTransferTransactionInformation2 tx : txList) {
            HashMap<String, String> prop = new HashMap();

            put(prop, Names.PMTINFID, pmtInf.getPmtInfId());
            put(prop, Names.SRC_NAME, pain.getGrpHdr().getInitgPty().getNm());
            put(prop, Names.SRC_IBAN, pmtInf.getDbtrAcct().getId().getIBAN());
            put(prop, Names.SRC_BIC, pmtInf.getDbtrAgt().getFinInstnId().getBIC());

            put(prop, Names.DST_NAME, tx.getCdtr().getNm());
            put(prop, Names.DST_IBAN, tx.getCdtrAcct().getId().getIBAN());
            put(prop, Names.DST_BIC, tx.getCdtrAgt().getFinInstnId().getBIC());

            EuroMax9Amount amt = tx.getAmt().getInstdAmt();
            put(prop, Names.VALUE, SepaUtil.format(amt.getValue()));
            put(prop, Names.CURR, amt.getCcy());

            if (tx.getRmtInf() != null) {
                put(prop, Names.USAGE, tx.getRmtInf().getUstrd());
            }

            XMLGregorianCalendar date = pmtInf.getReqdExctnDt();
            if (date != null) {
                put(prop, Names.DATE, SepaUtil.format(date, null));
            }

            PaymentIdentification1 pmtId = tx.getPmtId();
            if (pmtId != null) {
                put(prop, Names.ENDTOENDID, pmtId.getEndToEndId());
            }

            sepaResults.add(prop);
        }
    }
}
