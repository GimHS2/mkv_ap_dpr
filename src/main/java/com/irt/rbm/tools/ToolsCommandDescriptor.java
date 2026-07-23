/*
 *	File Name:	ToolsCommandDescriptor.java
 *	Version:	2.2.0
 *
 *	Description:
 *
 *	Note:
 *
 *	Modified	(YYYY/MM/DD)	Ver		Content
 *	stghr12		2011/09/30		2.2.0	create
 *
**/

package com.irt.rbm.tools;

import com.irt.util.MessageHandler;
import java.util.List;

/**
 *
 */
public class ToolsCommandDescriptor implements Comparable<ToolsCommandDescriptor> {
	String command;
	String[] aliases;
	String instruction, explanation;
	String[] details;
	List<ToolsCommandDescriptor> subCommandDescriptorList;

	public ToolsCommandDescriptor( String command, String instruction, String explanation ) {
		this( command, (String[])null, instruction, explanation, (String[])null, (ToolsCommandDescriptor[])null );
	}

	public ToolsCommandDescriptor( String command, String instruction, String explanation, ToolsCommandDescriptor... subCommandDescriptors ) {
		this( command, (String[])null, instruction, explanation, (String[])null, subCommandDescriptors );
	}

	public ToolsCommandDescriptor( String command, String[] aliases, String instruction, String explanation ) {
		this( command, aliases, instruction, explanation, (String[])null, (ToolsCommandDescriptor[])null );
	}

	public ToolsCommandDescriptor( String command, String[] aliases, String instruction, String explanation
						, ToolsCommandDescriptor... subCommandDescriptors ) {
		this( command, aliases, instruction, explanation, (String[])null, subCommandDescriptors );
	}

	public ToolsCommandDescriptor( String command, String[] aliases, String instruction, String explanation, String[] details ) {
		this( command, aliases, instruction, explanation, details, (ToolsCommandDescriptor[])null );
	}

	public ToolsCommandDescriptor( String command, String[] aliases, String instruction, String explanation, String[] details
						, ToolsCommandDescriptor... subCommandDescriptors ) {
		this.command = command;
		if( aliases == null )
			this.aliases = new String[] { command };
		else
			this.aliases = com.irt.util.Arrays.append( aliases, command );
		this.instruction = instruction;
		this.explanation = explanation;
		this.details = details;
		if( subCommandDescriptors == null )
			this.subCommandDescriptorList = null;
		else
			this.subCommandDescriptorList = java.util.Collections.unmodifiableList( java.util.Arrays.asList(subCommandDescriptors) );
	}

	public int compareTo( ToolsCommandDescriptor commandDescriptor ) {
		return command.compareTo( commandDescriptor.command );
	}

	public static ToolsCommandDescriptor createDescriptor( MessageHandler msghandler, String command, String instruction, String explanationKey ) {
		return createDescriptor( msghandler, command, (String[])null, instruction, explanationKey, (ToolsCommandDescriptor[])null );
	}

	public static ToolsCommandDescriptor createDescriptor( MessageHandler msghandler, String command, String instruction
						, String explanationKey, ToolsCommandDescriptor... subCommandDescriptors ) {
		return createDescriptor( msghandler, command, (String[])null, instruction, explanationKey, subCommandDescriptors );
	}

	public static ToolsCommandDescriptor createDescriptor( MessageHandler msghandler, String command, String[] aliases, String instruction
						, String explanationKey ) {
		return createDescriptor( msghandler, command, aliases, instruction, explanationKey, (ToolsCommandDescriptor[])null );
	}

	public static ToolsCommandDescriptor createDescriptor( MessageHandler msghandler, String command, String[] aliases, String instruction
						, String explanationKey, ToolsCommandDescriptor... subCommandDescriptors ) {
		String explanation = null;
		String[] details = null;

		if( explanationKey != null ) {
			explanation = msghandler.getMessage( explanationKey );

			List<String> detailList = new java.util.ArrayList<String>();

			try {
				for( int i = 1; i < 100; i++ )
					detailList.add( msghandler.getMessageValue( explanationKey + (i < 10 ? "_DETAIL_0"+ i : "_DETAIL_"+ i) ) );
			} catch( java.util.MissingResourceException missingEx ) {}

			if( detailList.size() > 0 ) details = detailList.toArray( new String[detailList.size()] );
		}

		return new ToolsCommandDescriptor( command, aliases, instruction, explanation, details, subCommandDescriptors );
	}

	public String getCommand() {
		return command;
	}

	public String[] getCommandAliases() {
		return aliases;
	}

	public String[] getDetailExplanations() {
		return details;
	}

	public String getExplanation() {
		return explanation;
	}

	public String getInstruction() {
		return instruction;
	}

	public List<ToolsCommandDescriptor> getSubCommandDescriptors() {
		return subCommandDescriptorList;
	}
}
