package tcrn.tbi.tm.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.CodeSource;
import java.util.Properties;

import org.omg.CORBA.SystemException;


public class SystemConfiguration {
	
	private String mmserverHost;
	private String mmserverPort;
	private String wsdServerEnabled;
	private String wsdServerhost;
	private String semantictype;
	private String inputDirectoryPath;
	private String outputDirectoryPath;
	private String outputType;
	
	public String getInputDirectoryPath() {
		return inputDirectoryPath;
	}

	public void setInputDirectoryPath(String inputDirectoryPath) {
		this.inputDirectoryPath = inputDirectoryPath;
	}

	public String getMmserverHost() {
		return mmserverHost;
	}

	public void setMmserverHost(String mmserverHost) {
		this.mmserverHost = mmserverHost;
	}

	public String getMmserverPort() {
		return mmserverPort;
	}

	public void setMmserverPort(String mmserverPort) {
		this.mmserverPort = mmserverPort;
	}

	public String getWsdServerEnabled() {
		return wsdServerEnabled;
	}

	public void setWsdServerEnabled(String wsdServerEnabled) {
		this.wsdServerEnabled = wsdServerEnabled;
	}

	public String getWsdServerhost() {
		return wsdServerhost;
	}

	public void setWsdServerhost(String wsdServerhost) {
		this.wsdServerhost = wsdServerhost;
	}

	public String getSemantictype() {
		return semantictype;
	}

	public void setSemantictype(String semantictype) {
		this.semantictype = semantictype;
	}

	public SystemConfiguration getPropertyValues() throws SystemException, Exception {
		CodeSource codeSource = SystemConfiguration.class.getProtectionDomain().getCodeSource();
		File jarFile = new File(codeSource.getLocation().toURI().getPath());
		File jarDir = jarFile.getParentFile();
		File propFile = null;
		if (jarDir != null && jarDir.isDirectory()) {
			propFile = new File(jarDir, "config.properties");
		}
		if(propFile==null)
			throw new tcrn.tbi.tm.exception.SystemException("could not find property file");
		//System.out.println(propFile.getName());
		Properties prop = new Properties();
		InputStream inputStream = new FileInputStream(propFile);
		prop.load(inputStream);
        mmserverHost = prop.getProperty("mm.server.host").trim();
        mmserverPort = prop.getProperty("mm.server.port").trim();
        wsdServerEnabled = prop.getProperty("wsd.server.enabled").trim();
        wsdServerhost = prop.getProperty("wsd.server.host").trim();
        inputDirectoryPath = prop.getProperty("hdrfsystem.input.directory").trim();
        outputDirectoryPath = prop.getProperty("hdrfsystem.output.directory").trim();
        outputType = prop.getProperty("hdrfsystem.output.type").trim();
        
        //System.out.println(mmserverHost+mmserverPort+wsdServerEnabled+wsdServerhost+semantictype);
        SystemConfiguration sysConfig = new SystemConfiguration();
        sysConfig.setMmserverHost(mmserverHost);
        sysConfig.setMmserverPort(mmserverPort);
        sysConfig.setWsdServerEnabled(wsdServerEnabled);
        sysConfig.setWsdServerhost(wsdServerhost);
        sysConfig.setInputDirectoryPath(inputDirectoryPath);
        sysConfig.setOutputDirectoryPath(outputDirectoryPath);
        sysConfig.setOutputType(outputType);
        return sysConfig;
	}

	public String getOutputDirectoryPath() {
		return outputDirectoryPath;
	}

	public void setOutputDirectoryPath(String outputDirectoryPath) {
		this.outputDirectoryPath = outputDirectoryPath;
	}

	/**
	 * @return the outputType
	 */
	public String getOutputType() {
		return outputType;
	}

	/**
	 * @param outputType the outputType to set
	 */
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

}
