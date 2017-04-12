
/*  $Id: AnalyzeReportOfTransactions.java,v 1.1 2011/05/04 22:37:45 willuhn Exp $

    This file is part of HBCI4Java
    Copyright (C) 2001-2008  Stefan Palme

    HBCI4Java is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    HBCI4Java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.kapott.hbci.tools;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportPinTan;
import org.kapott.hbci.status.HBCIExecStatus;
import org.kapott.hbci.structures.Konto;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * <p>Tool zum Abholen und Auswerten von KontoauszÃ¼gen, gleichzeitig
 * Beispielprogramm fÃ¼r die Verwendung von <em>HBCI4Java</em>. Dieses Tool sollte nicht
 * out-of-the-box benutzt werden, da erst einige Anpassungen im Quelltext
 * vorgenommen werden mÃ¼ssen. Es dient eher als Vorlage, wie <em>HBCI4Java</em>
 * im konkreten Anwendungsfall eingesetzt werden kann.</p>
 * <p>Die Methode {@link #main(String[])} zeigt die Verwendung mit einem einzelnen Haupt-
 * Thread. die Methode {@link #main_multithreaded(String[])} skizziert die Implementierung
 * fÃ¼r Anwendungen mit mehreren Threads.</p>
 * <p>Im Quelltext mÃ¼ssen folgende Stellen angepasst werden:</p>
 * <ul>
 * <li><p>Beim Aufruf der Methode <code>HBCIUtils.init()</code> wird
 * der Name eines Property-Files angegeben, in welchem alle benÃ¶tigten
 * Kernel-Parameter aufgelistet sind. Diese Datei muss erst erzeugt
 * (Kopieren und Anpassen von <code>hbci.props.template</code>)
 * und der Dateiname beim Aufruf angepasst werden.</p></li>
 * <li><p>Zum Festlegen des abzufragenden Kontos wird zurzeit automatisch das
 * erste Konto benutzt, auf welches Ã¼ber HBCI zugegriffen werden kann. Ist
 * diese Information nicht verfÃ¼gbar (einige Banken senden keine Informationen
 * Ã¼ber die verfÃ¼gbaren Konten), oder soll eine andere Kontoverbindung
 * benutzt werden, so sind entsprechende Ãnderungen bei der Initialisierung
 * der Variablen <code>myaccount</code> vorzunehmen.</p></li>
 * <li><p>Soll der Kontoauszug nur ab einem bestimmten Zeitpunkt (und nicht alle
 * verfÃ¼gbaren Daten) abgeholt werden, so ist beim Erzeugen des entsprechenden
 * Auftrages das Startdatum einzustellen (im Quelltext zur Zeit auskommentiert).</p></li>
 * <li><p>AuÃerdem ist im Quelltext Code zur eigentlichen Auswertung der AuszÃ¼ge
 * zu implementieren. In dieser Vorlage wird nur nach einer fest codierten
 * Rechnungsnummer im Verwendungszweck gesucht. Der entsprechende Abschnitt im
 * Quelltext ist den eigenen BedÃ¼rfnissen anzupassen.</p></li>
 * </ul>
 * <p>AnschlieÃend kann der Quelltext compiliert und mit
 * <pre>java&nbsp;-cp&nbsp;...&nbsp;org.kapott.hbci.tools.AnalyzeReportOfTransactions</pre>
 * gestartet werden.</p>
 * <p>Der Quellcode dieser Klasse zeigt die prinzipielle Benutzung von <em>HBCI4Java</em>.
 * Wurde der HBCI-Zugang, der mit diesem Programm benutzt werden soll, noch nie verwendet,
 * so werden alle benÃ¶tigten Schritte zur Initialisierung der Zugangsdaten und
 * Sicherheitsmedien automatisch von <em>HBCI4Java</em> durchgefÃ¼hrt. Es ist nicht
 * nÃ¶tigt, fÃ¼r die Initialisierung von "frischen" Sicherheitsmedien speziellen
 * Code in die HBCI-Anwendung einzubauen -- die entsprechenden Aktionen werden
 * automatisch und vÃ¶llig transparent von <em>HBCI4Java</em> durchgefÃ¼hrt. Das hat
 * den Vorteil, dass jede beliebige Anwendung, die <em>HBCI4Java</em> als HBCI-Bibliothek
 * benutzt, gleichzeitig zum Initialisieren von HBCI-Sicherheitsmedien benutzt
 * werden kann, ohne dass dafÃ¼r spezieller Programmcode nÃ¶tig wÃ¤re. AuÃerdem wird dadurch
 * sichergestellt, dass nur initialisierte und funktionierende HBCI-Sicherheitsmedien
 * benutzt werden (weil <em>HBCI4Java</em> beim Laden eines Sicherheitsmediums automatisch
 * entsprechende ÃberprÃ¼fungen vornimmt).</p>
 */
public final class AnalyzeReportOfTransactions {
    public static void main(String[] args)
            throws Exception {

        HBCIUtils.refreshBLZList(ClassLoader.getSystemResource("blz.properties").openStream());

        Properties properties = new Properties();
        properties.put("kernel.rewriter", "InvalidSegment,WrongStatusSegOrder,WrongSequenceNumbers,MissingMsgRef,HBCIVersion,SigIdLeadingZero,InvalidSuppHBCIVersion,SecTypeTAN,KUmsDelimiters,KUmsEmptyBDateSets");
        properties.put("client.passport.default", "PinTanNoFile");
        properties.put("log.loglevel.default", "2");
        properties.put("default.hbciversion", "FinTS3");
        properties.put("client.passport.PinTan.checkcert", "1");
        properties.put("client.passport.PinTan.init", "1");

        // Initialize Bank Data
        properties.put("client.passport.country", "DE");
        properties.put("client.passport.blz", "blz");
        properties.put("client.passport.customerId", "account");

        // Initialize User Passport
        HBCIPassportPinTan passport = (HBCIPassportPinTan)AbstractHBCIPassport
                .getInstance(new HBCICallbackConsole(), properties);

        passport.setPIN("pin");

        // Initialize and use HBCI handle
        HBCIHandler hbciHandle = null;
        try {
            String version = passport.getHBCIVersion();
            hbciHandle = new HBCIHandler((version.length() != 0) ? version : "300", passport);

            // Read bank account statement
            analyzeReportOfTransactions(passport, hbciHandle);

        } finally {
            if (hbciHandle != null) {
                hbciHandle.close();
            } else if (passport != null) {
                passport.close();
            }
        }
    }

    private static void analyzeReportOfTransactions(HBCIPassport hbciPassport, HBCIHandler hbciHandle) {
        // Use first available HBCI account
        // If this does not work, use: Konto myaccount=new Konto("DE","86055592","1234567890");
        Konto myaccount = hbciPassport.getAccounts()[0];

        // Create HBCI job
        HBCIJob bankAccountStatementJob = hbciHandle.newJob("KUmsAll");
        bankAccountStatementJob.setParam("my", myaccount);

        // Set bank account statement retrieval date
        // bankAccountStatementJob.setParam("startdate","21.5.2003");

        bankAccountStatementJob.addToQueue();

        // Execute all jobs
        HBCIExecStatus ret = hbciHandle.execute();

        // GVRKUms = GeschÃ¤fts Vorfall Result Konto Umsatz
        GVRKUms result = (GVRKUms) bankAccountStatementJob.getJobResult();

        if (result.isOK()) {
            // Log bank account statement result
            System.out.println("************************** RESULT of **************************");
            System.out.println("****************  AnalyzeReportOfTransactions  ****************\n");
            System.out.println(result.toString());
            System.out.println("***************************************************************");

            List<UmsLine> lines = result.getFlatData();

            // Iterate revenue entries
            for (Iterator<UmsLine> j = lines.iterator(); j.hasNext(); ) {
                UmsLine entry = j.next();

                List<String> usages = entry.usage;

                // Iterate intended purpose (usage) entries
                for (Iterator<String> k = usages.iterator(); k.hasNext(); ) {
                    String usageline = k.next();

                    // Check for content
                    if (usageline.equals("Rechnung 12345")) {
                        // Evaluate revenue entry (<entry>)

                        // entry.bdate contains booking date
                        // entry.value contains booked Sum
                        // entry.usage contains die usage-lines
                        // for more information, see documentation ->
                        // org.kapott.hbci.GV_Result.GVRKUms
                    }
                }
            }
        } else {
            // Log error messages
            System.out.println("Job-Error");
            System.out.println(result.getJobStatus().getErrorString());
            System.out.println("Global Error");
            System.out.println(ret.getErrorString());
        }
    }

    @Deprecated
    public final void main_multithreaded(String[] str){};
}