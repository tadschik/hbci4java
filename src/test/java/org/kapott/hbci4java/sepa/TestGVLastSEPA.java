package org.kapott.hbci4java.sepa;

import org.junit.Test;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci4java.AbstractTestGV;

import java.util.Properties;

/**
 * Testet die Ausfuehrung einer SEPA-Lastschrift.
 */
public class TestGVLastSEPA extends AbstractTestGV {
    /**
     * Testet das Ausfuehren einer SEPA-Lastschrift.
     */
    @Test
    public void test() {
        this.execute(new Execution() {

            @Override
            public String getJobname() {
                return "LastSEPA";
            }

            /**
             * @see org.kapott.hbci4java.AbstractTestGV.Execution#configure(org.kapott.hbci.GV.AbstractHBCIJob, org.kapott.hbci.passport.HBCIPassport, java.util.Properties)
             */
            @Override
            public void configure(AbstractHBCIJob job, HBCIPassport passport, Properties params) {
                Konto acc = new Konto();
                acc.blz = params.getProperty("blz");
                acc.number = params.getProperty("konto");
                acc.name = params.getProperty("name");
                acc.bic = params.getProperty("bic");
                acc.iban = params.getProperty("iban");
                job.setParam("dst", acc);

                int idx = Integer.parseInt(params.getProperty("passport_index", "0"));
                job.setParam("src", passport.getAccounts().get(idx));

                String value = params.getProperty("value", "1");
                job.setParam("btg", new Value(Integer.parseInt(value), "EUR"));
                job.setParam("usage", params.getProperty("usage"));
                job.setParam("targetdate", params.getProperty("date"));
                job.setParam("creditorid", params.getProperty("creditorid"));
                job.setParam("mandateid", params.getProperty("mandateid"));
                job.setParam("manddateofsig", params.getProperty("date_of_sig"));
            }

        });
    }
}
