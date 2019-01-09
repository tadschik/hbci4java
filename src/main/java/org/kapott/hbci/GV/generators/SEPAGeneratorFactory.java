package org.kapott.hbci.GV.generators;

import org.kapott.hbci.GV.AbstractSEPAGV;
import org.kapott.hbci.exceptions.InvalidUserDataException;
import org.kapott.hbci.sepa.SepaVersion;

import java.util.logging.Logger;

/**
 * Factory zum Ermitteln des passenden Pain-Generators fuer den angegebenen Job.
 * 
 * WICHTIG: Diese Klasse sowie die Ableitungen sollten auch ohne initialisiertes HBCI-System
 * funktionieren, um das XML ohne HBCI-Handler erstellen zu koennen. Daher sollte auf die
 * Verwendung von "HBCIUtils" & Co verzichtet werden. Das ist auch der Grund, warum hier
 * das Java-Logging verwendet wird und nicht das HBCI4Java-eigene.
 */
public class SEPAGeneratorFactory
{
    private final static Logger LOG = Logger.getLogger(SEPAGeneratorFactory.class.getName());
    
	/**
	 * Gibt den passenden SEPA Generator für die angegebene PAIN-Version.
	 * @param job der zu erzeugende Job.
	 * @param version die PAIN-Version.
	 * @return ISEPAGenerator
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static ISEPAGenerator get(AbstractSEPAGV job, SepaVersion version) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
        String jobname = job.getPainJobName(); // referenzierter pain-Geschäftsvorfall
        return get(jobname,version);
	}
	
    /**
     * Gibt den passenden SEPA Generator für die angegebene PAIN-Version.
     * @param jobname der Job-Name. Z.Bsp. "UebSEPA".
     * @param version die PAIN-Version.
     * @return ISEPAGenerator
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static ISEPAGenerator get(String jobname, SepaVersion version) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        if (!version.canGenerate(jobname))
            throw new InvalidUserDataException("SEPA version is not supported: " + version);

        String className = version.getGeneratorClass(jobname);
        LOG.fine("trying to init SEPA creator: " + className);
        Class cl = Class.forName(className);
        return (ISEPAGenerator) cl.newInstance();
    }
	
}
