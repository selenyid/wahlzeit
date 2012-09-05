/*
 * Copyright (c) 2006-2009 by Dirk Riehle, http://dirkriehle.com
 *
 * This file is part of the Wahlzeit photo rating application.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.wahlzeit.main;

import java.io.*;
import java.sql.*;

import org.mortbay.http.*;
import org.mortbay.http.handler.*;
import org.mortbay.jetty.servlet.*;

import org.wahlzeit.handlers.*;
import org.wahlzeit.model.*;
import org.wahlzeit.services.*;
import org.wahlzeit.webparts.*;

/**
 * 
 * @author driehle
 *
 */
public abstract class AbstractMain {
	
	/**
	 * 
	 */
	protected static AbstractMain instance;
	
	/**
	 * 
	 */
	protected static boolean isToStop = false;

	/**
	 * 
	 */
	protected static boolean isInProduction = false;
	
	/**
	 * 
	 */
	public static void requestStop() {
		synchronized(instance) {
			isToStop = true;
			instance.notify();
		}
	}
	
	/**
	 * 
	 */
	public static boolean isShuttingDown() {
		return isToStop;
	}
		
	/**
	 * 
	 */
	public static boolean isInProduction() {
		return isInProduction;
	}
	
	/**
	 * 
	 */
	public synchronized void run(String[] argv) {
		handleArgv(argv);
		
		try {
			startUp();
			execute();
		} catch(Exception ex) {
			SysLog.logThrowable(ex);
		}

		try {
			shutDown();
		} catch (Exception ex) {
			SysLog.logThrowable(ex);
		}
	} 

	/**
	 * 
	 */
	protected void handleArgv(String[] argv) {
		for (int i = 0; i < argv.length; i++) {
			String arg = argv[i];
			if (arg.equals("-P") || arg.equals("--production")) {
				instance.isInProduction = true;
			} else if (arg.equals("-D") || arg.equals("--development")) {
				instance.isInProduction = false;
			}
		}		
	}

	/**
	 * 
	 */
	protected void startUp() throws Exception {
		SysLog.initialize(isInProduction);
		SysConfig.setInstance(createSysConfig());
		
		Session ctx = new SysSession("system");
		ContextManager.setThreadLocalContext(ctx);
	}
	
	/**
	 * 
	 */
	protected SysConfig createSysConfig() {
		if (isInProduction()) {
			return createProdSysConfig();
		} else {
			return createDevSysConfig();
		}
	}
	
	/**
	 * 
	 */
	protected SysConfig createProdSysConfig() {
		return createDevSysConfig(); 
	}
	
	/**
	 * 
	 */
	protected SysConfig createDevSysConfig() {
		return new SysConfig("localhost", "8585");
	}
	
	/**
	 * 
	 */
	protected void execute() throws Exception {
		// do nothing
	}

	/**
	 * 
	 */
	protected void shutDown() throws Exception {
		SysConfig.dropInstance();
	}
	
}