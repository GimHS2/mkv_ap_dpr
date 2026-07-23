/*
 *	File Name:	FtpProp.java
 *	Version:	2.2.0c
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	jbaek		2019/04/30		2.2.0c	create
 *
**/

package com.irt.rbm.tools;

/**
 * Java Bean class used with Tools Process Runner configuration 
 *
 */
public class FtpProp {
	String hostname, username, password, transportType, serverPath, knownHosts;

	boolean useSsh, keepModTime;

	public boolean isKeepModTime() {
		return keepModTime;
	}

	public void setKeepModTime( boolean keepModTime ) {
		this.keepModTime = keepModTime;
	}

	int port = -1;

	public final String getConnectIdentity() {
		return getUsername() + "@" + getHostname() + ":" + getPort() + getServerPath();
	}

	public String getHostname() {
		return hostname;
	}

	public String getKnownHosts() {
		return knownHosts;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return ( port > 0 ? port : ( useSsh ? 22 : 21 ) );
	}

	public String getServerPath() {
		return ( serverPath == null ? "./" : serverPath );
	}

	public String getTransportType() {
		return transportType;
	}

	public String getUsername() {
		return username;
	}

	public boolean isUseSsh() {
		return useSsh;
	}

	public void setHostname( String hostname ) {
		this.hostname = hostname;
	}

	public void setKnownHosts( String knownHosts ) {
		this.knownHosts = knownHosts;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public void setPort( int port ) {
		this.port = port;
	}

	public void setServerPath( String serverPath ) {
		this.serverPath = serverPath;
	}

	public void setTransportType( String transportType ) {
		this.transportType = transportType;
	}

	public void setUsername( String username ) {
		this.username = username;
	}

	public void setUseSsh( boolean useSsh ) {
		this.useSsh = useSsh;
	}
}
