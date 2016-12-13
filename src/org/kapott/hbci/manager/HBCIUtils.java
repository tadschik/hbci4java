
/*  $Id: HBCIUtils.java,v 1.2 2011/11/24 21:59:37 willuhn Exp $

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

package org.kapott.hbci.manager;

import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.comm.Comm;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.InvalidArgumentException;
import org.kapott.hbci.exceptions.InvalidUserDataException;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.swift.Swift;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.*;
import java.util.*;
import java.util.Map.Entry;

/** <p>Hilfsklasse f�r diverse Tools. Diese Klasse definiert nur statische
    Methoden und Konstanten. Sie kann nicht instanziiert werden. </p>
    <p>Kernel-Parameter k�nnen zu jedem beliebigen Zeitpunkt der Laufzeit einer Anwendung gesetzt werden.
    Das Setzen eines Kernel-Parameters geschieht mit der Methode <code>setParam()</code>. Dieser Methode
    werden der Name eines Kernel-Parameters sowie der neue Wert f�r diesen Parameter �bergeben. Alternativ
    bzw. in Verbindung mit dieser Variante k�nnen diese Parameter in einer Datei abgelegt werden, die
    beim Initialiseren des HBCI-Kernels eingelesen wird (via <code>Properties.load()</code>).
    Folgende Kernel-Parameter werden zur Zeit von verschiedenen Subsystemen des HBCI-Kernels ausgewertet:</p>
    <ul>
      <li><code>client.product.name</code> und <code>client.product.version</code>
          <p>Diese beiden Parameter identifizieren die HBCI-Anwendung. Diese 
          Daten werden von einigen HBCI-Servern ausgewertet, um bestimmte 
          HBCI-Anwendungen besonders zu unterst�tzen. Werden diese 
          Parameter nicht explizit belegt, so erhalten sie die 
          Standardwerte "HBCI4Java" und "2.5". Es wird empfohlen, diese Werte 
          nicht zu �ndern.</p></li>
      <li><code>client.passport.DDV.path</code> (f�r DDV-Passports)
          <p>Hier wird eingestellt, wo die Datei 
          mit den Zusatzdaten ("Hilfsmedium") gespeichert werden soll. Der 
          Dateiname f�r das Hilfsmedium setzt sich zusammen aus dem Wert 
          dieses Parameters sowie der Seriennummer der HBCI-Chipkarte. 
          Ein Wert von "<code>/home/hbci/passports/</code>" f�hrt also zur Speicherung 
          der Dateien im Verzeichnis <code>/home/hbci/passports</code>, wobei der 
          Dateiname nur aus der Seriennummer der Chipkarte besteht ("/" am 
          Ende beachten!). Ein Wert von "<code>/home/hbci/passports/card-</code>" f�hrt 
          ebenfalls zur Speicherung der Dateien im Verzeichnis 
          <code>/home/hbci/passports</code>, allerdings bestehen die Dateinamen jetzt 
          aus dem Prefix card- sowie der Seriennummer der Chipkarte.</p>
          <p>In der Regel wird hier nur eine Pfadangabe verwendet werden, 
          dabei darf aber auf keinen Fall der Slash (oder Backslash unter 
          Windows) vergessen werden, da der Dateiname aus einer simplen 
          Aneinanderkettung von Parameterwert und Seriennummer besteht.</p></li>
      <li><code>client.passport.DDV.libname.ddv</code> (f�r DDV-Passports)
          <p>Hier wird der vollst�ndige 
          Dateiname (also mit Pfadangabe) der shared library (dynamisch 
          ladbaren Bibliothek) angegeben, welche als Bindeglied zwischen 
          Java und der CTAPI-Bibliothek f�r den Chipkartenleser fungiert. 
          Diese Bibliothek wird bereits mit dem <em>HBCI4Java</em>-Paket 
          mitgeliefert.</p></li>
      <li><code>client.passport.DDV.libname.ctapi</code> (f�r DDV-Passports)
          <p>Mit diesem Parameter wird 
          der komplette Dateiname (mit Pfad) der CTAPI-Bibliothek 
          eingestellt, die die CTAPI-Schnittstelle f�r den zu 
          verwendenden Chipkartenleser implementiert. Diese Bibliothek 
          ist vom Hersteller des Chipkartenterminals zu beziehen.</p></li> 
      <li><code>client.passport.DDV.port</code> (f�r DDV-Passports)
          <p>Die logische Portnummer, an der der 
          Chipkartenleser angeschlossen ist (i.d.R. 0, 1 oder 2, abh�ngig 
          vom Anschluss (COM1, COM2, USB) und vom Treiber (manche Treiber 
          beginnen mit der Z�hlung bei 1, andere bei 0)) (am besten 
          ausprobieren). Achtung -- unter UN*X darauf achten, dass f�r 
          den ausf�hrenden Nutzer Schreib- und Leserechte auf das 
          entsprechende Device (/dev/ttyS0, /dev/ttyUSB0 o.�.) bestehen.</p></li>
      <li><code>client.passport.DDV.ctnumber</code> (f�r DDV-Passports)
          <p>Die logische Nummer des 
          Chipkartenterminals, die im weiteren Verlauf verwendet werden 
          soll. Dies ist i.d.R. 0, falls mehrere Chipkartenterminals 
          angeschlossen und in Benutzung sind, sind diese einfach 
          durchzunummerieren.</p></li>
      <li><code>client.passport.DDV.usebio</code> (f�r DDV-Passports)
          <p>Dieser Parameter kann entweder 0, 1 oder -1 sein und hat
          nur Bedeutung, wenn die PIN-Eingabe direkt am Chipkartenterminal
          erfolgt (also wenn <code>client.passport.DDV.softpin</code> ungleich
          1 ist und wenn ein Keypad am Chipkartenterminal vorhanden ist).</p> 
          <p>Wenn dieser Wert auf 1 gesetzt wird, so bedeutet 
          das, dass die PIN-Eingabe nicht manuell erfolgt, sondern dass 
          statt dessen biometrische Merkmale des Inhabers ausgewertet 
          werden. Zurzeit ist dieses Feature speziell auf den 
          Chipkartenleser PinPad-Bio von Reiner-SCT angepasst, bei dem 
          einem Fingerabdruck eine PIN zugeordnet werden kann, deren 
          Eingabe beim Auflegen des Fingers simuliert wird. F�r 
          andere biometrief�hige Chipkartenterminals wird dieser 
          Parameter wahrscheinlich nicht funktionieren, entsprechende 
          Unterst�tzung ist aber geplant.</p>
          <p>Durch das Setzen dieses Wertes auf 0 wird das Benutzen der
          Biometrie-Einheit definitiv abgeschaltet. Bei einem Wert von -1 wird
          automatisch gepr�ft, ob eine Biometrie-Einheit verf�gbar ist. Wenn ja,
          so wird diese benutzt, ansonsten erfolgt die PIN-Eingabe �ber das
          Keypad des Chipkartenterminals</p></li>
      <li><code>client.passport.DDV.softpin</code> (f�r DDV-Passports)
          <p>Dieser Parameter kann entweder 0, 
          1 oder -1 enthalten. F�r alle Chipkartenterminals, die �ber 
          keine eigene Tastatur zur Eingabe der PIN verf�gen, ist er auf 
          1 zu setzen. Damit wird der HBCI-Kernel dar�ber informiert, 
          dass die PIN vom Anwender �ber die PC-Tastatur einzugeben ist. 
          Durch Setzen dieses Wertes auf 0 wird die PIN-Eingabe f�r das 
          Keypad des Chipkartenlesers erzwungen.</p> 
          <p>Setzt man den Parameter auf -1, so wird automatisch erkannt, 
          welches PIN-Eingabeverfahren bei dem jeweils verwendeten 
          Chipkartenterminal zu benutzen ist.</p></li>
      <li><code>client.passport.DDV.entryidx</code> (f�r DDV-Passports)
          <p>Prinzipiell kann auf einer DDV-Chipkarte mehr als ein Datensatz mit
          HBCI-Zugangsdaten gespeichert werden (bis zu f�nf). Dieser Parameter
          legt fest, welcher der f�nf Datens�tze tats�chlich benutzt werden soll.
          Da in den meisten F�llen aber nur der erste Datensatzu tats�chlich belegt
          ist, wird dieser Parameter meist den Wert "1" haben (ist auch default,
          falls dieser Parameter gar nicht gesetzt ist).</p></li>
      <li><code>client.passport.DDV.pcsc.name</code> (f�r DDV-Passports bei Verwendung von HBCIPassportDDVPCSC)
          <p>Wenn statt dem DDV-Passport der DDVPCSC-Passport (basierend auf javax.smartcardio)
          verwendet wird, kann hier der Name des Kartenlesers angegeben werden. Andernfalls
          wird der erste gefundene verwendet.</p></li>
      <li><code>client.passport.RDHNew.filename</code> (f�r RDHNew-Passports)
          <p>Dieser Parameter legt den 
          Dateinamen der Schl�sseldatei fest. Diese Datei sollte am 
          besten auf einem mobilen Datentr�ger (Diskette) gespeichert 
          sein. Au�erdem sollte ein Backup dieser Datei angefertigt 
          werden, da bei Verlust der Schl�sseldatei keine 
          HBCI-Kommunikation mehr m�glich ist.</p></li>
      <li><code>client.passport.RDHNew.init</code> (f�r RDHNew-Passports)
          <p>Dieser Parameter ist immer auf "1" zu 
          setzen (wird nur intern anders verwendet).</p></li>
      <li><code>client.passport.RDHNew.defaultprofile</code> (f�r RDHNew-Passports)
          <p>Kann verwendet werden, wenn die RDH-Profilversion beim Erstellen eines
          Schluessel nicht ermittelbar ist, weil die Bank den anonymen BPD-Abruf nicht
          unterstuetzt. Per Default wird hier "10" verwendet.</p></li>
      <li><code>client.passport.RDH.filename</code> (f�r RDH-Passports; <b><em>diese Variante der
          RDH-Passports sollte nicht mehr benutzt werden, sondern <code>RDHNew</code></em></b>;
          siehe Datei <code>README.RDHNew</code>)
          <p>analog zu <code>client.passport.RDHNew.filename</code>.</p></li>
      <li><code>client.passport.RDH.init</code> (f�r RDH-Passports; <b><em>diese Variante der
          RDH-Passports sollte nicht mehr benutzt werden, sondern <code>RDHNew</code></em></b>;
          siehe Datei <code>README.RDHNew</code>)
          <p>analog zu <code>client.passport.RDHNew.init</code>.</p></li>
      <li><code>client.passport.PinTan.filename</code> (f�r PIN/TAN-Passports)
          <p>Dieser Parameter legt den 
          Dateinamen der "Schl�sseldatei" fest. Beim PIN/TAN-Verfahren handelt
          es sich nicht wirklich um eine Schl�sseldatei, da bei diesem Sicherheitsverfahren
          keine kryptografischen Schl�ssel auf HBCI-Ebene eingesetzt werden. In dieser
          Datei werden also nur die HBCI-Zugangsdaten abgelegt.</p></li>
      <li><code>client.passport.PinTan.certfile</code> (f�r PIN/TAN-Passports)
          <p>Dieser Parameter gibt den Dateinamen einer Datei an, die
          ein Zertifikat f�r die Kommunikation via HTTPS (SSL-Verschl�sselung)
          enth�lt. Diese Datei kann mit dem Tool <code>keytool</code> erzeugt werden,
          welches zur Java-Laufzeitumgebung geh�rt. Das Zertifikat (ein best�tigter
          �ffentlicher Schl�ssel) kann i.d.R. von der Bank angefordert werden.</p>
          <p>Dieser Parameter wird nur dann ben�tigt, wenn das SSL-Zertifikat der Bank
          nicht mit dem defaultm��ig in die JRE eingebauten TrustStore �berpr�ft
          werden kann (also am besten erst ohne diesen Parameter ausprobieren -
          wenn eine entsprechende Fehlermeldung erscheint, muss das jeweilige Zertifikat
          von der Bank angefordert, mit <code>keytool</code> konvertiert und hier
          angegeben werden). Wenn ein entsprechendes Root-Zertifikat f�r die �berpr�fung
          gar nicht zur Verf�gung steht, so kann mit dem Parameter
          <code>client.passport.PinTan.checkcert</code> die Zertifikats�berpr�fung
          g�nzlich deaktiviert werden.</p></li>
      <li><code>client.passport.PinTan.checkcert</code> (f�r PIN/TAN-Passports)
          <p>Dieser Parameter steht defaultm��ig auf "<code>1</code>". Wird dieser
          Parameter allerdings auf "<code>0</code>" gesetzt, so wird die �berpr�fung
          des Bank-Zertifikates, welches f�r die SSL-Kommunikation verwendet
          wird, deaktiviert. Diese Vorgehensweise wird nicht empfohlen, da dann
          Angriffe auf das SSL-Protokoll und damit auch auf die HBCI-Kommunikation 
          m�glich sind. In einigen F�llen steht aber kein Root-Zertifikat f�r die 
          �berpr�fung des SSL-Zertifikats der Bank zur Verf�gung, so dass diese
          �berpr�fung abgeschaltet werden <em>muss</em>, um �berhaupt eine Kommunikation
          mit der Bank zu erm�glichen.</p></li>
      <li><code>client.passport.PinTan.proxy</code> (f�r PIN/TAN-Passports)
          <p>Falls ausgehende HTTPS-Verbindungen �ber einen Proxy-Server laufen
          sollen, kann der zu verwendende Proxy-Server mit diesem Parameter
          konfiguriert werden. Das Format f�r den Wert dieses Kernel-Parameters
          ist "HOST:PORT", also z.B. <code>proxy.intern.domain.com:3128</code>.</p></li>
      <li><code>client.passport.PinTan.proxyuser</code> (f�r PIN/TAN-Passports)
          <p>Falls f�r ausgehende HTTPS-Verbindungen (f�r HBCI-PIN/TAN) ein Proxy-Server
          verwendet wird, und falls dieser Proxy-Server eine Authentifizierung
          verlangt, kann mit diesem Parameter der Nutzername festgelegt werden.</p>
          <p>Wenn dieser Parameter nicht gesetzt wird, wird bei Bedarf �ber einen
          Callback (<code>NEED_PROXY_USER</code>) nach dem Nutzernamen gefragt.</p></li>
      <li><code>client.passport.PinTan.proxypass</code> (f�r PIN/TAN-Passports)
          <p>Falls f�r ausgehende HTTPS-Verbindungen (f�r HBCI-PIN/TAN) ein Proxy-Server
          verwendet wird, und falls dieser Proxy-Server eine Authentifizierung
          verlangt, kann mit diesem Parameter das Passwort festgelegt werden.</p>
          <p>Wenn dieser Parameter nicht gesetzt wird, wird bei Bedarf �ber einen
          Callback (<code>NEED_PROXY_PASS</code>) nach dem Passwort gefragt.</p></li>
      <li><code>client.passport.PinTan.init</code> (f�r PIN/TAN-Passports)
          <p>Dieser Parameter ist immer auf "1" zu 
          setzen (wird nur intern anders verwendet).</p></li>
      <li><code>client.passport.SIZRDHFile.filename</code> (f�r SIZRDHFile-Passports)
          <p>Dieser Parameter legt den 
          Dateinamen der SIZ-Schl�sseldatei fest. Dabei handelt es sich
          um die Schl�sseldatei, die von anderer HBCI-Software (z.B. <em>StarMoney</em>)
          erzeugt wurde.</p>
          <p>Siehe dazu auch <code>README.SIZRDHFile</code></p></li>
      <li><code>client.passport.SIZRDHFile.libname</code> (f�r SIZRDHFile-Passports)
          <p>Dieser Parameter gibt den vollst�ndigen Dateinamen der SIZ-RDH-Laufzeitbibliothek
          an. Diese Bibliothek ist <em>nicht</em> Teil von <em>HBCI4Java</em>, sondern muss separat 
          von <a href="http://hbci4java.kapott.org#download">http://hbci4java.kapott.org</a>
          heruntergeladen und installiert werden.</p>
          <p>Siehe dazu auch <code>README.SIZRDHFile</code></p></li>
      <li><code>client.passport.SIZRDHFile.init</code> (f�r SIZRDHFile-Passports)
          <p>Dieser Parameter ist immer auf "1" zu 
          setzen (wird nur intern anders verwendet).</p>
          <p>Siehe dazu auch <code>README.SIZRDHFile</code></p></li>
      <li><code>client.passport.RDHXFile.filename</code> (f�r RDHXFile-Passports)
          <p>Dieser Parameter legt den 
          Dateinamen der RDHXFile-Schl�sseldatei fest. Dabei handelt es sich
          um die Schl�sseldatei, die von anderer HBCI-Software (z.B. <em>VR-NetWorld</em>, 
          <em>ProfiCash</em>, ...) erzeugt wurde.</p>
      <li><code>client.passport.RDHXFile.init</code> (f�r RDHXFile-Passports)
          <p>Dieser Parameter ist immer auf "1" zu 
          setzen (wird nur intern anders verwendet).</p></li>
      <li><code>client.passport.Anonymous.filename</code> (f�r Anonymous-Passports)
          <p>Dieser Parameter legt den 
          Dateinamen der Schl�sseldatei fest.</p></li>
      <li><code>client.passport.Anonymous.init</code> (f�r Anonymous-Passports)
          <p>Dieser Parameter ist immer auf "1" zu 
          setzen (wird nur intern anders verwendet).</p></li>
      <li><code>client.passport.default</code>
          <p>Wird bei der Erzeugung eines Passport-Objektes 
          ({@link org.kapott.hbci.passport.AbstractHBCIPassport#getInstance()})
          nicht explizit angegeben, f�r welches Sicherheitsverfahren ein Passport-Objekt 
          erzeugt werden soll, so wird der Wert dieses Parameters 
          benutzt, um die entsprechende Variante auszuw�hlen. G�ltige 
          Werte sind "<code>DDV</code>", "<code>RDHNew</code>", "<code>RDH</code>" (nicht
          mehr benutzen!), "<code>PinTan</code>", "<code>SIZRDHFile</code>", "<code>RDHXFile</code>"
	  oder "<code>Anonymous</code>" (Gro�-/Kleinschreibung beachten).</p></li>
      <li><code>client.retries.passphrase</code>
          <p>Ist das Passwort f�r die Entschl�sselung der Passport-Datei falsch, so kann die Eingabe
          so oft wiederholt werden, wie dieser Parameter angibt, bevor eine Exception geworfen und
          die weitere Programmausf�hrung unterbrochen wird.</p></li>
      <li><code>client.connection.localPort</code>
          <p>F�r Anwendungen, die sich hinter einer Firewall befinden, 
          welche nur ausgehende Verbindungen mit bestimmten lokalen 
          Portnummern zul�sst (sowas soll's geben), kann mit diesem 
          Parameter die Portnummer festgelegt werden, die lokal benutzt 
          werden soll. Dieser Parameter hat im Moment nur bei "normalen" 
          HBCI-Verbindungen Auswirkungen. Beim PIN/TAN-Verfahren wird 
          eine HTTPS-Verbindung mit dem HBCI-Server aufgebaut, f�r diese 
          Verbindung wird der localPort-Parameter im Moment noch nicht ausgewertet.</p></li>
      <li><code>comm.standard.socks.server</code>
          <p>Soll fuer ausgehende Verbindungen ein SOCKS-Server verwendet werden, kann
          dieser SOCKS-Server im Format <code>hostname:port</code> festgelegt werden. 
          Diese Einstellung wird <em>NICHT</em> fuer HBCI-PIN/TAN verwendet, sondern nur 
          fuer alle "richtigen" HBCI-Verbindungen (alle Passport-Varianten von RDH und DDV).</p></li>
      <li><code>sepa.schema.validation</code>
          <p>Kann auf 1 gesetzt werden, wenn das erzeugte XML gegen das Schema validiert werden soll.</p></li>
      <li><code>bpd.maxage.days</code>
          <p>Maximales Alter der BPD in Tagen nach deren Ablauf die BPD erneut abgerufen werden - auch dann,
          wenn sich deren Versionsnummer nicht geaendert hat. Das ermoeglicht das automatische Aktualisieren
          der BPD, wenn die Bank die Versionsnummer nicht erhoeht. Ein Wert von "-1" bedeutet: Jedesmal
          BPD erneut abrufen. Ein Wert von "0" bedeutet: Niemals BPD ohne Versionsaenderung erneut abrufen.
          Der Default-Wet ist 7 - also einmal pro Woche.</p></li>
      <li><code>kernel.kernel.xmlpath</code>
          <p>(wird nicht gesetzt, zur Zeit nur intern benutzt)</p></li>
      <li><code>kernel.kernel.blzpath</code>
          <p>(wird nicht gesetzt, zur Zeit nur intern benutzt)</p></li>
      <li><code>kernel.kernel.challengedatapath</code>
          <p>(wird nicht gesetzt, zur Zeit nur intern benutzt)</p></li>
      <li><code>log.loglevel.default</code>
          <p>Mit diesem Parameter kann eingestellt werden, welche vom 
          HBCI-Kernel erzeugten Log-Ausgaben tats�chlich bis zur 
          Anwendung gelangen. Dieser Parameter kann Werte von 1 (nur 
          Fehlermeldungen) bis 5 (einschlie�lich aller Debug-Ausgaben) annehmen.</p>
          <p>Bei Problemen mit dem Kernel diesen Level bitte auf 4 oder 5 setzen, 
          alle erzeugten Log-Ausgaben protokollieren
          und zusammen mit einer Beschreibung des Problems an den 
          <a href="mailto:hbci4java@kapott.org">Autor</a> schicken.</p></li>
      <li><code>log.ssl.enable</code>
          <p>Dieser Parameter kann die Werte 0 und 1 annehmen. Ist er auf 1 gesetzt,
          wird s�mtliche Kommunikation, die bei Verwendung von HBCI-PIN/TAN �ber
          eine HTTPS-Verbindung geht, im Klartext (also unverschl�sselt!) mitgeschnitten.
          Das kann n�tzlich sein, um z.B. Probleme mit diversen HTTP-Request- oder
          -Response-Headern zu finden. Diese Einstellung funktioniert allerdings
          <b>NICHT mit Java-1.4.x</b> (Grund daf�r ist eine spezielle Einschr�nkung
          der JSSE). Der Standard-Wert f�r diese Einstellung ist 0 (also kein Logging). 
          Siehe dazu auch Kernel-Parameter <code>log.ssl.filename</code>.</p></li>
      <li><code>log.ssl.filename</code>
          <p>Wenn <code>log.ssl.enable=1</code>, so wird s�mtliche HTTPS-Kommunikation
          aller HBCI-PIN/TAN-Verbindungen mitgeschnitten und in die Datei geschrieben,
          deren Dateiname mit diesem Parameter angegeben wird. Ist die Datei nicht
          vorhanden, wird sie angelegt. Ist sie bereits vorhanden, werden die
          Log-Daten angeh�ngt. Wird kein Wert f�r diesen Parameter angegeben, gibt
          <em>HBCI4Java</em> eine Warnung aus und erzeugt Log-Meldungen �ber den
          <em>HBCI4Java</em>-Log-Mechanismus (Callback-Methode <code>log()</code>)
          mit Log-Level <code>LOG_DEBUG2</code>.</p></li>
      <li><code>infoPoint.enabled</code>
          <p>Der <em>HBCI4Java-InfoPoint-Server</em> ist ein Versuch, die richtigen
          HBCI-Konfigurations-Einstellungen f�r alle Banken zentral zu sammeln
          und �ber ein �ffentliches Interface allgemein zur Verf�gung zu stellen.
          N�here Infos dazu siehe Datei <em>README.InfoPoint</em>.</p>
          <p>Dieser Parameter legt fest, ob <em>HBCI4Java</em> nach einem erfolgreichen
          HBCI-Verbindungsaufbau Informationen �ber diese HBCI-Verbindung zum
          InfoPoint-Server senden soll oder nicht. Standardm��ig ist dieser
          Parameter deaktiviert (<code>0</code>). Durch setzen auf <code>1</code>
          kann das Senden der Daten zum InfoPoint-Server aktiviert werden.</p></li>
      <li><code>infoPoint.url</code>
          <p>Der "normale" InfoPoint-Server ist f�r <em>HBCI4Java</em> unter der
          URL <code>http://hbci4java.kapott.org/infoPoint</code> zu erreichen.
          Das ist auch der default-Wert f�r diesen Parameter. Dieser Parameter
          dient momentan nur Debugging-Zwecken und sollte normalerweise nicht
          ge�ndert werden.</p></li>
      <li><code>kernel.rewriter</code>
          <p>Einige HBCI-Server-Implementationen bzw. die Backend-Systeme 
          einiger Banken halten sich nicht strikt an die in der 
          HBCI-Spezifikation vorgeschriebenen Formate. Um solche 
          Unzul�nglichkeiten nicht direkt im HBCI-Kernel abfangen zu 
          m�ssen, existieren sogenannte Rewriter-Module. Ein solches 
          Modul ist f�r jeweils einen bekannten "Bug" zust�ndig. Kurz vor 
          dem Versand und direkt nach dem Eintreffen von HBCI-Nachrichten 
          werden diese durch alle registrierten Rewriter-Module 
          geschickt. F�r ausgehende Nachrichten werden hier u.U. nicht 
          HBCI-konforme Ver�nderungen vorgenommen, die vom jeweiligen 
          HBCI-Server so erwartet werden. Eingehende Nachrichten, die 
          nicht HBCI-konform sind, werden so umgeformt, dass sie der 
          Spezifikation entsprechen. Auf diese Art und Weise kann der 
          HBCI-Kernel immer mit streng HBCI-konformen Nachrichten arbeiten.
          Siehe dazu auch die Paketdokumentation zum Paket
          <code>org.kapott.hbci.rewrite</code>.</p>
          <p>Der Parameter <code>kernel.rewriter</code> legt die Liste aller 
          Rewriter-Module fest, welche eingehende und ausgehende Nachrichten 
          durchlaufen sollen. Wird dieser Parameter nicht gesetzt, so
          verwendet <em>HBCI4Java</em> eine default-Liste von aktivierten
          Rewriter-Modulen (kann mit etParam(String) ermittelt werden).
          Wird dieser Parameter gesetzt, so wird die default-Einstellung
          �berschrieben. Es k�nnen mehrere zu durchlaufende Rewriter-Module
          angegeben werden, indem sie durch Komma voneinander getrennt werden.</p></li>
      <li><code>kernel.threaded.maxwaittime</code>
          <p>Beim Verwenden des threaded-callback-Mechanismus (siehe Datei
          <code>README.ThreadedCallbacks</code>) wird die eigentliche Ausf�hrung
          der HBCI-Dialoge und die Interaktion mit der Anwendung auf mehrere Threads
          verteilt. Es ist jeweils einer der beteiligten Threads "aktiv" - die
          anderen Threads warten auf eine Nachricht vom gerade aktiven
          Thread. Um das System nicht mit "unendlich lange wartenden" Threads
          zu belasten, warten die jeweils inaktiven Threads nur eine bestimmte
          Zeitspanne auf eine Nachricht vom aktiven Thread. Diese Zeitspanne kann
          mit diesem Kernel-Parameter konfiguriert werden. Falls nach der hier
          konfigurierten Zeitspanne keine Nachricht empfangen wurde, beendet sich
          der jeweils wartende Thread selbst. Falls der aktive Thread nach Ablauf
          dieser Zeitspanne versucht, eine Nachricht an den wartenden Thread zu
          senden, wird eine <code>RuntimeException</code> geworfen.</p>
          <p>Die Zeitspanne wird in Sekunden angegeben. Der default-Wert betr�gt
          300 (5 Minuten).</p></li>
      <li><p>Die folgenden Parameter legen die Gr��e sog. Object-Pools fest, die
          intern von <em>HBCI4Java</em> verwendet werden. Object-Pools stellen eine
          Art Cache dar, um Instanzen h�ufig benutzter Klassen nicht jedesmal neu
          zu erzeugen. Statt dessen werden nicht mehr ben�tigte Objekte in einem
          Pool verwaltet, aus dem bei Bedarf wieder Objekte entnommen werden. Die
          Gr��e der Pools f�r die einzelnen Objekttypen kann hier festgelegt werden.
          Falls Speicherprobleme auftreten (<code>OutOfMemory</code>-Exception), so sollten 
          diese Werte verringert werden. Durch Setzen eines Wertes auf "<code>0</code>" wird
          das Object-Pooling f�r die entsprechenden Objekte komplett deaktiviert.
          Zur Zeit werden nur bei der Nachrichtenerzeugung und -analyse Object-Pools 
          verwendet. In der folgenden Auflistung steht in Klammern jeweils der 
          eingebaute default-Wert.</p>
          <ul>
            <li><p><code>kernel.objpool.MSG</code> -- Pool f�r Nachrichten-Objekte (3)</p></li>
            <li><p><code>kernel.objpool.SF</code>  -- Pool f�r SF- (Segmentfolgen-) Objekte (128)</p></li>
            <li><p><code>kernel.objpool.SEG</code> -- Pool f�r Segment-Objekte (256)</p></li>
            <li><p><code>kernel.objpool.DEG</code> -- Pool f�r DEG- (Datenelementgruppen-) Objekte (256)</p></li>
            <li><p><code>kernel.objpool.DE</code> -- Pool f�r Datenelement-Objekte (1024)</p></li>
            <li><p><code>kernel.objpool.Sig</code> -- Pool f�r Signatur-Objekte (3)</p></li>
            <li><p><code>kernel.objpool.Crypt</code> -- Pool f�r Crypt-Objekte (3)</p></li>
            <li><p><code>kernel.objpool.Syntax</code> -- Pool f�r Daten-Objekte (=Werte in Nachrichten) (128 je Datentyp)</p></li>
          </ul></li>
      <li><p>Mit den folgenden Parametern kann <em>HBCI4Java</em> veranlasst
          werden, beim Auftreten bestimmter Fehler keine Exception zu werfen, sondern
          diesen Fehler zu ignorieren bzw. den Anwender entscheiden zu lassen,
          ob der Fehler ignoriert werden soll. Bei den Fehlern handelt es sich
          haupts�chlich um Fehler, die beim �berpr�fen von Eingabedaten und
          Institutsnachrichten bzgl. der Einhaltung der HBCI-Spezifikation
          auftreten.</p>
          <p>Jeder der folgenden Parameter kann einen der Werte <code>yes</code>,
          <code>no</code> oder <code>callback</code> annehmen. Ist ein Parameter auf
          <code>no</code> gesetzt, so wird beim Auftreten des jeweiligen Fehlers
          eine entsprechende Exception geworfen. Dieses Verhalten ist das Standardverhalten
          und entspricht dem der Vorg�ngerversionen von <em>HBCI4Java</em>. Ist
          ein Parameter auf <code>yes</code> gesetzt, so wird der Fehler komplett
          ignoriert. Es wird nur eine entsprechende Warnung mit Loglevel <code>LOG_WARN</code>
          erzeugt. Wird ein Parameter auf <code>callback</code> gesetzt, so wird ein
          Callback mit dem Callback-Reason <code>HAVE_ERROR</code> erzeugt, bei dem
          die Callback-Message (Parameter <code>msg</code>) die entsprechende Fehlermeldung
          enth�lt. Gibt die Callback-Methode einen leeren String im <code>retData</code>-Objekt
          zur�ck, so bedeutet das f�r <em>HBCI4Java</em>, dass der entsprechende
          Fehler ignoriert werden soll (so als w�re der Parameter auf <code>yes</code>
          gesetzt). Ist der R�ckgabestring nicht leer, so wird <em>HBCI4Java</em> eine
          entsprechende Exception werfen, so als w�re der zugeh�rige Parameter gleich
          <code>no</code>. N�here Informationen zu Callbacks befinden sich in der
          Beschreibung des Interfaces {@link org.kapott.hbci.callback.HBCICallback}.</p>
          <p><b>"Normalen" Benutzern von <em>HBCI4Java</em> ist dringend von der Verwendung
          dieser Parameter abzuraten, weil sie bei falscher Anwendung dazu f�hren k�nnen,
          dass <em>HBCI4Java</em> gar nicht mehr funktioniert.</b> Diese Parameter sind
          nur f�r <em>HBCI4Java</em>-Entwickler (also mich ;-)) gedacht und sind hier nur
          der Vollst�ndigkeit halber aufgef�hrt.</p>
          <p>Eine genauere Beschreibung der einzelnen Parameter befindet sich in
          der Properties-Template-Datei <code>hbci.props.template</code>.</p></li>
      <li><code>client.errors.ignoreJobResultStoreErrors</code></li>
      <li><code>client.errors.ignoreWrongJobDataErrors</code></li>
      <li><code>client.errors.ignoreWrongDataLengthErrors</code></li>
      <li><code>client.errors.ignoreWrongDataSyntaxErrors</code></li>
      <li><code>client.errors.ignoreAddJobErrors</code></li>
      <li><code>client.errors.ignoreCreateJobErrors</code></li>
      <li><code>client.errors.ignoreExtractKeysErrors</code></li>
      <li><code>client.errors.ignoreDialogEndErrors</code></li>
      <li><code>client.errors.ignoreSecMechCheckErrors</code></li>
      <li><code>client.errors.ignoreVersionCheckErrors</code></li>
      <li><code>client.errors.ignoreSignErrors</code></li>
      <li><code>client.errors.ignoreMsgSizeErrors</code></li>
      <li><code>client.errors.ignoreCryptErrors</code></li>
      <li><code>client.errors.ignoreMsgCheckErrors</code></li>
      <li><code>client.errors.allowOverwrites</code></li>
      <li><code>client.errors.ignoreValidValueErrors</code></li>
      <li><code>client.errors.ignoreSegSeqErrors</code></li>
    </ul> */
public final class HBCIUtils
{
    private static final String VERSION="HBCI4Java-2.5.12";
    
    public static final int LOG_NONE=0;
    /** Loglevel f�r Fehlerausgaben */
    public static final int LOG_ERR=1;
    /** Loglevel f�r Warnungen */
    public static final int LOG_WARN=2;
    /** Loglevel f�r Informationen */
    public static final int LOG_INFO=3;
    /** Loglevel f�r Debug-Ausgaben */
    public static final int LOG_DEBUG=4;
    /** Loglevel f�r Debug-Ausgaben f�r extreme-Debugging */
    public static final int LOG_DEBUG2=5;
    /** Loglevel f�r devel-Debugging - nicht benutzen! */
    public static final int LOG_INTERN=6;

    private static char[] base64table={'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
                                       'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
                                       'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
                                       'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'};

    private Properties properties;
    private AbstractHBCICallback callback;
    static {
        initDataStructures();
    }
    
    private static void initDataStructures()
    {
        HBCIUtilsInternal.blzs=new Properties();
        HBCIUtilsInternal.banks= new HashMap<>();
    }
    
    public HBCIUtils(Properties properties, AbstractHBCICallback callback)
    {
        this.properties = properties;
        this.callback = callback;
    }
    
    /** L�dt ein Properties-File, welches �ber ClassLoader.getRessourceAsStream()
     * gefunden wird. Der Name des Property-Files wird durch den Parameter
     * <code>configfile</code> bestimmt. Wie dieser Name interpretiert wird,
     * um das Property-File tats�chlich zu finden, h�ngt von dem zum Laden
     * benutzten ClassLoader ab. Im Parameter <code>cl</code> kann dazu eine
     * ClassLoader-Instanz �bergeben werden, deren <code>getRessource</code>-Methode
     * benutzt wird, um das Property-File zu lokalisieren und zu laden. Wird
     * kein ClassLoader angegeben (<code>cl==null</code>), so wird zum Laden
     * des Property-Files der ClassLoader benutzt, der auch zum Laden der
     * aufrufenden Klasse benutzt wurde.
     * @param cl ClassLoader, der zum Laden des Property-Files verwendet werden soll
     * @param configfile Name des zu ladenden Property-Files (kann <code>null</code> 
     * sein - in dem Fall gibt diese Methode auch <code>null</code> zur�ck).
     * @return Properties-Objekt */
    public static Properties loadPropertiesFile(ClassLoader cl, String configfile)
    {
        Properties props=null;
        
        if (configfile!=null) {
            try {
                // load kernel params from properties file
                /* determine classloader to be used */
                if (cl == null) {
                    try {
                        throw new Exception();
                    } catch (Exception e) {
                        StackTraceElement[] stackTrace = e.getStackTrace();

                        if (stackTrace.length > 1) {
                            String classname = stackTrace[1].getClassName();
                            cl = Class.forName(classname).getClassLoader();
                        }
                    }

                    if (cl == null) {
                        cl = ClassLoader.getSystemClassLoader();
                    }
                }

                // TODO: im fehlerfall wird hier nur f==null zurueckgegeben,
                // so dass man ueber die eigentliche fehlerursache (file not found,
                // permission denied) nichts erfaehrt
                
                /* get an input stream */
                InputStream f = null;
                f = cl.getResourceAsStream(configfile);
                if (f == null)
                    throw new InvalidUserDataException("*** can not load config file "+configfile);

                props = new Properties();
                props.load(f);
                f.close();
            } catch (Exception e) {
                throw new HBCI_Exception("*** can not load config file "+configfile, e);
            }
        }

        return props;
    }
    
    /** Ermittelt zu einer gegebenen Bankleitzahl den Namen des Institutes.
        @param blz die Bankleitzahl
        @return den Namen des dazugeh�rigen Kreditinstitutes. Falls die Bankleitzahl unbekannt ist,
                so wird ein leerer String zur�ckgegeben */
    public static String getNameForBLZ(String blz)
    {
        BankInfo info = getBankInfo(blz);
        if (info == null)
            return "";
        return info.getName() != null ? info.getName() : "";
    }
    
    /**
     * Liefert die Bank-Informationen zur angegebenen BLZ.
     * @param blz die BLZ.
     * @return die Bank-Informationen oder NULL, wenn zu der BLZ keine Informationen bekannt sind.
     */
    public static BankInfo getBankInfo(String blz)
    {
        return HBCIUtilsInternal.banks.get(blz);
    }
    
    /**
     * Liefert eine Liste von Bank-Informationen, die zum angegebenen Suchbegriff passen.
     * @param query der Suchbegriff.
     * Der Suchbegriff muss mindestens 3 Zeichen enthalten und ist nicht case-sensitive.
     * Der Suchbegriff kann im Ort der Bank oder in deren Namen enthalten sein.
     * Oder die BLZ oder BIC beginnt mit diesem Text.
     * @return die Liste der Bank-Informationen.
     * Die Ergebnis-Liste ist nach BLZ sortiert.
     * Die Funktion liefert niemals NULL sondern hoechstens eine leere Liste.
     */
    public static List<BankInfo> searchBankInfo(String query)
    {
        if (query != null)
            query = query.trim();
        
        List<BankInfo> list = new LinkedList<BankInfo>();
        if (query == null || query.length() < 3)
            return list;
        
        query = query.toLowerCase();
        
        for (BankInfo info:HBCIUtilsInternal.banks.values())
        {
            String blz  = info.getBlz();
            String bic  = info.getBic();
            String name = info.getName();
            String loc  = info.getLocation();
            
            // Anhand der BLZ?
            if (blz != null && blz.startsWith(query))
            {
                list.add(info);
                continue;
            }
            
            // Anhand der BIC?
            if (bic != null && bic.toLowerCase().startsWith(query))
            {
                list.add(info);
                continue;
            }
            
            // Anhand des Namens?
            if (name != null && name.toLowerCase().contains(query))
            {
                list.add(info);
                continue;
            }
            // Anhand des Orts?
            if (loc != null && loc.toLowerCase().contains(query))
            {
                list.add(info);
                continue;
            }
        }
        
        Collections.sort(list,new Comparator<BankInfo>() {
            /**
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(BankInfo o1, BankInfo o2) {
                if (o1 == null || o1.getBlz() == null)
                    return -1;
                if (o2 == null || o2.getBlz() == null)
                    return 1;
                
                return o1.getBlz().compareTo(o2.getBlz());
            }
        });
        
        return list;
    }
    
    /**
     * Gibt zu einer gegebenen Bankleitzahl den BIC-Code zur�ck.
     * @param blz Bankleitzahl der Bank
     * @return BIC-Code dieser Bank. Falls kein BIC-Code bekannt ist, wird ein
     * leerer String zur�ckgegeben.
     * @deprecated Bitte {@link HBCIUtils#getBankInfo(String)} verwenden.
     */
    public static String getBICForBLZ(String blz)
    {
        BankInfo info = getBankInfo(blz);
        if (info == null)
            return "";
        return info.getBic() != null ? info.getBic() : "";
    }
    
    /**
     * Berechnet die IBAN fuer ein angegebenes deutsches Konto.
     * @param k das Konto.
     * @return die berechnete IBAN.
     */
    public static String getIBANForKonto(Konto k)
    {
      String konto = k.number;
      
      // Die Unterkonto-Nummer muss mit eingerechnet werden.
      // Aber nur, wenn sie numerisch ist. Bei irgendeiner Bank wurde
      // "EUR" als Unterkontonummer verwendet. Das geht natuerlich nicht,
      // weil damit nicht gerechnet werden kann
      // Wir machen das auch nur dann, wenn beide Nummern zusammen max.
      // 10 Zeichen ergeben
      if (k.subnumber != null &&
          k.subnumber.length() > 0 &&
          k.subnumber.matches("[0-9]{1,8}") &&
          k.number.length() + k.subnumber.length() <= 10)
        konto += k.subnumber;
      
      /////////////////
      // Pruefziffer berechnen
      // Siehe http://www.iban.de/iban-pruefsumme.html
      String zeros = "0000000000";
      String filledKonto = zeros.substring(0,10-konto.length()) + konto; // 10-stellig mit Nullen fuellen 
      StringBuffer sb = new StringBuffer();
      sb.append(k.blz);
      sb.append(filledKonto);
      sb.append("1314"); // hartcodiert fuer "DE
      sb.append("00"); // fest vorgegeben
      
      BigInteger mod = new BigInteger(sb.toString()).mod(new BigInteger("97")); // "97" ist fest vorgegeben in ISO 7064/Modulo 97-10
      String checksum = String.valueOf(98 - mod.intValue()); // "98" ist fest vorgegeben in ISO 7064/Modulo 97-10 
      if (checksum.length() < 2)
        checksum = "0" + checksum;
      //
      /////////////////

      StringBuffer result = new StringBuffer();
      result.append("DE");
      result.append(checksum);
      result.append(k.blz);
      result.append(filledKonto);
      
      return result.toString();
    }
    
    /**
     * Gibt zu einer gegebenen Bankleitzahl den HBCI-Host (f�r RDH und DDV)
     * zur�ck.
     * @param blz Bankleitzahl der Bank
     * @return HBCI-Host (DNS-Name oder IP-Adresse). Falls kein Host bekannt
     *         ist, wird ein leerer String zur�ckgegeben.
     * @deprecated Bitte {@link HBCIUtils#getBankInfo(String)} verwenden.
     */
    public static String getHBCIHostForBLZ(String blz)
    {
        BankInfo info = getBankInfo(blz);
        if (info == null)
            return "";
        return info.getRdhAddress() != null ? info.getRdhAddress() : "";
    }

    /**
     * Gibt zu einer gegebenen Bankleitzahl die PIN/TAN-URL
     * zur�ck.
     * @param blz Bankleitzahl der Bank
     * @return PIN/TAN-URL. Falls keine URL bekannt
     *         ist, wird ein leerer String zur�ckgegeben.
     * @deprecated Bitte {@link HBCIUtils#getBankInfo(String)} verwenden.
     */
    public static String getPinTanURLForBLZ(String blz)
    {
        BankInfo info = getBankInfo(blz);
        if (info == null)
            return "";
        return info.getPinTanAddress() != null ? info.getPinTanAddress() : "";
    }
    
    /**
     * Gibt zu einer gegebenen Bankleitzahl zur�ck, welche HBCI-Version f�r DDV 
     * bzw. RDH zu verwenden ist. Siehe auch {@link #getPinTanVersionForBLZ(String)}. 
     * @param blz
     * @return HBCI-Version
     * @deprecated Bitte {@link HBCIUtils#getBankInfo(String)} verwenden.
     */
    public static String getHBCIVersionForBLZ(String blz)
    {
        BankInfo info = getBankInfo(blz);
        if (info == null)
            return "";
        return info.getRdhVersion() != null ? info.getRdhVersion().getId() : "";
    }

    /**
     * Gibt zu einer gegebenen Bankleitzahl zur�ck, welche HBCI-Version f�r HBCI-PIN/TAN
     * bzw. RDH zu verwenden ist. Siehe auch {@link #getHBCIVersionForBLZ(String)} 
     * @param blz
     * @return HBCI-Version
     * @deprecated Bitte {@link HBCIUtils#getBankInfo(String)} verwenden.
     */
    public static String getPinTanVersionForBLZ(String blz)
    {
        BankInfo info = getBankInfo(blz);
        if (info == null)
            return "";
        return info.getPinTanVersion() != null ? info.getPinTanVersion().getId() : "";
    }
    
    /** Gibt den StackTrace einer Exception zur�ck. 
     *  @param e Exception
     *  @return kompletter StackTrace als String */ 
    public static String exception2String(Exception e)
    {
        StringWriter sw=new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString().trim();
    }
    
    /** Extrahieren der root-Exception aus einer Exception-Chain.
        @param e Exception
        @return String mit Infos zur root-Exception */
    public static String exception2StringShort(Exception e) 
    {
        StringBuffer st=new StringBuffer();
        Throwable    e2=e;

        while (e2!=null) {
            String exClass=e2.getClass().getName();
            String msg=e2.getMessage();

            if (msg!=null) {
                st.setLength(0);
                st.append(exClass);
                st.append(": ");
                st.append(msg);
            }
            e2=e2.getCause();
        }
        
        return st.toString().trim();
    }

    /** Wandelt ein Byte-Array in eine entsprechende hex-Darstellung um.
        @param data das Byte-Array, f�r das eine Hex-Darstellung erzeugt werden soll
        @return einen String, der f�r jedes Byte aus <code>data</code>
                zwei Zeichen (0-9,A-F) enth�lt. */ 
    public static String data2hex(byte[] data)
    {
        StringBuffer ret=new StringBuffer();
        
        for (int i=0;i<data.length;i++) {
            String st=Integer.toHexString(data[i]);
            if (st.length()==1) {
                st='0'+st;
            }
            st=st.substring(st.length()-2);
            ret.append(st).append(" ");
        }
        
        return ret.toString();
    }

    
    /** 
     * Wandelt ein gegebenes Datumsobjekt in einen String um. Das Format
     * des erzeugten Strings ist abh�ngig vom gesetzten <em>HBCI4Java</em>-Locale
     * (siehe Kernel-Parameter <code>kernel.locale.*</code>)
        @param date ein Datum
        @return die lokalisierte Darstellung dieses Datums als String */
    public static String date2StringLocal(Date date)
    {
        String ret;
        
        try {
            ret=DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(date);
        } catch (Exception e) {
            throw new InvalidArgumentException(date.toString());
        }
        
        return ret;
    }
    
    /** Wandelt einen String, der ein Datum in der lokalen Darstellung enth�lt
     * (abh�ngig von der <em>HBCI4Java</em>-Locale, siehe Kernel-Parameter
     * <code>kernel.locale.*</code>), in ein Datumsobjekt um
        @param date ein Datum in der lokalen Stringdarstellung
        @return ein entsprechendes Datumsobjekt */
    public static Date string2DateLocal(String date)
    {
        Date ret;
        
        try {
            ret=DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).parse(date);
        } catch (Exception e) {
            throw new InvalidArgumentException(date);
        }
        
        return ret;
    }

    /** Wandelt ein gegebenes Datums-Objekt in einen String um, der die Uhrzeit enth�lt.
     * Das Format des erzeugten Strings ist abh�ngig von der gesetzten
     * <em>HBCI4Java</em>-Locale (siehe Kernel-Parameter <code>kernel.locale.*</code>).
        @param date ein Datumsobjekt 
        @return die lokalisierte Darstellung der Uhrzeit als String */
    public static String time2StringLocal(Date date)
    {
        String ret;
        
        try {
            ret=DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(date);
        } catch (Exception e) {
            throw new InvalidArgumentException(date.toString());
        }
        
        return ret;
    }
    
    /** Wandelt einen String, der eine Uhrzeit in der lokalen Darstellung enth�lt
     * (abh�ngig von der <em>HBCI4Java</em>-Locale, siehe Kernel-Parameter
     * <code>kernel.locale.*</code>), in ein Datumsobjekt um
        @param date eine Uhrzeit in der lokalen Stringdarstellung
        @return ein entsprechendes Datumsobjekt */
    public static Date string2TimeLocal(String date)
    {
        Date ret;
        
        try {
            ret=DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).parse(date);
        } catch (Exception e) {
            throw new InvalidArgumentException(date);
        }
        
        return ret;
    }

    /** Wandelt ein gegebenes Datums-Objekt in einen String um, der sowohl Datum
     * als auch Uhrzeit enth�lt. Das Format des erzeugten Strings ist abh�ngig von der gesetzten
     * <em>HBCI4Java</em>-Locale (siehe Kernel-Parameter <code>kernel.locale.*</code>).
        @param date ein Datumsobjekt 
        @return die lokalisierte Darstellung des Datums-Objektes*/
    public static String datetime2StringLocal(Date date)
    {
        String ret;
        
        try {
            ret=DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(date);
        } catch (Exception e) {
            throw new InvalidArgumentException(date.toString());
        }
        
        return ret;
    }
    
    /** 
     * Erzeugt ein Datums-Objekt aus Datum und Uhrzeit in der String-Darstellung.
     * Die String-Darstellung von Datum und Uhrzeit m�ssen dabei der aktuellen
     * <em>HBCI4Java</em>-Locale entsprechen (siehe Kernel-Parameter <code>kernel.locale.*</code>)).
        @param date ein Datum in der lokalen Stringdarstellung
        @param time eine Uhrzeit in der lokalen Stringdarstellung (darf <code>null</code> sein)
        @return ein entsprechendes Datumsobjekt */
    public static Date strings2DateTimeLocal(String date,String time)
    {
        Date ret;
        
        try {
            if (time!=null)
                ret=DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT,Locale.getDefault()).parse(date+" "+time);
            else
                ret=DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault()).parse(date);
        } catch (Exception e) {
            throw new InvalidArgumentException(date+" / "+time);
        }
        
        return ret;
    }
    
    
    /** Erzeugt einen String im Format YYYY-MM-DD */
    public static String date2StringISO(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    
    /** Wandelt einen String der Form YYYY-MM-DD in ein <code>Date</code>-Objekt um. */
    public static Date string2DateISO(String st)
    {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(st);
        } catch (ParseException e) {
            throw new InvalidArgumentException(st);
        }
    }
    
    /** Erzeugt einen String der Form HH:MM:SS */
    public static String time2StringISO(Date date)
    {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }
    
    /** Wandelt einen String der Form HH:MM:SS in ein <code>Date</code>-Objekt um */
    public static Date string2TimeISO(String st)
    {
        try {
            return new SimpleDateFormat("HH:mm:ss").parse(st);
        } catch (ParseException e) {
            throw new InvalidArgumentException(st);
        }
    }
    
    /** Erzeugt einen String im Format YYYY-MM-DD HH:MM:SS */
    public static String datetime2StringISO(Date date)
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
    
    /** 
     * Erzeugt ein Datums-Objekt aus Datum und Uhrzeit in der String-Darstellung.
     * Die String-Darstellung von Datum und Uhrzeit m�ssen dabei im ISO-Format
     * vorlegen (Datum als yyyy-mm-dd, Zeit als hh:mm:ss). Der Parameter <code>time</code>
     * darf auch <code>null</code> sein, <code>date</code> jedoch nicht.
        @param date ein Datum in der ISO-Darstellung
        @param time eine Uhrzeit in der ISO-Darstellung (darf auch <code>null</code> sein)
        @return ein entsprechendes Datumsobjekt */
    public static Date strings2DateTimeISO(String date, String time)
    {
        if (date==null) {
	    throw new InvalidArgumentException("*** date must not be null");
	}

        Date result;
        try {
	    if (time!=null) {
                result=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date+" "+time);
	    } else {
                result=new SimpleDateFormat("yyyy-MM-dd").parse(date);
	    }
        } catch (ParseException e) {
            throw new InvalidArgumentException(date+" / "+time);
        }
        
        return result;
    }
    
    
    private static void errDeprecated(String method)
    {
        LoggerFactory.getLogger(HBCIUtils.class).info("programming error: the method "+method+"() has been deprecated, is very dangerous and will be removed soon.", HBCIUtils.LOG_ERR);
        LoggerFactory.getLogger(HBCIUtils.class).info("programming error: please check your application to replace calls to "+method+"() with calls to either "+method+"Local() or "+method+"ISO()", HBCIUtils.LOG_ERR);
    }
    
    /** Wrapper f�r {@link #date2StringLocal(Date)}. 
     * @deprecated */
    public static String date2String(Date date)
    {
        errDeprecated("date2String");
        return date2StringLocal(date);
    }
    
    /** Wrapper f�r {@link #string2DateLocal(String)}
     * @deprecated */
    public static Date string2Date(String st)
    {
        errDeprecated("string2Date");
        return string2DateLocal(st);
    }
    
    /** Wrapper f�r {@link #time2StringLocal(Date)}
     * @deprecated */
    public static String time2String(Date date)
    {
        errDeprecated("time2String");
        return time2StringLocal(date);
    }
    
    /** Wrapper f�r {@link #string2TimeLocal(String)}
     * @deprecated */
    public static Date string2Time(String st)
    {
        errDeprecated("string2Time");
        return string2TimeLocal(st);
    }
    
    /** Wrapper f�r {@link #datetime2StringLocal(Date)}
     * @deprecated */
    public static String datetime2String(Date date)
    {
        errDeprecated("datetime2String");
        return datetime2StringLocal(date);
    }
    
    /** Wrapper f�r {@link #string2DateLocal(String)}
     * @deprecated */
    public static Date strings2DateTime(String date, String time)
    {
        errDeprecated("strings2DateTime");
        return strings2DateTimeLocal(date,time);
    }
    
    
    /** Gibt Daten Base64-encoded zur�ck. Die zu kodierenden Daten m�ssen als Byte-Array �bergeben
        werden, als Resultat erh�lt man einen String mit der entsprechenden Base64-Kodierung.
        @param x zu kodierende Daten
        @return String mit Base64-kodierten Daten */
    public static String encodeBase64(byte[] x)
    {
        try {
            int origSize=x.length;

            if ((origSize%3)!=0) {
                byte[] temp=new byte[((origSize/3)+1)*3];
                System.arraycopy(x,0,temp,0,origSize);
                x=temp;
            }

            StringBuffer ret=new StringBuffer();
            int readPos=0;

            while (readPos<(x.length<<3)) {
                int modulus=readPos&7;
                int value;

                if ((readPos>>3)<origSize) {
                    if (modulus<=2) {
                        // six bits in one byte
                        value=(x[readPos>>3]>>(2-modulus))&0x3F;
                    } else {
                        // six bits in two bytes
                        value=((x[readPos>>3]<<(modulus-2))&0x3F)|
                              ((x[(readPos>>3)+1]>>(10-modulus))&((1<<(modulus-2))-1));
                    }

                    ret.append(base64table[value]);
                } else {
                    ret.append('=');

                }
                readPos+=6;
            }

            return ret.toString();
        } catch (Exception ex) {
            throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_UTIL_ENCB64"),ex);
        }
    }

    /** Dekodieren eines Base64-Datenstroms. Es wird zu einem gegebenen Base64-Datenstrom der dazugeh�rige
        "Klartext" zur�ckgegeben.
        @param st Base64-kodierten Daten
        @return dekodierter Datenstrom als Byte-Array */
    public static byte[] decodeBase64(String st)
    {
        try {
            byte[] source=st.getBytes(Comm.ENCODING);
            byte[] ret=new byte[st.length()];
            int    retlen=0;

            int needFromFirst=6;
            int needFromSecond=2;
            boolean abort=false;
            int    byteCounter=0;
            int[]  values=new int[2];

            for (int readPos=0;readPos<source.length;readPos++) {
                values[0]=0;
                values[1]=0;

                for (int step=0;step<2;step++) {
                    int value=0;

                    while ((readPos+step)<source.length) {
                        value=source[readPos+step];

                        if (value>='0'&&value<='9'||
                            value>='A'&&value<='Z'||
                            value>='a'&&value<='z'||
                            value=='+'||value=='/'||value=='=') {
                            break;
                        }

                        readPos++;
                    }

                    if (!(value>='0'&&value<='9'||
                          value>='A'&&value<='Z'||
                          value>='a'&&value<='z'||
                          value=='+'||value=='/')) {

                        abort=true;
                        break;
                    }

                    if ((char)value=='/') {
                        value=63;
                    } else if ((char)value=='+') {
                        value=62;
                    } else if ((char)value<='9') {
                        value=52+value-(byte)'0';
                    } else if ((char)value<='Z') {
                        value=value-(byte)'A';
                    } else {
                        value=26+value-(byte)'a';

                    }
                    if (step==0) {
                        values[0]=(value<<(8-needFromFirst))&0xFF;
                    } else {
                        values[1]=(value>>(6-needFromSecond))&0xFF;
                    }
                }

                if (abort) {
                    break;
                }

                ret[retlen++]=(byte)(values[0]|values[1]);

                if ((byteCounter&3)==2) {
                    readPos++;
                    byteCounter++;
                    needFromFirst=6;
                    needFromSecond=2;
                } else {
                    needFromFirst=6-needFromSecond;
                    needFromSecond=8-needFromFirst;
                }

                byteCounter++;
            }

            byte[] ret2=new byte[retlen];
            System.arraycopy(ret,0,ret2,0,retlen);
            return ret2;
        } catch (Exception ex) {
            throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_UTIL_DECB64"),ex);
        }
    }
    
    private static Method getAccountCRCMethodByAlg(String alg)
    {
        Class<AccountCRCAlgs> cl=null;
        Method method=null;
        
        try {
            cl=AccountCRCAlgs.class;
            method=cl.getMethod("alg_"+alg,new Class[] {int[].class, int[].class});
        } catch (Exception e) {
            LoggerFactory.getLogger(HBCIUtils.class).warn("CRC algorithm "+alg+" not yet implemented");
        }
        
        return method;
    }
    
    /** Ermittelt, ob die Kontonummern f�r eine bestimmte BLZ mit <em>HBCI4Java</em>
     * �berpr�ft werden k�nnen oder nicht. Je nach Bank werden unterschiedliche
     * Pr�f-Algorithmen verwendet. Es sind noch nicht alle Pr�f-Algorithmen
     * in <em>HBCI4Java</em> implementiert - f�r manche Banken existiert auch
     * keine Information dar�ber, welche Pr�f-Algorithmen diese verwenden.
     * <p>Mit dieser Methode kann nun ermittelt werden, ob f�r eine bestimmte
     * Bank eine Pr�fung m�glich ist oder nicht.</p>
     * @param blz Die BLZ der Bank
     * @return <code>true</code>, wenn die Kontonummern f�r diese Bank mit
     * <em>HBCI4Java</em> validiert werden k�nnen, sonst <code>false</code> */
    public static boolean canCheckAccountCRC(String blz)
    {
        BankInfo info = getBankInfo(blz);
        if (info == null)
            return false;

        String alg = info.getChecksumMethod();
        if (alg == null || alg.length() != 2)
            return false;
        
        Method method = getAccountCRCMethodByAlg(alg);
        return method != null;
    }
    
    /** <p>�berpr�ft, ob gegebene BLZ und Kontonummer zueinander passen.
        Bei diesem Test wird wird die in die Kontonummer "eingebaute"
        Pr�ziffer verifiziert. Anhand der BLZ wird ermittelt, welches 
        Pr�fzifferverfahren zur �berpr�fung eingesetzt werden muss.</p>
        <p>Ein positives Ergebnis dieser Routine bedeutet <em>nicht</em>, dass das 
        entsprechende Konto bei der Bank <em>existiert</em>, sondern nur, dass 
        die Kontonummer bei der entsprechenden Bank prinzipiell g�ltig ist.</p>
        @param blz die Bankleitzahl der Bank, bei der das Konto gef�hrt wird
        @param number die zu �berpr�fende Kontonummer
        @return <code>true</code> wenn die Kontonummer nicht verifiziert werden kann (z.B.
        weil das jeweilige Pr�fzifferverfahren noch nicht in <em>HBCI4Java</em>
        implementiert ist) oder wenn die Pr�fung erfolgreich verl�uft; <code>false</code>
        wird immer nur dann zur�ckgegeben, wenn tats�chlich ein Pr�fzifferverfahren
        zum �berpr�fen verwendet wurde und die Pr�fung einen Fehler ergab */ 
    public static boolean checkAccountCRC(String blz, String number)
    {
        BankInfo info = getBankInfo(blz);
        String alg = info != null ? info.getChecksumMethod() : null;

        // Im Zweifel lassen wir die Bankverbindung lieber durch
        if (alg == null || alg.length() != 2) {
            LoggerFactory.getLogger(HBCIUtils.class).warn("no crc information about "+blz+" in database");
            return true;
        }

        LoggerFactory.getLogger(HBCIUtils.class).debug("crc-checking "+blz+"/"+number);
        return checkAccountCRCByAlg(alg,blz,number);
    }
    
    /** Used to convert a blz or an account number to an array of ints, one
     * array element per digit. */
    private static int[] string2Ints(String st, int target_length)
    {
        int[] numbers=new int[target_length];
        int   st_len=st.length();
        char  ch;

        for (int i=0;i<st_len;i++) {
            ch=st.charAt(i);
            numbers[target_length-st_len+i]=ch-'0';
        }
        
        return numbers;
    }
    
    /** �berpr�fen einer Kontonummer mit einem gegebenen CRC-Algorithmus.
     * Diese Methode wird intern von {@link HBCIUtils#checkAccountCRC(String,String)}
     * aufgerufen und kann f�r Debugging-Zwecke auch direkt benutzt werden.
     * @param alg Nummer des zu verwendenden Pr�fziffer-Algorithmus (siehe
     * Datei <code>blz.properties</code>).
     * @param blz zu �berpr�fende Bankleitzahl
     * @param number zu �berpr�fende Kontonummer
     * @return <code>false</code>, wenn der Pr�fzifferalgorithmus f�r die
     * angegebene Kontonummer einen Fehler meldet, sonst <code>true</code>
     * (siehe dazu auch {@link #checkAccountCRC(String, String)}) */
    public static boolean checkAccountCRCByAlg(String alg, String blz, String number)
    {
        boolean ret=true;
        
        if (blz==null || number==null) {
            throw new NullPointerException("blz and number must not be null");
        }
        
        if (number.length()<=10) {
            Method  method=getAccountCRCMethodByAlg(alg);

            if (method!=null) {
                try {
                    int[] blz_digits=string2Ints(blz, 8);
                    int[] number_digits=string2Ints(number, 10);

                    Object[] args=new Object[] {blz_digits, number_digits};
                    ret=((Boolean)method.invoke(null,args)).booleanValue();

                    LoggerFactory.getLogger(HBCIUtils.class).debug("\"CRC check for \"+blz+\"/\"+number+\" with alg \"+alg+\": \"+ret");
                } catch (Exception e) {
                    throw new HBCI_Exception(e);
                }
            }
        } else {
            LoggerFactory.getLogger(HBCIUtils.class).warn("can not check account numbers with more than 10 digits ("+number+")- skipping CRC check");
        }
        
        return ret;
    }
    
    /** Use {@link #checkAccountCRCByAlg(String, String, String)} instead!
     * @deprecated */
    public static boolean checkAccountCRCByAlg(String alg, String number)
    {
        return checkAccountCRCByAlg(alg,"",number);
    }

    
    /** �berpr�fen der G�ltigkeit einer IBAN. Diese Methode pr�ft anhand eines
     * Pr�fziffer-Algorithmus, ob die �bergebene IBAN prinzipiell g�ltig ist.
     * @return <code>false</code> wenn der Pr�fzifferntest fehlschl�gt, sonst
     * <code>true</code>  */
    public static boolean checkIBANCRC(String iban)
    {
    	return AccountCRCAlgs.checkIBAN(iban);
    }
    
    /**
     * �berpr�fen der G�ltigkeit einer Gl�ubiger-ID. Diese Methode pr�ft anhand eines
     * Pr�fziffer-Algorithmus, ob die �bergebene ID prinzipiell g�ltig ist.
     * @param creditorId die zu pruefende Creditor-ID.
     * @return <code>false</code> wenn der Pr�fzifferntest fehlschl�gt, sonst
     * <code>true</code>  */
    public static boolean checkCredtitorIdCRC(String creditorId)
    {
        return AccountCRCAlgs.checkCreditorId(creditorId);
    }

    private static void refreshBLZList(String blzpath)
        throws IOException
    {
        if (blzpath==null) {
            blzpath="";
        }
        blzpath+="blz.properties";
        InputStream f=new FileInputStream(blzpath);
        
        if (f==null)
            throw new InvalidUserDataException(HBCIUtilsInternal.getLocMsg("EXCMSG_BLZLOAD",blzpath));
        
        refreshBLZList(f);
        f.close();
    }
    
    /** Aktivieren einer neuen Bankenliste. Diese Methode kann aufgerufen
     * werden, um w�hrend der Laufzeit einer <em>HBCI4Java</em>-Anwendung 
     * eine neue Bankenliste zu aktivieren. Die Bankenliste wird
     * aus dem �bergebenen InputStream gelesen, welcher Daten im Format eines
     * Java-Properties-Files liefern muss. Das konkrete Format der Property-Eintr�ge
     * der Bankenliste ist am Beispiel der bereits mitgelieferten Datei
     * <code>blz.properties</code> ersichtlich.
     * @param in Eingabe-Stream, der f�r das Laden der Bankleitzahlen-Daten verwendet
     *           werden soll 
     * @throws IOException
     **/
    public static void refreshBLZList(InputStream in)
        throws IOException
    {
        LoggerFactory.getLogger(HBCIUtils.class).debug("trying to load BLZ data");
        HBCIUtilsInternal.blzs.clear();
        HBCIUtilsInternal.blzs.load(in);
        
        HBCIUtilsInternal.banks.clear();
        for (Entry<Object,Object> e:HBCIUtilsInternal.blzs.entrySet())
        {
            String blz   = (String) e.getKey();
            String value = (String) e.getValue();
            
            BankInfo info = BankInfo.parse(value);
            info.setBlz(blz);
            HBCIUtilsInternal.banks.put(blz,info);
        }
    }

    /** Konvertiert einen String in einen BigDecimal-Wert mit zwei Nachkommastellen.
     * @param st String, der konvertiert werden soll (Format "<code>1234.56</code>");
     * @return BigDecimal-Wert */
    public static BigDecimal string2BigDecimal(String st)
    {
        BigDecimal result = new BigDecimal(st);
        result.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return result;
    }

    /** Wandelt einen BigDecimal-Wert in einen String im Format "<code>1234.56</code>"
     * um (also ohne Tausender-Trennzeichen und mit "." als Dezimaltrennzeichen).
     * @param value zu konvertierender BigDecimal-Wert
     * @return String-Darstellung dieses Wertes */
    public static String bigDecimal2String(BigDecimal value)
    {
        DecimalFormat format=new DecimalFormat("0.00");
        DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(symbols);
        format.setDecimalSeparatorAlwaysShown(true);
        return format.format(value);
    }

    /** Konvertiert einen String in einen double-Wert (entspricht 
     * <code>Double.parseDouble(st)</code>).
     * @param st String, der konvertiert werden soll (Format "<code>1234.56</code>");
     * @return double-Wert
     * @deprecated use {@link #string2BigDecimal(String)} */
    @Deprecated
    public static double string2Value(String st)
    {
        return Double.parseDouble(st);
    }

    /** Wandelt einen Double-Wert in einen String im Format "<code>1234.56</code>"
     * um (also ohne Tausender-Trennzeichen und mit "." als Dezimaltrennzeichen).
     * @param value zu konvertierender Double-Wert
     * @return String-Darstellung dieses Wertes
     * @deprecated use {@link #bigDecimal2String(BigDecimal)} */
    @Deprecated
    public static String value2String(double value)
    {
        DecimalFormat format=new DecimalFormat("0.00");
        DecimalFormatSymbols symbols=format.getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(symbols);
        format.setDecimalSeparatorAlwaysShown(true);
        return format.format(value);
    }
    
    /** Gibt die Versionsnummer der <em>HBCI4Java</em>-Bibliothek zur�ck.
     * @return verwendete <em>HBCI4Java</em>-Version */
    public static String version()
    {
        return VERSION;
    }
    
    /** Parsen eines MT940-Datenstroms (Kontoausz�ge). Kontoausz�ge k�nnen
     * von vielen Software-Produkten im MT940-Format exportiert werden. Diese
     * Methode nimmt einen solchen MT940-String entgegen, parst ihn und stellt
     * ein {@link org.kapott.hbci.GV_Result.GVRKUms GVRKUms}-Objekt mit den
     * geparsten Daten zur Verf�gung.
     * @param mt940 Der zu parsende MT940-String
     * @return {@link org.kapott.hbci.GV_Result.GVRKUms GVRKUms}-Objekt f�r den
     * einfachen Zugriff auf die Umsatzinformationen. */
    public static GVRKUms parseMT940(String mt940)
    {
        GVRKUms result=new GVRKUms();
        result.appendMT940Data(Swift.decodeUmlauts(mt940));
        return result;
    }

    public static void log(String s, int logDebug) {
        switch (logDebug) {
            case LOG_INTERN:
            case LOG_NONE:
                System.out.println(s);
                break;
            case LOG_ERR:
                LoggerFactory.getLogger(HBCIUtils.class).error(s);
                break;
            case LOG_WARN:
                LoggerFactory.getLogger(HBCIUtils.class).warn(s);
                break;
            case LOG_INFO:
                LoggerFactory.getLogger(HBCIUtils.class).info(s);
                break;
            case LOG_DEBUG:
            case LOG_DEBUG2:
                LoggerFactory.getLogger(HBCIUtils.class).debug(s);
                break;
        }

    }

    public static void log(Exception e) {
        LoggerFactory.getLogger(HBCIUtils.class).error(e.getMessage(), e);
    }

    public static String getParam(String s, String s1) {
        return s1;
    }

    public static HBCICallback getCallback() {
        return null;
    }

    public static String getParam(String s) {
        return getParam(s, null);
    }

    public static void setParam(String s, Object o) {
    }

    public static Locale getLocale() {
        return Locale.getDefault();
    }
}
