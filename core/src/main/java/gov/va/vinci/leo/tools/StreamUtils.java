package gov.va.vinci.leo.tools;

/*
 * #%L
 * Leo Core
 * %%
 * Copyright (C) 2010 - 2017 Department of Veterans Affairs
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {


	static final Log log = LogFactory.getLog(StreamUtils.class);

	public static void safeClose(InputStream is)
	{
		if ( is==null ) return;
		try
		{
			is.close();
		}
		catch ( Exception e)
		{
			log.error("unable to close inputstream : " + is);
		}    	
	}

	public static void safeClose(OutputStream os) {
		if ( os==null ) return;
		try
		{
			os.close();
		}
		catch ( Exception e)
		{
			log.error("unable to close outputstream : " + os);
		}    	
		
	}

}
